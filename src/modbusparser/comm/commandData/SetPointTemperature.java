package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;
import modbusparser.util.TemperatureUtils;

public class SetPointTemperature extends CommandData {

	public static final byte SETPOINT_TEMPERATURE = 0x3B;
	float fahrenheit = (float) 0.0;
	float celsius = (float) 0.0;

	public SetPointTemperature(byte[] payload) {
		super(SETPOINT_TEMPERATURE, payload);
		// flip the order from buffer!! so MSB is 0
		int temperature_ = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		fahrenheit = (float) temperature_ / 10;
		celsius = TemperatureUtils.round(TemperatureUtils.farenheitToCelsius(fahrenheit), 2);

	}

	public SetPointTemperature() {
		super(SETPOINT_TEMPERATURE);
	}

	public void setPayloadZero() {
		fahrenheit = 0000f;
	}

	public void setCelsius(float celsius) {
		this.celsius = celsius;
		fahrenheit = ((celsius * 9) / 5) + 32;
	}

	public float getCelsius() {
		return celsius;
	}

	@Override
	public String toString() {

		return "SetPoint Temp[" + HexString.convertToHexadecimal(SETPOINT_TEMPERATURE) + "]( "
				+ HexString.convertToHexadecimal(payload[0]) + ":" + HexString.convertToHexadecimal(payload[1]) + " -> "
				+ fahrenheit + "F/" + celsius + "C )  || ";
	}

	@Override
	public void generatePayload() {
		int temperature_ = (int) (fahrenheit * 10);
		byte[] temp = HexUtils.getWordFromInt(temperature_);
		payload[1] = temp[1];
		payload[0] = temp[0];
	}

	public static void main(String args[]) {
		SetPointTemperature rt = new SetPointTemperature();
		rt.setCelsius((float) 25.72);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print 3B030F
		rt.setCelsius((float) 22);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print 71.6 -> 3B02CC

	}
}
