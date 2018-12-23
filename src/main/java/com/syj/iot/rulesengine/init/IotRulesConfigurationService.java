
/**
 * 
 */
package com.syj.iot.rulesengine.init;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.cache.DataCache;
import com.syj.iot.rulesengine.rule.Condition;
import com.syj.iot.rulesengine.rule.IotRulesMapper;
import com.syj.iot.rulesengine.rule.Rule;
import com.syj.iot.rulesengine.rule.RuleChangingCallback;
import com.syj.iot.rulesengine.typedefine.CommandParameter;
import com.syj.iot.rulesengine.typedefine.EngineCommand;
import com.syj.iot.rulesengine.typedefine.RuleCompileType;
import com.syj.iot.rulesengine.typedefine.SystemConstant;
import com.syj.iot.rulesengine.utils.KieUtils;
import com.syj.iot.rulesengine.utils.SqlStatementParser;

/**
 * @des:RulesEngine service ,load rules db config to Rule's Mapper
 * @author shenyanjun1
 * @date: 2018年5月25日 下午5:26:52
 */
@Service
public class IotRulesConfigurationService {
	private static Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");
	@Autowired
	private IotRulesService rulesService;
	@Autowired
	private IotTopicService topicService;

	@Autowired
	private IotActionService actionService;
	
	@Autowired
	private IotProductRuleService productRuleService;
	@Autowired
	private IotRuleFunctionsService functionService;
	// now can not using it
	private String dynamicalRule;
	//kafka env 0:dev;1:product
	@Value("${kafka.env:1}")
    private String kafkaEnvFlag;
	//kafka env 0:dev;1:product
	@Value("${kafka.pull.flag:0}")
	private String kafkaPullFlag;
	//for get dataFormatId
	@Autowired
	private ShareBiz shareBiz;
	
	//for report
	@Autowired
	private IotDataReportService reportService; 
	
	/**
	 * @return the dynamicalRule
	 */
	public String getDynamicalRule() {
		return dynamicalRule;
	}
	
	/**
	 * @des init topics by config set
	 * @param exeType, deprecated
	 *            false :execute one rule one time,true:execute all rules one
	 *            time(faster)
	 * @return false init error ,true ok
	 */
	private boolean initTopics() {
		long startTime = System.currentTimeMillis();
		logger.info("######Init topics rules begin...");
		// get topics from db(select topicId,beanName from iot_topic where status = 1;)
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("status", 0);
		params.put("ruleStatus", 1);
		List<IotTopic> topics = topicService.getIotTopicList(params);
		if (topics == null) {
			logger.warn("###### topics  not set rules,may be set by product.");
			return false;
		}
		for (IotTopic topic : topics) {
			long topicId = topic.getId();
			String bean = topic.getBeanName();
			Long dataFormatId = shareBiz.getFormatIdByIotThing(topic.getThingId());
			if (dataFormatId == -1) {
				logger.warn("######Init topic rules, get data format id error,topicId:{}.",topicId);
				continue;
			}
			if (!initRules(topicId, bean,0,dataFormatId)) {
				logger.warn("######Init topic rules, topic not set rules,topicId:{}.",topicId);
				continue;
			}
			IotRulesMapper.addTopic(topicId, bean);
			IotRulesMapper.addDataFromat(dataFormatId, String.valueOf(topicId));
//			IotRulesMapper.addDataFromat(topic.getDataFormatId(), String.valueOf(topicId));
		}
		long endTime = System.currentTimeMillis();
		logger.info("######Init topics rules end,topic number:{} need:{} time ms. ",IotRulesMapper.getInitTopicSize(),(endTime - startTime));
		return true;

	}
	
	
	/**
	 * 
	 * @return true reload ok or false
	 */
	public boolean reload() {
		long startTime = System.currentTimeMillis();
		logger.info("RulesEngine ######reload rules begin.");
		IotRulesMapper.resetTopicMapper();
		if (!initTopics())
			return false;
		long endTime = System.currentTimeMillis();
		logger.info("RulesEngine ######reload rules end ,cost time: " + (endTime - startTime) + " ms");
		return true;
	}

