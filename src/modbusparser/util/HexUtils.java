package modbusparser.util;

import java.nio.ByteBuffer;

public class HexUtils {
	
	public static Integer getIntegerFrom16bWord(byte b1, byte b2) {
		byte[] int_bytes = new byte[] { 0x00, 0x00, b1, b2 };
		ByteBuffer wrapped = ByteBuffer.wrap(int_bytes);
		return Integer.valueOf(wrapped.getInt());
	}
	
	public static int getIntFrom16bWord(byte b1, byte b2) {
		byte[] int_bytes = new byte[] { 0x00, 0x00, b1, b2 };
		ByteBuffer wrapped = ByteBuffer.wrap(int_bytes);
		return wrapped.getInt();
	}
	
	// From https://stackoverflow.com/questions/12893758/how-to-reverse-the-byte-array-in-java
	public static void reverse(byte[] array) {
	      if (array == null) {
	          return;
	      }
	      int i = 0;
	      int j = array.length - 1;
	      byte tmp;
	      while (j > i) {
	          tmp = array[j];
	          array[j] = array[i];
	          array[i] = tmp;
	          j--;
	          i++;
	      }
	  }

	public static byte[] getWordFromInt(int num) {
		ByteBuffer bbint = ByteBuffer.allocate(4);
		bbint.putInt(num);
		byte[] bint = new byte[] {bbint.get(3),bbint.get(2)};
		return bint;
	}

}
