/**
 * 
 */
package com.syj.iot.rulesengine.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @des:Get bean by beanName
 * @author shenyanjun1
 * @date: 2018年6月7日 上午10:13:35
 */
public class SpringContextHolder implements ApplicationContextAware {

	private static ApplicationContext context;

	public void setApplicationContext(ApplicationContext context) {
		SpringContextHolder.context = context;
	}

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	public static <T> T getBean(String name) {
		return (T) context.getBean(name);
	}
}