	/**
	 * 
	 * @return true reload ok or false
	 */
	public boolean reloadRules() {
		long startTime = System.currentTimeMillis();
		logger.info("######reload rules begin.");
		IotRulesMapper.resetTopicMapper();
		if (!initTopics())
			return false;
		long endTime = System.currentTimeMillis();
		logger.info("######reload rules end ,cost time: " + (endTime - startTime) + " ms");
		return true;
	}

	/**
	 * @des add a rule dynamically by http request
	 * @param params
	 *            ( )
	 * @param ruletype:0 topic rule, 1:product rule
	 * @return true reload ok or false
	 */
	public boolean loadRuleDynamically(Map<String, Object> params,int ruletype,EngineCommand command,RuleChangingCallback callback) {
		long startTime = System.currentTimeMillis();
		logger.info("######load rule begin,ruletype:{},opertype:{}",ruletype,command.getMsg());
		CommandParameter cmdParameter = new CommandParameter();
		long productRuleId = -1;
		if (params.get("productRuleId") != null)
			productRuleId = Long.valueOf(params.get("productRuleId").toString());
		
		long topicId = -1;
		if (params.get("topicId") != null)
			topicId = Long.valueOf(params.get("topicId").toString());
		
		String productKey = null;
		if (params.get("productKey") != null)
			productKey = params.get("productKey").toString();
		
		//ruleId, dataFormatId must pass
		String id = null; //productRuleId or topicId
		String ruleId = params.get("ruleId").toString();
		Long dataFormatId = Long.valueOf(params.get("dataFormatId").toString());
		
		if (ruletype == 0) {//topic
			id = String.valueOf(topicId);
		}
		if (ruletype == 1) {//product
			id = String.valueOf(productRuleId);
		}
		if (command == EngineCommand.RULE_ADD) {
			if (ruletype == 0) {
				IotRulesMapper.addTopicDynamiclly(topicId, "bean");
				IotRulesMapper.addDataFromat(dataFormatId, id);
			}
			if (ruletype == 1) {
				IotProductRule proRule = new IotProductRule();
				proRule.setId(productRuleId);
				proRule.setProductKey(productKey);
				IotRulesMapper.addProductDynamiclly(productKey, proRule);
				IotRulesMapper.addDataFromat(dataFormatId, productKey);
			}
		}
		
		if (command == EngineCommand.RULE_DELETE) {
			if (ruletype == 0) {
				IotRulesMapper.deleteTopicRuleDynamiclly(topicId,Integer.parseInt(ruleId));
				IotRulesMapper.deleteDataFromat(dataFormatId, id);
			}
			if (ruletype == 1) {
				IotRulesMapper.deleteProductRuleDynamiclly(productRuleId,Integer.parseInt(ruleId));
				IotRulesMapper.deleteDataFromat(dataFormatId, productKey);
			}
		}
		if (command == EngineCommand.RULE_UPDATE) { //update rule,first delete and add
			if (ruletype == 0) 
				IotRulesMapper.deleteTopicRuleDynamiclly(topicId,Integer.parseInt(ruleId));
			if (ruletype == 1)
				IotRulesMapper.deleteProductRuleDynamiclly(productRuleId,Integer.parseInt(ruleId));
		}
		
		if (command == EngineCommand.PRODUCT_ONLINE ) {
			IotProductRule proRule = new IotProductRule();
			proRule.setId(productRuleId);
			proRule.setProductKey(productKey);
			IotRulesMapper.addProductDynamiclly(productKey, proRule);
			IotRulesMapper.addDataFromat(dataFormatId, productKey);
		}
		if (command == EngineCommand.PRODUCT_OFFLINE ) {
			IotRulesMapper.deleteProductRuleDynamiclly(productRuleId,Integer.parseInt(ruleId));
			IotRulesMapper.deleteDataFromat(dataFormatId, productKey);
		}
		
		if (command == EngineCommand.PRODUCT_ONLINE || command == EngineCommand.RULE_UPDATE
			|| command == EngineCommand.RULE_ADD) {
				if (!createRule(params,id,ruleId,ruletype,dataFormatId)) {
					logger.error("Create a rule error.");
					return false;
				}
		}
		if (command == EngineCommand.PRODUCT_MODIFY_DATEFORMAT) {
			Long oldDataFormatId = Long.valueOf(params.get("oldDataFormatId").toString());
			modifyDataFromat(dataFormatId, oldDataFormatId,productKey);
		}
		cmdParameter.setRuleStr(this.dynamicalRule);
		cmdParameter.setDataFormatId(dataFormatId);
		if (!callback.notifyEngine(command, cmdParameter)) {
			logger.error("######load rule dynamically error.");
			return false;
		}
		long endTime = System.currentTimeMillis();
		logger.info("######load rule dynamically end ,cost time: " + (endTime - startTime) + " ms");
		return true;
	}

