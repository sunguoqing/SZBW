package com.beiwai.shuzi.cache;

public abstract class CacheManager <T> {

    public abstract T getResource(String category, String key);
    
    public abstract boolean putResource(String category, String key, T res);
    
    public abstract void releaseResource(String category);
    
    public abstract void clearResource();
}
