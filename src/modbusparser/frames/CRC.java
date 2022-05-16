package modbusparser.frames;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class CRC {

	// FROM https://programmerclick.com/article/1179853890/

	public static String byteToStr(byte[] b, int size) {
		String ret = "";
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	/**
	 * Convertir una cadena hexadecimal a byte []
	 *
	 * @param str
	 * @return
	 */
	public static byte[] toBytes(String str) {
		byte[] bytes = new BigInteger(str, 16).toByteArray();
		return bytes;
	}

	public static byte[] getCRC(String str) {
		byte[] bytes = toBytes(str);
		return getCRC(bytes);
	}
	public static String getStringCRC(String str) {
		byte[] bytes = toBytes(str);
		return getCRCString(bytes);
	}
	public static byte[] getCRC(byte[] bytes) {
		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;

		int i, j;
		for (i = 0; i < bytes.length; i++) {
			CRC ^= ((int) bytes[i] & 0x000000ff);
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) != 0) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}
		byte[] CRCbytes = ByteBuffer.allocate(4).putInt(CRC).array();
		return new byte[]{CRCbytes[3],CRCbytes[2]};
	}

	public static String getCRCString(byte[] bytes) {
		int CRC = 0x0000ffff;
		int POLYNOMIAL = 0x0000a001;

		int i, j;
		for (i = 0; i < bytes.length; i++) {
			CRC ^= ((int) bytes[i] & 0x000000ff);
			for (j = 0; j < 8; j++) {
				if ((CRC & 0x00000001) != 0) {
					CRC >>= 1;
					CRC ^= POLYNOMIAL;
				} else {
					CRC >>= 1;
				}
			}
		}
		@SuppressWarnings("unused")
		byte[] CRCbytes = ByteBuffer.allocate(4).putInt(CRC).array();
		String crc = Integer.toHexString(CRC);
		if (crc.length() == 2) {
			crc = "00" + crc;
		} else if (crc.length() == 3) {
			crc = "0" + crc;
		}
		crc = crc.substring(2, 4) + crc.substring(0, 2);
		return crc.toUpperCase();
	}
	
	public static void main(String[] args)
	{
	  //System.out.println(Integer.toHexString(new getCRC("1104706dff")));	//should be b4 38
	}

}
