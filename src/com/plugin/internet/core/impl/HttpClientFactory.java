package com.plugin.internet.core.impl;

import android.content.Context;

public class HttpClientFactory {

    public static HttpClientInterface createHttpClientInterface(Context context) {
        return HttpClientImpl.getInstance(context);
    }
    
}