	/**
	 * 
	 * @param params
	 * @param id
	 * @param ruleId
	 * @param ruletype
	 * @param dataFormatId
	 * @return
	 */
	private boolean createRule(Map<String, Object> params,String id,String ruleId
			,int ruletype,Long dataFormatId) {
		String attribute = null;
		if (params.get("attribute") != null)
			attribute = params.get("attribute").toString();
		String con = null;
		if (params.get("con") != null)
			con = params.get("con").toString();
		if (!generateRulesDynamically(Long.parseLong(id), Integer.parseInt(ruleId), "bean", con, attribute, true,ruletype,dataFormatId)) {
			logger.info("###### generateRulesDynamically topicId:{} ,ruleId:{} error.", id, ruleId);
			return false;
		}
		//add rule's action
		String actionId = params.get("actionId").toString();
		IotRulesMapper.addRuleAction(Integer.parseInt(ruleId), Integer.parseInt(actionId));
		return true;
	}
	/**
	 * @des init rules by config set
	 * @param topicId
	 * @param bean
	 * @param type 0:get topic rule,1:get product rule
	 * @return false or true
	 */
	private boolean initRules(Long id, String bean,int type,Long formatId) {
		List<IotRules> rules = null;
		if (type == 0)
			rules = rulesService.getRulesByTopicId(id.intValue());
		if (type == 1)
			rules = rulesService.getRulesByProductRuleId(id.intValue());
		if (rules == null || rules.isEmpty()) 
			return false;
		
		for (IotRules rule : rules) {
			String seq = id.toString();
			String con = rule.getCon();

			Rule ruleObj = createOneRuleByConditions(rule.getRuleId(), con, Rule.eventType.TRANSPOND, 1);
			// get second event(filter)
			Rule subRuleObj = createOneRuleByConditions(rule.getRuleId(), con, Rule.eventType.TRANSPOND, 2);
			// after set db on every rule
			RuleCompileType compileType = null;
			if (subRuleObj.getConditions().isEmpty())
				compileType = RuleCompileType.TRANSPOND_ONLY;
			else
				compileType = RuleCompileType.TRANSPOND_FILTER;
			seq += "_";
			seq += rule.getRuleId().toString();

			String drl = compileRule(compileType, bean, ruleObj, subRuleObj, seq);
			if (drl == null) {
				logger.warn("######Compile rule error,topicId={},bean={}", id, bean);
				continue;
			}
			String attributes = rule.getAttribute();
			IotRulesMapper.addRule(id, rule.getRuleId(), drl, attributes,type,formatId);
			
			// add action
			IotAction action = actionService.getActionById(rule.getActionId());
			if (action == null) {
				logger.warn("######Rule set action error, action null,ruleId={},action id:{}", rule.getActionId(),rule.getActionId());
				continue;
			}
			IotRulesMapper.addRuleAction(rule.getRuleId(), action.getActionId());
		}
		return true;

	}

