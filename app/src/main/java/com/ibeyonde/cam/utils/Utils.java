package com.ibeyonde.cam.utils;

import java.net.InetAddress;

public class Utils {

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("ping.ibeyonde.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }
}
