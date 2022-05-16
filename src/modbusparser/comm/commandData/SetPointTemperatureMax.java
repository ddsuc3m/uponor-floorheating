package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;
import modbusparser.util.TemperatureUtils;

public class SetPointTemperatureMax extends CommandData {

	public static final byte SETPOINT_TEMPERATURE_MAX = 0x38;
	float fahrenheit = (float) 0.0;
	float celsius = (float) 0.0;
	public SetPointTemperatureMax(byte[] payload) {
		super(SETPOINT_TEMPERATURE_MAX, payload);
		// flip the order from buffer!! so MSB is 0
		int temperature_ = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		fahrenheit = (float) temperature_/10;
		celsius = TemperatureUtils.round(TemperatureUtils.farenheitToCelsius(fahrenheit),2);	
		
	}
	
	public SetPointTemperatureMax() {
		super(SETPOINT_TEMPERATURE_MAX);
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
		return "SetPoint Temp Max [" + HexString.convertToHexadecimal(SETPOINT_TEMPERATURE_MAX) + "]( " + HexString.convertToHexadecimal(payload[0]) + ":" + HexString.convertToHexadecimal(payload[1]) + " -> "+ fahrenheit +"F/" + celsius   + "C ) || "; 
	}


	@Override
	public void generatePayload() {
		int temperature_ = (int) (fahrenheit * 10);
		byte[] temp = HexUtils.getWordFromInt(temperature_);
		payload[1]=temp[1];
		payload[0]=temp[0];
	}
}
