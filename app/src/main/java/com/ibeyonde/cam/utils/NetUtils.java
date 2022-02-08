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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;

public class NetUtils {
    private static final String TAG= NetUtils.class.getCanonicalName();

    public static final int _broker_port=5020;
    public static InetAddress _broker_address;
    private static DatagramSocket _sock;
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
        _my_address = getLocalIp();
        _my_port = getRandomUdpPort();
        Log.d(TAG, "My Ip=" + _my_address.getHostAddress() + " port=" + _my_port);
        _broker_address = InetAddress.getByName("broker.ibeyonde.com");
        _sock = new DatagramSocket(_my_port, _my_address);
        //
        // _sock.setReuseAddress(true);
        _sock.setSoTimeout(400);
        _sock.setTrafficClass(4);
    }
    public static int getRandomUdpPort() {
        return (int)((Math.random() * ((max_udp_port - min_udp_port) + 1)) + min_udp_port);
    }
    public static InetAddress getLocalIp() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        ArrayList<InetAddress> validIps = new ArrayList<>();
        InetAddress preferredIp = null;//
        for (NetworkInterface netint : Collections.list(nets)){
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                Log.d(TAG, "InetAddress:" + inetAddress);
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    Log.d(TAG, "---:" + inetAddress);
                    validIps.add(inetAddress);
                }
            }
        }
        for (InetAddress inetAddress : validIps) {
            if(inetAddress.isSiteLocalAddress()){
                preferredIp = inetAddress;
            }
        }
        if (preferredIp == null){
            for (InetAddress inetAddress : validIps) {
                if(inetAddress.isLinkLocalAddress()){
                    preferredIp = inetAddress;
                }
            }
        }
        if (preferredIp == null){
            for (InetAddress inetAddress : validIps) {
                if(inetAddress.isAnyLocalAddress()){
                    preferredIp = inetAddress;
                }
            }
        }
        if (preferredIp == null){
            preferredIp = InetAddress.getLocalHost();
        }
        return preferredIp;
    }
    public DatagramPacket register() throws IOException {
        String cmd_str = new String("REGISTER:" + _my_uuid + ":");
        ByteBuffer cmd = ByteBuffer.allocate(cmd_str.length() + 6);
        cmd.put(cmd_str.getBytes());
        cmd.put(IpUtils.ipToBytes(_my_address.getHostAddress(), _my_port));//192.168.100.11 172.20.10.3
        sendCommandBroker(cmd);
        return recvCommandBroker();
    }
    public void close(){
        if (_sock!=null)
            _sock.close();
    }
    public DatagramPacket getPeerAddress(String uuid) throws IOException {
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
    public synchronized void sendCommandPeer(byte[] cmd, InetSocketAddress peer) throws IOException {
        Log.d(TAG, "sendCommandPeer " + new String(cmd) + "__@__" + peer.getHostString() + ":" + peer.getPort());
        DatagramPacket DpSend =   new DatagramPacket(cmd, cmd.length, peer.getAddress(), peer.getPort());
        _sock.send(DpSend);
    }

    public synchronized DatagramPacket recvCommandBroker() throws IOException {
        byte[] buf = new byte[IpUtils.CMDCHUNK];
        Arrays.fill(buf, (byte) -1);
        DatagramPacket DpRcv = new DatagramPacket(buf, buf.length, _broker_address, _broker_port);
        _sock.receive(DpRcv);
        Log.d(TAG, "recvCommandBroker " + new String(DpRcv.getData()));
        return DpRcv;
    }

    public synchronized DatagramPacket recvCommandPeer(InetSocketAddress peer) throws IOException {
        byte[] buf = new byte[IpUtils.CMDCHUNK];
        Arrays.fill(buf, (byte) -1);
        DatagramPacket DpRcv = new DatagramPacket(buf, buf.length, peer.getAddress(), peer.getPort());
        _sock.receive(DpRcv);
        Log.d(TAG, "recvCommandPeer " + new String(DpRcv.getData()));
        return DpRcv;
    }

    public synchronized byte[] recvAllPeer(InetSocketAddress peer, int size) throws IOException {
        byte[] buf = new byte[size];
        Arrays.fill(buf, (byte) -1);
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
