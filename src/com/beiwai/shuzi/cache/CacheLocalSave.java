package com.beiwai.shuzi.cache;

import android.text.TextUtils;

class CacheLocalSave {
    
    private String mType;
    
    CacheLocalSave(String type) {
        if (TextUtils.isEmpty(type)) {
            throw new IllegalArgumentException("argument can't be empty");
        }
        
        mType = type;
    }
    
    boolean saveToLocal(String category, String key, String value) {
        return false;
//        return DataBaseOperator.getInstance().addCacheValue(mType, category, key, value);
    }
    
    String loadFromLocal(String category, String key) {
        return null;
//        return DataBaseOperator.getInstance().queryCacheValue(mType, category, key);
    }
    
    void clearLocal() {
//        DataBaseOperator.getInstance().deleteCacheByType(mType);
    }
}
