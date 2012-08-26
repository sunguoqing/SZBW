package com.plugin.internet;

import android.content.Context;

import com.plugin.internet.core.impl.BeanRequestFactory;
import com.plugin.internet.core.impl.NetWorkException;
import com.plugin.internet.core.impl.RequestBase;

public class InternetUtils {

    /**
     * 同步接口
     * 发送REST请求
     * @param <T>
     * @param request
     *          REST请求
     * @return REST返回
     * @throws NetWorkException
     */
    public static <T> T request (Context context, RequestBase<T> request) throws NetWorkException {
        if (BeanRequestFactory.createBeanRequestInterface(context) != null) {
            return BeanRequestFactory.createBeanRequestInterface(context).request(request);
        }
        
        return null;
    }
    
}
