/*
 * @Description Read and Write EXIF information with Sanselan library
 * Considering the same EXIF interface in later Android version, KXExifInterface
 * class takes the same method name and parameter.
 * @author Miaoshunping
 * @create 2010-09-21
 */
package com.beiwai.shuzi.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.RationalNumber;
import org.apache.sanselan.common.RationalNumberUtilities;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import android.util.Log;

class RRExifInterface {
    public static final TagInfo TAG_ORIENTATION = TiffConstants.EXIF_TAG_ORIENTATION;

    /**
     * TAG_DATETIME String Format: YY:MM:DD HH:mm:ss
     */
    public static final TagInfo TAG_DATETIME = TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL;

    public static final TagInfo TAG_MAKE = TiffConstants.EXIF_TAG_MAKE;

    public static final TagInfo TAG_MODEL = TiffConstants.EXIF_TAG_MODEL;

    public static final TagInfo TAG_FLASH = TiffConstants.EXIF_TAG_FLASH;

    public static final TagInfo TAG_IMAGE_WIDTH = TiffConstants.EXIF_TAG_IMAGE_WIDTH_IFD0;

    public static final TagInfo TAG_IMAGE_LENGTH = TiffConstants.EXIF_TAG_IMAGE_HEIGHT_IFD0;

    /**
     * TAG_GPS_LATITUDE String Format: 000.0000000
     */
    public static final TagInfo TAG_GPS_LATITUDE = TiffConstants.GPS_TAG_GPS_LATITUDE;

    /**
     * GPS_TAG_GPS_LONGITUDE String Format: 000.0000000
     */
    public static final TagInfo TAG_GPS_LONGITUDE = TiffConstants.GPS_TAG_GPS_LONGITUDE;

    /**
     * GPS_TAG_GPS_LATITUDE_REF String Format: 'N' = North 'S' = South
     */
    public static final TagInfo TAG_GPS_LATITUDE_REF = TiffConstants.GPS_TAG_GPS_LATITUDE_REF;

    /**
     * GPS_TAG_GPS_LONGITUDE_REF String Format: 'E' = East 'W' = West
     */
    public static final TagInfo TAG_GPS_LONGITUDE_REF = TiffConstants.GPS_TAG_GPS_LONGITUDE_REF;
      
    public static final TagInfo TAG_WHITE_BALANCE = TiffConstants.EXIF_TAG_WHITE_BALANCE_1;
    
    private File mJpegFile = null;
    private IImageMetadata mMetadata = null;
    private JpegImageMetadata mJpegMetadata = null;
    private TiffImageMetadata mExif = null;
    private TiffOutputSet mOutputSet = null;
    /**
     * Saved GpsVersion Field Data or not yet
     */
    private boolean mGpsVersionSaved = false;
    /**
     * Controls the debug message output<br>
     * If you want to output all debug message, set it to true.<br>
     * Default value is false.
     */
    private final static boolean DEBUG = false;
    
    public RRExifInterface(String filename) throws java.io.IOException
    {
        mJpegFile = new File(filename);
        if (!mJpegFile.exists())
        {
            throw new IOException("File: " + filename+ " does not exist!");
        }
        
        Log.e("KXExifInterface Create", "File Name = " + filename + "\n");

        try {  
            mMetadata = Sanselan.getMetadata(mJpegFile);
        } catch (ImageReadException e) {
            if (RRExifInterface.DEBUG)
            {
                e.printStackTrace();
            }
        } catch (IOException e) {  
            if (RRExifInterface.DEBUG)
            {
                e.printStackTrace();  
            }
        }  
        
        if (null == mMetadata)
        {
            mOutputSet = new TiffOutputSet();
        }
        else if (mMetadata instanceof JpegImageMetadata) {  
            mJpegMetadata = (JpegImageMetadata) mMetadata;  
            
            // simple interface to GPS data  
            TiffImageMetadata mExif = mJpegMetadata.getExif();  
            if (mExif != null) {  
                try
                {
                    mOutputSet = mExif.getOutputSet();
                }
                catch (ImageWriteException e)
                {
                    e.printStackTrace();
                }
            }  
        } 
        
        Log.e("KXExifInterface Create", 
                mMetadata == null ? "metadata = null" : "metadata != null");
    }   
    
