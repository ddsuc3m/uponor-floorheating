package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import modbusparser.comm.commandData.CommandData;
import modbusparser.comm.commandData.CommandDataTypeCollection;
import modbusparser.comm.commandData.DemandWater;
import modbusparser.comm.commandData.OperatingMode;
import modbusparser.comm.commandData.OperatingMode.OPMODE;
import modbusparser.frames.ResponseFrame;
import modbusparser.comm.commandData.RoomTemperature;
import modbusparser.comm.commandData.SetPointTemperature;
import modbusparser.comm.commandData.SetPointTemperatureMax;
import modbusparser.comm.commandData.SetPointTemperatureMin;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import configuration.Configuration;
import configuration.MqttConfig;
import configuration.SystemConfig;


public class Entity {

	private transient EntityID id;
	
	private String fiendlyName;
	@SerializedName("ID")
	private String idstr = "";
	
	
	@SerializedName("DemandingWater")
	private boolean demanding;
	@SerializedName("Heating")
	private boolean heating;
	@SerializedName("Cooling")
	private boolean cooling;
	
	@SuppressWarnings("unused")
	private OPMODE mode;
	@SerializedName("RoomTemperature")
	private float roomTemperature;
	@SerializedName("SetPoint")
	private float setPointTemperature;
	@SuppressWarnings("unused")
	private float setPointTemperatureMax;
	@SuppressWarnings("unused")
	private float setPointTemperatureMin;
	@SerializedName("SetPointWithOffset")
	private float setPointTemperatureWithOffset;
	transient LocalDateTime  lastRequested;
	transient LocalDateTime  lastUpdated;
	@SerializedName("LastUpdated")
	String lastUpdatedStr;
	transient String publishTopic;
	transient String subscribeTopic;
	transient Configuration config;
	transient SystemConfig systemConfig;
	transient MqttConfig mqttConfig;
	
	
	public Entity(EntityID id, Configuration config)
	{
		this.id = id;
		this.config = config;
		this.systemConfig = config.getSystemConfig();
		this.mqttConfig = config.getMqttConfig();
		this.idstr = id.getIDStr();
		//this.fiendlyName = configuration.FriendlyName.lookup(id);
		this.fiendlyName = config.lookupFriendlyName(id);
		String pubTopic = mqttConfig.getPublishTopicPrefix();
		pubTopic += "/";
		pubTopic += getFriendlyname();
		setPublishTopic(pubTopic);
		String subsTopic = pubTopic + "/";
		subsTopic += mqttConfig.getSubscribeSetPointChildTopic();
		setSubscribeTopic(subsTopic);
		
	}
	
	
	
	public String getPublishTopic() {
		return publishTopic;
	}



	public void setPublishTopic(String publishTopic) {
		this.publishTopic = publishTopic;
	}



	public String getSubscribeTopic() {
		return subscribeTopic;
	}



	public void setSubscribeTopic(String subscribeTopic) {
		this.subscribeTopic = subscribeTopic;
	}



	public String getFriendlyname()
	{
		return fiendlyName;
	}
	
	public void updateValues(ResponseFrame res)
	{
		
		List<CommandData> cdl = res.commandData;
		for(CommandData cd : cdl)
		{
			processCommandData(cd);
		}
		updateLastUpdated();
	}

	private void processCommandData(CommandData cd) {
		
		if(cd.dataId == CommandDataTypeCollection.DEMAND_WATER.getDataId())
		{
			DemandWater dw = (DemandWater) cd;
			this.demanding =  dw.isDemanding();
			this.heating = dw.isHot();
			this.cooling = !this.heating;
		}
		if(cd.dataId == CommandDataTypeCollection.OPERATING_MODE.getDataId())
		{
			OperatingMode om = (OperatingMode) cd;
			this.mode =  om.getMode(); 
		}
		if(cd.dataId == CommandDataTypeCollection.ROOM_TEMPERATURE.getDataId())
		{
			RoomTemperature rt = (RoomTemperature) cd;
			this.roomTemperature =  rt.getCelsius(); 
		}
		if(cd.dataId == CommandDataTypeCollection.SETPOINT_TEMPERATURE.getDataId())
		{
			SetPointTemperature sp = (SetPointTemperature) cd;
			this.setPointTemperature =  sp.getCelsius();
			if(cooling)
				this.setPointTemperatureWithOffset =  sp.getCelsius() + systemConfig.getCOOLING_SETPOINT_OFFSET();
			else
				this.setPointTemperatureWithOffset =  sp.getCelsius();
		}
		if(cd.dataId == CommandDataTypeCollection.SETPOINT_TEMPERATURE_MAX.getDataId())
		{
			SetPointTemperatureMax sp = (SetPointTemperatureMax) cd;
			this.setPointTemperatureMax =  sp.getCelsius(); 
		}
		if(cd.dataId == CommandDataTypeCollection.SETPOINT_TEMPERATURE_MIN.getDataId())
		{
			SetPointTemperatureMin sp = (SetPointTemperatureMin) cd;
			this.setPointTemperatureMin =  sp.getCelsius(); 
		}

	}
	
	public float getCelsiusWithoutOffset(float celsiusWithOffset)
	{
		if(cooling)
			return celsiusWithOffset - systemConfig.getCOOLING_SETPOINT_OFFSET();
		else
			return celsiusWithOffset;
	}

	public void updateLastRequested() {
		lastRequested = LocalDateTime.now();
	}
	public void updateLastUpdated() {
		lastUpdated = LocalDateTime.now();
		DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		lastUpdatedStr = lastUpdated.format(dtf2);
	}

	public String getStatusString() {
		String res="";
		String friendlyNameCW = String.format("%-25s", this.fiendlyName);
		String rootTemperatureCW = String.format("%5.3f", this.roomTemperature);
		String setPointTemperatureCW = String.format("%5.3f", this.setPointTemperatureWithOffset);
		res += friendlyNameCW + "[" + id.getIDStr()  + "]" + "[" + "D:" + (this.demanding?"Y":"N") + "|" + "M:" + (this.heating?"H":"C") + "]" + "RoomTemp(c):" + rootTemperatureCW + " | SetPoint(c): " + setPointTemperatureCW;
		return res;
	}
	public EntityID getID()
	{
		return id;
	}
	public String getJSON()
	{
		Gson gson = new Gson();
		String json = gson.toJson(this);  
		return json;
	}
	
}
