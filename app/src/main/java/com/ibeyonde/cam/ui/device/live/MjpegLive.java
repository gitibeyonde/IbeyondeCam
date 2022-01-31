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
import java.net.SocketTimeoutException;

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
        InetSocketAddress peer_address = null;
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
                if (_net._peer_receive_errors > 5 || peer_address == null) {
                    try {
                        peer_address = getPeerAddress();
                    } catch (IOException e) {
                        Thread.sleep(1000);
                        continue;
                    }
                }

                byte[] rcv_img = null;

                _net.sendCommandPeer(("DHINJ:" + _device_uuid + ":").getBytes(), peer_address);
                //_net.sendCommandBroker("HINI:" + _device_uuid + ":");
                Thread.sleep(100);

                DatagramPacket DpRcv = _net.recvCommandPeer(peer_address);
                String cmd_str = new String(DpRcv.getData());
                if (cmd_str.startsWith("SIZE")) {
                    String uuid_size_str = cmd_str.substring("SIZE:".length());
                    String[] uuid_size = uuid_size_str.split("\\.");
                    int size = Integer.parseInt(uuid_size[1].trim());
                    String cur_uuid = uuid_size[0];
                    rcv_img = _net.recvAllPeer(peer_address, _device_uuid, size);
                    Log.d(TAG, "Size = " + size + " uuid=" + cur_uuid + " bytes " + rcv_img.length);
                }
                else {
                    Log.d(TAG, cmd_str);
                }

                if (rcv_img == null) {
                    continue;
                }

                Bitmap bmp = BitmapFactory.decodeByteArray(rcv_img, 0, rcv_img.length, options);
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (_isRunning) {
                            _cameraLive.setImageBitmap(bmp);
                        }
                    }
                });
            } catch (IOException ex) {
                Log.d(TAG,"ERROR :" + ex.getMessage());
                _net._peer_receive_errors++;
            } catch (InterruptedException ex) {
                Log.d(TAG,"ERROR :" + ex.getMessage());
            }
        }
        System.out.println("--------" + NetUtils._peer_receive_errors);
    }


}
