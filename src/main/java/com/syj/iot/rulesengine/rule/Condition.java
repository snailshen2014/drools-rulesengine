package com.syj.iot.rulesengine.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @des: RulesEngine SQL condition
 * @author shenyanjun1
 * @date: 2018年7月13日 上午9:51:04
 */
public class Condition {

    private String field;
    private Object value;
    
    private Condition.Operator operator;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Condition.Operator getOperator() {
        return operator;
    }

    public void setOperator(Condition.Operator operator) {
        this.operator = operator;
    }

    //modified by shenyj for sub event
    private boolean subEvent;
    
	/**
	 * @return the subEvent
	 */
	public boolean isSubEvent() {
		return subEvent;
	}

	/**
	 * @param subEvent the subEvent to set
	 */
	public void setSubEvent(boolean subEvent) {
		this.subEvent = subEvent;
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

	//function format(funcname(field,param1,parma2)
	private String funcName;
	/**
	 * @return the funcName
	 */
	public String getFuncName() {
		return funcName;
	}

	/**
	 * @param funcName the funcName to set
	 */
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	/**
	 * @return the funcField
	 */
	public String getFuncField() {
		return funcField;
	}

	/**
	 * @param funcField the funcField to set
	 */
	public void setFuncField(String funcField) {
		this.funcField = funcField;
	}

	/**
	 * @return the funcParams
	 */
	public List<Object> getFuncParams() {
		return funcParams;
	}

	/**
	 * @param funcParams the funcParams to set
	 */
	public void setFuncParams(List<Object> funcParams) {
		this.funcParams = funcParams;
	}

	private String funcField;
	private List<Object> funcParams;

	public static enum Operator {
        NOT_EQUAL_TO("NOT_EQUAL_TO"),
        EQUAL_TO("EQUAL_TO"),
        GREATER_THAN("GREATER_THAN"),
        LESS_THAN("LESS_THAN"),
        GREATER_THAN_OR_EQUAL_TO("GREATER_THAN_OR_EQUAL_TO"),
        LESS_THAN_OR_EQUAL_TO("LESS_THAN_OR_EQUAL_TO");
        private final String value;
        private static Map<String, Operator> constants = new HashMap<String, Operator>();

        static {
            for (Condition.Operator c : values()) {
                constants.put(c.value, c);
            }
        }

        private Operator(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Condition.Operator fromValue(String value) {
            Condition.Operator constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }

}
