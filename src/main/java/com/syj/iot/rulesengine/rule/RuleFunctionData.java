/**
 * 
 */
package com.syj.iot.rulesengine.rule;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年6月5日 下午2:48:38
 */
public class RuleFunctionData {
	/**
	 * @param startTimestamp
	 * @param endTimestamp
	 */
	public RuleFunctionData(long startTimestamp, long endTimestamp) {
		super();
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
	/**
	 * @return the startTimestamp
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}
	/**
	 * @param startTimestamp the startTimestamp to set
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	/**
	 * @return the endTimestamp
	 */
	public long getEndTimestamp() {
		return endTimestamp;
	}
	/**
	 * @param endTimestamp the endTimestamp to set
	 */
	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	private long startTimestamp;
	private long endTimestamp;
	
}
