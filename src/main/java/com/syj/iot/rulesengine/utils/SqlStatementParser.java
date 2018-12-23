
package com.syj.iot.rulesengine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.iot.rulesengine.rule.Condition;
import com.syj.iot.rulesengine.rule.RuleFunction;

/**
 * @des:for parser "SELECT attributes FROM topic WHERE xxx" sql statement.
 * @author shenyanjun1
 * @date: 2018年5月23日 下午1:37:29
 */
public class SqlStatementParser {
	private static Logger logger = LoggerFactory.getLogger(SqlStatementParser.class);
	private static final String logicOper = ">=|<=|!=|>|<|=";
	private static final String conditionOper = "and";// now only support "and"

	/**
	 * 
	 * @param selectedAttr(selected
	 *            attributes)
	 * @return key:aliase attribute,value:original attribute
	 */
	public static Map<String, String> getAttributes(String selectedAttr) {
		// '*' or attri1 alias1,attri2 alias2
		// no support function now
		String selAttr = selectedAttr.trim();
		if (selAttr.equals("*"))
			return null;

		Map<String, String> results = new HashMap<>();
		String[] attrs = selectedAttr.split(",");
		for (String attr : attrs) {
			if (attr.trim().indexOf(" ") == -1) {
				// no attri1 alias1 format
				logger.error("Config error,selected attributes no 'attr1 alias1' format.");
				return null;
			} else {
				if (attr.trim().split(" ").length != 2) {
					logger.error("Config error,selected attributes no 'attr1 alias1' format,no set alias.");
					return null;
				}
				String oriAttr = attr.trim().split(" ")[0];
				String alias = attr.trim().split(" ")[1];
				results.put(alias.trim(), oriAttr.trim());
			}

		}
		return results;
	}

	public static List<Condition> getConditions(String con) {
		List<Condition> cons = new ArrayList<>();
		// logic operator:>,=,<,!=,>=,<= , named LO
		// condition operator: and ,named CO
		// supprot (attr1 LO value) CO (attr2 LO value2) CO ... fromat
		String con1 = con.trim();
		// one condition
		if (con1.indexOf("and") == -1) {
			Condition conObj = getConditionByExpression(con1);
			if (conObj == null)
				return null;
			cons.add(conObj);
		} else { // multiple condition
			String[] conArry = con1.split(conditionOper);
			for (String one : conArry) {
				// filter white space condition
				if (one.trim().length() != 0) {
					Condition conObj = getConditionByExpression(one);
					if (conObj == null)
						continue;
					cons.add(conObj);

				}
			} // end for

		}
		return cons;
	}

	/**
	 * 
	 * @param oper
	 * @return enum operator
	 */
	private static Condition.Operator toEumLogicOperator(String oper) {
		if (oper.equals("="))
			return Condition.Operator.EQUAL_TO;
		if (oper.equals("!="))
			return Condition.Operator.NOT_EQUAL_TO;
		if (oper.equals(">"))
			return Condition.Operator.GREATER_THAN;
		if (oper.equals("<"))
			return Condition.Operator.LESS_THAN;
		if (oper.equals("<="))
			return Condition.Operator.LESS_THAN_OR_EQUAL_TO;
		if (oper.equals(">="))
			return Condition.Operator.GREATER_THAN_OR_EQUAL_TO;
		return null;
	}

	/**
	 * 
	 * @param attributes
	 *            like "a >= b" "a > b" etc
	 * @return >=,>
	 */
	private static String getLogicOperator(String con) {
		Pattern pattern = Pattern.compile(logicOper);
		Matcher matcher = pattern.matcher(con);
		boolean rs = matcher.find();
		if (rs)
			return matcher.group();
		else {
			logger.error("Condition set error,condition={} ,no 'attr1 {}' value format,match error.", con, logicOper);
			return null;
		}

	}

	/**
	 * 
	 * @param exp
	 * @return one expression
	 */
	private static Condition getConditionByExpression(String exp) {
		//filter function condition
//		if(RuleFunction.isContainsFunction(exp))
//			return null;
		String[] conArry = exp.split(logicOper);
		if (conArry.length != 2) {
			logger.error("Condition:{}, set error ,logic operator illegality, legal operator:{}.", exp, logicOper);
			return null;
		}
		String attr = conArry[0].trim();
		String value = conArry[1].trim();

		Condition conObj = new Condition();
		int pos = attr.indexOf(".");
		if (pos != -1) { // contains second condition
			conObj.setSubEvent(true);
			conObj.setEventItem(attr.substring(0, pos));
			conObj.setField(attr.substring(pos + 1, attr.length()));
		} else {
			conObj.setSubEvent(false);
			conObj.setField(attr);
		}
		conObj.setOperator(toEumLogicOperator(getLogicOperator(exp)));
		if (isNumeric(value)) {
			Integer iValue = Integer.parseInt(value);
			conObj.setValue(iValue);
		} else
			conObj.setValue(value);
		return conObj;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-[0-9]+(.[0-9]+)?|[0-9]+(.[0-9]+)?");

	}
	
	public static List<String> functionInterceptor (String con) {
//		function feature,now hard code
		//data.rssi > -64 and data.time >= getTimestampOfToday(9,0,0) and data.time <= getTimestampOfToday(22,0,0)
		List<String> funcs = new ArrayList<>();
		String src  = con;
		while(RuleFunction.isContainsFunction(src)) {
			String funcName = RuleFunction.getFunctionName(src);
			int pos = src.indexOf(funcName);
			int pos2 = src.indexOf(")");
			String exp = src.substring(pos,pos2+1);
			funcs.add(exp);
			src = src.substring(pos2+1,src.length());
			
		}
		return funcs;
	}
}
