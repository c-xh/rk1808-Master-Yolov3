package com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient;

import java.util.List;

/**
 * Created by cxh on 2018/6/7
 * E-mail: shon.chen@rock-chips.com
 *
 * 获取网络数据回调类
 */
public interface TCPClientCallback {

    /**
     * 断开连接
     */
    public static final int TCP_DISCONNECTED = 0;

    /**
     * 已连接
     */
    public static final int TCP_CONNECTED = 1;

    /**
     * 连接获得数据
     */
    public static final int TCP_DATA = 2;

    /**
     * 当建立连接时的回调
     */
    public abstract void tcp_connected();

    /**
     * 当获取网络数据回调接口
     *
     * @param buffer 字节数据
     */
    public abstract void tcp_receive(List<byte[]> buffer);

    /**
     * 当断开连接的回调
     */
    public abstract void tcp_disconnect();
}
