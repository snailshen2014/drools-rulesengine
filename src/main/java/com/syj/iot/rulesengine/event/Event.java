package com.syj.iot.rulesengine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.utils.SqlStatementParser;

/**
 * @des:devices data report event base class
 * @author shenyanjun1
 * @date: 2018年5月21日 下午7:03:31
 */
public  class Event {
	private  Logger logger = LoggerFactory.getLogger(Event.class);
	//one rule one attributes,apply for every rule building when using ,not apply for KJar
	private String attributes;
	//json object
	private JSONObject jsonObject;
	
	//store ruleId->placeHolder,apply for Using all rules for every event(KJar)
	private Map<Integer,String> mapIdPlaceHolder = new HashMap<>();
	
	//for add rule function parameter
	private long startTime;
	private long endTime;
	private Long topicId;
	
	/**
	 * @return the topicId
	 */
	public Long getTopicId() {
		return topicId;
	}


	/**
	 * @param topicId the topicId to set
	 */
	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}


	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}


	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}


	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	


	/**
	 * 
	 * @return json string for selectd attributes
	 */
	public  String  ToJsonString() {
		JSONObject json = new JSONObject();
		// '*' or attri1 alias1,attri2 alias2
		//no support function now
		if (attributes.trim().equals("*"))
			return jsonObject.toString();
		
		Map<String,String> attrMap = SqlStatementParser.getAttributes(attributes);
		if (attrMap == null) {
			logger.error("Sql statement parser getAttributes null.");
			return "ERROR";
		}
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			json.put(entry.getValue(), jsonObject.getString(entry.getValue()));
		}
		return json.toString();
	}
	
	
	public  void setAttributesPlaceholders(String attritubes) {
		this.attributes = attritubes;
	}
	
	public  void setJsonObject(JSONObject jsonObj) {
		this.jsonObject = jsonObj;
	}
	
	/**
	 * @return the jsonObject
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}


	public void addRulesPlaceholder(Integer ruleId,String placeHolder) {
		mapIdPlaceHolder.put(ruleId, placeHolder);
	}
	
	/**
	 * @des for execute all rules
	 * @param ruleId
	 * @return json string for selectd attributes
	 */
	public  String  ToJsonStringForOneRule(Integer ruleId) {
		String placeholder = mapIdPlaceHolder.get(ruleId);
		if (placeholder == null || placeholder.equals("")) {
			logger.error("Can not find placeholder or  null.");
			return "ERROR";
		}
		JSONObject json = new JSONObject();
		// '*' or attri1 alias1,attri2 alias2
		//no support function now
		if (placeholder.trim().equals("*"))
			return jsonObject.toString();
		
		Map<String,String> attrMap = SqlStatementParser.getAttributes(placeholder);
		if (attrMap == null) {
			logger.error("Sql statement parser getAttributes null.");
			return "ERROR";
		}
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
//			json.put(entry.getValue(), jsonObject.getString(entry.getValue()));
			json.put(entry.getValue(), jsonObject.get(entry.getValue()));
		}
		
		return json.toString();
	}
	
	public  String  ToJsonStringForOneRule(Integer ruleId,List<MobileData> retained,String holder) {
		String placeholder = mapIdPlaceHolder.get(ruleId);
		if (placeholder == null || placeholder.equals("")) {
			logger.error("Can not find placeholder or  null.");
			return "ERROR";
		}
		JSONObject json = new JSONObject();
		// '*' or attri1 alias1,attri2 alias2
		//no support function now
		if (placeholder.trim().equals("*"))
			return jsonObject.toString();
		
		Map<String,String> attrMap = SqlStatementParser.getAttributes(placeholder);
		if (attrMap == null) {
			logger.error("Sql statement parser getAttributes null.");
			return "ERROR";
		}
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			if (entry.getValue().equals("_extend"))
				json.put(entry.getValue(), jsonObject.getJSONObject(entry.getValue()));
			else
				json.put(entry.getValue(), jsonObject.getString(entry.getValue()));
		}
		
		json.put(holder, retained);
		return json.toString();
	}
}