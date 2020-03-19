package com.ashish2117.user.streammedia.util;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileImageHandler {

    public static void storeImage(Bitmap image, Context context, String name) {


            File pictureFile = getOutputMediaFile(context, name);
            if (pictureFile == null) {
                Log.d("savedd","gaghghjghd");
                return;
            }
            try {
                pictureFile.delete();
                FileOutputStream fos = new FileOutputStream(pictureFile);
                image.compress(Bitmap.CompressFormat.PNG, 90, fos);
                Log.d("saveddd","gaghghjghd");
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Err", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Err", "Error accessing file: " + e.getMessage());
            }

    }

    private static File getOutputMediaFile(Context context,String name){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/MusicWithFriends/Media/ProfileImages");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        File mediaFile;
        String mImageName=name;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public  static boolean isImageExists(Context context, String name){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/MusicWithFriends/Media/ProfileImages");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return false;
            }
        }

        File mediaFile;
        String mImageName=name;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        if(mediaFile.exists())
           return true;
        return false;
    }

    public static void setImage(String name, ImageView imageView,Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/MusicWithFriends/Media/ProfileImages");


        Bitmap bitmap = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator + name);
        imageView.setImageBitmap(bitmap);
    }

    public static Bitmap getProfileImageBitmap(String name, Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/MusicWithFriends/Media/ProfileImages");


        Bitmap bitmap = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator + name);
        return bitmap;
    }

}
