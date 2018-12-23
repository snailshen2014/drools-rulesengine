/**
 * 
 */
package com.syj.iot.rulesengine.event;
import java.io.File;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.syj.iot.rulesengine.rule.IotRulesMapper;
import com.syj.iot.rulesengine.typedefine.SystemConstant;

/**
 * @des:when container destory making some work
 * @author shenyanjun1
 * @date: 2018年7月24日 下午2:01:37
 */
@Component
public class ApplicationHookHandler implements ApplicationListener<ContextClosedEvent> {
	
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		// TODO Auto-generated method stub
		//write file /export/Logs/iot.rulesengine/
		if (IotRulesMapper.getReport().isEmpty())
			return;
		File report = new File(SystemConstant.REPORT_FILE);
		FileUtil.appendContentToFile(report, JSON.toJSONString(IotRulesMapper.getReport()));
		
	}
	
    
}

