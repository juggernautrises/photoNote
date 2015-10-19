package com.example.ashoknayar.photonote;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import java.io.IOException;

/**
 * Created by ashok on 4/28/14.
 */
public class ImgHelper {

    public static int getRotation(String filePath){
        try{
            ExifInterface exif = new ExifInterface(filePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
            else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
            else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
            return 0;
        }catch (IOException e){
            Log.e("AshokRoation","IOexception when finding rotation: "+filePath);
            return 0;
        }
    }


    public static Bitmap decodeScaledBitmapFromSdCard(String filePath,
                                                      int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp =  BitmapFactory.decodeFile(filePath, options);
        int rotation = getRotation(filePath);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotation);}
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }


    public static int getImageHeight(String filepath)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        return options.outHeight;
    }

    public static int getImageWidth(String filepath)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        return options.outWidth;
    }

    public static float getPercent(String filepath, float desired_width)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        float percent = desired_width/options.outWidth;
        return percent;
    }
    public static Bitmap resize(String filepath, float desired_width)
    {
        float percent = getPercent(filepath, desired_width);
        int newWidth = Math.round(getImageWidth(filepath) * percent);
        int newHeight = Math.round(getImageHeight(filepath) * percent);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap myBitmap = BitmapFactory.decodeFile(filepath);
        int rotation = getRotation(filepath);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotation);}
        matrix.postScale(percent, percent);
        Bitmap bmp = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        return bmp;

    }
    public static Bitmap resizeToWidth(String filepath, float desired_width)
    {


        float percent = getPercent(filepath, desired_width);
        int newWidth = Math.round(getImageWidth(filepath) * percent);
        int newHeight = Math.round(getImageHeight(filepath) * percent);

        BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filepath, options);


    }

}