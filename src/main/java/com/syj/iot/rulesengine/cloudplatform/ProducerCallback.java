/**
 * 
 */
package com.syj.iot.rulesengine.cloudplatform;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.iot.rulesengine.error.EsLogHandler;
import com.syj.iot.rulesengine.utils.MessageUtil;;

/**
 * @des:kafka send message callback
 * @author shenyanjun1
 * @date: 2018年7月5日 下午3:31:32
 */
public class ProducerCallback implements Callback{
	protected static final Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");
	private String msgId;
	private String topic;
	private String reportedData;
	public ProducerCallback(String msgId,String reportedData,String top) {
		this.msgId = msgId;
		this.topic = top;
		this.reportedData = reportedData;
	}
	/* (non-Javadoc)
	 * @see org.apache.kafka.clients.producer.Callback#onCompletion(org.apache.kafka.clients.producer.RecordMetadata, java.lang.Exception)
	 */
	@Override
	public void onCompletion(RecordMetadata metadata, Exception exception) {
		// TODO Auto-generated method stub
		if (exception != null) {
			logger.error("Kafka producer write  error, msgId:{} ,errorInfo:{},data:{}.",this.msgId, exception.getMessage(),this.reportedData);
			//process error data ,cache and so on
		} else {
			logger.info("Kafka producer write ok, msgId:{},kafka topic:{}.",this.msgId,this.topic);
			EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(this.reportedData), "", 
					MessageUtil.getMsgId(this.reportedData),this.reportedData,this.topic,DataLogEnum.RE_TRANSPOND_DATA, DataLogErrorCodeEnum.SUCCESS);
		}
	}

}
