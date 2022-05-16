package configuration;

import java.util.List;

import entities.EntityID;

public class Configuration {

	BusConfig busConfig;
	MqttConfig mqttConfig;
	SerialServerConfig serialServerConfig;
	SystemConfig systemConfig;
	List<ThermostatsFriendlyNameConfigItem> thermostatsFriendlyNamesConfig;
	

	public Configuration() {

	}

	public BusConfig getBusConfig() {
		return busConfig;
	}

	public void setBusConfig(BusConfig busConfig) {
		this.busConfig = busConfig;
	}

	public List<ThermostatsFriendlyNameConfigItem> getThermostatsFriendlyNamesConfig() {
		return thermostatsFriendlyNamesConfig;
	}

	public void setThermostatsFriendlyNamesConfig(
			List<ThermostatsFriendlyNameConfigItem> thermostatsFriendlyNamesConfig) {
		this.thermostatsFriendlyNamesConfig = thermostatsFriendlyNamesConfig;
	}

	public String lookupFriendlyName(EntityID id) {

		for (ThermostatsFriendlyNameConfigItem fni : thermostatsFriendlyNamesConfig) {
			if (id.getIDStr().equalsIgnoreCase(fni.hexStringID))
				return fni.friendlyName;
		}
		return "Unknown" + id.getIDStr();
	}

	public MqttConfig getMqttConfig() {
		return mqttConfig;
	}

	public void setMqttConfig(MqttConfig mqttConfig) {
		this.mqttConfig = mqttConfig;
	}

	public SerialServerConfig getSerialServerConfig() {
		return serialServerConfig;
	}

	public void setSerialServerConfig(SerialServerConfig serialServerConfig) {
		this.serialServerConfig = serialServerConfig;
	}

	public SystemConfig getSystemConfig() {
		return systemConfig;
	}

	public void setSystemConfig(SystemConfig systemConfig) {
		this.systemConfig = systemConfig;
	}

	
}
