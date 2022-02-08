package com.ibeyonde.cam.ui.device.live;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.ibeyonde.cam.R;
import com.ibeyonde.cam.ui.login.LoginViewModel;
import com.ibeyonde.cam.utils.IpUtils;
import com.ibeyonde.cam.utils.NetUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class MjpegLive implements Runnable {
    private static final String TAG= MjpegLive.class.getCanonicalName();

    private Handler _handler;
    private Resources _resources;
    private ImageView _cameraLive;
    public volatile boolean _isRunning = true;
    public volatile boolean _isPaused = false;

    String _client_uuid = null;
    String _device_uuid = null;
    String _username = null;
    static NetUtils _net = null;
    int _peer_error = 0;

    public MjpegLive(String device_uuid, Handler handler, Resources resources, ImageView cameraLive) {
        this._client_uuid = LoginViewModel._phoneId;
        this._username = LoginViewModel._username;
        this._handler = handler;
        this._resources = resources;
        this._cameraLive = cameraLive;
        this._device_uuid = device_uuid;
        Log.d(TAG, this._client_uuid + ", " + this._username + "," + this._device_uuid);
    }

    public InetSocketAddress getPeerAddress() throws IOException {
        if(_net!=null)
            _net.close();
        _net = new NetUtils(_username, _client_uuid);
        _net.register();
        DatagramPacket DpRcv = _net.getPeerAddress(_device_uuid);
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
        InetSocketAddress peer = null;

        while (_isRunning) {
            try {
                if (_isPaused) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (_peer_error > 5 || peer == null) {
                    try {
                        peer = getPeerAddress();
                        _peer_error = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Bitmap bmp = BitmapFactory.decodeResource(_resources, R.drawable.error);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                _cameraLive.setImageBitmap(bmp);
                            }
                        });
                        Thread.sleep(1000);
                    }
                    continue;
                }

                byte[] rcv_img = null;

                _net.sendCommandPeer(("DHINJ:" + _device_uuid + ":").getBytes(), peer);
                //_net.sendCommandBroker("HINI:" + _device_uuid + ":");

                DatagramPacket DpRcv = _net.recvCommandPeer(peer);
                String cmd_str = new String(DpRcv.getData());
                if (cmd_str.startsWith("SIZE")) {
                    String uuid_size_str = cmd_str.substring("SIZE:".length());
                    String[] uuid_size = uuid_size_str.split("\\.");
                    int size = Integer.parseInt(uuid_size[1].trim());
                    String cur_uuid = uuid_size[0];
                    rcv_img = _net.recvAllPeer(peer, size);
                    Log.d(TAG, "Size = " + size + " uuid=" + cur_uuid + " bytes " + rcv_img.length);
                    _peer_error = 0;
                }
                else {
                    _net.recvAllPeer(peer, 60000);
                }

                if (rcv_img == null) {
                    continue;
                }

                Bitmap bmp = BitmapFactory.decodeByteArray(rcv_img, 0, rcv_img.length, options);
                if (bmp != null) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            _cameraLive.setImageBitmap(bmp);
                        }
                    });
                }
            } catch (IOException ex) {
                Log.d(TAG,"ERROR IO :" + ex.getMessage());
                _peer_error++;
            } catch (InterruptedException ex) {
                Log.d(TAG,"ERROR INT :" + ex.getMessage());
            }
        }
        System.out.println("--------" + _peer_error);
    }


}
