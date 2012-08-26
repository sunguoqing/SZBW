package com.beiwai.shuzi.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.beiwai.shuzi.utils.FileOperator;
import com.beiwai.shuzi.utils.ImageUtils;
import com.beiwai.shuzi.utils.StringUtils;

class ImageCacheManager extends CacheManager <Bitmap> {
    private static final boolean DEBUG = true;
    
    private static final String TAG = "ImageCacheManager";
    private static final String CACHE_PATH = ".rr_sound_sns/";
    private static final String FILE_EX_NAME = ".rr_sound_bmp";

    private HashMap<String, HashMap<String, SoftReference<Bitmap>>> mCategoryCache;
    
    private static Object mObj = new Object();
    
    private static ImageCacheManager gImageCacheManager;
    
    public static ImageCacheManager getInstance() {
        if (gImageCacheManager == null) {
            synchronized (mObj) {
                if (gImageCacheManager == null) {
                    gImageCacheManager = new ImageCacheManager();
                }
            }
        }
        
        return gImageCacheManager;
    }

    /*
     * get round bitmap from sdcard or memory 
     */
    @Override
    public Bitmap getResource(String category, String key) {
        Bitmap ret = getBitmapByCategoryAndKey(category, key);
        
        if (ret == null) {
            ret = DiskTools.getBitmapFromDisk(key);
            if (ret != null) {
                cacheBitmapByCategoryAndKey(category, key, ret, false);
            }
        } else {
//            if (Config.LOGD) {
//                Log.d(TAG, "[[getRawBitmapByCategoryAndKey]] get image url = " +  key
//                        + " from cache success");
//            }
        }
        
        return ret;
    }
    
    private Bitmap getBitmapByCategoryAndKey(String category, String key) {
        Bitmap ret = null;
        synchronized (mObj) {
            if (mCategoryCache.containsKey(category)) {
                if (mCategoryCache.get(category).get(key) != null) {
                    ret = mCategoryCache.get(category).get(key).get();
                }
            }
        }
        
        return ret;
    }
    
    @Override
    public boolean putResource(String category, String key, Bitmap bt) {
        return cacheBitmapByCategoryAndKey(category, key, bt, true);
    }
    
    private boolean cacheBitmapByCategoryAndKey(String category, String key, Bitmap bt, boolean saveToDisk) {
        if (DEBUG) {
            Log.d(TAG, "[[cacheBitmapByCategoryAndKey]] category = " + category
                    + " key = " + key + " for bitmap   >>>>>>>>");
        }
        
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(category) 
                || bt == null || bt.isRecycled()) {
            return false;
        }
        
        synchronized (mObj) {
            HashMap<String, SoftReference<Bitmap>> keyMap = mCategoryCache.get(category);
            if (keyMap == null) {
                keyMap = new HashMap<String, SoftReference<Bitmap>>();
                mCategoryCache.put(category, keyMap);
            }
            keyMap.put(key, new SoftReference<Bitmap>(bt));
            
            if (saveToDisk) {
                try {
                    boolean ret = DiskTools.saveRawBitmap(key, ImageUtils.getBitmapBytes(bt));
                    if (!ret) {
                        DiskTools.removeBitmap(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return true;
    }

    public void releaseByCategory(String category, boolean ifRecyle) {
        HashMap<String, SoftReference<Bitmap>> keyMap = null;
        synchronized (mObj) {
            keyMap = mCategoryCache.remove(category);
        }
        if (keyMap != null) {
            if (ifRecyle) {
                for (SoftReference<Bitmap> softBt : keyMap.values()) {
                    if (softBt != null 
                            && softBt.get() != null 
                            && !softBt.get().isRecycled()) {
                        softBt.get().recycle();
                    }
                }
            }
            keyMap.clear();
        }
    }
    
    @Override
    public void releaseResource(String category) {
        releaseByCategory(category, true);
    }
    
    @Override
    public void clearResource() {
        for (String cat : mCategoryCache.keySet()) {
            releaseResource(cat);
        }
        mCategoryCache = null;
        gImageCacheManager = null;
        
        File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File cacheDir = new File(sdcard.getAbsolutePath() + "/" + CACHE_PATH);
        FileOperator.deleteDir(cacheDir);
    }
    
    private ImageCacheManager() {
        mCategoryCache = new HashMap<String, HashMap<String, SoftReference<Bitmap>>>();
    }
    
    private static void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
    
    static class DiskTools {
        
        public static boolean isExistSdcard() {
            boolean isExist = true;
            if (!android.os.Environment.getExternalStorageState().equals( 
                    android.os.Environment.MEDIA_MOUNTED)){
                isExist = false;
            }
            return isExist;
        }
        
        public static Bitmap getBitmapFromDisk(String strFileName) {
            LOGD("[[getBitmapFromDisk]] file name = " + strFileName);
            
            if(TextUtils.isEmpty(strFileName) || !isExistSdcard()) {
                return null;
            }
            
            File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File file = new File(sdcard.getAbsolutePath() + "/" + CACHE_PATH);
            
            String strBmpFile = file.getAbsolutePath() + "/" 
                                    + StringUtils.MD5Encode(strFileName) + FILE_EX_NAME;
            File bmpFile = new File(strBmpFile);
            if(!bmpFile.exists()) {
                LOGD("[[getBitmapFromDisk]] file name = " + strFileName + " <<false>>");
                return null;
            }
            Bitmap bmp = ImageUtils.loadBitmapWithSizeCheck(bmpFile, 0);
//            Bitmap roundBitmap = null;
//            if (bmp != null) {
//                roundBitmap = getRoundedCornerBitmap(bmp, 5.0f);
//                if (bmp != null && !bmp.isRecycled()) {
//                    bmp.recycle();
//                    bmp = null;
//                }
//            }
            LOGD("[[getBitmapFromDisk]] file name = " + strFileName + " <<true>>");
            return bmp;
        }
        
        public static boolean saveRawBitmap(String fileUrl, byte[] src) {
            LOGD("[[saveRawBitmap]] file name = " + fileUrl + " src data = " + src);
            
            if (!isExistSdcard()) {
                return false;
            }
            File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File file = new File(sdcard.getAbsolutePath() + "/" + CACHE_PATH);
            if (!file.exists() || !file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }
            
            return saveBitmapToPath(file.getAbsolutePath() + "/", fileUrl, src);
        }
        
        static void removeBitmap(String fileUrl) {
            if (!isExistSdcard()) {
                return;
            }
            
            File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File file = new File(sdcard.getAbsolutePath() + "/" + CACHE_PATH);
            if (!file.exists() || !file.isDirectory()) {
                file.delete();
                file.mkdirs();
            }
            
            File remove = new File(file.getAbsoluteFile() + "/" + StringUtils.MD5Encode(fileUrl) + FILE_EX_NAME);
            file.delete();
        }
        
        private static boolean saveBitmapToPath(String path, String fileUrl, byte[] src) {
            if (TextUtils.isEmpty(path) || TextUtils.isEmpty(fileUrl) || src == null || src.length == 0) return false;
            
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                return false;
            }
            
            String cache = null;
//            if (DEBUG_FILE_NAME) {
//                cache = path + getFileName(fileUrl) + mFileExName;
//            } else {
                cache = path + StringUtils.MD5Encode(fileUrl) + FILE_EX_NAME;
//            }
            File cacheFile = new File(cache);
            if (cacheFile.exists() && !cacheFile.delete()) {
                return false;
            }
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(cacheFile);
                fos.write(src);
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try { 
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
             
            return true;
        }
    }

}
