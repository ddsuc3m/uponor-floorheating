package modbusparser;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import configuration.BusConfig;
import configuration.Configuration;
import modbusparser.frames.Frame;
import modbusparser.frames.FrameParser;

public class FrameProcessor implements Runnable {

	private ByteBuffer incoming;
	private Object bufferWaitObject;
	private TCPClient tcpClient;
	private Configuration configuration;
	private BusConfig busConfig;
	private FrameParser fp;
	private boolean killThread = false;

	public FrameProcessor(ByteBuffer incoming, Object bufferWaitObject, TCPClient tcpClient) {
		this.incoming = incoming;
		this.bufferWaitObject = bufferWaitObject;
		this.tcpClient = tcpClient;
		this.configuration = tcpClient.getConfig();
		this.busConfig = configuration.getBusConfig();
		this.fp = new FrameParser(configuration);
	}

	private Logger logger;
	{
		logger = Logger.getLogger(TCPClient.class.getName());
	}

	@Override
	public void run() {
		Logger.getLogger(FrameParser.class.getName()).log(Level.WARNING, "Frame Process thread " + Thread.currentThread().getId() + " starting to process frames");
		while (true) {
			ByteBuffer check = null;
			synchronized (Locks.bufferLock) {
				/* if the frame processing thread has been waiting and it has been requested to end, 
				 * this should be checked as it will be interrupted by the main TCP thread. 
				 */
				if(killThread)
				{
					Logger.getLogger(FrameParser.class.getName()).log(Level.WARNING, "Frame Process thread " + Thread.currentThread().getId() + " killed");
					break;
				}
				check = incoming.duplicate();
			}
			
			Logger.getLogger(FrameParser.class.getName()).log(Level.FINEST,
					"check POS (PRE-FLIP):" + check.position() + " LIM:" + check.limit() + " REM:" + check.remaining());
			check.flip();
			Logger.getLogger(FrameParser.class.getName()).log(Level.FINEST,
					"check POS (POS-FLIP):" + check.position() + " LIM:" + check.limit() + " REM:" + check.remaining());
			if (check.limit() < busConfig.getFRAME_MIN_LEN()) {
				logger.log(Level.FINE,
						"Less than FRAME_MIN_LEN " + busConfig.getFRAME_MIN_LEN() + " wait 1 second for more");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "Cannot wait for more data (FRAME_MIN_LEN) " + e.getMessage());
				}
				continue;
			}
			if (check.limit() < busConfig.getFRAME_PROCESSING_MIN_LEN()) {
				logger.log(Level.FINE, "Less than FRAMEPROCESS_MIN_LEN " + busConfig.getFRAME_PROCESSING_MIN_LEN()
						+ " wait 1 second for more");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.log(Level.FINE, "Cannot wait for more data (FRAMEPROCESS_MIN_LEN) " + e.getMessage());
				}
				continue;
			}
			if (check.hasRemaining() && incoming.remaining() > busConfig.getFRAME_PROCESSING_MIN_LEN()) {

				List<Frame> latest_frames = fp.getConsistenData(incoming, bufferWaitObject, busConfig);
				
				synchronized (bufferWaitObject) {
					/* if the frame processing thread has been waiting and it has been requested to end, 
					 * this should be checked as it will be interrupted by the main TCP thread. 
					 */
					if(killThread)
					{
						Logger.getLogger(FrameParser.class.getName()).log(Level.WARNING, "Frame Process thread " + Thread.currentThread().getId() + " killed");
						break;
					}
					incoming.flip();
					logger.log(Level.FINEST,
							"Incoming possition set to " + fp.getUpdatedIncomingPointer() + " and then compacting, so we processed "
									+ (fp.getUpdatedIncomingPointer()) + " bytes");
					logger.log(Level.FINEST, "Incoming (PRE-POSUPDATE) POS:"
							+ incoming.position() + " LIM:" + incoming.limit() + " REM:" + incoming.remaining());
					incoming.position(fp.getUpdatedIncomingPointer());
					fp.clearUpdatedIncomingPointer();
					logger.log(Level.FINEST, "Incoming (POST-POSUPDATE-PRECOMPACT) POS:"
							+ incoming.position() + " LIM:" + incoming.limit() + " REM:" + incoming.remaining());
					incoming.compact();
					logger.log(Level.FINEST, "Incoming (POSTCOMPACT) POS:"
							+ incoming.position() + " LIM:" + incoming.limit() + " REM:" + incoming.remaining());
					

				}

				if (latest_frames != null) {
					tcpClient.processFramesToEntities(latest_frames);
					
				}
			}
		}

	}

	public void kill() {
		killThread = true;
		
	}

}
