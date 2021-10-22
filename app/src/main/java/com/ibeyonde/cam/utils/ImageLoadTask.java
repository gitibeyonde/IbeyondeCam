package com.ibeyonde.cam.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG= ImageLoadTask.class.getCanonicalName();

    private String url;
    private ImageView imageView;
    private static MessageDigest digest;
    private static final HashMap<String, Bitmap> s_cache = new HashMap<>();

    public ImageLoadTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            digest.update(url.getBytes());
            String messageDigest = new String(digest.digest());
            Bitmap myBitmap = s_cache.get(messageDigest);
            if (myBitmap != null){
                return myBitmap;
            }
            else {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setConnectTimeout(2000);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
                s_cache.put(messageDigest, myBitmap);
                input.close();
                connection.disconnect();
                return myBitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "doInBackground connection issue" ,e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);
    }

}