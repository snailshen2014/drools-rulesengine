/**
 * 
 */
package com.syj.iot.rulesengine.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @des:This class is designed for supply function of rules 
 * @author shenyanjun1
 * @date: 2018年6月5日 上午9:10:49
 */
public class RuleFunction {
	private static final List<String> functions = new ArrayList<>();
	private static  String funcs  = null;
	static {
		functions.add("getTimestampOfToday");
		functions.add("abs");
		for (String fun : functions) {
			funcs += fun;
			funcs += "|";
		}
	}
	
	public static boolean isContainsFunction(String str) {
		for (String fun : functions) {
			if (str.contains(fun))
				return true;
		}
		return false;
	}
	

	
	public static String getFunctionName(String str) {
		Pattern pattern = Pattern.compile(funcs);
		Matcher matcher = pattern.matcher(str);
		boolean rs = matcher.find();
		if (rs)
			return matcher.group();
		else {
			return null;
		}
	}
	
	public static String getFunctionField(String str) {
		int pos1 = str.indexOf("(");
		int pos2 = str.indexOf(",");
		if (pos1 == -1 || pos2 == -1)
			return null;
		return str.substring(pos1+1, pos2);
		
	}
	public static List<Object> getFunctionParams(String str) {
		int pos1 = str.indexOf(",");//default before , char is function field
		int pos2 = str.indexOf(")");
		if (pos1 == -1 || pos2 == -1)
			return null;
		Object[] array = str.substring(pos1 + 1,pos2).split(",");
		return Arrays.asList(array);
		
	}
	/**
	 * @des get timestamp of this time
	 * @param hour
	 * @param min
	 * @param sec
	 * @return
	 */
	public  long getTimestampOfToday(Integer hour,Integer min,Integer sec) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.SECOND, sec);
		return c.getTime().getTime() / 1000;
		
	}

	public  long abs(Integer value) {
		if (value < 0)
			return -value;
		return value;
		
	}
}
