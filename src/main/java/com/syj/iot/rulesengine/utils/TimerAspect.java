/**
 * 
 */
package com.syj.iot.rulesengine.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @des:logging method running time cost
 * @author shenyanjun1
 * @date: 2018年7月17日 下午3:37:18
 */
@Component
@Aspect
public class TimerAspect {
	private static final Logger logger = LoggerFactory.getLogger(TimerAspect.class);
	@Around("execution(* com.syj.iot.rulesengine.init.IotRulesConfigurationService.*(..))")
	public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint){
		logger.info("RulesEngine ###### begin call method:{}...", proceedingJoinPoint.getSignature());
		long startTime = System.currentTimeMillis();
		Object value = null;
		try {
			value = proceedingJoinPoint.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		logger.info("RulesEngine ###### end call method:{},timecost:{} ms.", proceedingJoinPoint.getSignature(),(endTime - startTime));
		return value;
	}
}
