package configuration;

import java.util.UUID;

public class MqttConfig {
	
	String publisherId = UUID.randomUUID().toString();
	String broker = "tcp://127.0.0.1:1883";
	Boolean password_authentication = false;
	String username = "user";
	String password = "pass";
	String publishTopicPrefix = "FloorHeating";
	String subscribeSetPointChildTopic = "Setpoint/Set";

	public MqttConfig() {
		super();
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public Boolean getPassword_authentication() {
		return password_authentication;
	}

	public void setPassword_authentication(Boolean password_authentication) {
		this.password_authentication = password_authentication;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}

	public String getPublishTopicPrefix() {
		return publishTopicPrefix;
	}

	public void setPublishTopicPrefix(String publishTopicPrefix) {
		this.publishTopicPrefix = publishTopicPrefix;
	}

	public String getSubscribeSetPointChildTopic() {
		return subscribeSetPointChildTopic;
	}

	public void setSubscribeSetPointChildTopic(String subscribeSetPointChildTopic) {
		this.subscribeSetPointChildTopic = subscribeSetPointChildTopic;
	}


    
}
