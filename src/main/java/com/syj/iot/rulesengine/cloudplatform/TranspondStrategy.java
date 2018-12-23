/**
 * 
 */
package com.syj.iot.rulesengine.cloudplatform;

import java.util.Map;

/**
 * @des:data tranpond strategy
 * @author shenyanjun1
 * @date: 2018年5月29日 上午9:57:30
 */
public abstract class TranspondStrategy {
	public  abstract void transpond(String data) ;
	public  abstract void initConfig(Map<String,String> config);
}
