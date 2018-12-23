/**
 * 
 */
package com.syj.iot.rulesengine.event;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年5月30日 上午11:18:30
 */
public class MobileData {

	/**
	 * @return the mac
	 */
	public String getMac() {
		return mac;
	}

	/**
	 * @param mac
	 *            the mac to set
	 */
	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the rssi
	 */
	public int getRssi() {
		return rssi;
	}

	/**
	 * @param rssi
	 *            the rssi to set
	 */
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public String mac;// 手机mac
	public long time;// 采集时间 long型 系统时间
	public int rssi;// 信号强度

}