	/**
	 * @des create Rule obj by contions
	 * @param ruleId
	 * @param con
	 * @param type
	 * @param flag
	 *            1:get primary event, 2:get second event,if no exists return null.
	 * @return
	 */
	private Rule createOneRuleByConditions(int ruleId, String con, Rule.eventType type, int flag) {
		List<Condition> results = new ArrayList<>();
		String item = null;
		if (con != null && !con.trim().equals("")) {
			List<Condition> cons = SqlStatementParser.getConditions(con);
			if (cons != null) {//only exists one condition and illegal format,cons is null
				for (Condition condition : cons) {
					if (flag == 1 && !condition.isSubEvent())
						results.add(condition);
					if (flag == 2 && condition.isSubEvent()) {
						results.add(condition);
						item = condition.getEventItem();
					}
				}//end for
			}//end if != null
		}
		Rule rule = new Rule();
		rule.setEventType(type);
		rule.setEventItem(item);
		rule.setRuleId(ruleId);
		if (results.isEmpty()) {
			logger.info("######Create rule id={},{} condition no set,condition={},event type={}", ruleId,
					flag == 1 ? "primary" : "second", con, Rule.eventType.TRANSPOND);
		}
		rule.setConditions(results);
		return rule;
	}

	/**
	 * @des compile rule
	 * @param compileType
	 *            1:only one level condition;2:two level condition
	 * @param bean
	 * @param rule
	 * @param rule
	 * @param seq
	 * @return
	 */
	private String compileRule(RuleCompileType compileType, String bean, Rule rule, Rule subRule, String seq) {
		String drl = null;
		try {
			drl = KieUtils.templatize(compileType, bean, rule, subRule, seq);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("######Compile rule error:bean id={}", bean);
		}
		return drl;
	}

	/**
	 * @des init rules by request
	 * @param topicId
	 * @param bean
	 * @param executeType:
	 *            false :execute one rule one time,true:execute all rules one
	 *            time(faster)
	 * @return false or true
	 */
	private boolean generateRulesDynamically(Long id, Integer ruleId, String bean, String con, String attri,
			boolean executeType,int type,Long dataformatId) {
		
		String seq = id.toString();

		Rule ruleObj = createOneRuleByConditions(ruleId, con, Rule.eventType.TRANSPOND, 1);
		Rule subRuleObj = createOneRuleByConditions(ruleId, con, Rule.eventType.TRANSPOND, 2);
		seq += "_";
		seq += ruleId;
		RuleCompileType compileType = null;
		if (subRuleObj.getConditions().isEmpty())
			compileType = RuleCompileType.TRANSPOND_ONLY;// no filter
		else
			compileType = RuleCompileType.TRANSPOND_FILTER;// filter

		String drl = compileRule(compileType, bean, ruleObj, subRuleObj, seq);
		if (drl == null) {
			logger.error("######Compile rule error,topicId={},bean={}", id, bean);
			return false;
		}
		dynamicalRule = drl;
		IotRulesMapper.addRule(id, ruleId, drl, attri,type,dataformatId);
		return true;
	}


	/**
	 * 
	 * @param topicId
	 * @param proKey product key
	 * @return class name
	 */
	public String getClsName(long topicId,String proKey) {
		// get topic data format by topicId(iot_topic)
		return IotRulesMapper.getBean(topicId, proKey);

	}
	
	public boolean isRuleConfigured(long topicId,String proKey) {
		return IotRulesMapper.isRuleConfigured(topicId, proKey);
	}
	
	public void bufferData(String topicId,String productKey, String data) {
		logger.info("######buffer one data.");
		DataCache.getDataCache().putBufferData(topicId, productKey,data);
	}
	
	public Integer addRuleToDb(IotRules rule) {
		return rulesService.addRule(rule);
	}
	public Integer getMaxRuleId() {
		return rulesService.getMaxRuleId();
	}
	public void updateRuleStatus(IotRules rule) {
		 rulesService.updateRule(rule);
	}
	public void updateTopicBean(IotTopic topic) {
		 topicService.updateBeanTopicById(topic);
	}
	
