package com.ibeyonde.cam.ui.device.live;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;

public class MjpegRunner implements Runnable {
    private static final String TAG= MjpegRunner.class.getCanonicalName();
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE = "Content-type: image/jpeg";
    private final URL url;
    private InputStream urlStream;
    private Queue<Bitmap> queue;
    public boolean isRunning = true; //TODO should be false by default

    public MjpegRunner(URL url, Queue q) throws IOException {
        this.url = url;
        this.queue = q;
        start();
    }

    private void start() throws IOException {
        URLConnection urlConn = url.openConnection();
        urlConn.setReadTimeout(5000);
        urlConn.connect();
        urlStream = urlConn.getInputStream();
    }

    public synchronized void stop() {
        isRunning = false;
    }

    public void run() {
        int i = 0;
        while (isRunning) {
            try {
                byte[] imageBytes = retrieveNextImage();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                Log.d(TAG, "Updating jpeg on viewer " + i++);
                queue.offer(bmp);
            } catch (IOException e) {
                Log.e(TAG, "failed stream read: " + e);
                e.printStackTrace();
                stop();
            }
        }
        try {
            urlStream.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to close the stream: " + ioe);
            ioe.printStackTrace();
        }
    }

    private byte[] retrieveNextImage() throws IOException {
        int currByte = -1;

        String header = null;
        // build headers
        // the DCS-930L stops it's headers

        boolean captureContentLength = false;
        StringWriter contentLengthStringWriter = new StringWriter(128);
        StringWriter headerWriter = new StringWriter(128);

        int contentLength = 0;

        while ((currByte = urlStream.read()) > -1) {
            if (captureContentLength) {
                if (currByte == 10 || currByte == 13) {
                    contentLength = Integer.parseInt(contentLengthStringWriter.toString());
                    break;
                }
                contentLengthStringWriter.write(currByte);

            } else {
                headerWriter.write(currByte);
                String tempString = headerWriter.toString();
                int indexOf = tempString.indexOf(CONTENT_LENGTH);
                if (indexOf > 0) {
                    captureContentLength = true;
                }
            }
        }

        // 255 indicates the start of the jpeg image
        while ((urlStream.read()) != 255) {
            // just skip extras
        }

        // rest is the buffer
        byte[] imageBytes = new byte[contentLength + 1];
        // since we ate the original 255 , shove it back in
        imageBytes[0] = (byte) 255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }

        return imageBytes;
    }

    //    // dirty but it works content-length parsing
//    private static int contentLength(String header) {
//        int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
//        int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
//        int indexOfEOL = header.indexOf('\n', indexOfContentLength);
//
//        String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();
//
//        int retValue = Integer.parseInt(lengthValStr);
//
//        return retValue;
//    }

}
