/**
 * 
 */
package com.syj.iot.rulesengine.error;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年8月3日 上午9:29:17
 */
public class EngineRuntimeException extends Exception{
	private static final long serialVersionUID = 8575471379720786805L;
	String msg;
	public EngineRuntimeException(String message) {
		super(message);
		this.msg = message;
	}
	public EngineRuntimeException(String message,Throwable e) {
		super(message,e);
		this.msg = message;
	}
	public String toString() {
		return ("EngineRuntimeException:" + this.msg);
	}
}
