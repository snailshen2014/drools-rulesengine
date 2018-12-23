package com.syj.iot.rulesengine.monitor;
/**
 * 
*  @des    :define monitor point
 * @author:shenyanjun1
 * @date   :2018-11-02 18:24
 */
public class PointMonitor {
	public PointMonitor(String key, MonitorType type) {
		super();
		this.key = key;
		this.type = type;
	}
	private String key;//topic or productkey
	private MonitorType type;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public MonitorType getType() {
		return type;
	}
	public void setType(MonitorType type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "PointMonitor [key=" + key + ", type=" + type + "]";
	}
}
