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
	private boolean inspect = false;

	private Logger logger;
	{
		logger = Logger.getLogger(TCPClient.class.getName());
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"'%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
	}
	protected ByteBuffer incoming;
	private FrameProcessor frameProcessor;

	public TCPClient(String configurationFilePath, boolean inspect) throws FileNotFoundException {

		configFile = new ConfigFromFile(configurationFilePath);
		config = configFile.getConfiguration();
		serialServerConfig = config.getSerialServerConfig();
		ec = new EntityCollection(config);
		bus = new Bus(config);
		incoming = ByteBuffer.allocate(4096);
		this.inspect = inspect;

		mqttClient = new MQTTClient(this);
		hostAddress = new InetSocketAddress(serialServerConfig.getMODBUS_TCP_ADDRESS(),
				serialServerConfig.getMODBUS_TCP_PORT());
		ConnectSocketAndStartThreads(null);

	}

	public void startProcessorThread() {
		if (frameProcessor != null) {
			frameProcessor.kill();
			processThread.interrupt();
		}
		frameProcessor = new FrameProcessor(incoming, Locks.bufferLock, this);
		processThread = new Thread(this.frameProcessor);
		processThread.start();
	}

	public void ConnectSocketAndStartThreads(Thread oldThread) {
		logger.log(Level.INFO,"Connecting or reconnecting");
		bus.resetTimeCalculationsAndSendClearance();
		if (oldThread != null) {
			if (frameProcessor != null) {
				logger.log(Level.INFO,"Interrupt old process thread");
				frameProcessor.kill();
				processThread.interrupt();
			}
			logger.log(Level.INFO,"Interrupt old TCP thread");
			oldThread.interrupt();
			
			
		}
		logger.log(Level.INFO,"Clear incoming buffer");
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
			logger.log(Level.INFO,"Create and start new threads");
			socketThread = new Thread(this);
			socketThread.start();
			startProcessorThread();
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
					logger.log(Level.WARNING, "Could not wait to reconnect " + ie.getMessage());
				}
			}
		}
	}

	public void processFramesToEntities(List<Frame> frames) {
		for (Frame f : frames) {
			
			Entity e = ec.frameBroker(f);
			if(inspect)
			{
				System.out.println(f.toString());
				System.out.println(e.getStatusString());
			}
			try {
				mqttClient.updateEntity(e);
			} catch (Exception ex) {
				logger.log(Level.WARNING,
						"Mqtt Client may have been interrupted. If after a TCP reconection do not worry! "
								+ ex.getMessage());
			}
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
						ConnectSocketAndStartThreads(socketThread);
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

						if (bus.getTimersReady()
								&& (bus.getWriteTimeMillis() > (serialServerConfig.getMAX_MEAN_TIMES_TIMEOUT()
										* bus.getWriteTimeMillisMean()))) {
							logger.log(Level.FINE, "Reading data " + bytes_read + " " + client.isConnected() + " "
									+ bus.getWriteTimeMillis() + " " + bus.getWriteTimeMillisMean());
							ConnectSocketAndStartThreads(socketThread);
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
						ConnectSocketAndStartThreads(socketThread);
					}

				}

			} catch (IOException | NotYetConnectedException e) {
				logger.log(Level.SEVERE, "Socket IOException or not Connected, reconnecting...");
				ConnectSocketAndStartThreads(socketThread);
			}

		}
	}

	public @Nullable Configuration getConfig() {
		return config;
	}

	public @Nullable EntityCollection getEntityCollection() {
		return ec;
	}

	public static void printUsage() {
		System.out.println("Usage: Normal case");
		System.out.println("Usage: TCPClient configurationFilePath");
		System.out.println("Usage: Testing and figuring out thermostats addresses");
		System.out.println("Usage: TCPClient configurationFilePath inspect");
	}

	public static void main(String args[]) {

		if (args.length == 0) {
			printUsage();
			System.exit(1); // Non zero termination
		}
		String configurationFilePath = "";
		String debug = "";
		boolean inspect = false;
		if (args.length >= 1)
			configurationFilePath = args[0];
		if (args.length == 2) {
			debug = args[1];
			if (!debug.equalsIgnoreCase("inspect")) {
				printUsage();
				System.exit(1);
			} else {
				inspect = true;
			}
		}
		try {
			TCPClient tcpc = new TCPClient(configurationFilePath, inspect);

		} catch (FileNotFoundException e) {
			System.out.println("Could not find the configuration file ");
			printUsage();
			System.exit(1);
		}

	}

}
