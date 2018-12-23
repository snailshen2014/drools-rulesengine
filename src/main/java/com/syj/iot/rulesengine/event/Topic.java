/**
 * 
 */
package com.syj.iot.rulesengine.event;

import java.util.List;

/**
 * @des:Now this class is for filter rules,only execute this topic or product's rules.
 * @author shenyanjun1
 * @date: 2018年6月5日 下午8:15:06
 */
public class Topic {
	/**
	 * @param topicId
	 * @param dataFormat
	 * @param beanName
	 */
	public Topic(Long topicId, String dataFormat, String beanName) {
		super();
		this.topicId = topicId;
		this.dataFormat = dataFormat;
		this.beanName = beanName;
	}
	private Long topicId;
	private String dataFormat;
	private String beanName;
	private List<Integer> ruleIdList;
	
	/**
	 * @return the ruleIdList
	 */
	public List<Integer> getRuleIdList() {
		return ruleIdList;
	}
	/**
	 * @param ruleIdList the ruleIdList to set
	 */
	
	public void setRuleIdList(List<Integer> ruleIdList) {
		this.ruleIdList = ruleIdList;
	}
	/**
	 * @return the topicId
	 */
	public Long getTopicId() {
		return topicId;
	}
	/**
	 * @param topicId the topicId to set
	 */
	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}
	/**
	 * @return the dataFormat
	 */
	public String getDataFormat() {
		return dataFormat;
	}
	/**
	 * @param dataFormat the dataFormat to set
	 */
	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}
	/**
	 * @return the beanName
	 */
	public String getBeanName() {
		return beanName;
	}
	/**
	 * @param beanName the beanName to set
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
}
