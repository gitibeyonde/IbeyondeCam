package com.ibeyonde.cam.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class NetUtils {
    private final static Logger LOG = Logger.getLogger(NetUtils.class.getName());

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

    public ByteBuffer _img_buf = ByteBuffer.allocate(20*1024*1024);

    public NetUtils(String username, String uuid, int port) throws UnknownHostException, SocketException {
        if (uuid==null) throw new IllegalStateException("Invalid uuid");
        _my_username = username;
        _my_uuid = uuid;
        _my_port = port;
        _my_address = InetAddress.getLocalHost();
        _broker_address = InetAddress.getByName("broker.ibeyonde.com");
        _sock = new DatagramSocket();
        _sock.setReuseAddress(true);
        _sock.setSoTimeout(1000);
        //_sock.setBroadcast(true);
    }
    public static int getRandomUdpPort() {
        return (int)((Math.random() * ((max_udp_port - min_udp_port) + 1)) + min_udp_port);
    }
    public DatagramPacket register() throws IOException {
        String cmd_str = new String("REGISTER:" + _my_uuid + ":");
        ByteBuffer cmd = ByteBuffer.allocate(cmd_str.length() + 6);
        cmd.put(cmd_str.getBytes());
        cmd.put(IpUtils.ipToBytes("172.20.10.3", _my_port));
        sendCommandBroker(cmd);
        return recvCommandBroker();
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
        System.out.println("sendCommandBroker " + new String(cmd.array(), StandardCharsets.ISO_8859_1));
        DatagramPacket DpSend =   new DatagramPacket(cmd.array(), cmd.capacity(), _broker_address, _broker_port);
        _sock.send(DpSend);
    }

    public synchronized DatagramPacket recvCommandBroker() throws IOException {
        byte[] buf = new byte[IpUtils.CMDCHUNK];
        DatagramPacket DpRcv = new DatagramPacket(buf, buf.length, _broker_address, _broker_port);
        _sock.receive(DpRcv);
        System.out.println("recvCommandBroker " + new String(DpRcv.getData()));
        return DpRcv;
    }

    public synchronized void sendCommandPeer(byte[] cmd, InetSocketAddress peer) throws IOException {
        System.out.println("sendCommandPeer " + new String(cmd) + "__@__" + peer.getHostString() + ":" + peer.getPort());
        DatagramPacket DpSend =   new DatagramPacket(cmd, cmd.length, peer.getAddress(), peer.getPort());
        _sock.send(DpSend);
    }

    public synchronized DatagramPacket recvCommandPeer(InetSocketAddress peer_address) throws IOException {
        DatagramPacket DpRcv = null;
        try {
            byte[] buf = new byte[IpUtils.CMDCHUNK];
            DpRcv = new DatagramPacket(buf, buf.length, peer_address.getAddress(), peer_address.getPort());
            _sock.receive(DpRcv);
        }
        catch(java.net.SocketTimeoutException e) {
            System.out.println(e.getMessage());
        }
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

    public byte[] getImageDirect(String device_uuid, String quality, InetSocketAddress peer_address) throws IOException {
        byte[] rcv_img = null;
        try {
            sendCommandPeer((quality + ":" + device_uuid + ":").getBytes(), peer_address);
            DatagramPacket DpRcv = recvCommandPeer(peer_address);
            String cmd_str = new String(DpRcv.getData());
            if (cmd_str.startsWith("SIZE")) {
                String uuid_size_str = cmd_str.substring("SIZE:".length());
                String[] uuid_size = uuid_size_str.split("\\.");
                int size = Integer.parseInt(uuid_size[1].trim());
                String cur_uuid = uuid_size[0];
                rcv_img = recvAllPeer(peer_address, device_uuid, size);
                System.out.println("Size = " + size + " uuid=" + cur_uuid + " bytes " + rcv_img.length);
                _peer_receive_errors = 0;
            }
            else  {
                System.out.println("Error " + cmd_str);
                _peer_receive_errors++;
                Thread.sleep(1000);
            }
        }
        catch (SocketTimeoutException | InterruptedException ex) {
            System.out.println("WARNING " + ex.getMessage());
            _peer_receive_errors++;
        }
        return rcv_img;
    }


    // ("192.168.100.17", 23456)
    // Long ip = 3232261137 int port = 23456
    // port short is unsigned short (16 bit, little endian byte order) java short
    // ip long is unsigned long (32 bit, big endian byte order) java int


}
