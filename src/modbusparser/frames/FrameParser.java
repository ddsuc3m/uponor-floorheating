package modbusparser.frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import configuration.BusConfig;
import configuration.Configuration;
import configuration.SystemConfig;
import modbusparser.comm.commandData.CommandData;
import modbusparser.exception.NotEnoughData;

public class FrameParser {

	private Configuration configuration;
	private SystemConfig systemConfig;
	private Logger logger;
	private int updatedIncomingPointer = 0;
	{
		logger = Logger.getLogger(FrameParser.class.getName());
	}

	/*
	 * the minimum length I have been able to see is 7 bytes long (MASTER Querying
	 * thermostats)
	 */

	/*
	 * in my installation frames are started with preamble 0x11 0x04 0x70. While
	 * this may be the modbus preample captured, every frame starts this way
	 * irrespectively of the content. From this preamble onwards seems to be not
	 * standard modbus... Example:: 11 04 70 6d ff b4 38 11 04 70 6d -- Address of
	 * thermostat ff -- Function FF (not MODBUS) b4 38 -- CRC (matches)
	 */

	public FrameParser(Configuration configuration) {
		this.configuration = configuration;
		this.systemConfig = configuration.getSystemConfig();
		
	}
	
	public int getUpdatedIncomingPointer() {
		return updatedIncomingPointer;
	}

	public void clearUpdatedIncomingPointer() {
		this.updatedIncomingPointer = 0;
	}



	public boolean checkPreamble(ByteBuffer data, int start) {
		if (data.get(start) == ((byte) systemConfig.getSYSTEM_FIRST_BYTE())
				&& data.get(start + 1) == ((byte) systemConfig.getSYSTEM_SECOND_BYTE())) {
			return true;

		}
		return false;
	}

	public CommandData getResponseData(ByteBuffer data, int frameNext, ByteBuffer frame) {
		if (frameNext >= data.position() + 3) {

			byte dataId = data.get();
			frame.put(dataId);
			byte[] payload = new byte[2];
			data.get(payload);
			frame.put(payload);
			CommandData resData = CommandData.getResponseData(dataId, payload);
			return resData;

		} else {

			return null;
		}
	}

	public @Nullable List<Frame> getConsistenData(@Nonnull ByteBuffer incoming, Object bufferWaitObject, BusConfig busConfig) {
		// Do not touch incoming as it will be handled by other thread
		// keep incoming buffer pointers untouched

		ByteBuffer frame = null;
		// utility variables
		boolean FRAME_FOUND = false;
		boolean FRAME_TYPE_FOUND = false;
		boolean FRAME_DATA_PROCESSED = false;
		@SuppressWarnings("unused")
		int FRAME_STARTS = 0;
		int FRAME_NEXT = 0;

		byte address1 = (byte) 0x00;
		byte address2 = (byte) 0x00;
		byte function = (byte) 0x00;
		Frame currentFrame = null;
		List<Frame> frames = null;
		int updatedIncomingPossition = 0;

		try {
			// we only touch working! not incoming
			ByteBuffer working = incoming.duplicate();
			logger.log(Level.FINEST, "Working POS (PRE-FLIP):" + working.position()
					+ " LIM:" + working.limit() + " REM:" + working.remaining());
			working.flip();
			logger.log(Level.FINEST, "Working POS (POST-FLIP):"
					+ working.position() + " LIM:" + working.limit() + " REM:" + working.remaining());

			while (working.hasRemaining()) {
				logger.log(Level.FINEST, "Processing frame");
				frame = ByteBuffer.allocate(busConfig.getFRAME_MAX_LEN());
				// incoming may change during this time, but we just process what e have so far!
				// get current possition
				int p = working.position();
				if (!FRAME_FOUND) {
					// look for preamble
					if (checkPreamble(working, p)) {
						FRAME_STARTS = p;
						FRAME_FOUND = true;
						// create working buffer
						frame.put(new byte[] { systemConfig.getSYSTEM_FIRST_BYTE(), systemConfig.getSYSTEM_SECOND_BYTE() });
						// forward the pointer
						working.position(p + 2);
					}
					// get Address1 (should be p+3)
					address1 = working.get();
					// get Address2 (should be p+4)
					address2 = working.get();

				}
				if (FRAME_FOUND && !FRAME_TYPE_FOUND) {
					// request: we just look for a FF (seems there is no response value starting
					// with FF, count for CRC and look if after that there is another preamble or
					// the end of the buffer
					// response: otherwise
					p = working.position();
					if (working.remaining() < 6) {
						// not enough data yet. Leave it alone.
						throw new NotEnoughData();
					}
					byte fn = working.get(p);
					byte fn_check = (byte) 0xFF;
					if (fn == fn_check && checkPreamble(working, p + 3)) {
						// it is clearly a REQUEST
						function = working.get();
						currentFrame = new RequestFrame(configuration);
						currentFrame.setAddress1(address1);
						currentFrame.setAddress2(address2);
						frame.put(address1);
						frame.put(address2);
						FRAME_TYPE_FOUND = true;
						FRAME_NEXT = working.position() + 2; // CRC

					} else {
						// it is a RESPONSE
						// look until new frame

						int p_ = p;
						while (p_ < working.limit()) {
							if (checkPreamble(working, p_)) {
								FRAME_NEXT = p_;
								FRAME_TYPE_FOUND = true;
								currentFrame = new ResponseFrame(configuration);
								currentFrame.setAddress1(address1);
								currentFrame.setAddress2(address2);
								frame.put(address1);
								frame.put(address2);
								break;
							}
							p_++;
						}
						if (!FRAME_TYPE_FOUND) {
							// not enough data yet. Leave it alone.
							throw new NotEnoughData();
						}

					}
				}
				if (FRAME_FOUND && FRAME_TYPE_FOUND && !FRAME_DATA_PROCESSED) {
					if (currentFrame.frameType == FrameType.REQUEST) {
						// copy function
						((RequestFrame) currentFrame).setFunction(function);
						frame.put(function);
						FRAME_DATA_PROCESSED = true;
					}
					if (currentFrame.frameType == FrameType.RESPONSE) {
						CommandData resData = null;
						while ((resData = getResponseData(working, FRAME_NEXT, frame)) != null) {
							((ResponseFrame) currentFrame).commandData.add(resData);
						}
						FRAME_DATA_PROCESSED = true;
					}
				}
				if (FRAME_FOUND && FRAME_TYPE_FOUND && FRAME_DATA_PROCESSED) {
					byte[] crc = new byte[2];
					working.get(crc);
					frame.put(crc);
					currentFrame.setCrc(crc);
					currentFrame.setRawFrame(frame);
				}
				if (currentFrame != null) {
					if (frames == null) {
						frames = new ArrayList<>();
					}
					frames.add(currentFrame);
					this.updatedIncomingPointer = FRAME_NEXT;
					// incoming.position(FRAME_NEXT);
					FRAME_FOUND = false;
					FRAME_TYPE_FOUND = false;
					FRAME_DATA_PROCESSED = false;
					FRAME_STARTS = 0;
					FRAME_NEXT = 0;
					currentFrame = null;
					frame.clear();
				}

			} // while
		} catch (NotEnoughData e) {

		}
		if (frames != null) {
			logger.log(Level.FINE, "Frames processed " + frames.size());
		} else {
			logger.log(Level.FINE, "Frames processed 0");
		}
		// this is the only place in which we touch the buffer:


		return frames;
	}
}
