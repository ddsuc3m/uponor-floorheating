package configuration;

public class SystemConfig {

	byte SYSTEM_FIRST_BYTE = 0x11;
	byte SYSTEM_SECOND_BYTE = 0x04;
	int SYSTEM_HEADER_LEN = 2;
	int ADDRESS_LEN = 2;
	int COMMANDDATA_LEN = 3;
	int CRC_LEN = 2;
	int COMMANDDATA_LENGTH = 3;
	float COOLING_SETPOINT_OFFSET = 2.00f;
	float HEATING_SETPOINT_OFFSET = 0.00f;

	public SystemConfig() {
		super();
	}

	public byte getSYSTEM_FIRST_BYTE() {
		return SYSTEM_FIRST_BYTE;
	}

	public void setSYSTEM_FIRST_BYTE(byte sYSTEM_FIRST_BYTE) {
		SYSTEM_FIRST_BYTE = sYSTEM_FIRST_BYTE;
	}

	public byte getSYSTEM_SECOND_BYTE() {
		return SYSTEM_SECOND_BYTE;
	}

	public void setSYSTEM_SECOND_BYTE(byte sYSTEM_SECOND_BYTE) {
		SYSTEM_SECOND_BYTE = sYSTEM_SECOND_BYTE;
	}

	public int getSYSTEM_HEADER_LEN() {
		return SYSTEM_HEADER_LEN;
	}

	public void setSYSTEM_HEADER_LEN(int sYSTEM_HEADER_LEN) {
		SYSTEM_HEADER_LEN = sYSTEM_HEADER_LEN;
	}

	public int getADDRESS_LEN() {
		return ADDRESS_LEN;
	}

	public void setADDRESS_LEN(int aDDRESS_LEN) {
		ADDRESS_LEN = aDDRESS_LEN;
	}

	public int getCOMMANDDATA_LEN() {
		return COMMANDDATA_LEN;
	}

	public void setCOMMANDDATA_LEN(int cOMMANDDATA_LEN) {
		COMMANDDATA_LEN = cOMMANDDATA_LEN;
	}

	public int getCRC_LEN() {
		return CRC_LEN;
	}

	public void setCRC_LEN(int cRC_LEN) {
		CRC_LEN = cRC_LEN;
	}

	public int getCOMMANDDATA_LENGTH() {
		return COMMANDDATA_LENGTH;
	}

	public void setCOMMANDDATA_LENGTH(int cOMMANDDATA_LENGTH) {
		COMMANDDATA_LENGTH = cOMMANDDATA_LENGTH;
	}

	public float getCOOLING_SETPOINT_OFFSET() {
		return COOLING_SETPOINT_OFFSET;
	}

	public void setCOOLING_SETPOINT_OFFSET(float cOOLING_SETPOINT_OFFSET) {
		COOLING_SETPOINT_OFFSET = cOOLING_SETPOINT_OFFSET;
	}

	public float getHEATING_SETPOINT_OFFSET() {
		return HEATING_SETPOINT_OFFSET;
	}

	public void setHEATING_SETPOINT_OFFSET(float hEATING_SETPOINT_OFFSET) {
		HEATING_SETPOINT_OFFSET = hEATING_SETPOINT_OFFSET;
	}

}
