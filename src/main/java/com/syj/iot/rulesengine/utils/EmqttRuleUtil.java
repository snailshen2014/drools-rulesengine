package com.syj.iot.rulesengine.utils;

import com.syj.iot.rulesengine.init.EngineListener;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.HashMap;
import java.util.Map;

public class EmqttRuleUtil {
    /**
     * @des :start listener to listen topic data for rules engine
     *
     */
    public static void initEmqListener() {
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
    }
}
