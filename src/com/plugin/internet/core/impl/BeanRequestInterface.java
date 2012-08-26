package com.plugin.internet.core.impl;


public interface BeanRequestInterface {

	public <T> T request(RequestBase<T> request) throws NetWorkException;
	
}
