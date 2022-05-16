package modbusparser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import configuration.ConfigFromFile;
import configuration.Configuration;
import configuration.SerialServerConfig;
import entities.Bus;
import entities.Entity;
import entities.EntityCollection;
import modbusparser.comm.commandData.SetPointTemperature;
import modbusparser.frames.CommanDataFrame;
import modbusparser.frames.Frame;
import modbusparser.mqtt.MQTTClient;
import modbusparser.util.HexString;

public class TCPClient implements Runnable {

	private InetSocketAddress hostAddress;
	private SocketChannel client;
	private Thread socketThread;
	private Thread processThread;
	private EntityCollection ec;
	private Bus bus;
	private MQTTClient mqttClient;
	private Queue<CommanDataFrame> framesToWrite = new ConcurrentLinkedQueue<CommanDataFrame>();
	private ConfigFromFile configFile;
	private Configuration config;
	private SerialServerConfig serialServerConfig;

	private Logger logger;
	{
		logger = Logger.getLogger(TCPClient.class.getName());
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"'%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
	}
	protected ByteBuffer incoming;
	private FrameProcessor frameProcessor;

	public TCPClient(String configurationFilePath) throws FileNotFoundException {

		configFile = new ConfigFromFile(configurationFilePath);
		config = configFile.getConfiguration();
		serialServerConfig = config.getSerialServerConfig();
		ec = new EntityCollection(config);
		bus = new Bus(config);
		incoming = ByteBuffer.allocate(4096);
		frameProcessor = new FrameProcessor(incoming, Locks.bufferLock, this);
		mqttClient = new MQTTClient(this);
		hostAddress = new InetSocketAddress(serialServerConfig.getMODBUS_TCP_ADDRESS(),
				serialServerConfig.getMODBUS_TCP_PORT());
		ConnectSocketAndStartThread(null);
		startProcessorThread();

	}

	public void startProcessorThread() {
		processThread = new Thread(this.frameProcessor);
		processThread.start();
	}

	public void ConnectSocketAndStartThread(Thread oldThread) {
		bus.resetTimeCalculationsAndSendClearance();
		if (oldThread != null) {
			oldThread.interrupt();
		}
		incoming.clear();
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ConnectSocket();
		if (client.isConnected()) {
			socketThread = new Thread(this);
			socketThread.start();
		}

	}

	public void ConnectSocket() {

		while (true) {
			try {
				logger.info("Trying to connect...");
				client = SocketChannel.open(hostAddress);
				client.configureBlocking(false);
				incoming.clear();
				logger.log(Level.INFO,
						"Client started, connected to " + hostAddress.getHostString() + ":" + hostAddress.getPort());
				break; // We connected! Exit the loop.
			} catch (IOException e) {
				logger.log(Level.WARNING, "Connect failed, retrying in 5 seconds");
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException ie) {
					// Interrupted.
				}
			}
		}
	}

	public void processFramesToEntities(List<Frame> frames) {
		for (Frame f : frames) {
			Entity e = ec.frameBroker(f);
			mqttClient.updateEntity(e);
		}

	}

	public void writeToBus() {
		if (writePending()) {
			CommanDataFrame frameTowrite = getFrameToWrite();

			if (bus.canWrite()) {
				ByteBuffer bufferToWrite = frameTowrite.getRawFrame();
				int written = 0;
				int pendingToWrite = bufferToWrite.limit();
				while (written < pendingToWrite) {
					try {
						bufferToWrite.flip();
						written += client.write(bufferToWrite);

						logger.log(Level.FINE,
								"-----------------------------------------------------------------------------------------------------------------------------------------------------");
						logger.log(Level.FINE, "Writing " + HexString.convertToHexadecimal(bufferToWrite));
					} catch (IOException e) {
						ConnectSocketAndStartThread(socketThread);
					}
				}
				removeFrameToWrite(frameTowrite);
			}
		}

	}

	private void removeFrameToWrite(CommanDataFrame frameTowrite) {
		framesToWrite.remove(frameTowrite);

	}

	private boolean writePending() {
		return getFrameToWrite() != null;
	}

	public void addFrameToWrite(CommanDataFrame cdf) {
		framesToWrite.add(cdf);
	}

	private CommanDataFrame getFrameToWrite() {
		return framesToWrite.peek();
	}

	
	
	@Override
	public void run() {
		int bytes_read = 0;
		bus.setPreviousTime(System.currentTimeMillis());
		while (true) {

			bytes_read = 0;
			try {

				logger.log(Level.FINE, "Reading data");
				synchronized (Locks.bufferLock) {
					bytes_read = client.read(incoming);
					if (bytes_read <= 0) {

						if (bus.getTimersReady() && (bus.getWriteTimeMillis() > (serialServerConfig.getMAX_MEAN_TIMES_TIMEOUT()
								* bus.getWriteTimeMillisMean()))) {
							logger.log(Level.FINE, "Reading data " + bytes_read + " " + client.isConnected() + " "
									+ bus.getWriteTimeMillis() + " " + bus.getWriteTimeMillisMean());
							ConnectSocketAndStartThread(socketThread);
						} else {
							bus.timeUpdateWaitingForFrame(incoming);
						}
					} else {
						bus.timeUpdateAfterReceive(incoming, bytes_read);
					}
					writeToBus();
					try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						ConnectSocketAndStartThread(socketThread);
					}

				}

			} catch (IOException | NotYetConnectedException e) {
				logger.log(Level.SEVERE, "Socket IOException or not Connected, reconnecting...");
				ConnectSocketAndStartThread(socketThread);
			}

		}
	}

	public @Nullable Configuration getConfig() {
		return config;
	}

	
	public @Nullable EntityCollection getEntityCollection() {
		return ec;
	}


	public static void main(String args[]) {

		if (args.length == 0) {
			System.out.println("Usage: TCPClient configurationFilePath");
			System.exit(1); // Non zero termination
		}
		String configurationFilePath = args[0];
		try {
			TCPClient tcpc = new TCPClient(configurationFilePath);

			CommanDataFrame cdf = new CommanDataFrame(tcpc.config);
			SetPointTemperature sp0 = new SetPointTemperature();
			sp0.setPayloadZero();
			cdf.addCommandData(sp0);
			SetPointTemperature spT = new SetPointTemperature();
			spT.setCelsius(20.00f);
			cdf.addCommandData(spT);
			// cdf.setAddress((byte) 0x9E, (byte) 0x8B);
			cdf.setAddress((byte) 0x70, (byte) 0x64);

			cdf.generateRawFrame();
			// System.out.println(cdf.toString());
			try {
				TimeUnit.SECONDS.sleep(40);
			} catch (InterruptedException ie) {
				// Interrupted.
			}
			// tcpc.addFrameToWrite(cdf);
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the configuration file ");
			e.printStackTrace();
		}

	}

}
