package entities;

import configuration.SystemConfig;
import modbusparser.util.HexString;
import modbusparser.util.HexUtils;

public class EntityID {
	//BEndian
	private byte syspreamble1 =  0;
	private byte syspreamble2 =  0;
	private byte addr1;
	private byte addr2;

	public EntityID(byte c, byte d, SystemConfig systemConfig) {
		addr1 = c;
		addr2 = d;
		syspreamble1 =  systemConfig.getSYSTEM_FIRST_BYTE();
		syspreamble2 =  systemConfig.getSYSTEM_SECOND_BYTE();
	}
	
	public Integer getSystemIDInt()
	{
		return getIDIntegerFromAddress(syspreamble1, syspreamble2);
	}

	public String getSystemIDStr()
	{
		return getIDStringFromAddress(syspreamble1, syspreamble2);
	}
	
	public Integer getIDInt()
	{
		return getIDIntegerFromAddress(addr1, addr2);
	}
	
	public String getIDStr()
	{
		return getIDStringFromAddress(addr1, addr2);
	}
	
	public static String getIDStringFromAddress(byte addr1, byte addr2) {
		return HexString.convertToHexadecimal(addr1)+HexString.convertToHexadecimal(addr2);
	}
	public static Integer getIDIntegerFromAddress(byte addr1, byte addr2) {
		return HexUtils.getIntegerFrom16bWord(addr1, addr2);
	}

	public byte getAddr1() {
		return addr1;
	}

	public byte getAddr2() {
		return addr2;
	}

	

}
