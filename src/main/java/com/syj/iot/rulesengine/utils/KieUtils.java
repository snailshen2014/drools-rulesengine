package com.syj.iot.rulesengine.utils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.command.Command;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.event.Event;
import com.syj.iot.rulesengine.event.Topic;
import com.syj.iot.rulesengine.rule.Action;
import com.syj.iot.rulesengine.rule.IotRulesMapper;
import com.syj.iot.rulesengine.rule.PrecompiledRule;
import com.syj.iot.rulesengine.rule.Rule;
import com.syj.iot.rulesengine.rule.RuleGroupActivator;
import com.syj.iot.rulesengine.rule.RuleProxy;
import com.syj.iot.rulesengine.rule.TrackingAgendaEventListener;
import com.syj.iot.rulesengine.typedefine.RuleCompileType;
import com.syj.iot.rulesengine.typedefine.SystemConstant;

/**
 * @des:kie common method to get KieContainer
 * @author shenyanjun1
 * @date: 2018年5月21日 下午1:30:51
 */
public class KieUtils {
	private static final String RULES_PATH = "rules/";

	/**
	 * @param compileType
	 * @param modified
	 *            event to className for precompile all topic rules
	 * 
	 * @param rule
	 * @param rule
	 * @param seq
	 *            :rules name seq
	 * @return the rule string
	 * @throws Exception
	 */
	public static String templatize(RuleCompileType compileType, /* Event event, */String className, Rule rule, Rule sub,
			String seq) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		ObjectDataCompiler objectDataCompiler = new ObjectDataCompiler();
		InputStream ruleFile = null;

