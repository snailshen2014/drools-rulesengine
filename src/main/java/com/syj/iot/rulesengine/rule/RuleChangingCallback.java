/**
 * 
 */
package com.syj.iot.rulesengine.rule;

import com.syj.iot.rulesengine.init.IotKieContex;
import com.syj.iot.rulesengine.typedefine.CommandParameter;
import com.syj.iot.rulesengine.typedefine.EngineCommand;

/**
 * @des:For rule changing ,notify rules engine
 * @author shenyanjun1
 * @date: 2018年7月14日 下午2:48:53
 */
public class RuleChangingCallback {
	/**
	 * @ notify rules engine
	 * @param command
	 * @param parameter
	 */
	public boolean notifyEngine(EngineCommand command,CommandParameter parameter ) {
		return IotKieContex.getIotKieContex().notifyKieEnv(command, parameter);
	}
}
