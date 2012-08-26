package com.beiwai.shuzi.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final boolean DEBUG = true;
    
    private static final int MAX_SIZE = 100 * 1024;
    
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, int padding, int width) {
        int size = width;
        roundPx = size * roundPx / 60;

        Bitmap output = Bitmap.createBitmap(size + padding * 2, size + padding * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final RectF rectF = new RectF(padding, padding, size, size);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xfffffce7);
//        paint.setColor(0xff000000);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap
                    , new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
                    , new Rect(padding, padding, size, size)
                    , paint);
        return output;
    }
	
    public static byte[] getBitmapBytes(Bitmap src) {
        if (src == null)
            return null;
        // ByteBuffer buffer = ByteBuffer.allocate(src.getWidth() *
        // src.getHeight());
        // src.copyPixelsToBuffer(buffer);
        // return buffer.array();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] srcSize = baos.toByteArray();

//        if (srcSize.length > MAX_SIZE) {
//            int quality = ((int) ((((double) MAX_SIZE) / srcSize.length) * 100));
//            if (DEBUG) {
//                Log.d(TAG, "[[getBitmapBytes]] quality = " + quality);
//            }
//            ByteArrayOutputStream baos_new = new ByteArrayOutputStream();
//            src.compress(Bitmap.CompressFormat.JPEG, quality, baos_new);
//            return baos_new.toByteArray();
//        }
        return srcSize;
    }
	
    public static Bitmap loadBitmapWithSizeOrientation(String fileFullPath) {
        if (TextUtils.isEmpty(fileFullPath)) {
            return null;
        }
        
        return loadBitmapWithSizeCheck(new File(fileFullPath), ExifHelper.getRotationFromExif(fileFullPath));
    }
    
    public static Bitmap loadBitmapWithSizeCheck(File bitmapFile, int orientataion) {
        if (DEBUG) {
            Log.d(TAG, "[[loadBitmapWithSizeCheck]] load file from path = " + bitmapFile.getPath());
        }
        
        Bitmap bmp = null;
        FileInputStream fis = null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            if (DEBUG) {
                Log.d(TAG, "[[loadBitmapWithSizeCheck]] bitmap file = " + bitmapFile.getAbsolutePath());
            }
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), opt);
            int width = opt.outWidth;
            int height = opt.outHeight;
            if (DEBUG) {
                Log.d(TAG, "source bitmap size : width =  " + width + " height = " + height);
            }
            BitmapFactory.Options newOpt = new BitmapFactory.Options();
            long fileSize = bitmapFile.length();
            if (fileSize <= MAX_SIZE) {
                newOpt.inSampleSize = 1;
            } else if (fileSize <= MAX_SIZE * 4) {
                newOpt.inSampleSize = 2;
            } else {
                long times = fileSize / MAX_SIZE;
                newOpt.inSampleSize = (int) (Math.log(times) / Math.log(2.0)) + 1;
            }
            newOpt.inDensity = 160;
            newOpt.outHeight = height;
            newOpt.outWidth = width;
            fis = new FileInputStream(bitmapFile);
            bmp = BitmapFactory.decodeStream(fis, null, newOpt);
            
            if (orientataion != 0 && bmp != null) {
                Matrix matrix = new Matrix();
                matrix.postRotate((float) orientataion);
                Bitmap tmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                if (tmp != null) {
                    bmp.recycle();
                    bmp = null;
                    bmp = tmp;
                }
                if (DEBUG) {
                    Log.d(TAG, "[[loadBitmapWithSizeCheck]] rotation = <<<<<<<<<<<< " + orientataion + " >>>>>>>>>>>>>");
                }
            }
            
//            if (DEBUG) {
//                Log.d(TAG, "create bmp size : width = " + bmp.getWidth() + " height = " + bmp.getHeight());
//            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
