package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;

public class HeatingCooling extends CommandData {

	// I have observer in winter is 0x0C02 in summer 0x1C02
	public static final byte HEATING_COOLING = 0x3F;

	
	private int mode_mask = 0x1000;
	private boolean cooling_mode = false;
	private boolean heating_mode = false;
	private int heating_allowed_mask = 0x800;
	private int cooling_allowed_mask = 0x400;
	private boolean heating_allowed = false;
	private boolean cooling_allowed = false;
	

	public HeatingCooling() {
		super(HEATING_COOLING);
	}

	public HeatingCooling(byte[] payload) {
		super(HEATING_COOLING, payload);
		// flip the order from 0uffer!! so MSB is 0
		int currently = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		cooling_mode = ((currently&mode_mask)==0x1000)?true:false;
		heating_mode = ((currently&mode_mask)==0x0000)?true:false;
		cooling_allowed = ((currently&cooling_allowed_mask)==cooling_allowed_mask)?true:false;
		heating_allowed = ((currently&heating_allowed_mask)==heating_allowed_mask)?true:false;
		
    }

	
	public boolean isCooling_mode() {
		return cooling_mode;
	}

	public void setCooling_mode(boolean cooling_mode) {
		this.cooling_mode = cooling_mode;
		this.heating_mode = !cooling_mode;
	}

	public boolean isHeating_mode() {
		return heating_mode;
	}

	public void setHeating_mode(boolean heating_mode) {
		this.heating_mode = heating_mode;
		this.cooling_mode = !heating_mode;
	}

	public boolean isHeating_allowed() {
		return heating_allowed;
	}

	public void setHeating_allowed(boolean heating_allowed) {
		this.heating_allowed = heating_allowed;
	}

	public boolean isCooling_allowed() {
		return cooling_allowed;
	}

	public void setCooling_allowed(boolean cooling_allowed) {
		this.cooling_allowed = cooling_allowed;
	}

	@Override
	public String toString() {
		return "Heating Cooling [" + HexString.convertToHexadecimal(HEATING_COOLING) + "] ( " + HexString.convertToHexadecimal(payload[0]) + ":"
				+ HexString.convertToHexadecimal(payload[1]) + " -> " + " [" + " CMode:"+ (cooling_mode?"Y":"N") + " HMode:"+ (heating_mode?"Y":"N") + " HAllow:"+ (heating_allowed?"Y":"N") + " CAllow:"+ (cooling_allowed?"Y":"N") + "] ) || ";
	}
	
	
	public void generatePayload() {
		
		payload[0] = 0x02;
		payload[1] = 0x00;
		if(cooling_mode) 
			payload[1] |= 0x10;
		
		if(cooling_allowed)
		    payload[1] |= 0x04;
		if(heating_allowed)
		    payload[1] |= 0x08;
	}
	
	public static void main(String args[]) {
		HeatingCooling dw = new HeatingCooling();
		dw.setHeating_mode(true);
		dw.setHeating_allowed(true);
		dw.setCooling_allowed(true);
		System.out.println(HexString.convertToHexadecimal(dw.getEncoding())); // should print CF0C02
		dw.setHeating_mode(false);
		dw.setHeating_allowed(true);
		dw.setCooling_allowed(true);
		System.out.println(HexString.convertToHexadecimal(dw.getEncoding())); // should print 3F1C02
		dw.setCooling_mode(true);
		dw.setHeating_allowed(true);
		dw.setCooling_allowed(false);
		System.out.println(HexString.convertToHexadecimal(dw.getEncoding())); // should print 3F1802
		
	}
}
