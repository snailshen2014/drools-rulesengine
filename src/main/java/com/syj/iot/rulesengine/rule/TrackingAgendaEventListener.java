/**
 * 
 */
package com.syj.iot.rulesengine.rule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.event.AfterActivationFiredEvent;
import org.drools.core.spi.Activation;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.kie.api.definition.rule.Rule;

/**
 * @des:event listener
 * @author shenyanjun1
 * @date: 2018年6月14日 下午5:24:09
 */
public class TrackingAgendaEventListener extends DefaultAgendaEventListener {
	private static Logger log = LoggerFactory.getLogger(TrackingAgendaEventListener.class);

	@Override
	public void afterMatchFired(AfterMatchFiredEvent event) {
//		System.out.println(event.getMatch().getObjects());
//		System.out.println(event);
		Rule rule = event.getMatch().getRule();
		String ruleName = rule.getName();
		Map<String, Object> ruleMetaDataMap = rule.getMetaData();
        StringBuilder sb = new StringBuilder("Rule fired: " + ruleName + ",rule id:" + rule.getId()) ;
        if (ruleMetaDataMap.size() > 0) {
            sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
            for (String key : ruleMetaDataMap.keySet()) {
                sb.append("\n    key=" + key + ", value="
                        + ruleMetaDataMap.get(key));
            }
        }
        log.info(sb.toString());	
	}
}