    /**
     * For Debug use only<br>
     * Output the information of exif by Logcat
     * @param exif Stores the EXIF data
     */
    public static void ShowExif(RRExifInterface exif)
    {
        try
        {
            String myAttribute="Exif information ---\n";
            myAttribute += getTagString(RRExifInterface.TAG_DATETIME, exif);
            myAttribute += getTagString(RRExifInterface.TAG_FLASH, exif);
            myAttribute += getTagString(RRExifInterface.TAG_GPS_LATITUDE, exif);
            myAttribute += getTagString(RRExifInterface.TAG_GPS_LATITUDE_REF, exif);
            myAttribute += getTagString(RRExifInterface.TAG_GPS_LONGITUDE, exif);
            myAttribute += getTagString(RRExifInterface.TAG_GPS_LONGITUDE_REF, exif);
            myAttribute += getTagString(RRExifInterface.TAG_IMAGE_LENGTH, exif);
            myAttribute += getTagString(RRExifInterface.TAG_IMAGE_WIDTH, exif);
            myAttribute += getTagString(RRExifInterface.TAG_MAKE, exif);
            myAttribute += getTagString(RRExifInterface.TAG_MODEL, exif);
            myAttribute += getTagString(RRExifInterface.TAG_ORIENTATION, exif);
            myAttribute += getTagString(RRExifInterface.TAG_WHITE_BALANCE, exif);
            
            Log.e("KXExifInterface ShowExif>>>", myAttribute);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static String getTagString(TagInfo tag, RRExifInterface exif)
    {
        return(tag + " : >" + exif.getAttribute(tag) + "<\n");
    }

    private static boolean copyFile(File srcFile, File destFile) throws IOException{ 
        boolean result = false; 
        try { 
            InputStream in = new FileInputStream(srcFile); 
            try { 
                result = copyToFile(in, destFile); 
            } finally  { 
                in.close(); 
            } 
        } catch (IOException e) { 
            result = false; 
        } 
        return result; 
    } 
    
    private static boolean copyToFile (InputStream inputStream, File destFile) { 
        try { 
            if (destFile.exists()) { 
                destFile.delete(); 
            } 
            OutputStream out = new FileOutputStream(destFile); 
            try { 
                byte[] buffer = new byte[4096]; 
                int bytesRead; 
                while ((bytesRead = inputStream.read(buffer)) >= 0) { 
                    out.write(buffer, 0, bytesRead); 
                } 
            } finally { 
                out.close(); 
            } 
            return true; 
        } catch (IOException e) { 
            return false; 
        } 
    }
    
    /**
     * Save all the change of exif to file
     * @throws java.io.IOException
     */
    public void saveAttributes() throws java.io.IOException
    {
        File tmp = null;
        OutputStream os = null;
        
        if (null == mJpegFile)
        {
            throw new IOException("File has not open yet!");
        }

        if (!mJpegFile.canWrite())
        {
            throw new IOException("Application has no permission to write image file!");
        }

        if (null == mOutputSet)
        {
            throw new IOException("pic does not have OutputSet!");
        }

        String tmpFileName = "temp-" + System.currentTimeMillis();
        // create stream using temp file for dst  
        try {  
            tmp = File.createTempFile(tmpFileName, ".jpg");
            os = new FileOutputStream(tmp);  
            os = new BufferedOutputStream(os);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  

        // write/update EXIF metadata to output stream  
        try {  
            new ExifRewriter().updateExifMetadataLossless(mJpegFile,  
                os, mOutputSet);  
        } catch (ImageReadException e) {  
            e.printStackTrace();  
        } catch (ImageWriteException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (os != null) {  
                try {  
                    os.close();  
                } catch (IOException e) {  
                }  
            }  
        }  

        // copy temp file over original file  
        try {
            if (tmp.length() > 0) {
                copyFile(tmp, mJpegFile);
            }
            tmp.delete();
        } catch (IOException e) {  
            e.printStackTrace();  
        }
    }
    
    /**
     * Description Stores the latitude and longitude value in a float array. <br>
     * The first element is the latitude, and the second element 
     * is the longitude. <br>
     * Returns false if the Exif tags are not available. <br>
     * @param output float array to receive the latitude and longitude
     * @return return false if there is no latitude and longitude information
     */
    public boolean getLatLong(float[] output)
    {
        if (null == output || null == mExif)
        {
            return false;
        }
        
        if (output.length < 2)
        {
            return false;
        }
        
        // simple interface to GPS data 
        try {  
            TiffImageMetadata.GPSInfo gpsInfo = mExif.getGPS();  
            if (null != gpsInfo) {  
                double longitude = gpsInfo.getLongitudeAsDegreesEast();  
                double latitude = gpsInfo.getLatitudeAsDegreesNorth();  
                output[0] = (float)latitude;
                output[1] = (float)longitude;
                
                return true;
            }  
        } catch (ImageReadException e) {  
            e.printStackTrace();  
        }
        
        return false;
    }
    
    /**
     * Description: Notice the string format of each value.<br>
     * TAG_DATETIME String Format: YY:MM:DD HH:mm:ss<br>
     * TAG_GPS_LATITUDE String Format: 000.0000000<br>
     * TAG_GPS_LONGITUDE String Format: 000.0000000<br>
     * TAG_GPS_LATITUDE_REF String Format: 'N' = North 'S' = South<br>
     * TAG_GPS_LONGITUDE_REF String Format: 'E' = East 'W' = West
     * 
     * @param tag stores the tag information
     * @param value stores the string of each field
     */
    public void setAttribute(TagInfo tag, String value)
    {
        if (null == mOutputSet || null == tag || null == value) 
        {
            return;
        }
        
        if (tag.tag == TAG_MAKE.tag
                || tag.tag == TAG_MODEL.tag
                || tag.tag == TAG_IMAGE_WIDTH.tag
                || tag.tag == TAG_IMAGE_LENGTH.tag)
        {
            setRootAttribute(tag, value);
        }
        else if (tag.tag == TAG_GPS_LATITUDE.tag
                || tag.tag == TAG_GPS_LATITUDE_REF.tag
                || tag.tag == TAG_GPS_LONGITUDE.tag
                || tag.tag == TAG_GPS_LONGITUDE_REF.tag)
        {
            setGpsAttribute(tag, value);
        }
        else
        {
            setExifAttribute(tag, value);
        }
    }
    
    private void setRootAttribute(TagInfo tag, String value)
    {
        if (null == mOutputSet || null == tag || null == value) 
        { 
            return;
        }
        
        if (tag.tag != TAG_MAKE.tag
                && tag.tag != TAG_MODEL.tag
                && tag.tag != TAG_IMAGE_WIDTH.tag
                && tag.tag != TAG_IMAGE_LENGTH.tag)
        {
            return;
        }

        // get the Root directory
        TiffOutputDirectory directory = null; 
        try { 
            directory = mOutputSet.getOrCreateRootDirectory();
        } catch (ImageWriteException e) {  
            e.printStackTrace();  
            return;
        }
        if (null == directory)
        {
            return;
        }
                  
        TiffOutputField field = new TiffOutputField(tag, 
                TagInfo.FIELD_TYPE_ASCII, 
                value.length() + 1, 
                getBytesWithZeroEnding(value)); 
        
        // remove old field data
        directory.removeField(tag.tag);
        // add field  
        directory.add(field);
    }
    
    private void setExifAttribute(TagInfo tag, String value)
    {
        if (null == mOutputSet || null == tag || null == value) 
        { 
            return;
        }
        
        if (tag.tag != TAG_ORIENTATION.tag
                && tag.tag != TAG_DATETIME.tag
                && tag.tag != TAG_FLASH.tag
                && tag.tag != TAG_WHITE_BALANCE.tag)
        {
            return;
        }
        
        // get the Exif directory
        TiffOutputDirectory directory = null; 
        try { 
            directory = mOutputSet.getOrCreateExifDirectory();
        } catch (ImageWriteException e) {  
            e.printStackTrace();  
            return;
        }
        if (null == directory)
        {
            return;
        }

        TiffOutputField field = new TiffOutputField(tag, 
                TagInfo.FIELD_TYPE_ASCII, 
                value.length() + 1, 
                getBytesWithZeroEnding(value)); 
        // remove old field data
        directory.removeField(tag.tag);
        // add field  
        directory.add(field);       
    }
    
    private void setGpsAttribute(TagInfo tag, String value)
    {
        if (null == mOutputSet || null == tag || null == value) 
        { 
            return;
        }

        if (tag.tag != TAG_GPS_LATITUDE.tag
                && tag.tag != TAG_GPS_LATITUDE_REF.tag
                && tag.tag != TAG_GPS_LONGITUDE.tag
                && tag.tag != TAG_GPS_LONGITUDE_REF.tag)
        {
            return;
        }

        // get the GPS directory
        TiffOutputDirectory gpsDirectory = null;
        try { 
            gpsDirectory = mOutputSet.getOrCreateGPSDirectory();
        } catch (ImageWriteException e) {  
            e.printStackTrace(); 
            return;
        }
        if (null == gpsDirectory)
        {
            return;
        }

        if (!mGpsVersionSaved)
        {
            mGpsVersionSaved = true;
            TagInfo tagGpsVersion = new TagInfo("GPSVersionID", 0x0000, 
                    TiffFieldTypeConstants.FIELD_TYPE_BYTE);
            TiffOutputField gpsversion = new TiffOutputField(tagGpsVersion, 
                    TiffFieldTypeConstants.FIELD_TYPE_BYTE, 
                    4, new byte[]{2, 2, 0, 0});
            gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_VERSION_ID);
            gpsDirectory.add(gpsversion);

        }

        TiffOutputField field = null;

        if (tag.tag == TAG_GPS_LATITUDE.tag || tag.tag == TAG_GPS_LONGITUDE.tag)
        {
            double dValue = 0.0;
            double dMinute = 0.0;
            double dSecond = 0.0;

            try
            {
                dValue = Double.parseDouble(value);
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
                return;
            }
            dMinute = (dValue - (int) dValue) * 60d;
            dSecond = (dMinute - (int) dMinute) * 60d;
            try
            {
                field = new TiffOutputField(tag, 
                        TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, 
                        3, 
                        getRationalArray((int) dValue, (int) dMinute, dSecond));
            }
            catch (ImageWriteException e)
            {
                e.printStackTrace();
                return;
            }
        }
        else // TAG_GPS_LATITUDE_REF & TAG_GPS_LONGITUDE_REF
        {
            value = value.toUpperCase();
            if (tag.tag == TAG_GPS_LATITUDE_REF.tag)
            {
                if (!value.equals("S"))
                {
                    value = "N";
                }
                else
                {
                    value = "S";
                }
            }
            else
            {
                if (!value.equals("W"))
                {
                    value = "E";
                }
                else
                {
                    value = "W";
                }
            }
            field = new TiffOutputField(tag, 
                    TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
                    value.length() + 1, 
                    getBytesWithZeroEnding(value));
        }
        
        // remove old field data
        gpsDirectory.removeField(tag.tag);
        // add field   
        gpsDirectory.add(field);
    }
    
    private static byte[] getRationalArray(double a, double b, double c) throws ImageWriteException {
        TagInfo taginfo = new TagInfo("GPSLongitude", 0x0004, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL);

        RationalNumber r1 = RationalNumberUtilities.getRationalNumber(a);
        RationalNumber r2 = RationalNumberUtilities.getRationalNumber(b);
        RationalNumber r3 = RationalNumberUtilities.getRationalNumber(c);
        List<Byte> bytes = new ArrayList<Byte>();
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r1, 'M')) {
            bytes.add(aByte);
        }
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r2, 'M')) {
            bytes.add(aByte);
        }
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r3, 'M')) {
            bytes.add(aByte);
        }
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return bytesArray;
    }

    public String getAttribute(TagInfo tag)
    {
        if (null == mJpegMetadata) {
            return null;
        }
        TiffField field = mJpegMetadata.findEXIFValue(tag);  
        if (null == field)
        {
            return null;
        } 
        
        String strValue = field.getValueDescription();
        if (null == strValue)
        {
            return null;
        }
        
        if (tag.tag == TAG_GPS_LATITUDE_REF.tag)
        {
            strValue = strValue.toUpperCase();
            if (0 <= strValue.indexOf("N"))
            {
                strValue = "N";
            }
            else // Default value is "N"
            {
                strValue = "N";
            }
        }
        else if (tag.tag == TAG_GPS_LONGITUDE_REF.tag)
        {
            strValue = strValue.toUpperCase();
            if (0 <= strValue.indexOf("W"))
            {
                strValue = "W";
            }
            else // Default value is "E"
            {
                strValue = "E";
            }
        }
        else if (tag.tag == TAG_GPS_LATITUDE.tag 
                || tag.tag == TAG_GPS_LONGITUDE.tag)
        {
            if (strValue.equals(""))
            {
                return null;
            }
            
            try
            {
                double dValue = parseLonLatString(strValue);                
                return String.valueOf(dValue);
            }
            catch (Exception e)
            {
                if (RRExifInterface.DEBUG)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }
        else
        {
            // remove the beginning "'" character
            if (strValue.startsWith("'"))
            {
                strValue = strValue.substring(1);
            }

            // remove the ending "'" character
            if (strValue.endsWith("'"))
            {
                strValue = strValue.substring(0, strValue.length() - 1);
            }
        }
        return strValue;
    }
    
    private byte[] getBytesWithZeroEnding(String str)
    {
        if (null == str)
        {
            return new byte[0];
        }
        
        int nLen = str.length();
        byte[] bts = new byte[nLen + 1];
        byte[] btStr = str.getBytes();
        for (int i = 0; i < nLen; i++)
        {
            bts[i] = btStr[i];
        }
        bts[nLen] = 0;
        
        return bts;
    }
    
    /**
     * Description: Parse Longitude and Latitude string into double value.<br>
     *  Longitude and Latitude String Format: DDD, MM, XXXXX/625 (SS.SSS)<br>
     *  For example: 116, 7, 15276/625 (24.442)<br>
     *  The number of Longitude or Latitude is <br>
     *          DDD + MM / 60.0 + XXXXX / 625.0 / 3600.0, or<br>
     *          DDD + MM / 60.0 + SS.SSS / 3600.0,<br>
     *  We take the first method for accuracy.<br>
     * @param strValue Longitude or Latitude string
     * @return return the double value of Longitude or Latitude
     * @throws Exception
     */
    private double parseLonLatString(String strValue) throws Exception
    {
        if (null == strValue || strValue.equals(""))
        {
            throw new Exception("LonLat String is empty!");
        }
        
        try
        {
            // Longitude and Latitude String Format: DDD, MM, XXXXX/625 (SS.SSS)
            // For example: 116, 7, 15276/625 (24.442)
            // The number of Longitude or Latitude is DDD + MM / 60.0 + XXXXX / 625.0 / 3600.0, or
            //                                        DDD + MM / 60.0 + SS.SSS / 3600.0,
            // We take the first method for accuracy.
            // Firstly, split the string into 3 parts
            Log.e("LONLATSTR>>>", ">>>" + strValue + "<<<\n");
            String[] strNums = strValue.split(",");
            if (strNums.length != 3)
            {
                throw new Exception("LonLat String does not have 3 part!");
            }
            
            // Secondly, get the Degree and Minute data
            double dValue = Double.parseDouble(strNums[0].trim()) 
                + Double.parseDouble(strNums[1].trim()) / 60.0;
            
            // Thirdly, get the Second data
            int nPos = strNums[2].indexOf("/");
            int nEnd = strNums[2].indexOf("(");
            if (0 >= nPos || 0 >= nEnd || nPos >= nEnd)
            {
                return dValue;
            }
            
            double dTmp = Double.parseDouble(strNums[2].substring(0, nPos).trim()) 
                / Double.parseDouble(strNums[2].substring(nPos + 1, nEnd).trim());
            
            dValue = dValue + dTmp / 3600.0;
            
            // Finally, return the double data
            return dValue;
        }
        catch (Exception e)
        {
            if (RRExifInterface.DEBUG)
            {
                e.printStackTrace();
            }
            throw e;
        }
    }
}
