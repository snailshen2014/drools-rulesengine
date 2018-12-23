/**
 * 
 */
package com.syj.iot.rulesengine.typedefine;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年7月16日 下午4:16:25
 */
public enum RuleCompileType {
	TRANSPOND_ONLY(1, "Only transpond data to cloudplatform."),
	TRANSPOND_FILTER(2, "Transpond data and filter data.");
	private int type;
	private String msg;

	private RuleCompileType(int type, String msg) {
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
