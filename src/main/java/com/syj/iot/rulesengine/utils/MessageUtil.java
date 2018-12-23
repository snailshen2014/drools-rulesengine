/**
 * 
 */
package com.syj.iot.rulesengine.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年8月9日 上午9:38:14
 */
public class MessageUtil {
	private static  String extendPlaceHolder = "_extend";
	/**
	 * @des get msgId from jsonobject
	 * @param data
	 * @return
	 */
	public static String getMsgId(JSONObject data) {
		JSONObject msgId = (JSONObject)data.get(extendPlaceHolder);
    	return msgId == null ? null : msgId.getString(CommonConstant.ES_LOG_MSG_ID);
	}
	/**
	 * @des  get msg id from json data
	 * @param jsonData
	 * @return
	 */
	public static String getMsgId(String jsonData) {
		JSONObject extend = (JSONObject) JSON.parseObject(jsonData).get(extendPlaceHolder);
		return extend == null ? null : extend.getString(CommonConstant.ES_LOG_MSG_ID);
	}
	/**
	 * @des get devicename
	 * @param jsonData
	 * @return
	 */
	public static String getDeviceName(String jsonData) {
    	JSONObject extend = (JSONObject) JSON.parseObject(jsonData).get(extendPlaceHolder);
		return extend == null ? null : extend.getString("deviceName");
	}
}
