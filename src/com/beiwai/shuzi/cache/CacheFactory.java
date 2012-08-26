package com.beiwai.shuzi.cache;

public class CacheFactory {

    public enum TYPE_CACHE {
        TYPE_IMAGE,
        TYPE_STRING,
        TYPE_FILE
    }
    
    public static CacheManager getCacheManager(TYPE_CACHE type) {
        switch (type) {
        case TYPE_IMAGE:
            return ImageCacheManager.getInstance();
        case TYPE_STRING:
            return StringCacheManager.getInstance();
        case TYPE_FILE:
            //TODO: should add file cache
            break;
        }
        
        throw new IllegalArgumentException("Cache type not supported");
    }
    
}
