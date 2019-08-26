package com.rockchip.inno.master_yolov3_demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.rockchip.inno.master_yolov3_demo.Util.PermissionUtils;
import com.rockchip.inno.master_yolov3_demo.Util.bytesConversionTool;
import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientCallback;
import com.rockchip.inno.master_yolov3_demo.Util.net.TCPClient.TCPClientConnect;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mRGBCameraView;

    public int CAMERA_NUM = 1;
    public static final int RGB_CAMERA_ID = 0;
    public static final int INFRARED_CAMERA_ID = 1;

    TCPClientConnect mBaseTcpClient;
    String ip = "192.168.180.8";
    int port = 8002;
    CameraFrameBufferQueue cameraFrameBufferQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_CAMERA, mPermissionGrant);
        PermissionUtils.requestPermission(this, PermissionUtils.READ_EXTERNAL_STORAGE, mPermissionGrant);
        PermissionUtils.requestPermission(this, PermissionUtils.READ_CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
        initTcp();
        cameraFrameBufferQueue = new CameraFrameBufferQueue(mBaseTcpClient);
        initCamera();
    }

    private void initTcp() {

        if (mBaseTcpClient == null) {
            mBaseTcpClient = new TCPClientConnect();
            mBaseTcpClient.setCallback(new TCPClientCallback() {
                @Override
                public void tcp_connected() {
                    Log.d(TAG, "tcp_connected: " + ip);
                }

                @Override
                public void tcp_receive(List<byte[]> buffer) {
                    List<Object[]> resultObject = new ArrayList<>();
                    try {
                        for (int i = 0; i < buffer.size(); i++) {
                            Object[] oo = ParseData.parse(buffer.get(i), true);
                            if (oo != null) {
                                resultObject.add(oo);
                            }
                        }
                        cameraFrameBufferQueue.setDetectResult(generateDetectResult(resultObject));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mBaseTcpClient.write(cameraFrameBufferQueue.getReadyJpgData());
                    cameraFrameBufferQueue.draw();
                    cameraFrameBufferQueue.calculateDetectFps();
//                    sleep(1);
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
                cameraFrameBufferQueue.putNewBuff(inputFrame.rgba());
//                    Mat mRgbaFrame = imread("/sdcard/123.jpg");
//                    Imgproc.resize(mRgbaFrame, mRgbaFrame, new Size(1280, 960));
//                    cameraFrameQueue[3].mat = mRgbaFrame;
                cameraFrameBufferQueue.calculateCameraFps();
                if (cameraFrameBufferQueue.cameraFrameBufferList[0].matBuff == null) {
                    return inputFrame.rgba();
                } else {
                    return cameraFrameBufferQueue.cameraFrameBufferList[0].matBuff;
                }
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
        Log.d(TAG, "byteAndMat: " + bytesConversionTool.byteArray2HexString(data));
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

    private List<DetectResult> generateDetectResult(List<Object[]> resultObject) {
        List<DetectResult> detectResultList = new ArrayList<>();
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

            for (int i = 0; i < boxesShare[0]; i++) {
                System.arraycopy(boxesData, i * boxesShare[1], data_t, 0, boxesShare[1]);
                detectResultList.add(new DetectResult(data_t, scoresData[i], classesData[i]));
//                Log.d(TAG, "generateDetectResult:" + i + " = " + detectResultList.get(i).toString());
            }
        }
        return detectResultList;
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mRGBCameraView != null) {
            mRGBCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeOpenCVEnv();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.READ_EXTERNAL_STORAGE:
//                    Toast.makeText(MainActivity.this, "Result Permission Grant EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.READ_CODE_WRITE_EXTERNAL_STORAGE:
//                    Toast.makeText(MainActivity.this, "Result Permission Grant READ_CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
//                case PermissionUtils.CODE_RECORD_AUDIO:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_RECORD_AUDIO", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_VIBRATE:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_VIBRATE", Toast.LENGTH_SHORT).show();
//                    break;
                case PermissionUtils.CODE_CAMERA:
//                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CAMERA", Toast.LENGTH_SHORT).show();
                    break;
//                case PermissionUtils.CODE_RECEIVE_BOOT_COMPLETED:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_RECEIVE_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_DISABLE_KEYGUARD:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_DISABLE_KEYGUARD", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_WAKE_LOCK:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_WAKE_LOCK", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_ACCESS_WIFI_STATE:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_ACCESS_WIFI_STATE", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_CHANGE_WIFI_STATE:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CHANGE_WIFI_STATE", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_BLUETOOTH:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_BLUETOOTH", Toast.LENGTH_SHORT).show();
//                    break;
//                case PermissionUtils.CODE_BLUETOOTH_ADMIN:
////                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_BLUETOOTH_ADMIN", Toast.LENGTH_SHORT).show();
//                    break;
                default:
                    break;
            }
        }
    };

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
                    if (mRGBCameraView == null) return;
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

