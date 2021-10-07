package com.ibeyonde.cam.ui.device.live;


import static java.lang.Thread.yield;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentLiveBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;

public class MjpegRunner implements Runnable {
    private static final String TAG= MjpegRunner.class.getCanonicalName();
    private URL url;
    private Handler handler;
    private ImageView cameraLive;
    public static boolean isRunning = true;

    private static final int SKIP_HEADER = "Content-Type: image/jpeg\nContent-Length: ".length();

    public MjpegRunner(URL url, Handler handler, ImageView cameraLive) throws IOException {
        this.url = url;
        this.handler = handler;
        this.cameraLive = cameraLive;
        isRunning = true;
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public InputStream getUrlInputStream() {
        InputStream urlStream = null;
        HttpURLConnection urlConnection = null;
        try {
            Log.i(TAG, "Trying to connect.." + url.toString());
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(5000);
            urlConnection.setAllowUserInteraction(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "close");
            //urlConnection.setRequestProperty("Accept-Encoding", "identity");
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.connect();
            urlStream = urlConnection.getInputStream();
            Log.i(TAG, "Starting mjpeg>>>>>>>");
        } catch (Exception e) {
            Log.e(TAG, "Url connection failed");
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (urlStream != null){
                try {
                    urlStream.close();
                } catch (IOException ioException) {
                    Log.e(TAG, "Unclean stream closure");
                }
                urlStream = null;
            }
        }
        return urlStream;
    }

    public synchronized static void stop() {
        isRunning = false;
    }

    public void run() {
        Integer i=0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        InputStream urlStream = null;
        while (isRunning) {
            if (urlStream == null){
                urlStream = getUrlInputStream();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                byte[] imageBytes = retrieveNextImage(urlStream);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraLive.setImageBitmap(bmp);
                        yield();
                        cameraLive.invalidate();
                    }
                });
            }
            catch (Exception e) {
                Log.e(TAG, "Url connection failed IOException", e);
                try {
                    if (urlStream != null) {
                        urlStream.close();
                        urlStream = null;
                    }
                } catch (Exception ioException) {
                    Log.e(TAG, "Exception while closing resources");
                }
            }
        }
        Log.d(TAG, "Closing Mjpeg thread<<<<<<");
    }


    private byte[] retrieveNextImage(InputStream urlStream) throws IOException, NumberFormatException {
        int currByte;
        StringWriter headerWriter = new StringWriter(128);
        StringWriter boundary = new StringWriter(16);

        //look for boundary --jpgboundary
         while ((currByte = urlStream.read()) > -1) {
            boundary.write(currByte);
            if (boundary.toString().endsWith("--jpgboundary\n")) {
                break;
            }
             if (boundary.toString().endsWith("--jpgboundary--\n")) {
                 throw new IOException("Stream Ends");
             }
        }

        //skip headers
        byte[] skipBytes = new byte[SKIP_HEADER];
        int offset = 0;
        int numRead;
        while (offset < SKIP_HEADER
                && (numRead = urlStream.read(skipBytes, offset, SKIP_HEADER - offset)) >= 0) {
            offset += numRead;
        }

        while ((currByte = urlStream.read()) > -1) {
            if (currByte == '\n')break;
            headerWriter.write(currByte);
        }

        while (urlStream.read() > -1) break;

        int content_length = Integer.parseInt(headerWriter.toString().trim());

        // rest is the buffer
        byte[] imageBytes = new byte[content_length];
        offset = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }
        //Log.d(TAG, "Image length = " + offset);

        return imageBytes;
    }

}
