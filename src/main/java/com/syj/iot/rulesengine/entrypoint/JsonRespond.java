/**
 * 
 */
package com.syj.iot.rulesengine.entrypoint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年8月27日 下午3:29:57
 */
public class JsonRespond {
	/**
	 * 返回json
	 *
	 * @param code
	 * @param msg
	 * @param data
	 * @return
	 */
	public static JSONObject getReturnJson(int code, String msg, String data) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", data);
		return json;
	}

	/**
	 * 返回json
	 *
	 * @param code
	 * @param msg
	 * @param object
	 * @return
	 */
	public static JSONObject getReturnJson(int code, String msg, JSONObject object) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", object);
		return json;
	}

	/**
	 * 返回json
	 *
	 * @param code
	 * @param msg
	 * @param array
	 * @return
	 */
	public static JSONObject getReturnJson(int code, String msg, JSONArray array) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", array);
		return json;
	}

	/**
	 * 返回json
	 *
	 * @param code
	 * @param msg
	 * @return
	 */
	public static JSONObject getReturnJson(int code, String msg) {
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		return json;
	}

	/**
	 * 返回結果
	 *
	 * @return
	 */
	public static JSONObject getReturnJson(ResponseMsgEnum responseMsgEnum) {
		JSONObject json = new JSONObject();
		json.put("code", responseMsgEnum.getCode());
		json.put("msg", responseMsgEnum.getMsg());
		return json;
	}
}
