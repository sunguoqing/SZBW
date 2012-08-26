package com.plugin.internet.core.impl;

public class NetWorkException extends Exception {

    public static final int NETWORK_NOT_AVILABLE = -1;
    public static final int SERVER_ERROR = -2;
    public static final int NETWORK_ERROR = -3;
    public static final int USER_NOT_LOGIN = -4;
    
    private static final long serialVersionUID = 1L;
    
    private int mExceptionCode;
    private String mDeveloperExceptionMsg;
    private String mUserExceptionMsg;
    
    public NetWorkException(String exceptionMsg) {
        super(exceptionMsg);
        mDeveloperExceptionMsg = exceptionMsg;
    }
    
    public NetWorkException(int code, String msg, String description) {
        super(msg);
        mExceptionCode = code;
        mDeveloperExceptionMsg = msg;
        mUserExceptionMsg = description;
    }
    
    public int getErrorCode() {
        return mExceptionCode;
    }
    
    public String getDeveloperExceptionMsg() {
        return mDeveloperExceptionMsg;
    }
    
    public String getUserExceptionMsg() {
        return mUserExceptionMsg;
    }
    
    @Override
    public String toString() {
        return "RRException [mExceptionCode=" + mExceptionCode + ", mExceptionMsg=" + mDeveloperExceptionMsg
                + ", mExceptionDescription=" + mUserExceptionMsg + "]";
    }
    
}
