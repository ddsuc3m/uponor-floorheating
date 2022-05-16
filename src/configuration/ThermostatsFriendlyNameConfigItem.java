package configuration;

public class ThermostatsFriendlyNameConfigItem {
	String friendlyName;
	String hexStringID;

	public ThermostatsFriendlyNameConfigItem() {
		super();
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getHexStringID() {
		return hexStringID;
	}

	public void setHexStringID(String hexStringID) {
		this.hexStringID = hexStringID;
	}

}