package com.plugin.internet.core.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.plugin.internet.config.Config;

class BeanRequestImpl implements BeanRequestInterface {

    private static final String TAG = "BeanRequestImpl";
    private static final boolean DEBUG = Config.DEBUG;

    private static final String BASE_URL = "";

    private static BeanRequestImpl mInstance;

    private HttpClientInterface mHttpClientInterface;
    private Context mContext;

    public static BeanRequestImpl getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BeanRequestImpl(context);
        }
        return mInstance;
    }

    private BeanRequestImpl(Context context) {
        this.mContext = context;
        mHttpClientInterface = HttpClientFactory.createHttpClientInterface(context);
    }

    @Override
    public <T> T request(RequestBase<T> request) throws NetWorkException {

        if (!mHttpClientInterface.isNetworkAvailable()) {
            throw new NetWorkException(NetWorkException.NETWORK_NOT_AVILABLE, "网络连接错误，请检查您的网络", null);
        }

        RequestEntity requestEntity = request.getRequestEntity();
        Bundle baseParams = requestEntity.getBasicParams();

        if (baseParams == null) {
            throw new NetWorkException("Basic Params MUST NOT be NULL");
        }

        // int sessionConfig = request.getSessinConfig();
        // if (sessionConfig == RequestBase.NEED_SESSIONKEY) {
        // if (mUserInfo == null || mUserInfo.getSessionKey() == null) {
        // throw new RRException(RRException.USER_NOT_LOGIN, "用户没有登陆", null);
        // }
        // }

        // String secretKey = appInfo.getAppSecret();
        //
        // switch (sessionConfig) {
        // case RequestBase.NO_SESSIONKEY:
        // break;
        // case RequestBase.OPTIONAL_SESSIONKEY:
        // if (mUserInfo != null && mUserInfo.getSessionKey() != null) {
        // secretKey = mUserInfo.getUserSecret();
        // baseParams.putString("session_key", mUserInfo.getSessionKey());
        // }
        // break;
        // case RequestBase.NEED_SESSIONKEY:
        // secretKey = mUserInfo.getUserSecret();
        // baseParams.putString("session_key", mUserInfo.getSessionKey());
        // break;
        // default:
        // break;
        // }
        // baseParams.putString("api_key", appInfo.getApiKey());
        // baseParams.putString("gz", "compression");
        // baseParams.putString("client_info",
        // EnvironmentUtil.getInstance(mContext).getClientInfo());
        // baseParams.putString("sig", getSig(baseParams, secretKey));

        String contentType = requestEntity.getContentType();
        if (contentType == null) {
            throw new NetWorkException("Content Type MUST be specified");
        }
        HttpEntity entity = null;
        if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_TEXT_PLAIN)) {

            List<NameValuePair> paramList = convertBundleToNVPair(baseParams);
            if (paramList != null) {
                try {
                    entity = new UrlEncodedFormEntity(paramList, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Unable to encode http parameters.");
                }
            }
        } else if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            requestEntity.setBasicParams(baseParams);
            entity = new MultipartHttpEntity(requestEntity);
        }

        String response = mHttpClientInterface.getResource(String.class, BASE_URL, "POST", entity);
        LOGD("[[RRConnect::request::" + request + "]] " + response + "<<<<<<<<<<<");
        if (response == null) {
            throw new NetWorkException(NetWorkException.SERVER_ERROR, "服务器错误，请稍后重试", null);
        }
        // RRFailureResponse failureResponse = JsonUtils.parseError(response);
        // if (failureResponse == null) {
        return JsonUtils.parse(response, request.getGenericType());
        // } else {
        // LOGD("[[response]] " + failureResponse.toString());
        // checkException(failureResponse.getErrorCode());
        // throw new RRException(failureResponse.getErrorCode()
        // , failureResponse.getErrorMessage(),
        // mCenter.getErrorStringByCode(failureResponse.getErrorCode()));
        // }
    }

    private String getSig(Bundle params, String secretKey) {
        return null;

        // if (params == null) {
        // return null;
        // }
        //
        // if (params.size() == 0) {
        // return "";
        // }
        //
        //
        // TreeMap<String, String> sortParams = new TreeMap<String, String>();
        // for (String key : params.keySet()) {
        // sortParams.put(key, params.getString(key));
        // }
        // LOGD("[[getSig]] params sorted is : " + sortParams.toString());
        //
        // Vector<String> vecSig = new Vector<String>();
        // for (String key : sortParams.keySet()) {
        // String value = sortParams.get(key);
        // if (value.length() > ImplementConfig.SIG_PARAM_MAX_LENGTH) {
        // value = value.substring(0, ImplementConfig.SIG_PARAM_MAX_LENGTH);
        // }
        // vecSig.add(key + "=" + value);
        // }
        // LOGD("[[getSig]] after operate, the params is : " + vecSig);
        //
        // String[] nameValuePairs = new String[vecSig.size()];
        // vecSig.toArray(nameValuePairs);
        //
        // for (int i = 0; i < nameValuePairs.length; i++) {
        // for (int j = nameValuePairs.length - 1; j > i; j--) {
        // if (nameValuePairs[j].compareTo(nameValuePairs[j - 1]) < 0) {
        // String temp = nameValuePairs[j];
        // nameValuePairs[j] = nameValuePairs[j - 1];
        // nameValuePairs[j - 1] = temp;
        // }
        // }
        // }
        // StringBuffer nameValueStringBuffer = new StringBuffer();
        // for (int i = 0; i < nameValuePairs.length; i++) {
        // nameValueStringBuffer.append(nameValuePairs[i]);
        // }
        //
        // nameValueStringBuffer.append(secretKey);
        // String sig = StringUtils.MD5Encode(nameValueStringBuffer.toString());
        // return sig;

    }

    private void checkException(int exceptionCode) {
        // switch (exceptionCode) {
        // case RRException.API_EC_INVALID_SESSION_KEY:
        // case RRException.API_EC_USER_AUDIT:
        // case RRException.API_EC_USER_BAND:
        // case RRException.API_EC_USER_SUICIDE:
        // LOGD("[[checkException]] should clean the user info in local");
        // //
        // mAccessTokenManager.clearUserLoginInfoByUid(mAccessTokenManager.getUID());
        // break;
        // default:
        // return;
        // }
    }

    private List<NameValuePair> convertBundleToNVPair(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            list.add(new BasicNameValuePair(key, bundle.getString(key)));
        }

        return list;
    }

    private static void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
