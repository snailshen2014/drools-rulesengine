/**
 * 
 */
package com.syj.iot.rulesengine.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @des:buffer data when Iot kie context init env
 * @author shenyanjun1
 * @date: 2018年6月6日 下午2:24:48
 */
public class DataCache {

	private Queue<Data> buffer = new ConcurrentLinkedQueue<>();

	private DataCache() {
	}

	private static class Inner {
		private static DataCache dataCache = new DataCache();
	}
	/**
	 * @des inner class  singleton pattern
	 * @return
	 */
	public static DataCache getDataCache() {
		return Inner.dataCache;
	}

	public Map<String, String> getBufferData() {
		Data one = buffer.poll();
		if (one == null)
			return null;
		return new HashMap() {
			{
				put("TOPICID", one.getTopicId());
				put("PRODUCTKEY", one.getProductKey());
				put("DATA", one.getData());
			}
		};

	}

	public void putBufferData(String topicId, String productKey, String data) {
		buffer.add(new Data(topicId, productKey, data));
	}

	private class Data {
		/**
		 * @param topicId
		 * @param data
		 */
		public Data(String topicId, String productKey, String data) {
			super();
			this.topicId = topicId;
			this.data = data;
			this.productKey = productKey;
		}

		private String topicId;
		private String productKey;

		/**
		 * @return the productKey
		 */
		public String getProductKey() {
			return productKey;
		}

		/**
		 * @param productKey the productKey to set
		 */
		public void setProductKey(String productKey) {
			this.productKey = productKey;
		}

		/**
		 * @return the topicId
		 */
		public String getTopicId() {
			return topicId;
		}

		/**
		 * @param topicId the topicId to set
		 */
		public void setTopicId(String topicId) {
			this.topicId = topicId;
		}

		/**
		 * @return the data
		 */
		public String getData() {
			return data;
		}

		/**
		 * @param data the data to set
		 */
		public void setData(String data) {
			this.data = data;
		}

		private String data;
	}
}
