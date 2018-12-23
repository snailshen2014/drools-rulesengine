/**
 * 
 */
package com.syj.iot.rulesengine.typedefine;

/**
 * @des:define kie message interface operation type
 * @author shenyanjun1
 * @date: 2018年7月14日 上午11:42:06
 */
public enum DataEntryPoint {
	CACHE_POINT(1, "data from cache entrypoint."),
	KAFKA_POINT(2,"data from kafak entrypoint."),
	HTTP_POINT(3,"http call entrypoint."),
	GRPC_POINT(4,"grpc call entrypoint.");
	
	private int type;
	private String msg;

	private DataEntryPoint(int type, String msg) {
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