	/**
	 * 
	 * @param 
	 * @return
	 */
	private boolean initPrudcutRules() {
		long startTime = System.currentTimeMillis();
		logger.info("######Init product rules begin...");
		// get topics from db(select topicId,beanName from iot_topic where status = 1;)
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("useStatus", 0);
		params.put("ruleStatus", 1);
		List<IotProductRule> productRules = productRuleService.getProductRules(params);
		if (productRules == null) {
			logger.info("######no set product rules,may be set by topic.");
			return false;
		}
		for (IotProductRule productRule : productRules) {
			String proKey = productRule.getProductKey();
			String bean = productRule.getBeanName();
			Long dataFormatId = shareBiz.getFormatIdByProductKey(proKey);
			if (dataFormatId == -1) {
				logger.warn("######Init product rules, get data format id error,product:{}.",productRule.getId());
				continue;
			}
			if (!initRules(productRule.getId(), bean,1,dataFormatId)) {
				logger.warn("######Init product rules ,product not set rules. product id:{}",productRule.getId());
				continue;
			}
			IotRulesMapper.addProductRules(proKey, productRule);
//			IotRulesMapper.addDataFromat(productRule.getDataFormatId(), proKey);
			IotRulesMapper.addDataFromat(dataFormatId, proKey);
		}

		long endTime = System.currentTimeMillis();
		logger.info("######Init product rules end,product number:{} need time:{} ms" ,
					IotRulesMapper.getInitProductSize(), (endTime - startTime));
		return true;

	}
	/**
	 * @des  init topic,product rules from db
	 * @return true ,false
	 */
	public  boolean init() {
		boolean initP = initPrudcutRules();
		if (!initP) {
			logger.info("RulesEngine ######Init product rules,product not set rules.");
		}
		boolean initT = initTopics();
		if (!initT) {
			logger.info("RulesEngine ######Init topic  rules ,topic not set rules.");
		}
		if (!initP && !initT) {
			logger.error("RulesEngine ######Init product and topic  rules error ,no rules set  in rulesEngine system.");
			return false;
		}
		if (!initActionFunc()) {
			logger.error("RulesEngine ######Init actions and functions, actions null, it must be set in rulesEngine system.");
			return false;
		}
		System.out.println("Kafka env flag:" + kafkaEnvFlag );
		IotRulesMapper.setKafkaEnv(kafkaEnvFlag);
		syncReportResult();
		return true;
	}
	
	private boolean initActionFunc() {
		List<IotAction> actions = actionService.getActions();
		if (actions == null || actions.isEmpty()) {
			logger.error("RulesEngine ###### load actions error,actions no set.");
			return false;
		}
		for (IotAction action : actions) {
			IotRulesMapper.addAction(action.getActionId(), action);
		}
		List<IotRuleFunctions> functions = functionService.getRuleFunctions();
		if (functions == null || functions.isEmpty()) {
			logger.info("RulesEngine ###### no supply functions feature.");
			return true;
		}
		for (IotRuleFunctions func : functions) {
			IotRulesMapper.addFunction(func.getFuncName().trim(), func.getFuncScript().trim());
		}
		return true;
	}
	
	public long addProductRule(IotProductRule productRule) {
		return productRuleService.addProductRule(productRule);
	}
	public void updateProductRule(IotProductRule productRule) {
		productRuleService.updateRuleStatus(productRule);
	}
	public void deleteProductRule(IotProductRule productRule) {
		productRuleService.deleteProductRule(productRule);
	}
	public List<IotRules> getRulesByProductKey(String productKey) {
		Map<String,Object> params = new HashMap<>();
		params.put("productKey", productKey);
		List<IotProductRule> productRules = productRuleService.getProductRules(params);
		if(productRules == null || productRules.isEmpty()) {
			logger.error("RulesEngine ######getRulesByProductKey error.");
			return null;
		}
		if (productRules.size() > 1) {
			logger.error("RulesEngine ######getRulesByProductKey error , one productKey have more than one rules.");
			return null;
		}
		Long productRuleId = productRules.get(0).getId();
		return rulesService.getRulesByProductRuleId(productRuleId.intValue());
	}
	
