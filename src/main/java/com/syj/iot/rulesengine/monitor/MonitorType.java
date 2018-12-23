package com.syj.iot.rulesengine.monitor;

/**
 * 
 * @des :monitor type define
 * @author:shenyanjun1
 * @date :2018-11-02 18:25
 */
public enum MonitorType {
	NO_SET_RULE(1, "no match rule."), 
	NO_FORMAT_ID(2, "no macth dataformat id."),
	EXECUTE_EXCEPTION(3, "exccute rule exception.");

	private int type;
	private String msg;

	private MonitorType(int type, String msg) {
		this.type = type;
		this.msg = msg;
	}

	public int getType() {
		return type;
	}

	public String getMsg() {
		return msg;
	}
}
