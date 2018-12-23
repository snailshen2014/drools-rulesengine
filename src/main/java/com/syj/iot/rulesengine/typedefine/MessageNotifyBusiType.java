/**
 * 
 */
package com.syj.iot.rulesengine.typedefine;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年7月14日 下午12:08:12
 */
public enum MessageNotifyBusiType {
	MANAGER_MESSAGE(1000, "When product online or off-line ,manager system notify rulesEngine."),
	RULESMANAGER_SYSTEM_MESSAGE(1001, "System administrator manager rule ,create ,delete ,update message.");
	private int type;
	private String msg;

	private MessageNotifyBusiType(int type, String msg) {
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
