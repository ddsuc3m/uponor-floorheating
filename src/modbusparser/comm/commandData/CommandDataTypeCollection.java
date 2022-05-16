package modbusparser.comm.commandData;

public enum CommandDataTypeCollection {

	ROOM_TEMPERATURE((byte) modbusparser.comm.commandData.RoomTemperature.ROOM_TEMPERATURE,
			modbusparser.comm.commandData.RoomTemperature.class),
	SETPOINT_TEMPERATURE((byte) modbusparser.comm.commandData.SetPointTemperature.SETPOINT_TEMPERATURE,
			modbusparser.comm.commandData.SetPointTemperature.class),
	SETPOINT_TEMPERATURE_MAX((byte) modbusparser.comm.commandData.SetPointTemperatureMax.SETPOINT_TEMPERATURE_MAX,
			modbusparser.comm.commandData.SetPointTemperatureMax.class),
	SETPOINT_TEMPERATURE_MIN((byte) modbusparser.comm.commandData.SetPointTemperatureMin.SETPOINT_TEMPERATURE_MIN,
			modbusparser.comm.commandData.SetPointTemperatureMin.class),
	DEMAND_WATER((byte) modbusparser.comm.commandData.DemandWater.DEMAND_WATER,
			modbusparser.comm.commandData.DemandWater.class),
	OPERATING_MODE((byte) modbusparser.comm.commandData.OperatingMode.OPERATING_MODE,
			modbusparser.comm.commandData.OperatingMode.class),
	HEATING_COOLING((byte) modbusparser.comm.commandData.HeatingCooling.HEATING_COOLING,
			modbusparser.comm.commandData.HeatingCooling.class),
	ECO_SET_BACK((byte) modbusparser.comm.commandData.EcoSetBack.ECO_SET_BACK,
	modbusparser.comm.commandData.EcoSetBack.class),
	UNKNOWN((byte) 0xFF, UnknownCommandData.class);

	private byte dataId;
	private Class<? extends CommandData> instantiatingClass;

	CommandDataTypeCollection(byte b, Class<? extends CommandData> instantiatingClass) {
		this.dataId = b;
		this.instantiatingClass = instantiatingClass;
	}

	public byte getDataId() {
		return dataId;
	}

	public Class<? extends CommandData> getInstantiatingClass() {
		return instantiatingClass;
	}

}
