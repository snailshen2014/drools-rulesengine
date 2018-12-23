package com.syj.iot.rulesengine.monitor;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThresholdManager {
	protected static final Logger logger = LoggerFactory.getLogger(ThresholdManager.class);
	private static final String REDIS_NORULE_KEY_PREFIX = "no-rule:";
	private static final String REDIS_NODATEFORMAT_KEY_PREFIX = "no-datafromat:";
	private static final String REDIS_EXE_KEY_PREFIX = "exe-exception:";
	private static final int NORULE_THRESHOLD_VALUE=100;
	private static final int NODATEFORMATID_THRESHOLD_VALUE=50;
	private static final int EXECUTE_THRESHOLD_VALUE=50;
	
	private static int alarmNum = 0;
	/**
	 * @des external data entry interface,no synchronized because one thread call
	 * @param key
	 */
	public static void entry(PointMonitor point) {
		String redis = null;
		int thresholdValue ;
		switch (point.getType()) {
		case NO_SET_RULE:
			redis = REDIS_NORULE_KEY_PREFIX + point.getKey();
			thresholdValue = NORULE_THRESHOLD_VALUE;
			break;
		case EXECUTE_EXCEPTION:
			redis = REDIS_EXE_KEY_PREFIX + point.getKey();
			thresholdValue = EXECUTE_THRESHOLD_VALUE;
			break;
		case NO_FORMAT_ID:
			redis = REDIS_NODATEFORMAT_KEY_PREFIX + point.getKey();
			thresholdValue = NODATEFORMATID_THRESHOLD_VALUE;
			break;
		default :
			logger.error("ThresholdManager monitor  type error:{}.",point.getType());
			return;
		}
		Integer current = getCurrentValue(redis);
		logger.info("ThresholdManager get key:{},threshold value:{},current number:{}.",redis,thresholdValue,current);
		if ( current >= thresholdValue) {
			//alarm
			alarmByMail(point);
			alarmNum++;
		}
		//increase
		if (ThresholdManager.alarmNum >= 10 ) {
			setCurrentValue(redis,0);
			ThresholdManager.alarmNum = 0;
		}
		else { 
			setCurrentValue(redis,current + 1);
		}
	}
	
	private static Integer  getCurrentValue(String key) {
		Integer curNumber = JedisUtil.getInstance().getObj(key);
		if (curNumber != null)
			return curNumber;
		else 
			return 0;
	}
	
	private static void  setCurrentValue(String key,Object value) {
		 JedisUtil.getInstance().setObj(key,value,0);
	}
	
	private static void alarmByMail(PointMonitor point) {
		MailDTO mailDTO = new MailDTO();
		// 邮件主题
		String subject = "RulesEngine no match rule";
		// 收件人数组,支持邮件组
		String[] to = { "xxx.com" };
		/*
		 * 
		 * //邮件正文 String content = "设备a离线30分钟了";
		 */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("begin");
		stringBuilder.append("\n");
		stringBuilder.append("\t");
		stringBuilder.append("key:" + point.getKey() + ",message:" + point.getType().getMsg() + "  reach limit number,please check!!!");
		stringBuilder.append("\n");

		mailDTO.setTo(to);
		/*
		 * mailDTO.setCc(cc); mailDTO.setBcc(bcc);
		 */
		mailDTO.setSubject(subject);
		mailDTO.setContent(stringBuilder.toString());
		try {
			MailAlarm.sendMail(mailDTO);
			logger.info("ThresholdManager  key:{},send mail to :{}  ok.",point.getKey(),to);
		} catch (MessagingException e) {
			e.printStackTrace();
			logger.info("ThresholdManager  key:{},send mail to :{}  error,error message:{}.",point.getKey(),to,e.getMessage());
		}
	}
}
