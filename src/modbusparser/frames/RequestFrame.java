package modbusparser.frames;

import configuration.Configuration;
import modbusparser.util.HexString;

public class RequestFrame extends Frame {

	public RequestFrame(Configuration config) {
		super(FrameType.REQUEST, config);
	}
	

	public byte function;
	
	@Override
	protected String payloadToString() {
		return HexString.convertToHexadecimal(function) + " | ";
	}

	public byte getFunction() {
		return function;
	}

	public void setFunction(byte funcion) {
		this.function = funcion;
	}
    
}
