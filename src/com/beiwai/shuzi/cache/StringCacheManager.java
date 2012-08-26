package com.beiwai.shuzi.cache;

import java.util.HashMap;

import android.text.TextUtils;

class StringCacheManager extends CacheManager<String> {
    
    private static final boolean SUPPORT_LOCAL_SAVE = true;

    private HashMap<String, HashMap<String, String>> mDataCache;
    private CacheLocalSave mCacheLocalSave;
    
    private static Object mObj = new Object();
    
    private static StringCacheManager gCacheManager;
    
    public static StringCacheManager getInstance() {
        if (gCacheManager == null) {
            synchronized (mObj) {
                if (gCacheManager == null) {
                    gCacheManager = new StringCacheManager();
                }
            }
        }
        
        return gCacheManager;
    }
    
    private StringCacheManager() {
        mDataCache = new HashMap<String, HashMap<String, String>>();
        mCacheLocalSave = new CacheLocalSave(String.class.getName());
    }
    
    @Override
    public String getResource(String category, String key) {
        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(key)) {
            return null;
        }
        
        String ret = getResourceInternal(category, key);
        if (SUPPORT_LOCAL_SAVE && ret == null) {
            ret = mCacheLocalSave.loadFromLocal(category, key);
            if (ret != null) {
                this.putResource(category, key, ret);
            }
        }
        
        return ret;
    }
    
    private String getResourceInternal(String category, String key) {
        synchronized (mObj) {
            if (mDataCache.containsKey(category)) {
                if (mDataCache.get(category).containsKey(key)) {
                    return mDataCache.get(category).get(key);
                }
            }
        }
        return null;
    }

    @Override
    public boolean putResource(String category, String key, String res) {
        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(key) 
                || TextUtils.isEmpty(res)) {
            return false;
        }
        
        synchronized (mObj) {
            HashMap<String, String> map = mDataCache.get(category);
            if (map == null) {
                map = new HashMap<String, String>();
                mDataCache.put(category, map);
            }
            map.put(key, res);
        }
        
        if (SUPPORT_LOCAL_SAVE) {
            mCacheLocalSave.saveToLocal(category, key, res);
        }
        
        return true;
    }

    @Override
    public void releaseResource(String category) {
        if (TextUtils.isEmpty(category))
        mDataCache.remove(category);
    }
    
    @Override
    public void clearResource() {
        mDataCache.clear();
        mDataCache = null;
        mCacheLocalSave = null;
        gCacheManager = null;
        
        mCacheLocalSave.clearLocal();
    }
}