	public IotProductRule getProductRuleByProductKey(String productKey,Integer ruleStatus) {
		Map<String,Object> params = new HashMap<>();
		params.put("productKey", productKey);
		params.put("ruleStatus", ruleStatus);
		List<IotProductRule> productRules = productRuleService.getProductRules(params);
		if(productRules == null  || productRules.isEmpty()) {
			logger.error("RulesEngine ######getRulesByProductKey error.");
			return null;
		}
		if (productRules.size() > 1) {
			logger.error("RulesEngine ######getRulesByProductKey error , one productKey have more than one rules.");
			return null;
		}
		return productRules.get(0);
	}
	
	public Long getProductRuleIdByProductKey(String productKey) {
		Map<String,Object> params = new HashMap<>();
		params.put("productKey", productKey);
		params.put("ruleStatus", 1);
		List<IotProductRule> productRules = productRuleService.getProductRules(params);
		if(productRules == null || productRules.isEmpty()) {
			logger.info("RulesEngine ######getProductRuleIdByProductKey null.");
			return null;
		}
		if (productRules.size() > 1) {
			logger.error("RulesEngine ######getProductRuleIdByProductKey error , one productKey have more than one rules.");
			return null;
		}
		Long productRuleId = productRules.get(0).getId();
		return productRuleId;
	}
	
	private void modifyDataFromat(Long dataFormatId,Long oldDataFormatId,String value) {
		IotRulesMapper.modifyDataFromat(dataFormatId, oldDataFormatId,value);
	}
	
	private void syncReportResult( ) {
		 Map<String,Object> report = parseFile();
		 if (report == null) {
			 return;
		 }
		 for (Entry<String,Object> entry : report.entrySet()) {
			 IotDataReport dbReport = reportService.getReportByKey(entry.getKey());
			 Long dbNum = 0L;
			 if (dbReport != null) {
				 dbNum = dbReport.getCnt();
				 dbNum += Long.valueOf(entry.getValue().toString());
				 dbReport.setCnt(dbNum);
				 System.out.println("Sync report,key=" + entry.getKey() + " ,num:" + entry.getValue());
				 reportService.updateCntByKey(dbReport);
			 } else {
				 dbReport = new IotDataReport(entry.getKey(),Long.valueOf(entry.getValue().toString()));
				 reportService.syncOneReport(dbReport);
				 System.out.println("Sync report, new key=" + entry.getKey() + " ,num:" + entry.getValue());
			 }
			 
		 }
	}

	public Map<String,Long> getReport() {
		Map<String,Long> result = new HashMap<>();
		List<IotDataReport> report = reportService.getReport();
		if (report == null || report.isEmpty()) {
			for (Entry<String,Long> rep : IotRulesMapper.getReport().entrySet()) {
				result.put(rep.getKey(), rep.getValue());
			}
			return result;
		}
		
		List<String> dbKeys = new ArrayList<>();
		for (IotDataReport rep : report) {
			Long memCnt = IotRulesMapper.getReport().get(rep.getReportKey());
			result.put(rep.getReportKey(), rep.getCnt() + (memCnt == null ? 0 : memCnt));
			dbKeys.add(rep.getReportKey());
		}
		for (Entry<String,Long> rep : IotRulesMapper.getReport().entrySet()) {
			if (dbKeys.contains(rep.getKey()))
				continue;
			result.put(rep.getKey(), rep.getValue());
		}
		return result;
	}
	
	/**
	 * @des parse report file
	 * @return
	 */
	private Map<String,Object> parseFile() {
		InputStream input = null;
		try {
			input = new FileInputStream(SystemConstant.REPORT_FILE);
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			System.out.println("Report file:" + SystemConstant.REPORT_FILE + " file no found.");
			return null;
		}
		String sContent = FileUtil.inputStream2String(input);
		boolean isDelete = FileUtil.deleteFile(SystemConstant.REPORT_FILE);
		if (!isDelete) 
			System.out.println("Delete file:" + SystemConstant.REPORT_FILE + ",  error.");
		return JSONObject.parseObject(sContent);
		
	}

	/**
	 * @return the kafkaEnvFlag
	 */
	public String getKafkaEnvFlag() {
		return kafkaEnvFlag;
	}

	/**
	 * @return the kafkaPullFlag
	 */
	public String getKafkaPullFlag() {
		return kafkaPullFlag;
	}

	
}