		data.put("rules", rule);
		data.put("eventType", className);
		data.put("ruleSeq", seq);
		data.put("ruleId", rule.getRuleId());
		String groupId = seq.substring(0, seq.indexOf("_"));
		data.put("groupId", groupId);
		if (compileType == RuleCompileType.TRANSPOND_ONLY) { // no subcondition only transpond
			// ruleFile = getRuleFile("rule-fireall-template.drl");
			ruleFile = getRuleFile("rule-fireall-json-nosubcondition-template.drl");
		}
		if (compileType == RuleCompileType.TRANSPOND_FILTER) {// filter
			// ruleFile = getRuleFile("rule-fireall-po-template.drl");
			ruleFile = getRuleFile("rule-fireall-json-template.drl");
			// ruleFile = getRuleFile("rule-fireall-test-template.drl");
			// subEventType,subrules,item
			data.put("item", sub.getEventItem());
			// after set db
			data.put("subrules", sub);
		}
		// deprecated it ,every rule dynamically build.now kjar
		// ruleFile = getRuleFile("rule-template.drl");
		return objectDataCompiler.compile(Arrays.asList(data), ruleFile);
	}

	/**
	 * 
	 * @param drl
	 * @param event
	 * @return AlertDecision
	 * @throws Exception
	 */
	private static Action execute(String drl, Event event, int ruleId) throws Exception {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		long startTime = System.currentTimeMillis();
		kfs.write("src/main/resources/" + "rule_" + drl.hashCode() + ".drl", drl);
		KieBuilder kb = ks.newKieBuilder(kfs);
		kb.buildAll();
		if (kb.getResults().hasMessages(Message.Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
		}
		long endTime = System.currentTimeMillis();
		System.out.println("RulesEngine ###### time to build rules : " + (endTime - startTime) + " ms");

		KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
		startTime = System.currentTimeMillis();
		StatelessKieSession statelessKieSession = kContainer.getKieBase().newStatelessKieSession();
		Action action = new Action();
		action.setRuleId(ruleId);
		statelessKieSession.getGlobals().set("action", action);
		statelessKieSession.execute(event);
		endTime = System.currentTimeMillis();
		System.out.println("RulesEngine ###### time to execute rule: " + (endTime - startTime) + " ms");
		return action;
	}

	/**
	 * @param filename:the
	 *            drl template file name
	 * @return InputStream
	 * 
	 */
	private static InputStream getRuleFile(String filename) throws IOException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "*.*");
		for (Resource file : resources) {
			if (file.getFilename().equals(filename)) {
				return file.getInputStream();
			}
		}
		return null;
	}

	/**
	 * @des execute rule dynamically,slow deprecated
	 * @param event
	 * @param topicId
	 * @return actions
	 * @throws Exception
	 */
	public static List<Action> fireAllRules(Event event, long topicId) throws Exception {
		List<Action> actions = new ArrayList<>();
		// get rule for topic and execute it
		CopyOnWriteArrayList<PrecompiledRule> rules = IotRulesMapper.getRulesByTopicId(topicId);
		for (PrecompiledRule rule : rules) {
			System.out.println(
					"Fire rule begin,id:" + rule.getRuleId() + ", placehodlers:" + rule.getAttributesPlaceholders());
			// set attributest placeholders for every rule
			event.setAttributesPlaceholders(rule.getAttributesPlaceholders());
			actions.add(KieUtils.execute(rule.getContent(), event, rule.getRuleId()));

			System.out.println("Fire rule end*************************,id:" + rule.getRuleId() + ", placehodlers:"
					+ rule.getAttributesPlaceholders());
		}
		return actions;

	}
	
	/**
	 * 
	 * @des get all rules content
	 * @return
	 * @throws IOException
	 */
	private static List<PrecompiledRule> getRules() {
		List<PrecompiledRule> ls = new ArrayList<>();
		
		for (CopyOnWriteArrayList<PrecompiledRule> rules : IotRulesMapper.getRules().values()) {
			for (PrecompiledRule rule : rules) {
				ls.add(rule);
			}

		}
		for (CopyOnWriteArrayList<PrecompiledRule> rules : IotRulesMapper.getProRules().values()) {
			for (PrecompiledRule rule : rules) {
				ls.add(rule);
			}

		}

		// add default rule for agenda-group activating
		try {
			InputStream stream = getRuleFile("rulegroup-activator.drl");
			String defaultRule = FileUtil.inputStream2String(stream);
			PrecompiledRule rule = new PrecompiledRule( 0, defaultRule, "*",55555L,"ALL");
			ls.add(rule);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Load default rule file error.");
			e.printStackTrace();
		}
		return ls;
	}
	
	/**
	 * @des 
	 * @param ks
	 * @param releaseId
	 * @param isStateful :true stateful, false stateless session
	 * @return
	 * @throws Exception
	 */
	public static InternalKieModule initKieJar(KieServices ks, ReleaseId releaseId,boolean isStateful) throws Exception {
		KieFileSystem kfs = createKieFileSystemWithPackage(ks, isStateful);
		kfs.writePomXML(getPom(releaseId));
		// get rules from IotRulesMapper
		for (PrecompiledRule rule : getRules()) {
			String ruleStr = addPkgName(rule.getContent(),rule.getPkgSuffix());
			kfs.write("src/main/resources/pkg/" + rule.getFormatId() + "/" + "rule_" + rule.getRuleId()+ ".drl", ruleStr);
		}
		
		long startTime = System.currentTimeMillis();
		KieBuilder kieBuilder = ks.newKieBuilder(kfs);
		if (!kieBuilder.buildAll().getResults().getMessages().isEmpty()) {
			System.out.println(kieBuilder.getResults().getMessages());
			throw new IllegalStateException("Error creating KieBuilder.");
		}
		long endTime = System.currentTimeMillis();
		System.out.println("RulesEngine ###### time to build all rules : " + (endTime - startTime) + " ms");
		return (InternalKieModule) kieBuilder.getKieModule();
	}

	public static InternalKieModule createKieJar(KieServices ks, ReleaseId releaseId, ResourceWrapper resourceWrapper) {
		KieFileSystem kfs = createKieFileSystemWithKProject(ks, false);
		kfs.writePomXML(getPom(releaseId));
		kfs.write("src/main/resources/" + resourceWrapper.getTargetResourceName(), resourceWrapper.getResource());
		KieBuilder kieBuilder = ks.newKieBuilder(kfs);
		if (!kieBuilder.getResults().getMessages().isEmpty()) {
			System.out.println(kieBuilder.getResults().getMessages());
			throw new IllegalStateException("Error creating KieBuilder.");
		}
		return (InternalKieModule) kieBuilder.getKieModule();
	}

	/**
	 * @des create kie file system
	 * @param ks
	 * @param true:sateful
	 *            ,false sateless session
	 * @return
	 */
	public static KieFileSystem createKieFileSystemWithKProject(KieServices ks, boolean isStateful) {
		KieModuleModel kproj = ks.newKieModuleModel();
		KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase").setDefault(true)
				.setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
				.setEventProcessingMode(EventProcessingOption.STREAM);
		// Configure the KieSession.
		kieBaseModel1.newKieSessionModel("KSession").setDefault(true)
				.setType(
						isStateful ? KieSessionModel.KieSessionType.STATEFUL : KieSessionModel.KieSessionType.STATELESS)
				.setClockType(ClockTypeOption.get("realtime"));
		
		KieFileSystem kfs = ks.newKieFileSystem();
		System.out.println("kie project xml:" + kproj.toXML());
		kfs.writeKModuleXML(kproj.toXML());
		return kfs;
	}

	/**
	 * @des generate a pom file
	 * @param releaseId
	 * @param dependencies
	 * @return
	 */
	public static String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
		String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n" + "\n" + "  <groupId>" + releaseId.getGroupId()
				+ "</groupId>\n" + "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" + "  <version>"
				+ releaseId.getVersion() + "</version>\n" + "\n";
		if (dependencies != null && dependencies.length > 0) {
			pom += "<dependencies>\n";
			for (ReleaseId dep : dependencies) {
				pom += "<dependency>\n";
				pom += "  <groupId>" + dep.getGroupId() + "</groupId>\n";
				pom += "  <artifactId>" + dep.getArtifactId() + "</artifactId>\n";
				pom += "  <version>" + dep.getVersion() + "</version>\n";
				pom += "</dependency>\n";
			}
			pom += "</dependencies>\n";
		}
		pom += "</project>";
		return pom;
	}

	/**
	 * @des fire all rules one time depend on precompiled kjar,depend on
	 *      dataformat's po
	 * @param event
	 * @param topicId
	 * @param productKey
	 * @return actions
	 * @throws Exception
	 */
	public static Action fireAllRulesOneTime(Event event, long topicId, String productKey, StatelessKieSession session)
			throws Exception {

		// get rule for topic and config it
		List<Integer> ruleIdList = new ArrayList<>();
		CopyOnWriteArrayList<PrecompiledRule> rules = null;
		rules = IotRulesMapper.getRulesByTopicId(topicId);
		if (rules == null || rules.isEmpty()) { // get rules from product
			System.out.println("RulesEngine topic id:{" + topicId + "},product key:{" + productKey
					+ "} ,execute rules by product.");
			rules = IotRulesMapper.getRulesByProductKey(productKey);
		} else
			System.out.println("RulesEngine topic id:{" + topicId + "},product key:{" + productKey
					+ "} ,execute rules by device.");

		for (PrecompiledRule rule : rules) {
			System.out.println("RulesEngine set rule placeholder,id:" + rule.getRuleId() + ", placehodlers:"
					+ rule.getAttributesPlaceholders());
			// set attributest placeholders for every rule
			event.addRulesPlaceholder(rule.getRuleId(), rule.getAttributesPlaceholders());
			ruleIdList.add(rule.getRuleId());
		}

		// event.setStartTime(getTimestampOfToday(9,0,0));
		// event.setEndTime(getTimestampOfToday(22,0,0));
		event.setTopicId(topicId);
		Topic topic = new Topic(topicId, "", "");
		topic.setRuleIdList(ruleIdList);
		// execute all rules
		return KieUtils.executeAllRules(topic, event, session);

	}

	/**
	 * 
	 * @param event
	 * @return AlertDecision
	 * @throws Exception
	 */
	private static Action executeAllRules(Topic topic, Event event, StatelessKieSession session) throws Exception {
		synchronized (KieUtils.class) {
			Action action = new Action();
			// session.getGlobals().set("action", action);
			/*
			 * declare local varible FactType funcData =
			 * IotKieContex.getIotKieContex().getIotKieBase().getFactType(
			 * funcData.set(oFuncData, "endTimestamp", 200);
			 */

			List<Command> cmds = new ArrayList<>();
			cmds.add(CommandFactory.newInsert(topic, "topic"));
			cmds.add(CommandFactory.newInsert(event, "event"));
			cmds.add(CommandFactory.newSetGlobal("action", action));
			ExecutionResults results = session.execute(CommandFactory.newBatchExecution(cmds));
			return (Action) results.getValue("action");
			// session.execute(event);
			// return action;

		}

	}

	/**
	 * @des execute all rules one time ,depend on dataformat(json)
	 * @param topicId
	 * @param productKey
	 * @param json
	 * @param session
	 * @return
	 * @throws Exception
	 */
	public static void fireRules(long topicId, String productKey, JSONObject json, StatelessKieSession session)
			throws Exception {

		// get rule for topic and config it
		String groupId = "";
		List<Integer> ruleIdList = new ArrayList<>();
		RuleProxy proxy = new RuleProxy();
		CopyOnWriteArrayList<PrecompiledRule> rules = null;
		rules = IotRulesMapper.getRulesByTopicId(topicId);
		if (rules == null || rules.isEmpty()) { // get rules from product
			rules = IotRulesMapper.getRulesByProductKey(productKey);
			if (rules == null || rules.isEmpty()) {
				System.out.println("RulesEngine topic id:{" + topicId + "},product key:{" + productKey
						+ "} ,topic,product no set rules.");
				return;
			}
			long productRuleId = IotRulesMapper.getProducts().get(productKey).getId();
			groupId = String.valueOf(productRuleId);
			//add report
			IotRulesMapper.addReport(productKey);
		}
		else {
			groupId = String.valueOf(topicId);
			//add report
			IotRulesMapper.addReport(String.valueOf(topicId));
		}
		for (PrecompiledRule rule : rules) {
//			System.out.println(
//					"RulesEngine topic id:{" + topicId + "},product key:{" + productKey + "},set rule placeholder,ruleId:"
//							+ rule.getRuleId() + ", placehodlers:" + rule.getAttributesPlaceholders());
			// set attributest placeholders for every rule
			proxy.addRulesPlaceholder(rule.getRuleId(), rule.getAttributesPlaceholders());
			ruleIdList.add(rule.getRuleId());
		}
		//for different group have same groupId,because productId,topicId possible same.
		Topic topic = new Topic(topicId, "", "");
		topic.setRuleIdList(ruleIdList);
		RuleGroupActivator activator = new RuleGroupActivator(groupId);
		executeNoSync(activator,topic, proxy, json, session);
	}


	/**
	 * @des no synchronized execute
	 * @param topic
	 * @param proxy
	 * @param json
	 * @param session
	 * @return
	 * @throws Exception
	 */
	private static void executeNoSync(RuleGroupActivator activator,Topic topic, RuleProxy proxy, JSONObject json, StatelessKieSession session)
			throws Exception {
		// Action action = new Action();
		List<Command> cmds = new ArrayList<>();
		cmds.add(CommandFactory.newInsert(activator, "activator"));
		cmds.add(CommandFactory.newInsert(topic, "topic"));
		cmds.add(CommandFactory.newInsert(proxy, "proxy"));
		cmds.add(CommandFactory.newInsert(json, "json"));
		session.addEventListener(new TrackingAgendaEventListener());
		ExecutionResults results = session.execute(CommandFactory.newBatchExecution(cmds));
	}
	
	/**
	 * @des create kie file system
	 * @param ks
	 * @param true:sateful
	 *            ,false sateless session
	 * @return
	 */
	public static KieFileSystem createKieFileSystemWithPackage(KieServices ks, boolean isStateful) {
		KieModuleModel kproj = ks.newKieModuleModel();
		Map<Long,CopyOnWriteArrayList<String>> formats = IotRulesMapper.getDataFormats();
		for (Long formatId :formats.keySet() ) {
			String kBaseName = "kbase" + formatId;
			KieBaseModel kBaseModel = kproj.newKieBaseModel(kBaseName)
					.setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
					.setEventProcessingMode(EventProcessingOption.STREAM);
			String pkgName = "pkg." + formatId + ".*";
			//add default pkg
			pkgName += ",pkg.55555.*";
			kBaseModel.addPackage(pkgName);
			String kSessionName = "jobsession" + formatId;
			// Configure the KieSession.
			kBaseModel.newKieSessionModel(kSessionName).setDefault(false)
					.setType(
							isStateful ? KieSessionModel.KieSessionType.STATEFUL : KieSessionModel.KieSessionType.STATELESS)
					.setClockType(ClockTypeOption.get("realtime"));
		}
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.writeKModuleXML(kproj.toXML());
		return kfs;
	}
	
	private static String addPkgName(String str,String pkgSuffix) {
		String pkgName = "package " + SystemConstant.RULE_PKG_PREFIX + pkgSuffix + ";\n";
		String drlStr = pkgName + str;
		return drlStr;
	}
	/**
	 * @des get topicId or productKey 's rules set
	 * @param topicId
	 * @param productKey
	 * @return
	 */
	public static Map<String,Object> getMemRulesSet(String topicId,String productKey) {
		String title;
		String tmpValue;
		
		CopyOnWriteArrayList<PrecompiledRule> rules = null;
		Map<String,Object> result = new HashMap<>();
		if (topicId != null) {
			rules = IotRulesMapper.getRulesByTopicId(Long.valueOf(topicId));
			if (rules != null) {
				title = "topicId";
				tmpValue = topicId;
				result.put(title, tmpValue);
			}
		}
		if (productKey != null && rules == null) {
			rules = IotRulesMapper.getRulesByProductKey(productKey);
			title = "productKey";
			tmpValue = productKey;
			result.put(title, tmpValue);
		}
		if (rules == null) 
			return null;
		List<Map<String,Object>> ls = new ArrayList<>();
		for (PrecompiledRule rule :rules) {
			Map<String,Object> oneRule = new HashMap<>();
			oneRule.put("ruleId", rule.getRuleId());
			oneRule.put("dataFormatId", rule.getFormatId());
			oneRule.put("pkgSuffix", rule.getPkgSuffix());
			ls.add(oneRule);
		}
		result.put("rules", ls);
		return result;
	}
	/**
	 * @des if rule dependcy product,return true
	 * @param str
	 * @return
	 */
	public static boolean isDependcyProduct(String str) {
		return IotRulesMapper.getProducts().containsKey(str);
	}
	/**
	 * @des if rule dependcy topic,return true
	 * @param str
	 * @return
	 */
	public static boolean isDependcyTopicId(String str) {
		if (!str.matches("-[0-9]+(.[0-9]+)?|[0-9]+(.[0-9]+)?"))
			return false;
		return IotRulesMapper.getTopics().containsKey(Long.valueOf(str));
	}
	
	/**
	 * @des get format id
	 * @param topicId
	 * @param productKey
	 * @return
	 */
	public static Long getDataFormatId(String topicId,String productKey) {
		Long formatId = null;
		Map<Long,CopyOnWriteArrayList<String>> formats = IotRulesMapper.getDataFormats();
		for (Long tmpKey : formats.keySet()) {
			if (formats.get(tmpKey).contains(topicId) || formats.get(tmpKey).contains(productKey)) {
				formatId = tmpKey;
				break;
			}
		}
		return formatId;
	}
	
	/**
	 * @des load knowledge as package
	 * @param content
	 * @return
	 */
	public Collection<KiePackage> loadKnowledgePackagesFromString(String... content) {
		return loadKnowledgePackagesFromString(null, content);
	}
	/**
	 * @des load knowledge as package with config
	 * @param kbuilderConf
	 * @param content
	 * @return
	 */
	public Collection<KiePackage> loadKnowledgePackagesFromString(KnowledgeBuilderConfiguration kbuilderConf,
			String... content) {
		if (kbuilderConf == null) {
			kbuilderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		}
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbuilderConf);
		for (String r : content) {
			kbuilder.add(ResourceFactory.newByteArrayResource(r.getBytes()), ResourceType.DRL);
		}
		if (kbuilder.hasErrors()) {
			fail(kbuilder.getErrors().toString());
		}
		Collection<KiePackage> knowledgePackages = kbuilder.getKnowledgePackages();
		return knowledgePackages;
	}
}
