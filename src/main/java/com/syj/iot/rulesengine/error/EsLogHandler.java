/**
 * 
 */
package com.syj.iot.rulesengine.error;

import com.alibaba.fastjson.JSONObject;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年8月6日 上午10:53:06
 */
public class EsLogHandler {

	/**
	 * @des log to es
	 * @param deviceName device's name
	 * @param productKey product's key
	 * @param msgId      message's id
	 * @param reportData data
	 * @param kfTopic kafka topic
	 * @param action
	 * @param code
	 */
	public static void toEsLogSystem(String deviceName,String productKey,String msgId,String reportData,String kfTopic,DataLogEnum action,DataLogErrorCodeEnum code) {
		JSONObject msg = new JSONObject();
//		msg.put(CommonConstant.ES_LOG_MSG_ID, msgId);
		msg.put(CommonConstant.ES_LOG_MSG_CONTENT, action.msg);
		msg.put(CommonConstant.ES_LOG_MSG_CODE, code.code);
		EsLogProp esLog = new EsLogProp();
		esLog.setDeviceName(deviceName);
		esLog.setProductCode(productKey);
		esLog.setMsgType(action.name);
		esLog.setBussType("上行消息");
		esLog.setMsgId(msgId);
		esLog.setContent(reportData);//reported data
		esLog.setTopic(kfTopic);//kafka topic
		EsService.$().deviceLog(esLog,msg.toJSONString());
	}
}
