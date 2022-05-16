package modbusparser.util;

import java.nio.ByteBuffer;

public class HexString {

	public static String convertToHexadecimal(byte[] byteArray) {
		String hex = "";

		// Iterating through each byte in the array
		for (byte i : byteArray) {
			hex += String.format("%02X", i);
		}

		return hex;
	}
	public static String convertToHexadecimal(byte b)
	{
		return String.format("%02X", (byte)b);
	}
	public static String convertToHexadecimal(ByteBuffer bufferToWrite) {
		return convertToHexadecimal(bufferToWrite.array());
	}
}
