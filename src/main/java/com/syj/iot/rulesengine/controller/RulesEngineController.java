package com.syj.iot.rulesengine.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.entrypoint.EntryPointTranspond;
import com.syj.iot.rulesengine.error.EngineConfigException;
import com.syj.iot.rulesengine.error.EngineRuntimeException;
import com.syj.iot.rulesengine.error.EsLogHandler;
import com.syj.iot.rulesengine.event.Event;
import com.syj.iot.rulesengine.init.IotKieContex;
import com.syj.iot.rulesengine.init.IotRulesConfigurationService;
import com.syj.iot.rulesengine.monitor.RuleMonitor;
import com.syj.iot.rulesengine.typedefine.DataEntryPoint;
import com.syj.iot.rulesengine.utils.KieUtils;
import com.syj.iot.rulesengine.utils.MessageUtil;

/**
 * 
 * @des:device data reported controller,is a entry for data transpond
 * @author shenyanjun1
 * @date: 2018年5月21日 下午1:33:01
 */
@RestController
@RequestMapping("/rulesengine")
public class RulesEngineController extends BaseController {
	@Autowired
	private IotRulesConfigurationService ioTRulesService;
	
	@Deprecated
	@RequestMapping("/reload")
	public String reload() throws IOException {
		JSONObject json = null;
		if (!ioTRulesService.reloadRules()) {
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(), json);
			return json.toJSONString();
		}
		//construct kie env
		try {
			IotKieContex.getIotKieContex().initIotKieContex(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
		return json.toJSONString();
	}
	/**
	 * @des get data report count
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/report")
	public String report() throws IOException {
		JSONObject json = null;
		Map<String,Long> report = ioTRulesService.getReport();
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),JSON.toJSONString(report));
		return json.toJSONString();
	}
	/**
	 * @des get topicId, or productKey rules
	 * @param topicId
	 * @param productKey
	 * @return
	 */
	@RequestMapping(value = "/rules", method = RequestMethod.GET)
	public String listRules(@RequestParam(value = "topicId") String topicId,
			@RequestParam(value = "productKey") String productKey) {
		JSONObject json = null;
		Map<String,Object> rules = KieUtils.getMemRulesSet(topicId, productKey);
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),JSON.toJSONString(rules));
		return json.toJSONString();
	}
	/**
	 * @des get productKey ,topicId rules
	 * @param productKey
	 * @return
	 */
	@RequestMapping(value = "/rules/{item}", method = RequestMethod.GET)
	public String listRulesByItem(@PathVariable String item) {
		JSONObject json = null;
		if (StringUtils.isBlank(item)) {
			json = getReturnJson(ResponseMsgEnum.RE_TOPIC_ERROR.getCode(), ResponseMsgEnum.RE_TOPIC_ERROR.getMsg(),json);
			return json.toJSONString();
		}
		Map<String,Object> rules = null;
		if (KieUtils.isDependcyProduct(item))
			rules = KieUtils.getMemRulesSet(null, item);
		if (KieUtils.isDependcyTopicId(item))
			rules = KieUtils.getMemRulesSet(item, null);
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),JSON.toJSONString(rules));
		return json.toJSONString();
	}
	
	/**
	 * @des get topicId's dataformat
	 * @param topicId
	 * @return
	 */
	@RequestMapping(value = "/dataFormat/{item}", method = RequestMethod.GET)
	public String getDataFromatIdByItem(@PathVariable String item) {
		JSONObject json = null;
		if (StringUtils.isBlank(item)) {
			json = getReturnJson(ResponseMsgEnum.RE_TOPIC_ERROR.getCode(), ResponseMsgEnum.RE_TOPIC_ERROR.getMsg(),json);
			return json.toJSONString();
		}
		Long id = null;
		if (KieUtils.isDependcyProduct(item))
			id = KieUtils.getDataFormatId(null, item);
		if (KieUtils.isDependcyTopicId(item))
			id = KieUtils.getDataFormatId(item, null);
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),String.valueOf(id));
		return json.toJSONString();
	}
	
	/**
	 * @des get topicId, or productKey rules
	 * @param topicId
	 * @param productKey
	 * @return
	 */
	@RequestMapping(value = "/dataFormat", method = RequestMethod.GET)
	public String getDataFromatId(@RequestParam(value = "topicId") String topicId,
			@RequestParam(value = "productKey") String productKey) {
		JSONObject json = null;
		Long id = KieUtils.getDataFormatId(topicId, productKey);
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),String.valueOf(id));
		return json.toJSONString();
	}
	
	//deprecated ,now do not depned on java bean
	@Deprecated
