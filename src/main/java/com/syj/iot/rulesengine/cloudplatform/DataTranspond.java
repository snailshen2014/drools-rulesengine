
package com.syj.iot.rulesengine.cloudplatform;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.syj.iot.rulesengine.rule.IotRulesMapper;

/**
 * @des:This class is designed for data transponding to cloudplatform
 * @author shenyanjun1
 * @date: 2018年5月29日 上午10:06:42
 */
public class DataTranspond {
	private static DataTranspond dataTranspond;
	//transpondStrategys contains kafka,mq...
	private static Map<Integer,TranspondStrategy> transpondInstances = new ConcurrentHashMap<>();
	private  DataTranspond() {}
	/**
	 * @des get cloudplatfrom data transpdond instance
	 * @return
	 */
	public static DataTranspond getInstance() {
		if (dataTranspond == null) {
			synchronized (DataTranspond.class) {
				if (dataTranspond == null) {
					dataTranspond = new DataTranspond();
					initActionsInstance();
				}
			}
		}
		return dataTranspond;
	}
	
	/**
	 * @des init actions instances
	 */
	private static void initActionsInstance() {
		Map<Integer,IotAction> actions = IotRulesMapper.getActions();
		if (actions == null || actions.isEmpty()) {
			System.out.println("###### rulesEngine actions error,no configure.");
			return;
		}
		for(Map.Entry<Integer, IotAction> action: actions.entrySet()) {
			System.out.println("Init actions instance,actionId:" + action.getKey());
			TranspondStrategy transpondProxy = null;
			try {
				Class<?> classObj = Class.forName(action.getValue().getTranspondObject());
				Object proxy = classObj.newInstance();
				transpondProxy = (TranspondStrategy)proxy;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			//transpond
			Map<String,String> configMap = new HashMap<>();
			configMap.put("topicName", action.getValue().getTopicName());
			configMap.put("userName", action.getValue().getUserName());
			configMap.put("password",action.getValue().getPassword() );
			configMap.put("producerClientId", action.getValue().getProducerClientId());
			configMap.put("brokers", action.getValue().getBrokers());
			configMap.put("consumerClientId", action.getValue().getConsumerClientId());
			configMap.put("consumerGroup", action.getValue().getConsumerGroup());
			configMap.put("producerGroup", action.getValue().getProducerGroup());
			configMap.put("transpondObject", action.getValue().getTranspondObject());
			configMap.put("reserve1", action.getValue().getReserve1());//topic USER_KEY
			configMap.put("reserve2", action.getValue().getReserve2());//topic EMQTT_ACL
			configMap.put("reserve3", action.getValue().getReserve3());//topic PUB_SUB
			configMap.put("flag", IotRulesMapper.getKafkaEnv());
			transpondProxy.initConfig(configMap);
			transpondInstances.put(action.getKey(), transpondProxy);
			System.out.println("Init actions instance,actionId:" + action.getKey() + " ok.");
		}
	}
	
	/**
	 * @des transpond data for special action
	 * @param ruleId
	 * @param data
	 */
	public void doTranspond(Integer ruleId,String data) {
//		long start = System.currentTimeMillis();
		TranspondStrategy transpondProxy = null;
		transpondProxy = transpondInstances.get(IotRulesMapper.getActionIdByRuleId(ruleId));
		transpond(transpondProxy,data);
//		long end = System.currentTimeMillis();
//		System.out.println("Transpond data time:" + (end-start) + " :ms");
		
	}
	/**
	 * @des transpond data
	 * @param transpondType
	 * @param data
	 */
	private void transpond(TranspondStrategy transpondType,String data) {
		transpondType.transpond(data);
	}
	
	public static void main(String[] args) {
		
//		DataTranspond.getInstance().transpond(transpondType);
//		DataTranspond dataTrans = new DataTranspond();
//		dataTrans.addData("dddd");
//		dataTrans.transpond(new KafkaTranspond());
//		dataTrans.transpond(new DeviceTranspond());//m2m
//		dataTrans.transpond(new MqTranspond());
	}
}
