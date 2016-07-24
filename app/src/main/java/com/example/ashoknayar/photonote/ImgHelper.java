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

    // TODO: Scale based on max(height, width)

    public static Bitmap resizeToMax(String filepath, float max_dimension)
    {
        float desiredScale;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        float reSampleSize;
        if(srcWidth > srcHeight){
            reSampleSize =  srcWidth;
        }
        else{
            reSampleSize = srcHeight;
        }

        // Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
        if(max_dimension > srcWidth)
            max_dimension = srcWidth;

        // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
        // from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
        int inSampleSize = 1;
        while(reSampleSize / 2 > max_dimension){
            reSampleSize /= 2;
            inSampleSize *= 2;
        }

        /*
        if(max_dimension/srcWidth < max_dimension/srcHeight)
        {
            desiredScale = (float) max_dimension / srcWidth;
            //Log.d("nayara- max/width", String.valueOf(desiredScale));
        }
        else
        {
            desiredScale = (float) max_dimension / srcHeight;
            //Log.d("nayara- max/height", String.valueOf(desiredScale));
        }
        */

        desiredScale = max_dimension / reSampleSize;

        // Decode with inSampleSize
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(filepath, options);

        // Resize
        Matrix matrix = new Matrix();
        Log.d("nayara-postscale-MAX ", String.valueOf(desiredScale));
        matrix.postScale(desiredScale, desiredScale);
        int rotation = getRotation(filepath);

        if (rotation != 0f) {matrix.preRotate(rotation);}
        Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
        sampledSrcBitmap = null;
        return scaledBitmap;

    }

    public static Bitmap resize(String filepath, float desiredWidth)
    {
        // Get the source image's dimension
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        int srcWidth = options.outWidth;

        // Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
        if(desiredWidth > srcWidth)
            desiredWidth = srcWidth;

        // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
        // from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
        int inSampleSize = 1;
        while(srcWidth / 2 > desiredWidth){
            srcWidth /= 2;
            inSampleSize *= 2;
        }

        float desiredScale = (float) desiredWidth / srcWidth;
        Log.d("nayara-resize_original", String.valueOf(srcWidth));

        // Decode with inSampleSize
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(filepath, options);

        // Resize
        Matrix matrix = new Matrix();
        matrix.postScale(desiredScale, desiredScale);
        int rotation = getRotation(filepath);

        if (rotation != 0f) {matrix.preRotate(rotation);}
        Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
        sampledSrcBitmap = null;
        return scaledBitmap;
    }

}