//	@RequestMapping(value = "/topic", method = RequestMethod.POST)
	public String topicTranspond2(@RequestParam(value = "topicId", required = true) String topicId,
			@RequestParam(value = "productKey", required = true) String productKey,
			@RequestBody String devicesData) {
		logger.info("RulesEngine received topic id:{},productKey:{},data:{}", topicId,productKey, devicesData);
		JSONObject json = null;
		if (IotKieContex.getIotKieContex().isIotKieBusy()) {
			//buffer data
			ioTRulesService.bufferData(topicId,productKey,devicesData);
			logger.info("RulesEngine cached data, topic id:{},productKey:{},data:{}.", topicId, productKey,devicesData);
			json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
			return json.toJSONString();
		}
		// get topic data format by topicId(iot_topic)
		String clsName = ioTRulesService.getClsName(Integer.parseInt(topicId),productKey);
		if (clsName == null || clsName.equals("")) {
			json = getReturnJson(ResponseMsgEnum.RE_RULE_ERROR.getCode(), ResponseMsgEnum.RE_RULE_ERROR.getMsg(),
					json);
			return json.toJSONString();

		}

		try {
			long startTime = System.currentTimeMillis();
			Event event = (Event) JSON.parseObject(devicesData, Class.forName(clsName));
			event.setJsonObject(JSON.parseObject(devicesData));
			KieUtils.fireAllRulesOneTime(event, Integer.parseInt(topicId),productKey,IotKieContex.getIotKieContex().getJobSession());
			long endTime = System.currentTimeMillis();
			logger.info("RulesEngine transpond topic id:{} data finished,time cost {}ms ", topicId,endTime - startTime);
		} catch (Exception e) {
			e.printStackTrace();
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(), json);
			return json.toJSONString();
		}
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
		return json.toJSONString();
	}
	
	/**
	 * 
	 * @param ruletype 0:topic 1:product
	 * @param opertype 0:start using,1:stop using
	 * @param params
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	@RequestMapping(value = "/topic/addrule", method = RequestMethod.POST)
	public String addRule(@RequestParam(value = "ruletype", required = true)int ruletype,
						  @RequestParam(value = "opertype", required = true)int opertype,
						  @RequestBody Map<String,Object> params)   throws IOException {
		logger.info("RulesEngine received params:{}.",JSON.toJSONString(params));
		JSONObject json = null;
//		if (!ioTRulesService.addRuleDynamically(params,ruletype,opertype,new RuleChangingCallback())) {
//			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(), json);
//			return json.toJSONString();
//		}
//		
//		//construct kjar
//		IotKieContex.getIotKieContex().addOneRule(ioTRulesService.getDynamicalRule());
		
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
		return json.toJSONString();

	}
	
	/**
	 * @des transpond data to cloudplatform
	 * @param topicId
	 * @param productKey
	 * @param devicesData
	 * @return
	 */
	@RequestMapping(value = "/topic", method = RequestMethod.POST)
	public String topicTranspond(@RequestParam(value = "topicId", required = true) String topicId,
			@RequestParam(value = "productKey", required = true) String productKey,
			@RequestBody String devicesData) {
		logger.info("RulesEngine received device's reported data,msgId:{},topic id:{},productKey:{}.", MessageUtil.getMsgId(devicesData),topicId,productKey);
		EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(devicesData), productKey, 
									MessageUtil.getMsgId(devicesData),devicesData,"",DataLogEnum.RE_RECEIVE_DATA, DataLogErrorCodeEnum.SUCCESS);
		JSONObject json = null;
		if (IotKieContex.getIotKieContex().isIotKieBusy()) {
			//buffer data
			ioTRulesService.bufferData(topicId,productKey,devicesData);
			json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
			return json.toJSONString();
		}
		try {
			EntryPointTranspond.transpond(topicId, productKey, devicesData, DataEntryPoint.HTTP_POINT);
		} catch (EngineConfigException e) {
			System.out.print("Error occur:" + new Date() + ",");
			e.printStackTrace();
			error.error(e.getMessage());
			json = getReturnJson(ResponseMsgEnum.RE_RULE_ERROR.getCode(), ResponseMsgEnum.RE_RULE_ERROR.getMsg(),
					json);
			return json.toJSONString();
		} catch(EngineRuntimeException e) {
			System.out.print("Error occur:" + new Date() + ",");
			e.printStackTrace();
			error.error(e.getMessage());
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(),
					json);
			return json.toJSONString();
		}  catch(Exception e) {
			System.out.print("Error occur:" + new Date() + ",");
			e.printStackTrace();
			error.error(e.getMessage());
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(),
					json);
			return json.toJSONString();
		}
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(), json);
		return json.toJSONString();
	}
	
	
	/**
	 * @des product online add product's default rule
	 * @param productKey
	 * @return
	 */
	@RequestMapping(value = "/productOnline", method = RequestMethod.POST)
	public String productOnline(@RequestParam(value = "productKey", required = true) String productKey) {
		logger.info("RulesEngine received product online call,productKey:{}",productKey);
		JSONObject json = null;
		JSONObject data = new JSONObject();
		try {
			Long productRuleId = ioTRulesService.getProductRuleIdByProductKey(productKey);
			if (productRuleId != null) {
				logger.error("RulesEngine-productAction,product exists,productKey:{},productRuleId:{}",productKey,productRuleId);
				json = getReturnJson(ResponseMsgEnum.RE_PRODUCTKEY_EXISTS_ERROR.getCode(), ResponseMsgEnum.RE_PRODUCTKEY_EXISTS_ERROR.getMsg(),
						json);
				return json.toJSONString();
			}
			IotProductRule productRule = new IotProductRule();
			productRule.setProductKey(productKey);
			productRule.setCreateTime(new Date());
			productRule.setRuleStatus(1);
			productRule.setUseStatus(0);
			long proRuleId = ioTRulesService.addProductRule(productRule);
			if (proRuleId <= 0) {
				ioTRulesService.deleteProductRule(productRule);
				logger.error("RulesEngine-productAction,add product rule to db ,productRuleId respond null.");
				json = getReturnJson(ResponseMsgEnum.RE_PRODUCTKEY_DUPLICATE_ERROR.getCode(), ResponseMsgEnum.RE_PRODUCTKEY_DUPLICATE_ERROR.getMsg(),
						json);
				return json.toJSONString();
			}
			data.put("id", proRuleId);
			logger.info("RulesEngine add product,productKey:{} ok.",productKey);
			
		} catch (Exception e) {
			e.printStackTrace();
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(), json);
			return json.toJSONString();
		}
	
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg(),data);
		return json.toJSONString();
	}
	
	/**
	 * @des :for product creating add default rule
	 * @param productRuleId
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/rule", method = RequestMethod.POST)
	public String addDefaultRule(@RequestParam(value = "id", required = true) String productRuleId,
			@RequestParam(value = "userId", required = true) Integer userId,
			@RequestParam(value="actionId",defaultValue = "1") Integer actionId) {
		logger.info("RulesEngine received rule add call,productRuleId:{},userId:{},actionId={}.",productRuleId,userId,actionId);
		JSONObject json = null;
		try {
			// add default rule
			Map<String, Object> params = new HashMap<>();
			params.put("productRuleId", productRuleId);
			params.put("attribute", "*");
//			params.put("con", "data.rssi >= -67 and data.time >= $proxy.method(\"getTimestampOfToday\",9,0,0) and data.time <= $proxy.method(\"getTimestampOfToday\",22,0,0)");
			params.put("actionId", actionId);
			params.put("userId", userId);
			Integer ruleId = addRuleToDb(params);
			if (ruleId == null || ruleId == 0) {
				logger.error("EngineListener-productAction,add rule to db ,ruleId respond null.");
				json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(),
						json);
				return json.toJSONString();
			}
			logger.info("RulesEngine add default rule ok,productRuleId:{}.",productRuleId);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json = getReturnJson(ResponseMsgEnum.SYSTEM_ERROR.getCode(), ResponseMsgEnum.SYSTEM_ERROR.getMsg(), json);
			return json.toJSONString();
		}
	
		json = getReturnJson(ResponseMsgEnum.RE_SUCCESS.getCode(), ResponseMsgEnum.RE_SUCCESS.getMsg());
		return json.toJSONString();
	}
	
	/**
	 * @des add rule to db
	 * @param params
	 * @return
	 */
	private Integer addRuleToDb(Map<String, Object> params) {
		// String topId = params.get("topicId").toString();
//		String con = params.get("con").toString();
		String attribute = params.get("attribute").toString();
		String actionId = params.get("actionId").toString();
		String proRuleId = params.get("productRuleId").toString();
		Integer userId = Integer.parseInt(params.get("userId").toString());
		// add default rule for topic
		IotRules rule = new IotRules();
		rule.setAttribute(attribute);
		rule.setActionId(Integer.parseInt(actionId));// default action to kafka
		rule.setCreateTime(new Date());
		// rule.setTopicId(Integer.parseInt(topId));
		rule.setTopicId(0);// default value
		rule.setProductRuleId(Integer.parseInt(proRuleId));
		rule.setStatus(1);
//		rule.setCon(con);
		rule.setSqlRule("product-auto");
		rule.setUserId(userId);
		return ioTRulesService.addRuleToDb(rule);

	}
}
