package modbusparser.util;

public class TemperatureUtils {

	
	public static float farenheitToCelsius(float fahrenheit)
	{
		float  celsius =(float) (( 5 *(fahrenheit - 32.0)) / 9.0);
		return celsius;
	}
	
	
	// from https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	public static float round(float value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (float) tmp / factor;
	}
}
