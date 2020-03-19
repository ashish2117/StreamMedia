package com.ashish2117.user.streammedia.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;
    Context context;
    String fileName;

    public ImageLoadTask(String url, ImageView imageView, Context context, String fileName) {
        this.url = url;
        this.imageView = imageView;
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            ProfileImageHandler.storeImage(myBitmap,context,fileName);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);
        SharedPreferences preferences = context.getSharedPreferences("ProfilePicUris",Context.MODE_PRIVATE);
        String oldurl = preferences.getString(fileName,"null");
        if(!oldurl.equals(url)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(fileName,url);
            editor.commit();
        }
    }

}
