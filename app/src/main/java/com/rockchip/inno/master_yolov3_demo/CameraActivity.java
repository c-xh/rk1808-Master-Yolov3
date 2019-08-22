package com.rockchip.inno.master_yolov3_demo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientCallback;
import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientConnect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxh on 2019/8/21
 * E-mail: shon.chen@rock-chips.com
 */
public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";

    TCPClientConnect mBaseTcpClient;
    String ip = "192.168.180.8";
    int port = 8002;
    List<DetectResult> detectResultList = new ArrayList<>();
    private final Object receiveLock = new Object();
    private final Object resultLock = new Object();
    private boolean receiveResult = true;
    float cameraFps = 0;
    float detectFps = 0;
    long lastCameraTime = System.currentTimeMillis();
    long lastDetectTime = System.currentTimeMillis();
    long t1 = System.currentTimeMillis();
    long t2 = System.currentTimeMillis();
    long t3 = System.currentTimeMillis();

    long t4 = System.currentTimeMillis();
    long t5 = System.currentTimeMillis();
    long t6 = System.currentTimeMillis();

//    LiveCameraView liveCameraView;
//    TextureView textureView;

    private GLSurfaceView mGLSurfaceView;
    private CameraSurfaceRender mRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera);

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mRender = new CameraSurfaceRender(mGLSurfaceView, null);
        mGLSurfaceView.setRenderer(mRender);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        if (mBaseTcpClient == null) {
            mBaseTcpClient = new TCPClientConnect();
            mBaseTcpClient.setCallback(new TCPClientCallback() {
                @Override
                public void tcp_connected() {
                    Log.d(TAG, "tcp_connected: " + ip);
                }

                @Override
                public void tcp_receive(List<byte[]> buffer) {
                    synchronized (receiveLock) {
//                        Log.d(TAG, "tcp_receive: " + bytes_Srting.byteArray2HexString(buffer));
                        List<Object[]> resultObject = new ArrayList<>();
                        try {

                            t4= System.currentTimeMillis();
                            for (int i = 0; i < buffer.size(); i++) {
                                Object[] oo = ParseData.parse(buffer.get(i), true);
                                if (oo != null) {
                                    resultObject.add(oo);
                                }
                            }
                            t5= System.currentTimeMillis();
                            detectFps = 1000.0f / (System.currentTimeMillis() - lastDetectTime);
//                            Log.d(TAG, "detectFps: " +detectFps +"  "+ ((System.currentTimeMillis()-lastDetectTime)));
                            lastDetectTime = System.currentTimeMillis();
                            generateDetectResult(resultObject);
                            t6= System.currentTimeMillis();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        StringBuilder sb =new StringBuilder();
                        sb.append("一帧时间: " + (t6-t1));
                        sb.append("\n");
                        sb.append("复制时间: " + (t2-t1));
                        sb.append("\n");
                        sb.append("tcp发送时间: " + (t3-t2));
                        sb.append("\n");
                        sb.append("1808处理时间: " + (t4-t3));
                        sb.append("\n");
                        sb.append("解析数据: " + (t5-t4));
                        sb.append("\n");
                        sb.append("打包数据: " + (t6-t5));
                        sb.append("\n");
                        Log.d(TAG, sb.toString());
                        receiveResult = true;
                        receiveLock.notify();// 取消等待
                    }
                }

                @Override
                public void tcp_disconnect() {
                    Log.d(TAG, "tcp_disconnect: " + ip);
                }
            });
            mBaseTcpClient.setAddress(ip, port);
            mBaseTcpClient.setTimeOut(10000);
            new Thread(mBaseTcpClient).start();
        }
    }
    private void generateDetectResult(List<Object[]> resultObject) {
        synchronized (resultLock) {
            detectResultList.clear();
        }
//        Log.d(TAG, "generateDetectResult resultObject.size(): " + resultObject.size());
        if (resultObject.size() == 3) {
            Object[] boxes = resultObject.get(0);
            Object[] classes = resultObject.get(1);
            Object[] scores = resultObject.get(2);
            int[] boxesShare = (int[]) boxes[ParseData.SHAPE];
            int[] classesShare = (int[]) classes[ParseData.SHAPE];
            int[] scoresShare = (int[]) scores[ParseData.SHAPE];
            float[] boxesData = (float[]) boxes[ParseData.DATA_ARRAY];
            long[] classesData = (long[]) classes[ParseData.DATA_ARRAY];
            float[] scoresData = (float[]) scores[ParseData.DATA_ARRAY];

            float[] data_t = new float[boxesShare[1]];

            synchronized (resultLock) {
                for (int i = 0; i < boxesShare[0]; i++) {
                    System.arraycopy(boxesData, i * boxesShare[1], data_t, 0, boxesShare[1]);
                    detectResultList.add(new DetectResult(data_t, scoresData[i], classesData[i]));
//                Log.d(TAG, "generateDetectResult:" + i + " = " + detectResultList.get(i).toString());
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
//        if (liveCameraView!=null){
//            liveCameraView.stopCamera();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();

//        if (liveCameraView!=null){
//            liveCameraView.startCamera();
//        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBaseTcpClient.disconnect();
        System.exit(0);
    }

}
