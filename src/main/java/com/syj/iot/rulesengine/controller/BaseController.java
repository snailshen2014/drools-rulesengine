package com.syj.iot.rulesengine.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName: BaseController
 * @Description: 基础controller
 */
public class BaseController {

	protected static final Logger logger = LoggerFactory.getLogger("ASYNC_STDOUT");
	protected static final Logger error = LoggerFactory.getLogger("STDERR");
	protected static final String ENCODING = "UTF-8";

	@Autowired
	UserService userService;

	/**
	 * 返回json
	 *
	 * @param code
	 * @param msg
	 * @param data
	 * @return
	 */
	public JSONObject getReturnJson(int code, String msg, String data) {
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
	public JSONObject getReturnJson(int code, String msg, JSONObject object) {
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
	public JSONObject getReturnJson(int code, String msg, JSONArray array) {
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
	public JSONObject getReturnJson(int code, String msg) {
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
	public JSONObject getReturnJson(ResponseMsgEnum responseMsgEnum) {
		JSONObject json = new JSONObject();
		json.put("code", responseMsgEnum.getCode());
		json.put("msg", responseMsgEnum.getMsg());
		return json;
	}

}