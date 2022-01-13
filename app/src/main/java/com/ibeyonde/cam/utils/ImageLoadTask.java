package com.ibeyonde.cam.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG= ImageLoadTask.class.getCanonicalName();

    private String url;
    private ImageView imageView;
    private static final HashMap<String, Bitmap> s_cache = new HashMap<>();

    public ImageLoadTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            String urlSnippet = url;
            if (url.contains("/com.ibeyonde.cam/") && url.contains(".jpg?")) {
                urlSnippet = url.substring(url.indexOf("/com.ibeyonde.cam/"), url.indexOf(".jpg?"));
            }
            Bitmap myBitmap = s_cache.get(urlSnippet);
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
                s_cache.put(urlSnippet, myBitmap);
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
        if (result == null) return;
        imageView.setImageBitmap(result);
    }

}