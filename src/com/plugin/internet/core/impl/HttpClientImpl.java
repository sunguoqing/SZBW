/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.plugin.internet.core.impl;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.beiwai.shuzi.utils.StringUtils;
import com.plugin.internet.config.Config;

class HttpClientImpl implements HttpClientInterface {
    private static final String TAG = "HttpUtil";
    private static final boolean DEBUG = Config.DEBUG;
	
	public static final String HTTP_REQUEST_METHOD_POST = "POST";
	
	public static final String HTTP_REQUEST_METHOD_GET = "GET";
	
	private static HttpClientImpl instance;
	
	public static HttpClientImpl getInstance (Context context) {
		if (instance == null) {
			instance = new HttpClientImpl(context);
		}
		return instance;
	}

	private HttpClientImpl (Context context) {
		this.mContext = context;
		this.init();
	}
	
	private Context mContext;

	private HttpClient httpClient;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getResource(Class<T> resourceType, String url, String method,
			HttpEntity entity) {

		HttpRequestBase requestBase = createHttpRequest(url, method, entity);
		if (resourceType == byte[].class) {
			try {
				return (T) getBytesResponse(requestBase);
			} catch (NetWorkException e) {
				e.printStackTrace();
			}
		} else if (resourceType == String.class) {
			try {
				return (T) getStringResponse(requestBase);
			} catch (NetWorkException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("Unknown resoureType :" + resourceType);
		}
		
		return null;
	}
	
	private class StringResponseHandler implements ResponseHandler<String> {

		@Override
		public String handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			String r = null;
			LOGD("[[HttpUtil::handleResponse]] http response status = " + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				try {
					LOGD("[HttpUtil]:: try ungzip first");
				    r = StringUtils.unGzipBytesToString(response.getEntity().getContent()).trim();
				} catch (IOException e) {
					LOGD("[HttpUtil]::ungzip fail");
					e.printStackTrace();
				}
			}
			return r;
		}
		
	}
	
	private class ByteDataResponseHandler implements ResponseHandler<byte[]> {

		@Override
		public byte[] handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			byte[] data = EntityUtils.toByteArray(response.getEntity());
			return data;
		}
		
	}
	
	
//	public String openUrl (String url, String method, Bundle params) throws RRException {
//	    LOGD("[[HttpUtil::openUrl]] url = " + url + " params = " + params.toString());
//	    
//		HttpRequestBase httpRequest = createHttpRequest(url, method, params);
//		return getStringResponse(httpRequest);
//	}
//	
//	public byte[] getBytesData (String url, Bundle params) throws RRException {
//		HttpRequestBase httpRequest = createHttpRequest(url, HTTP_REQUEST_METHOD_POST, params);
//		return getBytesResponse(httpRequest);
//	}
	
//	public String uploadPhoto (String url, File file, Bundle params) throws RRException {
//		PhotoHttpEntity photoHttpEntity = new PhotoHttpEntity(params, file);
//		HttpRequestBase httpRequest = createHttpRequest(url, HTTP_REQUEST_METHOD_POST, photoHttpEntity);
//		return getStringResponse(httpRequest);
//	}
//
//	public String uploadPhoto(String url, Bundle params, String filename, String contentType, byte[] data) throws RRException {
//		
//		PhotoHttpEntity photoHttpEntity = new PhotoHttpEntity(params, contentType, data);
//		HttpRequestBase httpRequest = createHttpRequest(url, HTTP_REQUEST_METHOD_POST, photoHttpEntity);
//		return getStringResponse(httpRequest);
//	}
	
	private void init () {
		httpClient = createHttpClient();
	}
	
	private DefaultHttpClient createHttpClient() {
		final SchemeRegistry supportedSchemes = new SchemeRegistry();
		supportedSchemes.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		supportedSchemes.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		final HttpParams httpParams = createHttpParams();
		final ThreadSafeClientConnManager tccm = new ThreadSafeClientConnManager(httpParams, supportedSchemes);
		DefaultHttpClient client = new DefaultHttpClient(tccm, httpParams);
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
		return client;
	}
	
	private HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, 45000);
		HttpConnectionParams.setSoTimeout(params, 100000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpClientParams.setRedirecting(params, false);
		ConnManagerParams.setMaxTotalConnections(params, 50);
		ConnManagerParams.setTimeout(params, 30000);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(20));
		return params;
	}
	
	private HttpRequestBase createHttpRequest (String url, String method, HttpEntity entity) {

		checkParams(url, method);
		
		HttpRequestBase httpRequest = null;
		if (method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET)) {
			httpRequest = new HttpGet(url);
		} else {
			httpRequest = new HttpPost(url);
			if (entity != null) {
				((HttpPost)httpRequest).setEntity(entity);
			}
		}
		
		HttpHost host = HttpProxy.getProxyHttpHost(mContext);
		if (host != null) {
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
		} else {
			httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
		}
		return httpRequest;
	}
	
