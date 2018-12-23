/**
 * 
 */
package com.syj.iot.rulesengine.rule;

/**
 * @des:The  rule  represent
 * @author shenyanjun1
 * @date: 2018年5月22日 下午5:57:53
 */
public class PrecompiledRule {
	
	/**
	 * @return the pkgSuffix
	 */
	public String getPkgSuffix() {
		return pkgSuffix;
	}

	/**
	 * @param pkgSuffix the pkgSuffix to set
	 */
	public void setPkgSuffix(String pkgSuffix) {
		this.pkgSuffix = pkgSuffix;
	}

	//define this rule belong to which package(Toici/Product ID)
	private String pkgSuffix;
	
	/**
	 * @return the formatId
	 */
	public Long getFormatId() {
		return formatId;
	}

	/**
	 * @param formatId the formatId to set
	 */
	public void setFormatId(Long formatId) {
		this.formatId = formatId;
	}

	/**
	 * @param ruleId
	 * @param content
	 * @param attributesPlaceholders
	 */
	public PrecompiledRule(int ruleId, String content, String attributesPlaceholders,Long formatId
						  ,String suffix) {
		super();
		this.ruleId = ruleId;
		this.content = content;
		this.attributesPlaceholders = attributesPlaceholders;
		this.formatId = formatId;
		this.pkgSuffix = suffix;
	}

	private int ruleId;
	
	//	drl file string represent)
	private String content;
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	// selected attributest
	private String attributesPlaceholders;

	/**
	 * @return the ruleId
	 */
	public int getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId the ruleId to set
	 */
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}


	
	public void setAttributesPlaceholders(String holder) {
		this.attributesPlaceholders = holder;
	}

	public String getAttributesPlaceholders() {
		return attributesPlaceholders;
	}
	
	private Long formatId;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PrecompiledRule [pkgSuffix=" + pkgSuffix + ", ruleId=" + ruleId 
				+ ", attributesPlaceholders=" + attributesPlaceholders + ", formatId=" + formatId + "]";
	}
}
