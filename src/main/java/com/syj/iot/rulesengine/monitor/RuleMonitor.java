package com.syj.iot.rulesengine.monitor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @des :monitor the rule executing when no match rule and alarm by mail or sms
 * @author:shenyanjun1
 * @date :2018-11-02 15:27
 */
public class RuleMonitor {
	protected static final Logger logger = LoggerFactory.getLogger(RuleMonitor.class);

	private RuleMonitor() {
	}

	public static RuleMonitor getRuleMonitor() {
		return Inner.RULE_MONITOR;
	}

	private static class Inner {
		private static final RuleMonitor RULE_MONITOR = new RuleMonitor();

	}

	// define a cache
	private static final Integer CACHESIZE = 10000;
	private static BlockingQueue<PointMonitor> cache = new LinkedBlockingQueue<>(CACHESIZE);

	// new thread for monitor cache
	static {
		new Thread(() -> {
			while (true) {
				try {
					PointMonitor point = cache.take();
					logger.info("monitor thread take a data key:{},and dispatch threshold manager",point);
					ThresholdManager.entry(point);
				} catch (InterruptedException e) {
					logger.error("monitor thread take data error:{}", e.getMessage());
					e.printStackTrace();
				}
			}
		}, "monitor").start();

	}

	public  boolean noMatchEntry(PointMonitor point) {
		try {
			cache.add(point);
			logger.info("add data key:{} to cache for rule monitor checking.",point);
		} catch (Exception e) {
			logger.error("noMatchEntry key:{} error:{}", point, e.getMessage());
			return false;
		}
		return true;
	}

}
