package com.syj.iot.rulesengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.cloudplatform.DataTranspond;
import com.syj.iot.rulesengine.event.Event;
import com.syj.iot.rulesengine.event.MobileData;
import com.syj.iot.rulesengine.utils.MessageUtil;
/**
 * 
 * @des:As drl file global variable,it call api to finish transpond data
 * @author shenyanjun1
 * @date: 2018年5月23日 上午11:23:40
 */
public class Action {
	private  Logger logger = LoggerFactory.getLogger(Action.class);
    //record action status
	private int status ;
	private int ruleId;
	/**
	 * @return the ruleId
	 */
	public int getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId the ruleId to set
	 */
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	public Action() {
		setStatus(-1);
    }
	/**
	 * @des deprecated interface
	 * @param event
	 * @param ruleId
	 */
    public void transpond(Event event,int ruleId) {
    	setRuleId(ruleId);
    	String packageData = event.ToJsonStringForOneRule(ruleId);
    	if (packageData.equals("ERROR")) {
    		logger.error("Execute rule ,rule id:{},action(transpond data) error.",ruleId);
    		setStatus(-1);
    		return;
    	}
    	DataTranspond.getInstance().doTranspond(ruleId, packageData);
    	logger.info("Execute rule ,rule id:{},action(transpond data):{} ok.",ruleId,packageData);
    	setStatus(0);
    }
    /**
     * @des deprecated interface
     * @param topicId
     * @param event
     * @param retained
     * @param placeholder
     * @param ruleId
     * @param isTrans
     */
    public void transpond(long topicId,Event event,List<MobileData> retained,String placeholder,int ruleId,int isTrans) {
    	setRuleId(ruleId);
    	String packageData = event.ToJsonStringForOneRule(ruleId,retained,placeholder);
    	if (packageData.equals("ERROR")) {
    		logger.error("Execute rule ,topic id:{},rule id:{},action(transpond data) error.",topicId,ruleId);
    		setStatus(-1);
    		return;
    	}
    	if (isTrans == 1) {
    		DataTranspond.getInstance().doTranspond(ruleId, packageData);
    		logger.info("Execute rule ,topic id:{},rule id:{},action(transpond data):{} ok.",topicId,ruleId,packageData);
    	}
    	//only reserve data
    	if (isTrans == 2) {
    		logger.info("Execute rule ,topic id:{},rule id:{},reserve data:{} ok.",topicId,ruleId,packageData);
    	}
    	setStatus(0);
    }
    
    /**
     * @des transpond data to cloud platform
     * @param source
     * @param retained
     * @param proxy
     * @param placeholder
     * @param ruleId
     * @param isTrans
     */
    public void transpond(JSONObject source,List<JSONObject> retained,RuleProxy proxy,String placeholder,int ruleId,int isTrans) {
    	setRuleId(ruleId);
    	String messageId = MessageUtil.getMsgId(source);
    	String packageData = proxy.marshal(ruleId,source,retained,placeholder);
    	if (packageData.equals("ERROR")) {
    		logger.error("Execute rule ,rule id:{},msgId:{},action(transpond data) error.",ruleId,messageId);
    		setStatus(-1);
    		return;
    	}
    	if (isTrans == 1) {
    		DataTranspond.getInstance().doTranspond(ruleId, packageData);
//    		logger.info("Execute rule ,rule id:{},action(transpond data):{} ok.",ruleId,packageData);
    		logger.info("Execute rule,Call cloud platform  interface finished,rule id:{},msgId:{}",ruleId,messageId);
    	}
    	//only reserve data
    	if (isTrans == 2) {
    		logger.info("Execute rule,no match rule data,rule id:{},msgId:{},filtered data:{} ok.",ruleId,messageId,packageData);
    	}
    	setStatus(0);
    }
    
    /**
     * @des :no subcondition,transpond only
     * @param source
     * @param proxy
     * @param ruleId
     * @param isTrans
     */
    public void transpond(JSONObject source,RuleProxy proxy,int ruleId,int isTrans) {
    	setRuleId(ruleId);
    	String messageId = MessageUtil.getMsgId(source);
    	String packageData = proxy.marshal(ruleId,source);
    	if (packageData.equals("ERROR")) {
    		logger.error("Execute rule ,rule id:{},msgId:{},action(transpond data) error.",ruleId,messageId);
    		setStatus(-1);
    		return;
    	}
    	if (isTrans == 1) {
    		DataTranspond.getInstance().doTranspond(ruleId, packageData);
    		logger.info("Execute rule ,rule id:{},msgId:{},action(transpond data only) ok.",ruleId,messageId);
    	}
    	//only reserve data
    	if (isTrans == 2) {
    		logger.info("Execute rule ,rule id:{},msgId:{},filtered data ok.",ruleId,messageId);
    	}
    	setStatus(0);
    }
    
}
