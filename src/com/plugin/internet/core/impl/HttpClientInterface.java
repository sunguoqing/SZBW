package com.plugin.internet.core.impl;

import org.apache.http.HttpEntity;

public interface HttpClientInterface {
	
	public <T> T getResource(Class<T> resourceType, String url, String method, HttpEntity entity) throws NetWorkException;
	
	public boolean isNetworkAvailable();
	
}
