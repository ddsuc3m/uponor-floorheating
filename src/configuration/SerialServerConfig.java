package configuration;


public class SerialServerConfig {
	String MODBUS_TCP_ADDRESS;
	int MODBUS_TCP_PORT;
	int MAX_MEAN_TIMES_TIMEOUT = 6;

	public SerialServerConfig() {
		super();
	}

	public String getMODBUS_TCP_ADDRESS() {
		return MODBUS_TCP_ADDRESS;
	}

	public void setMODBUS_TCP_ADDRESS(String mODBUS_TCP_ADDRESS) {
		MODBUS_TCP_ADDRESS = mODBUS_TCP_ADDRESS;
	}

	public int getMODBUS_TCP_PORT() {
		return MODBUS_TCP_PORT;
	}

	public void setMODBUS_TCP_PORT(int mODBUS_TCP_PORT) {
		MODBUS_TCP_PORT = mODBUS_TCP_PORT;
	}

	public int getMAX_MEAN_TIMES_TIMEOUT() {
		return MAX_MEAN_TIMES_TIMEOUT;
	}

	public void setMAX_MEAN_TIMES_TIMEOUT(int mAX_MEAN_TIMES_TIMEOUT) {
		MAX_MEAN_TIMES_TIMEOUT = mAX_MEAN_TIMES_TIMEOUT;
	}

}
