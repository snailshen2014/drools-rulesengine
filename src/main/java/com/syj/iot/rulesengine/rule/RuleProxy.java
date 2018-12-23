package com.syj.iot.rulesengine.rule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaNil;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.syj.iot.rulesengine.utils.LuaUtil;
import com.syj.iot.rulesengine.utils.SqlStatementParser;

/**
 * 
 * @des:with drools rules,filter and conver json data
 * @author shenyanjun1
 * @date: 2018年6月13日 上午10:00:55
 */
public class RuleProxy {
	private Logger logger = LoggerFactory.getLogger(RuleProxy.class);

	// store ruleId->placeHolder,apply for Using all rules for every event(KJar)
	private Map<Integer, String> mapIdPlaceHolder = new HashMap<>();

	public void addRulesPlaceholder(Integer ruleId, String placeHolder) {
		mapIdPlaceHolder.put(ruleId, placeHolder);
	}

	/**
	 * @des marshal json package
	 * @param ruleId
	 * @param retained
	 * @param holder
	 * @return
	 */
	public String marshal(Integer ruleId, JSONObject source, List<JSONObject> retained, String holder) {
		String placeholder = mapIdPlaceHolder.get(ruleId);
		if (placeholder == null || placeholder.equals("")) {
			logger.error("Can not find placeholder or  null.");
			return "ERROR";
		}
		JSONObject json = new JSONObject();
		// '*' or attri1 alias1,attri2 alias2
		// no support function now
		if (placeholder.trim().equals("*")) {
			for (String key : source.keySet()) {
				if (key.equals(holder))
					continue;
				json.put(key, source.get(key));
			}
			json.put(holder, retained);
			return json.toString();
		}

		Map<String, String> attrMap = SqlStatementParser.getAttributes(placeholder);
		if (attrMap == null) {
			logger.error("Sql statement parser getAttributes null.");
			return "ERROR";
		}
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			String funcName = getFunctionName(entry.getValue());
			if (funcName != null) {
				//now only suprrort field as parameter to function,for example abs(field)...
				Object param = source.get(fromFunctionAttribute(entry.getValue()));
				json.put(entry.getKey(), method(funcName,param));
			} else {
				json.put(entry.getKey(), source.get(entry.getValue()));
			}
		}

		json.put(holder, retained);
		return json.toString();
	}

	public Object method(String funName, Object... args) {
		Class[] types = null;
		if (args != null && args.length > 0) {
			types =  new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof String)
					types[i] = String.class;
				if (args[i] instanceof Integer)
					types[i] = Integer.class;
				if (args[i] instanceof Float)
					types[i] = Float.class;
			}
		}
		
		try {
//			long start = System.currentTimeMillis();
			Class<?> cls = Class.forName("com.syj.iot.rulesengine.rule.RuleFunction");
			Object obj = cls.newInstance();
			Method method = cls.getMethod(funName, types);
			Object value = method.invoke(obj, args);
			long end = System.currentTimeMillis();
//			System.out.println("Call java method time:" + (end-start) + " :ms");
			return value;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Method call error:{}",e.getMessage());
		}

		return null;
	}

	/**
	 * @des marshal json package
	 * @param ruleId
	 * @param retained
	 * @param holder
	 * @return
	 */
	public String marshal(Integer ruleId, JSONObject source) {
		String placeholder = mapIdPlaceHolder.get(ruleId);
		if (placeholder == null || placeholder.equals("")) {
			logger.error("Can not find placeholder or  null.");
			return "ERROR";
		}
		JSONObject json = new JSONObject();
		// '*' or attri1 alias1,attri2 alias2
		// no support function now
		if (placeholder.trim().equals("*")) {
			for (String key : source.keySet()) {
				json.put(key, source.get(key));
			}
			return json.toString();
		}
		
		Map<String, String> attrMap = SqlStatementParser.getAttributes(placeholder);
		if (attrMap == null) {
			logger.error("Sql statement parser getAttributes null.");
			return "ERROR";
		}
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			json.put(entry.getKey(), source.get(entry.getValue()));
		}
		//if condition contains function ,parsing entry.getValue and call function
		return json.toString();
	}

	public Object methodLua(String funName, Object... args) {
		try {
//			long start = System.currentTimeMillis();
			Globals globals = JsePlatform.standardGlobals();
			String script = IotRulesMapper.getScript(funName);
			if (script == null) {
				System.out.println("MethodLua funName null:" + funName);
				logger.error("Get lua function error,function's name:{}",funName);
				return null;
			}
			
			globals.load(script).call();
			LuaValue func = globals.get(LuaValue.valueOf(funName));
//			long end = System.currentTimeMillis();
//			System.out.println("Init lua env time:" + (end-start) + " :ms");
//			start = System.currentTimeMillis();
			Varargs result = LuaUtil.callFunction(func, args);
//			end = System.currentTimeMillis();
//			System.out.println("Call lua method time:" + (end-start) + " :ms");
			//only one return value
			LuaValue rValue = result.arg1();
//			System.out.println("lua function rtn value:" + LuaUtil.Lua2Object(rValue));
			return LuaUtil.Lua2Object(rValue);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.error("Method call lua function error:{}",e.getMessage());
		}
		return null;
	}
	
	private String getFunctionName(String attribute) {
		if (RuleFunction.isContainsFunction(attribute))
			return RuleFunction.getFunctionName(attribute);
		else
			return null;
	}
	
	private String fromFunctionAttribute(String attribute) {
		int pos = attribute.indexOf("(");
		int pos2 = attribute.indexOf(")");
		return attribute.substring(pos+1, pos2);
	}
}