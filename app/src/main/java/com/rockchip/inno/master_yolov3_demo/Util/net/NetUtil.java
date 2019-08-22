package com.rockchip.inno.master_yolov3_demo.Util.net;

import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetUtil {
    private static final String TAG = "NetUtil";
    private static final boolean DEBUG = false;

    /**
     * TODO<获取本地ip地址>
     *
     * @return String
     */
    public static String getLocAddress() {
        return getIPAddress("eth0");
    }

    public synchronized static String getIPAddress(String name) {
        String ipaddress = "0.0.0.0";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                if (networks.getName().equals(name)) {
                    // 得到每一个网络接口绑定的所有ip
                    Enumeration<InetAddress> address = networks.getInetAddresses();
                    // 遍历每一个接口绑定的所有ip
                    while (address.hasMoreElements()) {
                        InetAddress ip = address.nextElement();
                        if (!ip.isLoopbackAddress() && (ip instanceof Inet4Address)) {
                            ipaddress = ip.getHostAddress();
                            if (DEBUG) Log.i(TAG, "本机IP:" + ipaddress);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "获取本地ip地址失败");
            e.printStackTrace();
        }

        if (DEBUG) Log.i(TAG, "本机IP:" + ipaddress);
        return ipaddress;
    }

    /**
     * 获取ip地址
     * @return Srting
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress
     *            // 本机IP地址
     * @return String
     */
    public static String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }
}
