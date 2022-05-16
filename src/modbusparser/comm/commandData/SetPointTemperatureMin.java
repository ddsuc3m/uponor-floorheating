package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;
import modbusparser.util.TemperatureUtils;

public class SetPointTemperatureMin extends CommandData {

	public static final byte SETPOINT_TEMPERATURE_MIN = 0x37;
	float fahrenheit = (float) 0.0;
	float celsius = (float) 0.0;
	public SetPointTemperatureMin(byte[] payload) {
		super(SETPOINT_TEMPERATURE_MIN, payload);
		// flip the order from buffer!! so MSB is 0
		int temperature_ = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		fahrenheit = (float) temperature_/10;
		celsius = TemperatureUtils.round(TemperatureUtils.farenheitToCelsius(fahrenheit),2);	
		
	}
	public SetPointTemperatureMin() {
		super(SETPOINT_TEMPERATURE_MIN);
	}
	@Override
	public String toString() {
		return "SetPoint Temp Min [" + HexString.convertToHexadecimal(SETPOINT_TEMPERATURE_MIN) + "] ( " + HexString.convertToHexadecimal(payload[0]) + ":" + HexString.convertToHexadecimal(payload[1]) + " -> "+ fahrenheit +"F/" + celsius   + "C ) || "; 
	}
	
	public void setCelsius(float celsius) {
		this.celsius = celsius;
		fahrenheit = ((celsius * 9) / 5) + 32;
	}
	
	public float getCelsius() {
		return celsius;
	}

	
	@Override
	public void generatePayload() {
		int temperature_ = (int) (fahrenheit * 10);
		byte[] temp = HexUtils.getWordFromInt(temperature_);
		payload[1]=temp[1];
		payload[0]=temp[0];
	}
}
