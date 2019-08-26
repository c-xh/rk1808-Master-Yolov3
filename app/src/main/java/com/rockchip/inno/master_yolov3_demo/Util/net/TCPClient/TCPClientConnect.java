package com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient;

import android.util.Log;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by cxh on 2018/6/7
 * E-mail: shon.chen@rock-chips.com
 *
 * 连接服务器线程类
 */
public class TCPClientConnect implements Runnable {
    private static final String TAG = "TCPClientConnect";
    private boolean DEBUG = true;

    public boolean isConnect = false;// 是否连接服务器
    private boolean isWrite = false;// 是否发送数据
    private static Vector<byte[]> datas = new Vector<byte[]>();// 待发送数据队列
    private Object lock = new Object();// 连接锁对象
    private TCPClientFactory mSocket;// socket连接
    private WriteRunnable writeRunnable;// 发送数据线程
    private String ip = null;
    private int port = -1;

    /**
     * 创建连接
     * <p>
     * //     * @param callback 回调接口
     */
    public TCPClientConnect(/*TCPClientCallback callback*/) {
    }

    public void setCallback(TCPClientCallback callback) {
        mSocket = new TCPClientFactory(callback);// 创建socket连接
        writeRunnable = new WriteRunnable();// 创建发送线程
//        sendHeartbeatRunnable = new SendHeartbeatRunnable();
    }

    @Override
    public void run() {
        if (ip == null || port == -1) {
            return;
        }
        isConnect = true;
        while (isConnect) {
            synchronized (lock) {
                try {
                    if (DEBUG) Log.i(TAG, ">TCP连接服务器ip = " + ip + "<");
                    mSocket.connect(ip, port);// 连接服务器
                } catch (Exception e) {
                    try {
                        if (DEBUG) Log.e(TAG, ">TCP连接服务器ip = " + ip +":"+port+ "失败, 3秒后重新连接<" + "isConnect = " + isConnect);
                        resetConnect();// 断开连接
                        lock.wait(3000);
                        continue;
                    } catch (InterruptedException e1) {
                        continue;
                    }
                }
            }
            if (DEBUG) Log.e(TAG, ">TCP连接服务器ip = " + ip + "成功<");

            isWrite = true;// 设置可发送数据
            new Thread(writeRunnable).start();// 启动发送线程
//            new Thread(sendHeartbeatRunnable).start();
            try {
                mSocket.read();// 获取数据
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, ">TCP连接ip = " + ip + "异常<", e);
            } finally {
                if (DEBUG) Log.e(TAG, ">TCP连接ip = " + ip + "中断<");
                resetConnect();// 断开连接
                isConnect = false;
            }
        }
        if (DEBUG) Log.e(TAG, ">=TCP" + ip + "结束连接线程=<");
    }

    /**
     * 关闭服务器连接
     */
    public void disconnect() {
        synchronized (lock) {
            isConnect = false;
            lock.notify();
//            resetConnect();
        }
    }

    /**
     * 设置是否显示log
     * @param isDebug true:显示 false:不显示
     */
    public void showDebufInfo(boolean isDebug){
        DEBUG = isDebug;
    }

    /**
     * 重置连接
     */
    public void resetConnect() {
        if (DEBUG) Log.w(TAG, ">TCP" + ip + "重置连接<");
        writeRunnable.stop();// 发送停止信息
//        sendHeartbeatRunnable.stop();
        try {
            mSocket.disconnect();
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    /**
     * 向发送线程写入发送数据
     */
    public void write(byte[] buffer) {
        if (buffer!=null) {
            writeRunnable.write(buffer);
        }
    }

    /**
     * shezhi 设置连接超时时间
     * @param timeOut 连接超时时间ms
     */
    public void setTimeOut(int timeOut) {
        mSocket.setTimeOut(timeOut);
    }

    /**
     * 设置IP和端口
     * @param host 要连接服务端的IP地址
     * @param port 要连接服务端的端口
     */
    public void setAddress(String host, int port) {
        this.ip = host;
        this.port = port;
    }

    /**
     * 发送数据
     */
    private boolean writes(byte[] buffer) {
        try {
            mSocket.writeMsg(buffer);
            Thread.sleep(1);
            return true;
        } catch (Exception e) {
            resetConnect();
            return false;
        }
    }


    /**
     * 发送线程
     */
    private class WriteRunnable implements Runnable {

        private Object wlock = new Object();// 发送线程锁对象

        @Override
        public void run() {
            if (DEBUG) Log.e(TAG, ">TCP发送线程开启<");
            while (isWrite) {
                synchronized (wlock) {
                    if (datas.size() <= 0) {
                        try {
                            wlock.wait();// 等待发送数据
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    while (datas.size() > 0) {
                        byte[] buffer = datas.remove(0);// 获取一条发送数据
                        if (isWrite) {
                            writes(buffer);// 发送数据
                        } else {
                            wlock.notify();
                        }
                    }
                }
            }
            if (DEBUG) Log.e(TAG, ">TCP发送线程结束<");
        }

        /**
         * 添加数据到发送队列
         *
         * @param buffer 数据字节
         */
        public void write(byte[] buffer) {
            synchronized (wlock) {
                datas.add(buffer);// 将发送数据添加到发送队列
                wlock.notify();// 取消等待
            }
        }

        public void stop() {
            synchronized (wlock) {
                isWrite = false;
                wlock.notify();
            }
        }
    }
}
