package com.ibeyonde.cam.ui.device.live;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.ibeyonde.cam.ui.device.lastalerts.DeviceViewModel;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.Camera;
import com.ibeyonde.cam.utils.IpUtils;
import com.ibeyonde.cam.utils.NetUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class MjpegLive implements Runnable {
    private static final String TAG= MjpegLive.class.getCanonicalName();

    private Handler _handler;
    private ImageView _cameraLive;
    public volatile boolean _isRunning = true;
    public volatile boolean _isPaused = false;

    String _client_uuid = null;
    String _device_uuid = null;
    String _username = null;
    NetUtils _net = null;

    public MjpegLive(String device_uuid, Handler handler, ImageView cameraLive) {
        this._client_uuid = LoginViewModel._phoneId;
        this._username = LoginViewModel._username;
        this._handler = handler;
        this._cameraLive = cameraLive;
        this._device_uuid = device_uuid;
        Log.d(TAG, this._client_uuid + ", " + this._username + "," + this._device_uuid);
    }

    public InetSocketAddress getPeerAddress() throws IOException {
        _net = new NetUtils(_username, _client_uuid);
        DatagramPacket DpRcv = _net.register();
        DpRcv = _net.getPeerAddress(_device_uuid);
        return IpUtils.getAddress(DpRcv);
    }

    public synchronized void stop() {
        _isRunning = false;
    }

    public synchronized void pause() {
        _isPaused = true;
    }

    public synchronized void resume() {
        _isPaused = false;
    }

    @Override
    public void run() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        InetSocketAddress peer_address = null;
        while (_isRunning) {
            try {
                if (_net._peer_receive_errors > 5 || peer_address == null){
                    try {
                        peer_address = getPeerAddress();
                        _isPaused = true;
                    } catch (IOException e) {
                        Thread.sleep(2000);
                        continue;
                    }
                }

                byte[] imageBytes = _net.getImageDirect(_device_uuid, "DHINJ", peer_address);
                if (imageBytes==null)continue;
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (_isRunning) {
                            _cameraLive.setImageBitmap(bmp);
                        }
                    }
                });
                Thread.sleep(100);
            } catch (IOException ex) {
                Log.d(TAG,"ERROR :" + ex.getMessage());
            } catch (InterruptedException ex) {
                Log.d(TAG,"ERROR :" + ex.getMessage());
            }
        }
        System.out.println("--------" + NetUtils._peer_receive_errors);
    }


}
