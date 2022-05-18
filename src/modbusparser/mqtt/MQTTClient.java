package modbusparser.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import configuration.Configuration;
import configuration.MqttConfig;
import entities.Entity;
import entities.EntityCollection;
import modbusparser.TCPClient;
import modbusparser.comm.commandData.SetPointTemperature;
import modbusparser.frames.CommanDataFrame;

public class MQTTClient implements MqttCallback {
	IMqttClient publisher;
	MqttConnectOptions options = new MqttConnectOptions();
	TCPClient tcpClient = null;
	Configuration config;
	MqttConfig mqttConfig;
	EntityCollection ec = null;
	List<String> subscribedTopics;


	public MQTTClient(TCPClient tcpClient) {
		this.tcpClient = tcpClient;
		this.config = tcpClient.getConfig();
		this.mqttConfig = tcpClient.getConfig().getMqttConfig();
		this.ec = tcpClient.getEntityCollection();
		subscribedTopics = new ArrayList<>();
		connect();
	}

	private void connect() {
			try {
				if (publisher == null) {

				
					publisher = new MqttClient(mqttConfig.getBroker(), mqttConfig.getPublisherId());

					options.setAutomaticReconnect(true);
					options.setCleanSession(true);
					options.setConnectionTimeout(10);
					if (mqttConfig.getPassword_authentication()) {
						options.setUserName(mqttConfig.getUsername());
						options.setPassword(mqttConfig.getPassword().toCharArray());
					}

					publisher.connect(options);
					publisher.setCallback(this);

				} 
			} catch (MqttException e) {
				try {
					e.printStackTrace();
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			
	}

	public void updateEntity(Entity ec) {

		boolean alreadySubscribed = false;
		for (String subTopic : subscribedTopics) {

			if (ec.getSubscribeTopic().equalsIgnoreCase(subTopic)) {
				alreadySubscribed = true;
			}
		}
		if (!alreadySubscribed) {
			try {
				publisher.subscribe(ec.getSubscribeTopic(), 0);
			} catch (MqttException e) {
				e.printStackTrace();
				return;
			}
		}
		String publish = ec.getJSON();
		String topic = ec.getPublishTopic();
		byte[] payload = publish.getBytes(StandardCharsets.US_ASCII);
		MqttMessage msg = new MqttMessage(payload);
		msg.setQos(0);
		try {
			publisher.publish(topic, msg);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void connectionLost(Throwable cause) {

		cause.printStackTrace();

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String msg = new String(message.getPayload());
		Entity targetEntity = null;
		float temperatureWithOffset = 0;
		try {

			temperatureWithOffset = Float.parseFloat(msg);
		} catch (NumberFormatException e) {
			java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Could not parse number, received " + msg + " expecting number");
			return;
		}

		// Look for the appropriate entity
		for (Entity e : ec.getEntityCollection().values()) {
			if (e.getSubscribeTopic().equalsIgnoreCase(topic)) {
				targetEntity = e;
				break;
			}
		}
		if (targetEntity != null) {
			float setPoint = targetEntity.getCelsiusWithoutOffset(temperatureWithOffset);
			java.util.logging.Logger.getLogger(this.getClass().getName()).log(Level.INFO,
					"Updating entity " + targetEntity.getFriendlyname() + "[" + targetEntity.getID().getIDStr() + "]" + " topic " + topic + " with value (Real) " + temperatureWithOffset + " bus value " + setPoint);
			
			// build frame
			CommanDataFrame cdf = new CommanDataFrame(config);
			SetPointTemperature sp0 = new SetPointTemperature();
			sp0.setPayloadZero();
			cdf.addCommandData(sp0);
			SetPointTemperature spT = new SetPointTemperature();
			spT.setCelsius(setPoint);
			cdf.addCommandData(spT);
			cdf.setAddress(targetEntity.getID().getAddr1(), targetEntity.getID().getAddr2());
			cdf.generateRawFrame();
			tcpClient.addFrameToWrite(cdf);

		}

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
