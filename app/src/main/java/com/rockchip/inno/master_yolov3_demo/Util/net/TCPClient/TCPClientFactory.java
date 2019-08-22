package com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient;

import android.util.Log;


import com.rockchip.inno.master_yolov3_demo.Util.bytes_Srting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxh on 2018/6/7
 * E-mail: shon.chen@rock-chips.com
 * <p>
 * Socket连接操作类
 */
public class TCPClientFactory {
    private static final String TAG = "TCPClientFactory";
    private Socket mSocket;// socket连接对象
    private DataOutputStream out;
    private DataInputStream in;// 输入流
    private byte[] buffer = new byte[1024 * 100];// 缓冲区字节数组，信息不能大于此缓冲区
    private TCPClientCallback callback;// 信息回调接口
    private int timeOut = 1000 * 5;

    /**
     * 构造方法传入信息回调接口对象
     * <p>
     * //     * @param callback 回调接口
     */
    public TCPClientFactory(TCPClientCallback callback) {
        this.callback = callback;
    }

    /**
     * 连接网络服务器
     *
     * @throws UnknownHostException
     * @throws IOException
     */
    public void connect(String ip, int port) throws Exception {
        mSocket = new Socket();
        mSocket.setTcpNoDelay(true);
        SocketAddress address = new InetSocketAddress(ip, port);
        mSocket.connect(address, timeOut);// 连接指定IP和端口
        if (isConnected()) {
            out = new DataOutputStream(mSocket.getOutputStream());// 获取网络输出流
            in = new DataInputStream(mSocket.getInputStream());// 获取网络输入流

            if (isConnected()) {
                callback.tcp_connected();
            }
        }
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * 返回连接服是否成功
     *
     * @return
     */
    public boolean isConnected() {
        if (mSocket == null || mSocket.isClosed()) {
            return false;
        }
        return mSocket.isConnected();
    }

    /**
     * 发送数据
     *
     * @param buffer 信息字节数据
     * @throws IOException
     */
    public void writeMsg(byte[] buffer) throws IOException {
        if (out != null) {
//            byte[] head = bytes_Srting.intToByteArray(buffer.length);
//            Log.d("TAG", "writeMsg: "+bytes_Srting.byteArray2HexString(head));
//            write(head);
            write(buffer);
        }
    }


    private synchronized void write(byte[] buffer) throws IOException {
        if (out != null) {
            out.write(buffer);
            out.flush();
        }
    }

    /**
     * 断开连接
     *
     * @throws IOException
     */
    public void disconnect() throws Exception {
        try {
            if (mSocket != null) {
                if (!mSocket.isInputShutdown()) {
                    mSocket.shutdownInput();
                }
                if (!mSocket.isOutputShutdown()) {
                    mSocket.shutdownOutput();
                }
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (mSocket != null && !mSocket.isClosed()) {// 判断socket不为空并且是连接状态
                mSocket.close();// 关闭socket
            }
        } finally {
            callback.tcp_disconnect();
            out = null;
            in = null;
            mSocket = null;// 制空socket对象
        }
    }

    /**
     * 读取网络数据
     *
     * @throws IOException
     */
    public void read() throws IOException {
        if (in != null) {
            int len = 0;// 读取长度

            in.read(buffer, 0, 5);

            byte[] countArr = new byte[8];
            while ((len = in.read(countArr, 0, 8)) > 0) {
                int count = Integer.parseInt(bytes_Srting.byteArray2UTF8Str(countArr).replace(" ",""));
//                Log.d(TAG, "read: " + count);

                int[] buf = new int[count];// 缓冲区字节数组，信息不能大于此缓冲区
                len = in.read(buffer, 0, count * 4);
                for (int i = 0; i < buf.length; i++) {
                    byte[] buf_t = new byte[4];
                    buf_t[0] = buffer[i * 4];
                    buf_t[1] = buffer[i * 4 + 1];
                    buf_t[2] = buffer[i * 4 + 2];
                    buf_t[3] = buffer[i * 4 + 3];
                    buf[i] = bytes_Srting.byteArray2Int(buf_t);
//                    Log.d(TAG, "read: +buf[" + i + "] = " + buf[i]);
                }
                List<byte[]> receiveList = new ArrayList<>();
                for (int i1 : buf) {
                    len = in.read(buffer, 0, i1);
                    byte[] tmp = new byte[len];
                    System.arraycopy(buffer, 0, tmp, 0, len);
                    receiveList.add(tmp);
                }
                callback.tcp_receive(receiveList);
            }
        }
    }
}


