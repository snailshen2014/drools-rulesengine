/**
 * 
 */
package com.syj.iot.rulesengine.test;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年6月1日 下午4:58:29
 */
public class Test {

	public static void main(String args[]) {
		try {
			JSONObject obj = new JSONObject();
			obj.put("mmac", "F0: 27: 2 D: FA: 6 A: 5A");
			obj.put("version", 139);
			obj.put("rate", 10);
			String str = "[{ \"mac\": \"F0: 27: 2 D: FA: 6 A: 5B\"," + 
					"              \"time\": 1524202182, " + 
					"              \"rssi\": -69 " + 
					"       }, {" + 
					"              \"mac\": \"F0: 27: 2 D: FA: 6 A: 5C\"," + 
					"              \"time\": 1524202182," + 
					"              \"rssi\": -70      }]";
			
			
			JSONArray ja = JSON.parseArray(str);
			obj.put("data", ja);
			System.out.println("Json:" + obj.toJSONString());
			
			CloseableHttpClient httpclient = HttpClients.createDefault();

			HttpPost httpPost = new HttpPost("http://localhost:80/rulesengine/topic?topic=1");
			httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
			// 解决中文乱码问题
			StringEntity stringEntity = new StringEntity(obj.toString(), "UTF-8");
			stringEntity.setContentEncoding("UTF-8");
			httpPost.setEntity(stringEntity);

			// CloseableHttpResponse response =
			// httpclient.execute(httpPost);

			System.out.println("Executing request " + httpPost.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {//
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {

						HttpEntity entity = response.getEntity();

						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = httpclient.execute(httpPost, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
