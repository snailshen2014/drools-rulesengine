/**
 * 
 */
package com.syj.iot.rulesengine.cloudplatform;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @des:device transpond instance ,M2M scene
 * @author shenyanjun1
 * @date: 2018年5月29日 上午10:00:02
 */
public class DeviceTranspond extends TranspondStrategy {
	protected static final Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 */
	private String ruleUserKey;
	private String emqttUser;
	private String emqttAcl;
	private String topic;
	private String pubSubType;

	@Override
	public void transpond(String data) {
		// TODO Auto-generated method stub
		try {
		String ruleUserKey = this.ruleUserKey;
			String emqKey = this.emqttUser + ruleUserKey;
			String emqAclKey = this.emqttAcl + ruleUserKey;
			String password = JedisUtil.getInstance().hget(emqKey, "password");
			if (password == null) {
				Map<String, String> sysParams = new HashMap<>();
				sysParams.put("sysRuleInit", StringUtil.random(6));
				password = SignUtil.sign(sysParams, StringUtil.random(6), "hmacsha1");
				
			}
			String topic = this.topic;
			String pubSub = JedisUtil.getInstance().hget(emqAclKey, topic);
			if (pubSub == null) {
				
			}

			long t = System.currentTimeMillis();
			String sysMqttclientId = "sysRuleInit" + t;
			MqttMessage mqttMessage = new MqttMessage();

			mqttMessage.setPayload(data.getBytes());
			mqttMessage.setQos(1);
			
		} catch (Exception e) {
			logger.error("Send topic data error.");
		}
		logger.info("Send topic data ok.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public void initConfig(Map<String, String> config) {
		// TODO Auto-generated method stub
		ruleUserKey = config.get("reserve1");
		emqttUser = config.get("userName");
		emqttAcl = config.get("reserve2");
		topic = config.get("topicName");
		pubSubType = config.get("reserve3");
		System.out.println("initConfig topic,ruleUserKey:" + ruleUserKey);
		System.out.println("initConfig topic,emqttUser:" + emqttUser);
		System.out.println("initConfig topic,emqttAcl:" + emqttAcl);
		System.out.println("initConfig topic,topic:" + topic);
		System.out.println("initConfig topic,pubSubType:" + pubSubType);
	}

}
