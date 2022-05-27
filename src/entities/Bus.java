package entities;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import configuration.BusConfig;
import configuration.Configuration;
import modbusparser.util.HexString;

public class Bus {

	private Logger logger;
	{
		logger = Logger.getLogger(Bus.class.getName());
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"'%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
	}

	private int iterations = 0;
	private long writeTimeMillis = 0;
	private int iterationsWriteWindow = 0;
	private long writeWindowMean = 0;
	private long writeTimeMillisMax = 0;
	private long writeTimeMillisMin = Long.MAX_VALUE;
	private long writeTimeMillisMean = 0;
	private boolean CAN_WRITE = false;
	private int backoff = 5;
	private int currentPos = 0;
	private int lastFramePos = 0;
	private long previous = System.currentTimeMillis();
	private BusConfig busConfig;

	public long getWriteTimeMillis() {
		return writeTimeMillis;
	}

	public Bus( Configuration config) {
		this.busConfig = config.getBusConfig();
		backoff = busConfig.getBUS_MIN_WATING_ITERATIONS_AFTER_SEND();
		resetTimeCalculationsAndSendClearance();
	}

	public void timeUpdateWaitingForFrame(ByteBuffer incoming) {
		lastFramePos = incoming.position();
		logger.log(Level.FINE, "Incoming (AFTER READ) POS:" + incoming.position() + " LIM:" + incoming.limit() + " REM:"
				+ incoming.remaining());
		writeTimeMillis += (System.currentTimeMillis() - previous);
		previous = System.currentTimeMillis();
	}

	public void resetTimeCalculationsAndSendClearance() {
		iterations = 0;
		iterationsWriteWindow = 0;
		writeTimeMillis = 0;
		writeWindowMean = 0;
		writeTimeMillisMax = 0;
		writeTimeMillisMin = Long.MAX_VALUE;
		writeTimeMillisMean = 0;
		CAN_WRITE = false;
		backoff = busConfig.getBUS_MIN_WATING_ITERATIONS_AFTER_SEND();
		currentPos = 0;
		lastFramePos = 0;
		previous = System.currentTimeMillis();
	}

	public void setPreviousTime(long time) {
		previous = time;
	}

	public void timeUpdateAfterReceive(ByteBuffer incoming, int bytes_read) {

		iterations += 1;
		if (iterations > busConfig.getBUS_TIMEWINDOW_VALIDITY_ITERATIONS_MAX()) {
			logger.log(Level.INFO, "Reset timers");
			resetTimeCalculationsAndSendClearance();

		}
		if (iterations > 10) {

			currentPos = incoming.position();

			if (lastFramePos > currentPos) {
				// Discards this as other thread have just changed the buffer
				return;
			}
			byte[] curFrame = new byte[currentPos - lastFramePos];
			
			//For any reason this is not available in Java 11...
			//incoming.get(lastFramePos, curFrame);
			//incoming.get(lastFramePos, curFrame, 0, curFrame.length);
			// a workaround...
			ByteBuffer tmp = incoming.duplicate();
			tmp.position(lastFramePos);
			tmp.get(curFrame);

			if (writeTimeMillis < writeTimeMillisMin)
				writeTimeMillisMin = writeTimeMillis;
			if (writeTimeMillis > writeTimeMillisMax)
				writeTimeMillisMax = writeTimeMillis;
			writeTimeMillisMean = ((writeTimeMillisMean * (iterations - 10 - 1)) + writeTimeMillis) / (iterations - 10);

			boolean DEVICE_SENT_SECOND = (writeTimeMillis < writeTimeMillisMean) ? true : false;
			boolean CTRL_SENT_REQ_DEV_SENT_FIRST = (writeTimeMillis > writeTimeMillisMean) ? true : false;
			boolean CTRL_SENT_REQ_DEV_SENT_FIRST_VERIFIED = (incoming
					.get(lastFramePos + busConfig.getBUS_PING_OFFSET()) == busConfig.getBUS_PING_FUNCTION()) ? true
							: false;
			boolean DEVICE_SENT_SECOND_VERIFIED = (incoming
					.get(lastFramePos + busConfig.getBUS_PING_OFFSET()) != busConfig.getBUS_PING_FUNCTION()) ? true
							: false;
			CAN_WRITE = DEVICE_SENT_SECOND_VERIFIED && (backoff <= 0);
			if (CTRL_SENT_REQ_DEV_SENT_FIRST_VERIFIED && CTRL_SENT_REQ_DEV_SENT_FIRST) {
				backoff--;
				iterationsWriteWindow++;
				if (writeWindowMean == 0)
					writeWindowMean = writeTimeMillis;
				writeWindowMean = ((writeWindowMean * (iterationsWriteWindow - 1)) + writeTimeMillis)
						/ (iterationsWriteWindow);
			}

			String frame_str = HexString.convertToHexadecimal(curFrame);
			logger.log(Level.FINE, "-------------------");
			logger.log(Level.FINE, frame_str);
			logger.log(Level.FINE,
					"Read:" + bytes_read + " WTimeC:" + writeTimeMillis + " Min:" + writeTimeMillisMin + " Max:"
							+ writeTimeMillisMax + " Mean:" + writeTimeMillisMean + " WriteWindowMean:"
							+ writeWindowMean + (DEVICE_SENT_SECOND ? " :: DEVICE_SENT_SECOND" : "")
							+ (CTRL_SENT_REQ_DEV_SENT_FIRST ? " ::CTRL_SENT_REQ_DEV_SENT_FIRST" : "")
							+ (CTRL_SENT_REQ_DEV_SENT_FIRST_VERIFIED ? " ::CTRL_SENT_REQ_DEV_SENT_FIRST_VERIFIED" : "")
							+ (DEVICE_SENT_SECOND_VERIFIED ? " ::DEVICE_SENT_SECOND_VERIFIED" : "") + " CAN_SEND"
							+ (CAN_WRITE ? " ::YES" : "::NO"));

			logger.log(Level.FINE, bytes_read + " bytes read ");
			currentPos = 0;
			lastFramePos = 0;
		}

		writeTimeMillis = 0;
	}

	public boolean canWrite() {
		return CAN_WRITE;
	}

	public long getWriteTimeMillisMean() {
		return writeTimeMillisMean;
	}

	public boolean getTimersReady() {
		return (iterations > 10);
	}

	public void resetSendClearance() {
		backoff = busConfig.getBUS_MIN_WATING_ITERATIONS_AFTER_SEND();
		
	}
}
