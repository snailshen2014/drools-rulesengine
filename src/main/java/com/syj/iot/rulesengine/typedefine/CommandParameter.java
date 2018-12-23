/**
 * 
 */
package com.syj.iot.rulesengine.typedefine;

import java.util.List;

/**
 * @des:define rulesEngine command parameters
 * @author shenyanjun1
 * @date: 2018年7月14日 下午2:00:40
 */
public class CommandParameter {
	/**
	 * 
	 */
	public CommandParameter() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param pkgName
	 * @param ruleName
	 */
	public CommandParameter(String pkgName, List<String> ruleNames) {
		super();
		this.pkgName = pkgName;
		this.ruleNames = ruleNames;
	}
	private String pkgName;
	private List<String> ruleNames;
	private String ruleStr;
	private Long dataFormatId;
	/**
	 * @return the dataFormatId
	 */
	public Long getDataFormatId() {
		return dataFormatId;
	}
	/**
	 * @param dataFormatId the dataFormatId to set
	 */
	public void setDataFormatId(Long dataFormatId) {
		this.dataFormatId = dataFormatId;
	}
	/**
	 * @return the ruleStr
	 */
	public String getRuleStr() {
		return ruleStr;
	}
	/**
	 * @param ruleStr the ruleStr to set
	 */
	public void setRuleStr(String ruleStr) {
		this.ruleStr = ruleStr;
	}
	/**
	 * @return the pkgName
	 */
	public String getPkgName() {
		return pkgName;
	}
	/**
	 * @param pkgName the pkgName to set
	 */
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	/**
	 * @return the ruleName
	 */
	public List<String> getRuleNames() {
		return this.ruleNames;
	}
	/**
	 * @param ruleName the ruleName to set
	 */
	public void setRuleNames(List<String> ruleNames) {
		this.ruleNames = ruleNames;
	}
	
}
