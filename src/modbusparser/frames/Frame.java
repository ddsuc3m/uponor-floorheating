package modbusparser.frames;

import java.nio.ByteBuffer;

import configuration.Configuration;
import modbusparser.util.HexString;

public abstract class Frame {
	
	FrameType frameType;
	ByteBuffer rawFrame;
	public byte address1;
	public byte address2;
	byte[] crc;
	byte[] crc_calc;
	byte[] frame_without_crc;
	boolean crc_calculated = false;
	boolean crc_ok = false;
	Configuration config;
	
	
	
	public FrameType getFrameType() {
		return frameType;
	}

	public Frame(FrameType frameType, Configuration config) {
		super();
		this.config = config;
		this.frameType = frameType;
	}

	public ByteBuffer getRawFrame() {
		return rawFrame;
	}
	
	public void setAddress(byte address1, byte address2)
	{
		this.address1 = address1;
		this.address2 = address2;
	}
	

	public boolean isCrc_ok() {
		if(crc_calculated)
			return crc_ok;
		else {
			calculateValidateCRC();
			return crc_ok;
		}
	}


	public void setRawFrame(ByteBuffer rawFrame_) {
		crc_calculated = false;
		crc_ok=false;
		rawFrame_.flip();
		rawFrame = ByteBuffer.allocate(rawFrame_.limit());
		rawFrame.put(rawFrame_);
		frame_without_crc = new byte[rawFrame.limit()-2];
		rawFrame.flip();
		rawFrame.get(frame_without_crc);
		
		calculateValidateCRC();
		
	}
	
	private void calculateValidateCRC()
	{
		crc_calc = CRC.getCRC(frame_without_crc);
		crc_calculated = true;
		if(crc_calc[0]==crc[0] && crc_calc[1]==crc[1])
		{
			crc_ok = true;
		}
	}

	public byte getAddress1() {
		return address1;
	}

	public void setAddress1(byte address) {
		this.address1 = address;
	}
	
	public byte getAddress2() {
		return address2;
	}

	public void setAddress2(byte address) {
		this.address2 = address;
	}


	public byte[] getCrc() {
		return crc;
	}

	public void setCrc(byte[] crc) {
		crc_calculated = false;
		crc_ok=false;
		this.crc = crc;
	}
	
	@Override
	public String toString()
	{
		String repr = (frameType==FrameType.REQUEST?"<-":"->") + "  " 
	+ HexString.convertToHexadecimal(this.getRawFrame().array()) + "\n" 
	+ HexString.convertToHexadecimal((byte) config.getSystemConfig().getSYSTEM_FIRST_BYTE())  + " " 
	+ HexString.convertToHexadecimal((byte) config.getSystemConfig().getSYSTEM_SECOND_BYTE())  + " | "
	+ HexString.convertToHexadecimal((byte) address1)  + " " 
	+ HexString.convertToHexadecimal((byte) address2) + " | ";
		repr += payloadToString();
		repr += "CRC " + HexString.convertToHexadecimal(crc) + " [" + (isCrc_ok()?" OK ": " Err ") + "]";
		return repr;
		
	}

	protected abstract String payloadToString();

	public ByteBuffer encode() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
