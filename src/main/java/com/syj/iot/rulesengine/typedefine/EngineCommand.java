/**
 * 
 */
package com.syj.iot.rulesengine.typedefine;

/**
 * @des:define kie message interface operation type
 * @author shenyanjun1
 * @date: 2018年7月14日 上午11:42:06
 */
public enum EngineCommand {
	PRODUCT_ONLINE(1, "product online"),
	PRODUCT_OFFLINE(2,"product off-line"),
	PRODUCT_MODIFY_DATEFORMAT(3,"modify data format ."),
	DEVICE_ONLINE(4,"device online"),
	DEVICE_OFFLINE(5,"device offline"),
	RULE_ADD(6,"add rule"),
	RULE_DELETE(7,"delete a rule"),
	RULE_UPDATE(8,"update rue");
	
	private int type;
	private String msg;

	private EngineCommand(int type, String msg) {
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
