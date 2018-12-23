/**
 * 
 */
package com.syj.iot.rulesengine.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.syj.iot.rulesengine.error.EngineConfigException;
import com.syj.iot.rulesengine.error.EngineRuntimeException;
import com.syj.iot.rulesengine.error.EsLogHandler;
import com.syj.iot.rulesengine.init.IotKieContex;
import com.syj.iot.rulesengine.monitor.MonitorType;
import com.syj.iot.rulesengine.monitor.PointMonitor;
import com.syj.iot.rulesengine.monitor.RuleMonitor;
import com.syj.iot.rulesengine.rule.IotRulesMapper;
import com.syj.iot.rulesengine.typedefine.DataEntryPoint;
import com.syj.iot.rulesengine.utils.KieUtils;
import com.syj.iot.rulesengine.utils.MessageUtil;

/**
 * @des:pull entrypoint data and send it
 * @author shenyanjun1
 * @date: 2018年8月2日 下午5:24:50
 */
public class EntryPointTranspond {
	protected static final Logger logger = LoggerFactory.getLogger(EntryPointTranspond.class);
	/**
	 * @des : transpond data from entrypoint to palatform
	 * @param topicId
	 * @param productKey
	 * @param repData
	 * @param point
	 * @return
	 */
	public static  void transpond(String topicId,String productKey,String repData,DataEntryPoint point) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("RulesEngine transponded devices's reported data,msgId:{" +MessageUtil.getMsgId(repData) + "}, topic id:{" + topicId);
		sb.append("},porductKey:{" + productKey );
		sb.append("},entrypoint:{" + point + "},");
		long startTime = System.currentTimeMillis();
		boolean isRuleConfigured = IotRulesMapper.isRuleConfigured(Integer.parseInt(topicId), productKey);
		if (!isRuleConfigured) {
			//add monitor
			RuleMonitor.getRuleMonitor().noMatchEntry(new PointMonitor(productKey,MonitorType.NO_SET_RULE));
			sb.append("no set rule.");
			EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(repData), productKey, MessageUtil.getMsgId(repData),"","", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
			throw new EngineConfigException(sb.toString());
		
		}
		Long formatId = KieUtils.getDataFormatId(topicId, productKey);
		if (formatId == null) {
			//add monitor
			RuleMonitor.getRuleMonitor().noMatchEntry(new PointMonitor(productKey,MonitorType.NO_FORMAT_ID));
			sb.append("can not find dataformat.");
			EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(repData), productKey, MessageUtil.getMsgId(repData),"","", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
			throw new EngineConfigException(sb.toString());
		}
		try {
			KieUtils.fireRules(Integer.parseInt(topicId), productKey, JSON.parseObject(repData),
					IotKieContex.getIotKieContex().getJobSession(formatId));
			long endTime = System.currentTimeMillis();
			logger.info("RulesEngine transponded devices's reported data,msgId:{},topic id:{},porductKey:{},entrypoint:{} ,finished,time cost {}ms ",
					MessageUtil.getMsgId(repData),topicId, productKey,point,endTime - startTime);
			EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(repData), productKey, MessageUtil.getMsgId(repData),"","", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.SUCCESS);
		} catch (Exception e) {
			//add monitor
			RuleMonitor.getRuleMonitor().noMatchEntry(new PointMonitor(productKey,MonitorType.EXECUTE_EXCEPTION));
			e.printStackTrace();
			sb.append("execute rule exception.");
			EsLogHandler.toEsLogSystem(MessageUtil.getDeviceName(repData), productKey, MessageUtil.getMsgId(repData),"","", DataLogEnum.RE_EXECUTE, DataLogErrorCodeEnum.RE_EXECUTE_EXCEPTION);
			throw new EngineRuntimeException(sb.toString(),e);
		}
		
	}
}
