
package com.syj.iot.rulesengine.rule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @des:RulesEngine system mapper module,for faster getting config
 * @author shenyanjun1
 * @date: 2018年5月22日 下午4:11:08
 */
public class IotRulesMapper {
	
	//save topicId,beanName string
	private static final Map<Long,String> topics = new ConcurrentHashMap<>();
	//save topicId,drl string(rule)
	private static final Map<Long,CopyOnWriteArrayList<PrecompiledRule>> topicRules =
			new ConcurrentHashMap<>();
	
	//(ruleId,actionId)
	private static final Map<Integer,Integer> rulesActions = new ConcurrentHashMap<>();
	//(actionId,Action)
	private static final Map<Integer,IotAction> actions = new ConcurrentHashMap<>();
	
	//productKey,IotProductRule
	private static final Map<String,IotProductRule> products = new ConcurrentHashMap<>();
	
	//productRuleId,rule
	private static final Map<Long,CopyOnWriteArrayList<PrecompiledRule>> productRules =
				new ConcurrentHashMap<>();
	
	//dataFormatId,{topicId,productKey(productRuleId)}
	private static final Map<Long,CopyOnWriteArrayList<String>> dataFormats =
			new ConcurrentHashMap<>();
	//kafka env flag
	private static String kafkaEnv ;
	//devices report results
	private static final Map<String,Long> report = new ConcurrentHashMap<>();
	
	public static void addReport(String key) {
		report.put(key, (report.get(key) == null ? 1 : report.get(key)+1));
	}
	public static Map<String,Long> getReport() {return report;}
	
	public static void addDataFromat(Long formatId,String id ) {
		CopyOnWriteArrayList<String> ids  = dataFormats.get(formatId);
		if (ids == null) {
			ids = new CopyOnWriteArrayList<String>();
			ids.add(id);
			dataFormats.put(formatId, ids);
		} else {
			ids.add(id);
		}
	}
	
	public static void deleteDataFromat(Long formatId,String id ) {
		CopyOnWriteArrayList<String> ids  = dataFormats.get(formatId);
		if (ids != null) {
			for (String tmpId :ids) {
				if (tmpId.equals(id)) {
					ids.remove(tmpId);
					break;
				}
			}
		} 
		
	}
	/**
	 * 
	 * @param formatId
	 * @param oldFormatId
	 * @param value :productKey or topicId
	 */
	public static void modifyDataFromat(Long formatId,Long oldFormatId,String value) {
		CopyOnWriteArrayList<String> ids  = dataFormats.get(oldFormatId);
		if (ids != null) {
			for (String tmp :ids) {
				if (tmp.equals(value)) {
					ids.remove(tmp);
					break;
				}
			}
		}
		if (dataFormats.get(formatId) == null) {
			CopyOnWriteArrayList<String> ls = new CopyOnWriteArrayList<String>();
			ls.add(value);
			dataFormats.put(formatId, ls);
		} else {
			dataFormats.get(formatId).add(value);
		}
		//modify rule's formatId
		Long productRuleId = products.get(value).getId();
		System.out.println("berore  rule list:" + productRules.get(productRuleId));
		for(PrecompiledRule rule: productRules.get(productRuleId)) {
			rule.setFormatId(formatId);
		}
	}
	
	public static Map<Long,CopyOnWriteArrayList<String>> getDataFormats() {
		return dataFormats;
	}
	
	public static void addProductRules(String productKey,IotProductRule proRule ) {
		products.put(productKey, proRule);
	}
	
	public static Map<Long,String> getTopics() {
		return topics;
	}
	
	public static Map<Long,CopyOnWriteArrayList<PrecompiledRule>> getRules() {
		return topicRules;
	}
	public static Map<String,IotProductRule> getProducts() {
		return products;
	}
	public static Map<Long,CopyOnWriteArrayList<PrecompiledRule>> getProRules() {
		return productRules;
	}
	public static void addTopic(Long topicId,String bean ) {
		topics.put(topicId, bean);
	}
	
	public static void addTopicDynamiclly(Long topicId,String bean ) {
		if (topics.get(topicId) == null)
			topics.put(topicId, bean);
		
	}
	
	public static void addProductDynamiclly(String porductKey,IotProductRule proRule ) {
		if (products.get(porductKey) == null)
			products.put(porductKey, proRule);
		
	}
	
