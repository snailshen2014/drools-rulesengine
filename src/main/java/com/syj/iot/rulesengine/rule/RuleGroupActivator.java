/**
 * 
 */
package com.syj.iot.rulesengine.rule;

/**
 * @des:for activate agenda-group
 * @author shenyanjun1
 * @date: 2018年7月10日 下午2:58:22
 */
public class RuleGroupActivator {
	/**
	 * 
	 */
	public RuleGroupActivator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 */
	public RuleGroupActivator(String id) {
		super();
		this.id = id;
	}

	//topicid or product's rule id
	private String id;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
}
