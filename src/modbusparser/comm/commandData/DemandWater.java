package modbusparser.comm.commandData;

import modbusparser.util.HexString;
import modbusparser.util.HexUtils;

public class DemandWater extends CommandData {

	public static final byte DEMAND_WATER = 0x3D;
	
	// 0x0040
	private final int TRUE = 0x0040;

	// 0x0000
	private final int HOT = 0x0000;
	private boolean demanding = false;
	private boolean cooling = false;
	private boolean heating = false;
	
	public DemandWater() {
		super(DEMAND_WATER);
	}

	public DemandWater(byte[] payload) {
		super(DEMAND_WATER, payload);
		// flip the order from buffer!! so MSB is 0
		int currently = HexUtils.getIntFrom16bWord(payload[0], payload[1]);
		
		demanding = ((currently&0x00FF) == TRUE) ? true : false;
		heating = ((currently&0xFF00) == HOT) ? true : false;
		cooling = !heating;

	}

	public void setDemanding(boolean demanding) {
		this.demanding = demanding;
	}

	public boolean isDemanding() {
		return demanding;
	}
	
	

	public boolean isCold() {
		return cooling;
	}

	public void setCold(boolean cold) {
		this.cooling = cold;
		this.heating = !cold;
	}

	public boolean isHot() {
		return heating;
	}

	public void setHot(boolean hot) {
		this.heating = hot;
		this.cooling = !hot;
	}

	@Override
	public String toString() {
		return "Demand water[" + HexString.convertToHexadecimal(DEMAND_WATER) + "]( " + HexString.convertToHexadecimal(payload[0]) + ":"
				+ HexString.convertToHexadecimal(payload[1]) + " -> " + (demanding?"YES":"NO") + " [" + (heating?"heating":"cooling") + "] ) || ";
	}
	
	
	public void generatePayload() {
		
		if (demanding) {
			payload[0] = 0x40;
		}else {
			payload[0] = 0x00;
		}
		
		if(heating) {
			payload[1] = 0x00;
		}else {
			payload[1] = 0x10;
		}
	}
	
	public static void main(String args[]) {
		DemandWater dw = new DemandWater();
		dw.setDemanding(true);
		dw.setCold(true);
		System.out.println(HexString.convertToHexadecimal(dw.getEncoding())); // should print 3D1040
		dw.setDemanding(false);
		dw.setHot(true);
		System.out.println(HexString.convertToHexadecimal(dw.getEncoding())); // should print 3D0000
		
	}
}
