package com.syj.iot.rulesengine.init;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.error.EsLogHandler;
import com.syj.iot.rulesengine.rule.RuleChangingCallback;
import com.syj.iot.rulesengine.typedefine.EngineCommand;
import com.syj.iot.rulesengine.typedefine.MessageNotifyBusiType;
import com.syj.iot.rulesengine.utils.EmqttRuleUtil;
import com.syj.iot.rulesengine.utils.SpringContextHolder;

/**
 * 
 * @des:RulesEngine listener to listen mq message contains rule's manager system and product operator message.
 * @author shenyanjun1
 * @date: 2018年7月13日 上午9:44:30
 */
public class EngineListener extends BaseCallBack {
	protected static final Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");

	private IotRulesConfigurationService ioTRulesService = SpringContextHolder.getBean("iotRulesConfigurationService");

	@Override
	public void connectionLost(Throwable cause) {
		while (true){
			System.out.println("Subscribe emqtt server connection lost at :" + new Date());
			try {//如果没有发生异常说明连接成功，如果发生异常，则死循环
				Thread.sleep(1000) ;
				EmqttRuleUtil.initEmqListener();
				break;
			}catch (Exception e){
				System.out.println(cause.getMessage());
				continue;
			}
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		try {
			String messageStr = new String(message.getPayload(), "UTF-8");
			JSONObject infoJson = JSONObject.parseObject(messageStr);
			// data format {"formatId": "data format","productKey" :"product
			// key","msgType":"1:create product,2:delete product"
			// ,"businessType":"1000,product data,1001,rules manager data"}
			logger.info("EngineListener-messageArrived,topic={},message={}", topic, messageStr);
			EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",infoJson.getString("msgId") ,messageStr,"EmqMessage", DataLogEnum.RE_RECEIVE_EMQ_DATA, DataLogErrorCodeEnum.SUCCESS);
			String businessType = infoJson.get("businessType").toString();
			if (businessType == null) {
				logger.error("EngineListener..messageArrived..,businessType null error,msgId:{}.",infoJson.getString("msgId"));
				return;
			}
			
			if (Integer.parseInt(businessType) == MessageNotifyBusiType.MANAGER_MESSAGE.getType()) {// product message
				productAction(infoJson);
			}
			if (Integer.parseInt(businessType) == MessageNotifyBusiType.RULESMANAGER_SYSTEM_MESSAGE.getType()) {// rulesEngine manager messsage
				notifyRuleEngineByManager(infoJson);
			}
			logger.info("EngineListener-finished deal message,topic={},msgId={},message={}", topic,infoJson.getString("msgId"), messageStr);
		} catch (Exception e) {
			logger.error("EngineListener exception={}", e);
		}

	}

	/**
	 * @des : create ,deleted product
	 * @param jsonObject
	 */
	private void productAction(JSONObject jsonObject) {
		try {
			logger.info("EngineListener-productAction begin.");
			String productKey = jsonObject.get("productKey").toString();
			Long dataFormatId = Long.valueOf(jsonObject.get("formatId").toString());
			Object msgType = jsonObject.get("msgType");
			if (productKey == null || dataFormatId == null || msgType == null) {
				logger.error("EngineListener-productAction param error,null error,msgId:{}",jsonObject.getString("msgId"));
				return;
			}
			if ((Integer) msgType == EngineCommand.PRODUCT_ONLINE.getType()) {// create product
				notifyRulesEngineByProduct(EngineCommand.PRODUCT_ONLINE, productKey, dataFormatId,-1, 1, 0,jsonObject.getString("msgId"));
			}
			if ((Integer) msgType == EngineCommand.PRODUCT_OFFLINE.getType()) {// deleted product
				notifyRulesEngineByProduct(EngineCommand.PRODUCT_OFFLINE, productKey, dataFormatId, -1,0, 1,jsonObject.getString("msgId"));
			}
			if ((Integer) msgType == EngineCommand.PRODUCT_MODIFY_DATEFORMAT.getType()) {//  product modify dataformat 
				Long oldDataFormatId = Long.valueOf(jsonObject.get("oldFormatId").toString());
				notifyRulesEngineByProduct(EngineCommand.PRODUCT_MODIFY_DATEFORMAT, productKey, dataFormatId,oldDataFormatId,0, 1,jsonObject.getString("msgId"));
			}
		} catch (Exception e) {
			logger.error("EngineListener-productAction exception,msgId:{},e={}", jsonObject.getString("msgId"),e);
		}
	}

	/**
	 * 
	 * @param type
	 *            :1 create product,2:deleted product
	 * @param productKey
	 * @param dataFormatId
	 * @param ruleStatus
	 *            :1 have rules,0:no rules
	 * @param useStatus
	 *            :0 product online,1:product offline
	 */
	private void notifyRulesEngineByProduct(EngineCommand command, String productKey, long dataFormatId, long oldDataFormatId,int ruleStatus,
			int useStatus,String msgId) {
		
		try {
			IotProductRule productRule = new IotProductRule();
			productRule.setProductKey(productKey);
			productRule.setDataFormatId(dataFormatId);
			productRule.setCreateTime(new Date());
			productRule.setRuleStatus(ruleStatus);
			productRule.setUseStatus(useStatus);
			Long productRuleId = ioTRulesService.getProductRuleIdByProductKey(productKey);
			if (command == EngineCommand.PRODUCT_MODIFY_DATEFORMAT) {
				if (productRuleId == null) {
					logger.error("EngineListener-productAction,modify data format,product not exists,productKey:{},productRuleId:{},msgId:{}.",productKey,productRuleId,msgId);
					return;
				}
				Map<String, Object> params = new HashMap<>();
				params.put("dataFormatId", dataFormatId);
				params.put("oldDataFormatId", oldDataFormatId);
				params.put("productKey", productKey);
				params.put("ruleId", 0);//for parse
				if (!ioTRulesService.loadRuleDynamically(params, 1,command,new RuleChangingCallback())) {
					logger.error("PRODUCT_MODIFY_DATEFORMAT addRuleDynamiclly error,msgId:{}.",msgId);
				}
				
			}
			if (command == EngineCommand.PRODUCT_ONLINE) { // create product
				if (productRuleId == null) {
					logger.error("EngineListener-productAction,product no exists,productKey:{},productRuleId:{},msgId:{}.",productKey,productRuleId,msgId);
					return;
				}
				List<IotRules> rules = ioTRulesService.getRulesByProductKey(productKey);
				if (rules == null) {
					logger.error("EngineListener-productAction ,get rules null ,productKey:{},msgId:{}.",productKey,msgId);
					EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",msgId ,"","EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
					return;
				}
				for (IotRules rule : rules) {
					// add default rule
					Map<String, Object> params = new HashMap<>();
					params.put("productRuleId", productRuleId);
					params.put("attribute",rule.getAttribute());
					params.put("con", rule.getCon());
					params.put("actionId", rule.getActionId());
					params.put("ruleId", rule.getRuleId());
					params.put("productKey", productKey);
					params.put("dataFormatId", dataFormatId);
					if (!ioTRulesService.loadRuleDynamically(params, 1,command,new RuleChangingCallback())) {
						logger.error("EngineListener-productAction ,load rule dynamically error,ruleId:{},msgId:{}.",rule.getRuleId(),msgId);
						EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",msgId ,"","EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
					}
				}
			}
			
			if (command == EngineCommand.PRODUCT_OFFLINE) {// delete product
				Map<String, Object> params = new HashMap<>();
				if (productRuleId == null) {
					logger.error("EngineListener-productAction,product not exists,productKey:{},productRuleId:{},msgId:{}.",productKey,productRuleId,msgId);
					return;
				}
				IotProductRule tmpProRule = ioTRulesService.getProductRuleByProductKey(productKey,1);
				if (tmpProRule == null) {
					logger.info("EngineListener-productAction,product already off-line,productKey:{},productRuleId:{},msgId:{}.",productKey,productRuleId,msgId);
					return;
				}
				ioTRulesService.updateProductRule(productRule);
				params.put("productRuleId", productRuleId);
				List<IotRules> rules = ioTRulesService.getRulesByProductKey(productKey);
				if (rules == null ) {
					logger.error("Product online ,not find rules,msgId:{}.",msgId);
					return;
				}
				for (IotRules rule : rules) {
					//update iot_rules status = 0 db
					if(rule.getStatus() == 1) {
						IotRules temp = new IotRules();
						temp.setRuleId(rule.getRuleId());
						temp.setStatus(0);
						ioTRulesService.updateRuleStatus(temp);
					}
					// delete all rules of the productKey
					params.put("ruleId", rule.getRuleId());
					params.put("productKey", productKey);
					params.put("dataFormatId", dataFormatId);
					if (!ioTRulesService.loadRuleDynamically(params, 1,command,new RuleChangingCallback())) {
						logger.error("PRODUCT_OFFLINE addRuleDynamiclly error,deleted product,msgId:{}.",msgId);
						EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",msgId ,"","EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
					}
				}
				return;
			}
			EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",msgId ,"","EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.SUCCESS);
			logger.info("EngineListener-productAction finished,msgId:{}.",msgId);
		} catch (Exception e) {
			logger.error("EngineListener-productAction exception,msgId:{},e={}",msgId, e);
		}
	}

