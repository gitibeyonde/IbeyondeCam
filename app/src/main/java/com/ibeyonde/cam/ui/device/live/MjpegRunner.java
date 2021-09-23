package com.ibeyonde.cam.ui.device.live;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibeyonde.cam.databinding.FragmentLiveBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;

public class MjpegRunner implements Runnable {
    private static final String TAG= MjpegRunner.class.getCanonicalName();
    private URL url;
    private InputStream urlStream;
    private Handler handler;
    private FragmentLiveBinding binding;
    public static boolean isRunning = true;
    private static int _timout_count=0;

    private static final int SKIP_HEADER = "Content-Type: image/jpeg\nContent-Length: ".length();

    public MjpegRunner(URL url, Handler handler, FragmentLiveBinding binding) throws IOException {
        this.url = url;
        this.handler = handler;
        this.binding = binding;
        isRunning = true;
        while(start() == false);
    }

    private boolean start() {
        if (isRunning == false)return true;
        URLConnection urlConn = null;
        try {
            urlConn = url.openConnection();
            urlConn.setReadTimeout(5000);
            urlConn.connect();
            urlStream = urlConn.getInputStream();
            Log.i(TAG, "Starting mjpeg");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep((_timout_count < 1000 ? _timout_count++ : _timout_count) * 128);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public synchronized static void stop() {
        isRunning = false;
    }

    public void run() {
        Integer i=0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        while (isRunning) {
            try {
                byte[] imageBytes = retrieveNextImage();
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.cameraLive.setImageBitmap(bmp);
                    }
                });
            }
            catch (IOException e) {
                Log.e(TAG, "failed stream read: " + e);
                e.printStackTrace();
                //reinitialize
                while(start() == false);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "failed stream read: " + e);
                e.printStackTrace();
                //reinitialize
                while(start() == false);
            }
        }
        try {
            if (urlStream != null)
                urlStream.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to close the stream: " + ioe);
            ioe.printStackTrace();
        }
    }


    private byte[] retrieveNextImage() throws IOException, NumberFormatException {
        int currByte = -1;

        StringWriter headerWriter = new StringWriter(128);
        StringWriter boundary = new StringWriter(16);

        //look for boundary --jpgboundary
        while ((currByte = urlStream.read()) > -1) {
            boundary.write(currByte);
            if (boundary.toString().endsWith("--jpgboundary\n")) {
                break;
            }
        }
        //skip headers
        byte[] skipBytes = new byte[SKIP_HEADER];
        int offset = 0;
        int numRead = 0;
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
        System.out.println("Content length = " + content_length);

        // rest is the buffer
        byte[] imageBytes = new byte[content_length];
        offset = 0;
        numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }

        return imageBytes;
    }

}
