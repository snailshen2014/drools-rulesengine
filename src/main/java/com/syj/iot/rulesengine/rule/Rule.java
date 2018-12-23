package com.syj.iot.rulesengine.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @des:The rule user defined  abstract
 * @author shenyanjun1
 * @date: 2018年7月13日 上午9:53:13
 */
public class Rule {
	//this id is a topic's rule id,because this class only store rule's condition,so by it can get rule other info.
	private int ruleId;
	private List<Condition> conditions;
	private Rule.eventType eventType;

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public Rule.eventType getEventType() {
		return eventType;
	}

	public void setEventType(Rule.eventType eventType) {
		this.eventType = eventType;
	}
	/**
	 * @return the ruleId
	 */
	public int getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId the ruleId to set
	 */
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}
	
	private String eventItem;
	
	/**
	 * @return the eventItem
	 */
	public String getEventItem() {
		return eventItem;
	}

	/**
	 * @param eventItem the eventItem to set
	 */
	public void setEventItem(String eventItem) {
		this.eventItem = eventItem;
	}

	@Override
	public String toString() {
		StringBuilder statementBuilder = new StringBuilder();
		boolean isHaveCon = false;
		if (getConditions() == null || getConditions().isEmpty())
			return "1 == 1";
		for (Condition condition : getConditions()) {
			isHaveCon = true;
			String operator = null;

			switch (condition.getOperator()) {
			case EQUAL_TO:
				operator = "==";
				break;
			case NOT_EQUAL_TO:
				operator = "!=";
				break;
			case GREATER_THAN:
				operator = ">";
				break;
			case LESS_THAN:
				operator = "<";
				break;
			case GREATER_THAN_OR_EQUAL_TO:
				operator = ">=";
				break;
			case LESS_THAN_OR_EQUAL_TO:
				operator = "<=";
				break;
			}

			statementBuilder.append(condition.getField()).append(" ").append(operator).append(" ");
			
			if (condition.getValue() instanceof String) {
				 String ss = (String)condition.getValue();
				 if (ss.contains("$proxy")) {
					 statementBuilder.append(condition.getValue());
				 } else
				statementBuilder.append("'").append(condition.getValue()).append("'");
			} else {
				statementBuilder.append(condition.getValue());
			}

			statementBuilder.append(" && ");
		}
		if (!isHaveCon)
			return "1 == 1";
		String statement = statementBuilder.toString();

		// remove trailing &&
		return statement.substring(0, statement.length() - 4);
	}

	public static enum eventType {

		TRANSPOND("TRANSPOND"), INVOICE("INVOICE");
		private final String value;
		private static Map<String, Rule.eventType> constants = new HashMap<String, Rule.eventType>();

		static {
			for (Rule.eventType c : values()) {
				constants.put(c.value, c);
			}
		}

		private eventType(String value) {
			this.value = value;
		}

		public static Rule.eventType fromValue(String value) {
			Rule.eventType constant = constants.get(value);
			if (constant == null) {
				throw new IllegalArgumentException(value);
			} else {
				return constant;
			}
		}

	}
}
