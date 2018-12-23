/**
 * 
 */
package com.syj.iot.rulesengine.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.syj.iot.rulesengine.cache.CacheRunnable;
import com.syj.iot.rulesengine.cache.DataCache;
import com.syj.iot.rulesengine.entrypoint.KafkaDataEntryPoint;
import com.syj.iot.rulesengine.test.EchoServer;
import com.syj.iot.rulesengine.utils.EmqttRuleUtil;

/**
 * @des:init iot emq,thread component
 * @author shenyanjun1
 * @date: 2018年8月13日 下午2:05:17
 */
@Component
public class IotComponentInit implements ApplicationListener<ApplicationReadyEvent> {
	@Autowired
	private IotRulesConfigurationService ioTRulesService;

	/**
	 * init iot emq listener ,datacache thread etc
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {

		// init emq listener to receive topic data for rule engine
		EmqttRuleUtil.initEmqListener();
		// start cached thread
		for (int i = 0; i < 2; ++i) {
			Thread dealCache = new Thread(new CacheRunnable(DataCache.getDataCache()));
			dealCache.start();
		}

		// start pull data threads
		if (ioTRulesService.getKafkaPullFlag().equals("1")) {
			for (int j = 0; j < 1; ++j) {
				Thread kfThread = new Thread(new KafkaDataEntryPoint("zcjuser", "zcjpass",
						"192.168.144.110:9092,192.168.144.111:9092,192.168.144.112:9092", "zcjclient", "zcjgroup",
						"zcjtopic", ioTRulesService.getKafkaEnvFlag()));
				kfThread.start();
			}
		}
		
		System.out.println("RulesEngine iot component init ok.");
	}

	/**
	 * @des :start listener to listen topic data for rules engine
	 * 
	 *//*
	private void initEmqListener() {
		// start emq listner
		String topic = EmqTopicConstant.RULE_ENGINE_SUB_MANAGE_RULE_TOPIC;
		//long t = System.currentTimeMillis();
		String linuxLocalIp = IPUtil.getLinuxLocalIp();//修改为IP地址，保证每台机器订阅端的唯一性,方便更新路与表
		String sysMqttclientId = "ruleEngineAddDevice_" + linuxLocalIp;
		String ruleUserKey = CommonConstant.RULE_ENGINE_RULE_INIT_TOPIC_USER_KEY;
		String emqKey = CacheConstant.EMQTT_USER + ruleUserKey;
		String emqAclKey = CacheConstant.EMQTT_ACL + ruleUserKey;
		String pubSub = JedisUtil.getInstance().hget(emqAclKey, topic);
		if (pubSub == null) {
			JedisUtil.getInstance().hset(emqAclKey, topic, MqttConstant.PUB_SUB);
		}
		String password = JedisUtil.getInstance().hget(emqKey, "password");
		if (password == null) {
			Map<String, String> sysParams = new HashMap<>();
			sysParams.put("sysRuleInit", StringUtil.random(6));
			password = SignUtil.sign(sysParams, StringUtil.random(6), "hmacsha1");
			JedisUtil.getInstance().hset(emqKey, "password", password);
		}
		MqttClient mqttClient = null;
		int retry = 0;
		while (retry++ < 3) {
			mqttClient = EmqClientUtil.backendSubscribeMsg(topic, sysMqttclientId, ruleUserKey, password, new EngineListener());
			if (mqttClient.isConnected())
				break;
			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (mqttClient.isConnected())
			System.out.println("Mqtt client connected mqtt server.");
		else
			System.out.println("Mqtt client can not connect mqtt server,retry 3 times.");
	}*/
}
