package com.ibeyonde.cam.utils;

import android.util.Log;

import com.ibeyonde.cam.ui.device.live.MjpegLive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;

public class NetUtils {
    private static final String TAG= NetUtils.class.getCanonicalName();

    public static final int _broker_port=5020;
    public static int _peer_receive_errors =0;
    public static boolean peer_receive_initial=true;
    public static InetAddress _broker_address;
    private DatagramSocket _sock;
    public static InetAddress  _my_address;
    public static int  _my_port;
    public static String  _my_username;
    public static String  _my_uuid;

    public static final int min_udp_port = 20000;
    public static final int max_udp_port = 65535;

    public ByteBuffer _img_buf = ByteBuffer.allocate(10*1024*1024);

    public NetUtils(String username, String uuid) throws UnknownHostException, SocketException {
        if (uuid==null) throw new IllegalStateException("Invalid uuid");
        _my_username = username;
        _my_uuid = uuid;
        _my_port = getRandomUdpPort();
        _my_address = getLocalIp();
        Log.d(TAG, "Ip=" + _my_address.getHostAddress() + " port=" + _my_port);
        _broker_address = InetAddress.getByName("broker.ibeyonde.com");
        _sock = new DatagramSocket();
        _sock.setReuseAddress(true);
        _sock.setSoTimeout(1000);
        //_sock.setBroadcast(true);
    }
    public static int getRandomUdpPort() {
        return (int)((Math.random() * ((max_udp_port - min_udp_port) + 1)) + min_udp_port);
    }
    public static InetAddress getLocalIp() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress.isSiteLocalAddress() && inetAddress instanceof Inet4Address) {
                    Log.d(TAG, "InetAddress:" + inetAddress);
                    return inetAddress;
                }
            }
        }
        return InetAddress.getLocalHost();
    }
    public DatagramPacket register() throws IOException {
        String cmd_str = new String("REGISTER:" + _my_uuid + ":");
        ByteBuffer cmd = ByteBuffer.allocate(cmd_str.length() + 6);
        cmd.put(cmd_str.getBytes());
        cmd.put(IpUtils.ipToBytes(_my_address.getHostAddress(), _my_port));//192.168.100.11 172.20.10.3
        sendCommandBroker(cmd);
        return recvCommandBroker();
    }

    public DatagramPacket getPeerAddress(String uuid) throws IOException {
        _peer_receive_errors = 0;
        String cmd_str = new String("PADDR:" + uuid + ":");
        ByteBuffer cmd = ByteBuffer.allocate(cmd_str.length());
        cmd.put(cmd_str.getBytes());
        sendCommandBroker(cmd);
        return recvCommandBroker();
    }
    public synchronized void sendCommandBroker(String cmd_str) throws IOException {
        ByteBuffer cmd = ByteBuffer.allocate(cmd_str.length());
        cmd.put(cmd_str.getBytes());
        sendCommandBroker(cmd);
    }
    public synchronized void sendCommandBroker(ByteBuffer cmd) throws IOException {
        Log.d(TAG, "sendCommandBroker " + new String(cmd.array(), StandardCharsets.ISO_8859_1));
        DatagramPacket DpSend =   new DatagramPacket(cmd.array(), cmd.capacity(), _broker_address, _broker_port);
        _sock.send(DpSend);
    }

    public synchronized DatagramPacket recvCommandBroker() throws IOException {
        byte[] buf = new byte[IpUtils.CMDCHUNK];
        DatagramPacket DpRcv = new DatagramPacket(buf, buf.length, _broker_address, _broker_port);
        _sock.receive(DpRcv);
        Log.d(TAG, "recvCommandBroker " + new String(DpRcv.getData()));
        return DpRcv;
    }

    public synchronized void sendCommandPeer(byte[] cmd, InetSocketAddress peer) throws IOException {
        Log.d(TAG, "sendCommandPeer " + new String(cmd) + "__@__" + peer.getHostString() + ":" + peer.getPort());
        DatagramPacket DpSend =   new DatagramPacket(cmd, cmd.length, peer.getAddress(), peer.getPort());
        _sock.send(DpSend);
    }

    public synchronized DatagramPacket recvCommandPeer(InetSocketAddress peer_address) throws IOException {
        DatagramPacket DpRcv = null;
        byte[] buf = new byte[IpUtils.CMDCHUNK];
        DpRcv = new DatagramPacket(buf, buf.length, peer_address.getAddress(), peer_address.getPort());
        _sock.receive(DpRcv);
        return DpRcv;
    }

    public synchronized byte[] recvAllPeer(InetSocketAddress peer, String my_uuid, int size) throws IOException {
        byte[] buf = new byte[size];
        int remaining = size;
        DatagramPacket DpRcv = new DatagramPacket(buf, buf.length, peer.getAddress(), peer.getPort());
        _img_buf.clear();
        while (remaining > 0) {
            _sock.receive(DpRcv);
            _img_buf.put(buf, 0, DpRcv.getLength());
            remaining -= DpRcv.getLength();
            //System.out.print(",r=" + remaining);
        }
        //System.out.println("");
        _img_buf.flip();
        _img_buf.get(buf);
        return buf;
    }

}
