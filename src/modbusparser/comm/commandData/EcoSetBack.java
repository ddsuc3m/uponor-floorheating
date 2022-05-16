package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;

public class EcoSetBack extends CommandData {

	public static final byte ECO_SET_BACK = (byte) 0x3C;
	float fahrenheit = (float) 0.0;
	float celsius = (float) 0.0;
	
	public EcoSetBack() {
		super(ECO_SET_BACK);
	}

	public EcoSetBack(byte[] payload) {
		super(ECO_SET_BACK, payload);
		// flip the order from buffer!! so MSB is 0
		int temperature_ = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		fahrenheit = (float) temperature_;
		celsius = fahrenheit/18;

	}

	public float getCelsius() {
		return celsius;
	}

	public void setCelsius(float celsius) {
		this.celsius = celsius;
		fahrenheit = ((celsius * 18));
	}

	@Override
	public String toString() {
		return "Eco SetBack[" + HexString.convertToHexadecimal(ECO_SET_BACK) + "]( " + HexString.convertToHexadecimal(payload[0]) + ":"
				+ HexString.convertToHexadecimal(payload[1]) + " -> " + fahrenheit + " Val/" + celsius + "C ) || ";
	}

	@Override
	public void generatePayload() {
		int temperature_ = (int) (fahrenheit);
		byte[] temp = HexUtils.getWordFromInt(temperature_);
		payload[1]=temp[1];
		payload[0]=temp[0];
	}
	
	public static void main(String args[]) {
		EcoSetBack rt = new EcoSetBack();
		rt.setCelsius((float)6.0);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print 3C006C
		rt.setCelsius((float)4);
		System.out.println(HexString.convertToHexadecimal(rt.getEncoding())); // should print  3C0048
		
	}
}
