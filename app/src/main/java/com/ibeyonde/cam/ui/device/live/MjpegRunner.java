package com.ibeyonde.cam.ui.device.live;


import static java.lang.Thread.yield;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MjpegRunner implements Runnable {
    private static final String TAG= MjpegRunner.class.getCanonicalName();
    private URL url;
    private Handler handler;
    private ImageView cameraLive;
    public static boolean isRunning = true;
    public static boolean isPaused = false;

    private static final int SKIP_HEADER = "Content-Type: image/jpeg\nContent-Length: ".length();

    public MjpegRunner( Handler handler, ImageView cameraLive)  {
        this.handler = handler;
        this.cameraLive = cameraLive;
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.d(TAG, "MjpegRunner constructor");
    }

    public void setURL(URL url){
        this.url = url;
        isRunning = true;
        isPaused = false;
        Log.d(TAG, "MjpegRunner setURL");
    }

    public synchronized static void stop() {
        isRunning = false;
    }

    public synchronized static void pause() {
        isPaused = true;
    }

    public synchronized static void resume() {
        isPaused = false;
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


    public void run() {
        Integer i=0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        InputStream urlStream = null;
        while (isRunning) {
            if (isPaused) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                if (urlStream == null && isRunning){
                    urlStream = getUrlInputStream();
                    try {
                        Thread.sleep(1000);
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
        }
        try {
            if (urlStream != null) {
                urlStream.close();
                urlStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Closing Mjpeg thread<<<<<<");
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
