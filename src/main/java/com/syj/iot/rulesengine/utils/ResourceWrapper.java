/**
 * 
 */
package com.syj.iot.rulesengine.utils;

import org.kie.api.io.Resource;

/**
 * @des:
 * @author shenyanjun1
 * @date: 2018年5月26日 上午11:40:25
 */
public class ResourceWrapper {
	private Resource resource;

	private String targetResourceName;

	public ResourceWrapper(Resource resource, String targetResourceName) {
		this.resource = resource;
		this.targetResourceName = targetResourceName;
	}

	public Resource getResource() {
		return resource;
	}

	public String getTargetResourceName() {
		return targetResourceName;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setTargetResourceName(String targetResourceName) {
		this.targetResourceName = targetResourceName;
	}
}