//	private HttpRequestBase createHttpRequest (String url, String method, Bundle params) {
//		
//		checkParams(url, method);
//		
//		HttpRequestBase httpRequest = null;
//		List<NameValuePair> paramList = convertBundleToNVPair(params);
//		if (method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET)) {
//			String getUrl = url;
//			if (paramList != null) {
//				String query = URLEncodedUtils.format(paramList, HTTP.UTF_8);
//				getUrl += "?" + query;
//			}
//			httpRequest = new HttpGet(getUrl);
//		} else {
//			httpRequest = new HttpPost(url);
//			if (paramList != null) {
//				HttpEntity entity = null;
//				try {
//					entity = new UrlEncodedFormEntity(paramList, HTTP.UTF_8);
//				} catch (UnsupportedEncodingException e1) {
//					throw new IllegalArgumentException("Unable to encode http parameters.");
//				}
//				if (entity != null) {
//					((HttpPost)httpRequest).setEntity(entity);
//				}
//			}
//		}
//		HttpHost host = HttpProxy.getProxyHttpHost(context);
//		if (host != null) {
//			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
//		} else {
//			httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
//		}
//		return httpRequest;
//	}
	
//	private List<NameValuePair> convertBundleToNVPair (Bundle bundle) {
//		if (bundle == null) {
//			return null;
//		}
//		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
//		Set<String> keySet = bundle.keySet();
//		for (String key : keySet) {
//			list.add(new BasicNameValuePair(key, bundle.getString(key)));
//		}
//		
//		return list;
//	}
	
	private void checkParams (String url, String method) throws IllegalArgumentException {
		if (TextUtils.isEmpty(url)) {
			throw new IllegalArgumentException("Request url MUST NOT be null");
		}
		if (TextUtils.isEmpty(method)) {
			throw new IllegalArgumentException("Request method MUST NOT be null");
		} else {
			if (!method.equalsIgnoreCase(HTTP_REQUEST_METHOD_GET) && !method.equalsIgnoreCase(HTTP_REQUEST_METHOD_POST)) {
				throw new IllegalArgumentException("Only support GET and POST");
			}
		}		
	}
	
	private void preExecuteHttpRequest () {

		httpClient.getConnectionManager().closeExpiredConnections();
	}
	
	private void onExecuteException (HttpRequestBase httpRequest) {
		httpRequest.abort();
	}
	
	private byte[] getBytesResponse(HttpRequestBase httpRequest) throws NetWorkException {
		try {
			preExecuteHttpRequest ();
			ByteDataResponseHandler handler = new ByteDataResponseHandler();
			return httpClient.execute(httpRequest, handler);
		} catch (Exception e) {
			onExecuteException (httpRequest);
			throw new NetWorkException(NetWorkException.NETWORK_ERROR, "网络连接错误", e.toString());
		}
	}
	
	private String getStringResponse(HttpRequestBase httpRequest) throws NetWorkException {
		try {
			preExecuteHttpRequest ();
			StringResponseHandler handler = new StringResponseHandler();
			return httpClient.execute(httpRequest, handler);
		} catch (Exception e) {
			onExecuteException (httpRequest);
			throw new NetWorkException(NetWorkException.NETWORK_ERROR, "网络连接错误", e.toString());
		}
	}
	
    public boolean isNetworkAvailable() {
        if (mContext == null) {
            LOGD("[[checkNetworkAvailable]] check context null");
            return false;
        }

        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            LOGD("[[checkNetworkAvailable]] connectivity null");
            return false;
        }

        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
    
}
