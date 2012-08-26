package com.plugin.internet.core.impl;

import android.content.Context;

public class BeanRequestFactory {

    public static BeanRequestInterface createBeanRequestInterface(Context context) {
        return BeanRequestImpl.getInstance(context);
    }
    
}
