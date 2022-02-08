package com.ibeyonde.cam.utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.logging.Logger;

public class IpUtils {
    private final static Logger LOG = Logger.getLogger(IpUtils.class.getName());

    public static final int CMDCHUNK=32;
    public static final int PORT_START=20000;
    public static final int PORT_END=65534;
    public static final int IMGCHUNK=1024;
    private final static char[] hexArray = "0123456789abcdef".toCharArray();


    public static String longToIp(long ip) {
        StringBuilder result = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            result.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                result.insert(0, '.');
            }
            ip = ip >> 8;
        }
        return result.toString();
    }

    public static long ipToLong(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);
        }
        return result;
    }

    public static String bytesGetIp(byte[] bt) {
        if (bt.length != 6) {
            LOG.warning("byteToIp bad byte array " + bt.length);
            return null;
        }
        long ip;
        byte[] arr1 = {0, 0, 0, 0, bt[0], bt[1],  bt[2], bt[3] };
        ByteBuffer bb = ByteBuffer.wrap(arr1);
        ip = bb.getLong();
        return longToIp(ip);
    }
    public static int bytesGetPort(byte[] bt) {
        if (bt.length != 6) {
            LOG.warning("byteToIp bad byte array " + bt.length);
            return -1;
        }
        return (int)(((bt[5] & 0xFF) << 8) | (bt[4] & 0xFF));
    }
    public static byte[] shortToLittleEndianBytes(int s) {
        return new byte[] { (byte)(s & 0xFF), (byte)(s >> 8 & 0xFF) };
    }
    public static byte[] longTo4Bytes(long l) {
        byte[] lb = new byte[] { (byte)(l & 0xFF), (byte)(l >>=8 & 0xFF), (byte)(l >>=8 & 0xFF), (byte)(l >>=8 & 0xFF) };
        for(int i=0; i<lb.length/2; i++){
            byte temp = lb[i];
            lb[i] = lb[lb.length -i -1];
            lb[lb.length -i -1] = temp;
        }
        return lb;
    }
    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
    public static synchronized byte[] ipToBytes(String ip, int port) {
        long ipl = ipToLong(ip);
        byte[] ipb = longTo4Bytes(ipl);
        byte[] ptp = shortToLittleEndianBytes(port);
        byte[] result = new byte[6];
        System.arraycopy(ipb, 0, result, 0, ipb.length);
        System.arraycopy(ptp, 0, result, ipb.length, ptp.length);
        return result;
    }

    public static InetSocketAddress getAddress(DatagramPacket dp) {
        byte[] rcv = dp.getData();
        byte[] address = new byte[6];
        int j = 0;
        boolean toggle = false;
        for (int i = 0; i < dp.getLength(); i++) {
            if (toggle)
                address[j++] = rcv[i];
            if (rcv[i] == 58)
                toggle = true;
            if (j == 6)
                toggle = false;
        }
        String ip = IpUtils.bytesGetIp(address);
        int port = IpUtils.bytesGetPort(address);
        System.out.println("---getAddress " + ip + ":" + port);
        return new InetSocketAddress(ip, port);
    }

    public static String getData(DatagramPacket dp) {
        byte[] rcv = dp.getData();
        byte[] data = new byte[dp.getLength()];
        int j = 0;
        boolean toggle = false;
        for (int i = 0; i < dp.getLength(); i++) {
            if (toggle)
                data[j++] = rcv[i];
            if (rcv[i] == 58)
                toggle = true;
        }
        return new String(Arrays.copyOfRange(data, 0, j));
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String args[]) throws IOException {
        System.out.println("------------- TEST 1-----------");
        File file = new File("/Users/aprateek/work/test/pyipaddr");
        byte[] bt = Files.readAllBytes(file.toPath());
        String ipaddr = IpUtils.bytesGetIp(bt);
        int port = IpUtils.bytesGetPort(bt);
        System.out.println("Ip address in file " + ipaddr + " port is " + port);

        System.out.println("------------- TEST 2-----------");
        System.out.println("Test ip port are 192.168.100.16" + ":" + 61649 + " ==c0:a8:64:10:d1:f0" );
        byte[] ipb = IpUtils.ipToBytes("192.168.100.16", 61649);
        for (int i = 0; i < ipb.length; i++) {
            System.out.print(String.format("%02x", ipb[i]) + ":");
        }
        ipaddr = IpUtils.bytesGetIp(ipb);
        port = IpUtils.bytesGetPort(ipb);
        System.out.println("\nConvert Back Ip address is " + ipaddr + " port is " + port);


        System.out.println("------------- TEST 3-----------");
        ipb = IpUtils.ipToBytes("192.168.100.17", (short)23456);
        File filen = new File("/Users/aprateek/work/test/javaipaddr");
        Files.write(filen.toPath(), ipb, StandardOpenOption.CREATE);

        byte bt1[] = {-64, -88, 100, 16, -103, -7}; //c0:a8:64:10:99:f9; 192.168.100.16 63897
        for (int i = 0; i < bt1.length; i++) {
            System.out.println(String.format("%02x ", bt1[i]) + "=====" + bt1[i]);
        }

        ipaddr = IpUtils.bytesGetIp(bt1);
        port = IpUtils.bytesGetPort(bt1);
        System.out.println("Ip address is " + ipaddr + " port is " + port);
    }
}