	public static void deleteTopicRuleDynamiclly(Long topicId,int ruleId) {
		List<PrecompiledRule> rules = topicRules.get(topicId);
		if ( rules != null) {
			////UnsupportedOperationException
//			Iterator<PrecompiledRule> it = rules.iterator();
//			while (it.hasNext()) {
//				PrecompiledRule rule = it.next();
//				if (rule.getRuleId() == ruleId ) {
//					it.remove();
//					break;
//				}
//			}
			for (PrecompiledRule rule :rules) {
				if (rule.getRuleId() == ruleId ) {
					rules.remove(rule);
					break;
				}
			}
		}
		
	}
	
	public static void deleteProductRuleDynamiclly(Long productId,int ruleId) {
		List<PrecompiledRule> rules = productRules.get(productId);
		if ( rules != null) {
			for (PrecompiledRule rule :rules) {
				if (rule.getRuleId() == ruleId ) {
					rules.remove(rule);
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param topicId
	 * @param ruleId
	 * @param type 0:from topic,1:from product
	 * @return
	 */
	public static boolean isExistsRule(Long topicId,String productKey,int ruleId,int type) {
		CopyOnWriteArrayList<PrecompiledRule> rules = null;
		if (type == 0) 
			rules = getRulesByTopicId(topicId);
		if (type == 1)
			rules = getRulesByProductKey(productKey);
		if (rules == null) return false;
		for (PrecompiledRule rule :rules ) {
			if (rule.getRuleId() == ruleId)
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param id(topicid or productRuleId)
	 * @param content:rule's content(drl file string represent)
	 * @param type 0:add topic rule,1:add product rule
	 */
	public static void addRule(Long id,int ruleId,String content,String placeholder,int type,Long formatId) {
		String prefix = (type == 0) ?  "T" :"P";
		String pkg = prefix + id;
		PrecompiledRule preCompiledRule = new PrecompiledRule(ruleId,content,placeholder,formatId,pkg);
		CopyOnWriteArrayList<PrecompiledRule> rules = null;
		if (type == 0 )
			rules = topicRules.get(id);
		if (type == 1)
			rules = productRules.get(id);
		if (rules == null) {
			rules = new CopyOnWriteArrayList<>();
			rules.add(preCompiledRule);
			if (type == 0)
				topicRules.put(id, rules);
			if (type == 1)
				productRules.put(id, rules);
		} else {
			rules.add(preCompiledRule);
		}
	}
	
	public static CopyOnWriteArrayList<PrecompiledRule> getRulesByTopicId(Long topicId) {
		return topicRules.get(topicId);
	}
	
	public static CopyOnWriteArrayList<PrecompiledRule> getRulesByProductKey(String proKey) {
		if (!products.containsKey(proKey))
			return null;
		Long productId = products.get(proKey).getId();
		return  productRules.get(productId);
	}
	private static String getBeanByTopicId(Long topicId) {
		return topics.get(topicId);
	}

	
	private static String getBeanByProductKey(String proKey) {
		IotProductRule proRule = products.get(proKey);
		if (proRule != null)
			return proRule.getBeanName();
		else 
			return null;
	}
	
	public static String getBean(Long topicId,String proKey) {
		String bean = getBeanByTopicId(topicId);
		if (bean == null || bean.equals("")) { //device no set rule ,return porduct rule
			return getBeanByProductKey(proKey);
		} else
			return bean;
	}
	public static boolean isRuleConfigured(long topicId,String proKey) {
		return products.containsKey(proKey) || topics.containsKey(topicId) ;
	}
	public static void resetTopicMapper( ) {
		topics.clear();
		topicRules.clear();
	}
	
	public static void addAction(Integer actionId,IotAction action) {
		actions.put(actionId, action);
	}
	
	public static IotAction getActionById(Integer actionId) {
		return actions.get(actionId);
	}
	
	public static Map<Integer,IotAction> getActions() {
		return actions;
	}
	
	public static void addRuleAction(Integer ruleId,Integer actionId) {
		rulesActions.put(ruleId, actionId);
	}
	
	public static Integer getActionIdByRuleId(Integer ruleId) {
		return rulesActions.get(ruleId);
	}
	public static int getInitProductSize() {
		return products.size();
	}
	public static int getInitTopicSize() {
		return topics.size();
	}
	
	//save topicId,beanName string
	private static final Map<String,String> luaFunctions = new HashMap<>();
	public static void addFunction(String funName,String script) {
		luaFunctions.put(funName, script);
	}
	public static String getScript(String funName) {
		return luaFunctions.get(funName);
	}
	/**
	 * @return the kafkaEnv
	 */
	public static String getKafkaEnv() {
		return kafkaEnv;
	}

	/**
	 * @param kafkaEnv the kafkaEnv to set
	 */
	public static void setKafkaEnv(String env) {
		kafkaEnv = env;
	}

}
