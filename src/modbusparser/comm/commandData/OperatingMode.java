package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;

public class OperatingMode extends CommandData {

	public static final byte OPERATING_MODE = (byte) 0x3E;

	private int opmodeint = 0;
	private OPMODE mode;

	public enum OPMODE {
		ROOM_TEMPERATURE(0x0000, "Room Temperature"), INTERNAL_FLOOR_SENSOR(0x0002, "Internal floor sensor"),
		REMOTE_OUTDOOR_SENSOR(0x0003, "Remote Outdoor sensor"), REMOTE_SENSOR(0xB300, "Remote sensor");

		int code;
		String description;

		OPMODE(int code, String desc) {
			this.code = code;
			this.description = desc;
		}

	}

	public OperatingMode() {
		super(OPERATING_MODE);
	}

	public OperatingMode(byte[] payload) {
		super(OPERATING_MODE, payload);
		opmodeint = HexUtils.getIntFrom16bWord(payload[1], payload[0]);
		if (opmodeint == OPMODE.ROOM_TEMPERATURE.code)
			mode = OPMODE.ROOM_TEMPERATURE;
		if (opmodeint == OPMODE.INTERNAL_FLOOR_SENSOR.code)
			mode = OPMODE.INTERNAL_FLOOR_SENSOR;
		if (opmodeint == OPMODE.REMOTE_OUTDOOR_SENSOR.code)
			mode = OPMODE.REMOTE_OUTDOOR_SENSOR;
		if (opmodeint == OPMODE.REMOTE_SENSOR.code)
			mode = OPMODE.REMOTE_SENSOR;
	}

	public OPMODE getMode() {
		return mode;
	}

	public void setMode(OPMODE mode) {
		this.mode = mode;
		this.opmodeint = mode.code;
	}

	@Override
	public String toString() {
		return "Operating mode [" + HexString.convertToHexadecimal(OPERATING_MODE) + "] ( " + HexString.convertToHexadecimal(payload[0]) + ":"
				+ HexString.convertToHexadecimal(payload[1]) + " -> " + mode.name() + " || ";
	}

	@Override
	public void generatePayload() {
		if (opmodeint == OPMODE.ROOM_TEMPERATURE.code) {
			payload[1] = 0x00;
			payload[0] = 0x00;
		}
		if (opmodeint == OPMODE.INTERNAL_FLOOR_SENSOR.code) {
			payload[1] = 0x00;
			payload[0] = 0x01;
		}
		if (opmodeint == OPMODE.REMOTE_OUTDOOR_SENSOR.code) {
			payload[1] = 0x00;
			payload[0] = 0x02;
		}
		if (opmodeint == OPMODE.REMOTE_SENSOR.code) {
			payload[1] = (byte) 0xB3;
			payload[0] = 0x00;
		}
	}

	public static void main(String args[]) {
		OperatingMode om = new OperatingMode();
		om.setMode(OPMODE.ROOM_TEMPERATURE);
		System.out.println(HexString.convertToHexadecimal(om.getEncoding())); // should print 3E0000
		om.setMode(OPMODE.REMOTE_SENSOR);
		System.out.println(HexString.convertToHexadecimal(om.getEncoding())); // should print 3EB300

	}
}