	/**
	 * @des: received rules engine manager message and notify rulesEngine
	 * @param params
	 */
	private void notifyRuleEngineByManager(JSONObject jsonObject) {
		Object ruleType = jsonObject.get("ruletype");
		Object operType = jsonObject.get("opertype");
		String msgId = jsonObject.getString("msgId");
		if (ruleType == null || operType == null) {
			logger.error("EngineListener-notifyRuleEngineByManager paramers error,msgId:{}.",msgId);
			return;
		}
		//0:topic rule;1:product rule
		int ruletype = Integer.parseInt(ruleType.toString());
		//0:start;1:stop;3:update
		int opertype = Integer.parseInt(operType.toString());
		try {
			if (!ioTRulesService.loadRuleDynamically(jsonObject, ruletype ,parseCommand(opertype),new RuleChangingCallback())) {
				logger.error("EngineListener-notifyRuleEngineByManager addRuleDynamiclly error,msgId:{}.",msgId);
				EsLogHandler.toEsLogSystem("rulesEngineEmq", "EmqMessage",jsonObject.getString("msgId") ,jsonObject.toJSONString(),"EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
				return;
			}
		} catch (Exception e) {
			EsLogHandler.toEsLogSystem("rulesEngineEmq", "",jsonObject.getString("msgId") ,jsonObject.toJSONString(),"EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
			e.printStackTrace();
		}
		EsLogHandler.toEsLogSystem("rulesEngineEmq", "",jsonObject.getString("msgId") ,jsonObject.toJSONString(),"EmqMessage", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.SUCCESS);
	}

	private EngineCommand parseCommand(int opertype) {
		//0:start;1:stop;3:update
		if (opertype == 0)
			return EngineCommand.RULE_ADD;
		if (opertype == 1)
			return EngineCommand.RULE_DELETE;
		if (opertype == 3)
			return EngineCommand.RULE_UPDATE;
		return null;
	}
}
