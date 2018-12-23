
/**
 * 
 */
package com.syj.iot.rulesengine.cloudplatform;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.syj.iot.rulesengine.utils.MessageUtil;

/**
 * @des:kafka transpond instance
 * @author shenyanjun1
 * @date: 2018年5月29日 上午10:00:02
 */
public class KafkaTranspond extends TranspondStrategy {

	protected static final Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");

	private  volatile KafkaProducer<byte[], byte[]> instanceProducer = null;

	private  String userName;
	private  String password;
	private  String brokerList;
	private  String clientId;
	private  String topic;
    private  String envFlag;
	
	public  void initConfig(String user, String pass, String brokers, String id, String top,String flag) {
		userName = user;
		password = pass;
		brokerList = brokers;
		clientId = id;
		topic = top;
		envFlag = flag;
		System.out.println("initConfig kafka,userName:" + userName);
		System.out.println("initConfig kafka,password:" + password);
		System.out.println("initConfig kafka,brokerList:" + brokerList);
		System.out.println("initConfig kafka,clientId:" + clientId);
		System.out.println("initConfig kafka,topic:" + topic);
		System.out.println("initConfig kafka,env flag:" + envFlag);
	}

	/**
	 * 
	 * @param flag
	 *            true:test,false: product
	 * @return
	 */
	private  KafkaProducer<byte[], byte[]> getProducer(boolean flag) {
		if (instanceProducer == null) {
			synchronized (KafkaTranspond.class) {
				if (instanceProducer == null) {
					Properties props = getProperties(userName, password, brokerList, clientId, flag);
					instanceProducer = new KafkaProducer(props);
				}
			}
		}
		return instanceProducer;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 */

	@Override
	public void transpond(String data) {
		// TODO Auto-generated method stub
		// 1.正式 0：测试
		produceMsg(data, Integer.parseInt(envFlag), true, new ProducerCallback(MessageUtil.getMsgId(data),data,topic));
	}

	private  Properties getProperties(String username, String password, String brokerlist, String clientId,
			boolean isTest) {
		Properties props = new Properties();

		
		return props;
	}

	/**
	 *
	 * @param msg
	 *            要发送的消息
	 * @param type
	 *            类型：0是测试 1是wifi 2是热力 3是人脸
	 * @param autoFlush
	 *            是否自动刷新，是：每条都flush一次，否：可自动控制flush频率
	 * @param callback
	 *            异步发送的回调函数，可获得分区及offset信息，进行异常处理
	 * @return
	 */
	public void produceMsg(String msg, int type, boolean autoFlush, Callback callback) {
		KafkaProducer<byte[], byte[]> producer = null;
		// 测试
		if (type == 0) {
			producer = getProducer(true);
			localProduceMsg(producer, msg, topic, System.currentTimeMillis(), autoFlush, callback);
		}

		// Wifi
		if (type == 1) {
			producer = getProducer(false);
			localProduceMsg(producer, msg, topic, System.currentTimeMillis(), autoFlush, callback);
		}

	}

	/**
	 *
	 * @param producer
	 *            producer
	 * @param msg
	 *            要发送的消息
	 * @param topic
	 *            主题
	 * @param index
	 *            索引
	 * @param autoFlush
	 *            是否自动刷新，是：每条都flush一次，否：可自动控制flush频率
	 * @param callback
	 *            异步发送的回调函数，可获得分区及offset信息，进行异常处理
	 */
	private void localProduceMsg(KafkaProducer<byte[], byte[]> producer, String msg, String topic, long index,
			boolean autoFlush, Callback callback) {
		try {
			byte[] key = (topic + index).getBytes("UTF-8");
			ProducerRecord<byte[], byte[]> record = new ProducerRecord(topic, key, msg.getBytes("UTF-8"));
			if (callback == null) {
				producer.send(record);
			} else {
				/**
				 * 默认都是异步发送，如果想同步发送producer.send(xxx).get()单条发送，效率比较低
				 * 每条消息都会有callback，可以根据需要来执行发送成功或者失败后的操作
				 */
				producer.send(record, callback);
				/*
				 * producer.send(record, new Callback() {
				 *//**
					 * 当存在异常的时候,发送失败,大家可以根据自己需要设置失败逻辑
					 */
				/*
				 * @Override public void onCompletion(RecordMetadata metadata, Exception
				 * exception) { if(exception != null){
				 * 
				 * }else{
				 
			}
			/**
			 * flush : 执行flush代表内存中的消息数据都会send到服务端，可根据callback来判断成功与否 自己控制flush
			 * 注意：这里可发送一批数据之后再掉flush，达到批量的效果
			 */
			if (autoFlush) {
				producer.flush();
			}

		} catch (Exception e) {
			logger.error("##error=", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public void initConfig(Map<String, String> config) {
		// TODO Auto-generated method stub
		initConfig(config.get("userName"), config.get("password"), config.get("brokers"),
				config.get("producerClientId"), config.get("topicName"),config.get("flag"));
	}

}