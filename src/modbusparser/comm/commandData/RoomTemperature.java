package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;
import modbusparser.util.TemperatureUtils;

public class RoomTemperature extends CommandData {

	public static final byte ROOM_TEMPERATURE = (byte) 0x40;
	float fahrenheit = (float) 0.0;
	float celsius = (float) 0.0;
	
	public RoomTemperature() {
		super(ROOM_TEMPERATURE);
	}

	public RoomTemperature(byte[] payload) {
		super(ROOM_TEMPERATURE, payload);
		// flip the order from buffer!! so MSB is 0
		int temperature_ = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		fahrenheit = (float) temperature_ / 10;
		celsius = TemperatureUtils.round(TemperatureUtils.farenheitToCelsius(fahrenheit), 2);

	}

	public float getCelsius() {
		return celsius;
	}

	public void setCelsius(float celsius) {
		this.celsius = celsius;
		fahrenheit = ((celsius * 9) / 5) + 32;
	}

	@Override
	public String toString() {
		return "Room Temp[" + HexString.convertToHexadecimal(ROOM_TEMPERATURE) + "]( " + HexString.convertToHexadecimal(payload[0]) + ":"
				+ HexString.convertToHexadecimal(payload[1]) + " -> " + fahrenheit + "F/" + celsius + "C ) || ";
	}

	@Override
	public void generatePayload() {
		int temperature_ = (int) (fahrenheit * 10);
		byte[] temp = HexUtils.getWordFromInt(temperature_);
		payload[1]=temp[1];
		payload[0]=temp[0];
	}
	
	public static void main(String args[]) {
		RoomTemperature rt = new RoomTemperature();
		rt.setCelsius((float)25.72);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print 40030F
		rt.setCelsius((float)22);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print 71.6 -> 4002CC
		
	}
}
