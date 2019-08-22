package com.rockchip.inno.master_yolov3_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.rockchip.inno.master_yolov3_demo.Util.bytes_Srting;
import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientCallback;
import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientConnect;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mRGBCameraView;
    // TODO: Camera Config
    public int CAMERA_NUM = 1;
    public static final int RGB_CAMERA_ID = 0;
    public static final int INFRARED_CAMERA_ID = 1;
    private Mat mRgbaDraw;
    private Mat mRgbaFrame;
    TCPClientConnect mBaseTcpClient;
    String ip = "192.168.180.8";
    int port = 8002;
    List<DetectResult> detectResultList = new ArrayList<>();
    private final Object receiveLock = new Object();
    private final Object resultLock = new Object();
    private boolean receiveResult = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCamera();
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


    private void initCamera() {
        Log.d(TAG, "camera num: " + CAMERA_NUM);

        mRGBCameraView = findViewById(R.id.rgb_camera_view);
        mRGBCameraView.setCameraIndex(RGB_CAMERA_ID);
        mRGBCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "onCameraViewStarted: width=" + width + " height=" + height);
            }

            @Override
            public void onCameraViewStopped() {
            }

            @SuppressLint("DefaultLocale")
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                mRgbaFrame = inputFrame.rgba();
//                mRgbaFrame = imread("/sdcard/123.jpg");
//                Imgproc.resize(mRgbaFrame, mRgbaFrame, new Size(1280, 960));
                cameraFps = 1000.0f / (System.currentTimeMillis() - lastCameraTime);
//                Log.d(TAG, "onCameraFrame: " +cameraFps );
                lastCameraTime = System.currentTimeMillis();


                synchronized (receiveLock) {
                    if (receiveResult) {
                        t1 = System.currentTimeMillis();
                        receiveResult = false;
                        mRgbaDraw = mRgbaFrame.clone();
                        Imgproc.resize(mRgbaFrame, mRgbaDraw, new Size(416, 416));
                        byte[] data = mat2Byte(mRgbaDraw, ".jpg");
                        t2= System.currentTimeMillis();
                        int len = 16;
                        String str2 = String.format("%01$-" + len + "s", String.valueOf(data.length));
                        byte[] bytes = new byte[str2.getBytes().length + data.length];

                        System.arraycopy(str2.getBytes(),0,bytes,0,str2.getBytes().length);
                        System.arraycopy(data,0,bytes,str2.getBytes().length,data.length);
//                        mBaseTcpClient.write(str2.getBytes());
                        mBaseTcpClient.write(bytes);
                        t3= System.currentTimeMillis();
                        try {
                            receiveLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                synchronized (resultLock) {
                    org.opencv.core.Scalar textColor = new Scalar(255, 0, 0);
                    org.opencv.core.Point fpsPoint = new Point();
                    fpsPoint.x = 10;
                    fpsPoint.y = 40;
                    Imgproc.putText(mRgbaFrame,
                            String.format("cameraFps: %.2f", cameraFps),
                            fpsPoint, Core.FONT_HERSHEY_DUPLEX,
                            1, textColor);
                    fpsPoint.y = 75;
                    Imgproc.putText(mRgbaFrame,
                            String.format("detectFps: %.2f", detectFps),
                            fpsPoint, Core.FONT_HERSHEY_TRIPLEX,
                            1, textColor);

                    for (DetectResult detectResult : detectResultList) {
                        detectResult.initPoint(mRgbaFrame.width(), mRgbaFrame.height());
                        Imgproc.rectangle(mRgbaFrame,
                                detectResult.getPt1(),
                                detectResult.getPt2(),
                                detectResult.getColor(), 2);
                        Imgproc.putText(mRgbaFrame,
                                String.format("%s %.2f", detectResult.getClassesName(), detectResult.getScores()),
                                detectResult.getTextPt(),
                                Core.FONT_HERSHEY_TRIPLEX,
                                1, textColor);
                    }

                }
                return mRgbaFrame;
            }
        });
        mRGBCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);

    }

    /**
     * Mat转换成byte数组
     *
     * @param matrix        要转换的Mat
     * @param fileExtension 格式为 ".jpg", ".png", etc
     * @return
     */
    public static byte[] mat2Byte(Mat matrix, String fileExtension) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        byte[] byteArray = mob.toArray();
        return byteArray;
    }

    public Mat byteAndMat(Mat image) {
        int width = image.cols();
        int height = image.rows();
        int dims = image.channels();
        byte[] data = new byte[width * height * dims];
        image.get(0, 0, data); //Mat转byte
        Log.d(TAG, "byteAndMat: " + bytes_Srting.byteArray2HexString(data));
        int index = 0;
        int r = 0, g = 0, b = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width * dims; col += dims) {
                index = row * width * dims + col;
                b = data[index] & 0xff;
                g = data[index + 1] & 0xff;
                r = data[index + 2] & 0xff;

                data[index] = (byte) b;
                data[index + 1] = (byte) g;
                data[index + 2] = (byte) r;
            }
        }

        image.put(0, 0, data); //byte转Mat
        return image;
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
        if (mRGBCameraView != null) {
            mRGBCameraView.disableView();
        }
        mBaseTcpClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeOpenCVEnv();
    }

    private void initializeOpenCVEnv() {
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    protected BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mRGBCameraView.disableFpsMeter();
                    mRGBCameraView.enableView();
                    if (CAMERA_NUM >= 2) {
//                        mInfraredCameraView.disableFpsMeter();
//                        mInfraredCameraView.enableView();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mRGBCameraView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


}

