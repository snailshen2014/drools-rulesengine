/**
 * 
 */
package com.syj.iot.rulesengine.cache;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.iot.rulesengine.entrypoint.EntryPointTranspond;
import com.syj.iot.rulesengine.error.EngineRuntimeException;
import com.syj.iot.rulesengine.init.IotKieContex;
import com.syj.iot.rulesengine.typedefine.DataEntryPoint;

/**
 * @des:When Kie evn redo,this will dealing the cache data
 * @author shenyanjun1
 * @date: 2018年6月6日 下午3:52:42
 */
public class CacheRunnable implements Runnable {

	private DataCache dataCache;
	protected static final Logger logger = LoggerFactory.getLogger(CacheRunnable.class);
	public CacheRunnable(DataCache cache) {
		this.dataCache = cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			while (!isBlocking() ) {
				Map<String,String> data = dataCache.getBufferData();
				if (data == null)
					break;
				logger.info("[Cache] threadId:{},Read one data from data cache.",Thread.currentThread().getId());
				String topicId = null;
				String deviceData = null;
				String productKey = null;
				for(String key : data.keySet()) {
					if (key.equals("TOPICID"))
						topicId = data.get(key);
					if (key.equals("PRODUCTKEY"))
						productKey = data.get(key);
					if (key.equals("DATA"))
						deviceData = data.get(key);
				}
				try {
					EntryPointTranspond.transpond(topicId, productKey, deviceData, DataEntryPoint.CACHE_POINT);
				} catch (EngineRuntimeException e) {
					e.printStackTrace();
					logger.error("[Cache] threadId:{},topicId:{},productKey:{},transpond data error,EngineRuntimeException.",Thread.currentThread().getId(),topicId,productKey);
				} catch(Exception e) {
					e.printStackTrace();
					logger.error("[Cache] threadId:{},topicId:{},productKey:{},transpond data error,Exception.",Thread.currentThread().getId(),topicId,productKey);
				}
				logger.info("[Cache] threadId:{},transpond data ok.",Thread.currentThread().getId());
			}
			
			try {
				Thread.sleep(5000 * 2);
				logger.info("[Cache] threadId:{},no more data to read.",Thread.currentThread().getId());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * @des when kie runtime busy then blocking
	 * @return
	 */
	private boolean isBlocking() {
		return IotKieContex.getIotKieContex().isIotKieBusy();
	}
	
}
