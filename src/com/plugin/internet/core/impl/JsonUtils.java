/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.plugin.internet.core.impl;

import android.text.TextUtils;

import com.plugin.internet.core.json.JsonMapper;

/**
 * Provides methods for converting JSON response string to 
 * object.
 * 
 * @author Shaofeng Wang (shaofeng.wang@renren-inc.com)
 */
public class JsonUtils {
		
	private static JsonMapper mapper;
//	private static ObjectMapper mapper;
	
	/**
	 * Convert JSON response string to corresponding object
	 * @param response Response string 
	 * @param valueType The target class
	 * @return The object with type T will be returned if successfully parsing
	 * 		the JSON response string, or return null if failed.
	 */
    public static <T> T parse(String response, Class<T> valueType) {
    	if(TextUtils.isEmpty(response) || valueType == null) {
    		return null;
    	}
		if(mapper == null) {
			mapper = new JsonMapper();
//			mapper = new ObjectMapper();
		}
		try {
			return mapper.readValue(response, valueType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    
    /**
     * Check whether the response is a failure response
     * @param response The json response
     * @return Return a RRFailureResponse object is the response is a failure response,
     * 	or return null if request succeeds
     */
//    public static RRFailureResponse parseError(String response) {
//    	if(TextUtils.isEmpty(response)) {
//    		return null;
//    	}
//    	
//    	if(mapper == null) {
//			mapper = new JsonMapper();
////			mapper = new ObjectMapper();
//    	}
//    	try {
//    		RRFailureResponse failureResponse = mapper.readValue(response, RRFailureResponse.class);
//    		if (failureResponse != null && failureResponse.getErrorCode() == 0 && failureResponse.getErrorMessage() == null) {
//    			// may be Null Element like "{}"
//    			return null;
//    		}
//			return failureResponse;
//	    } catch (Exception e) {
//			e.printStackTrace();
//		}
//	return null;
//    }
}
