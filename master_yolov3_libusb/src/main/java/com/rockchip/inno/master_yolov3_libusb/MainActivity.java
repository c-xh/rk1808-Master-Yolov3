package com.rockchip.inno.master_yolov3_libusb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rockchip.inno.util_library.CameraFrameBufferQueue;
import com.rockchip.inno.util_library.DetectResult;
import com.rockchip.inno.util_library.ParseData;
import com.rockchip.inno.util_library.PermissionUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mRGBCameraView;
    public int CAMERA_NUM = 1;
    public static final int RGB_CAMERA_ID = 0;
    public static final int INFRARED_CAMERA_ID = 1;
    CameraFrameBufferQueue cameraFrameBufferQueue;

    usbDevicesManagerUtil mUsbDevicesManagerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_CAMERA, mPermissionGrant);
        PermissionUtils.requestPermission(this, PermissionUtils.READ_EXTERNAL_STORAGE, mPermissionGrant);
        PermissionUtils.requestPermission(this, PermissionUtils.READ_CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
        cameraFrameBufferQueue = new CameraFrameBufferQueue();


        mUsbDevicesManagerUtil = new usbDevicesManagerUtil(MainActivity.this);
        StringBuffer sb = new StringBuffer();
        sb.append("初始化usb设备\n");
        String devicesList = mUsbDevicesManagerUtil.enumeraterDevices();
        sb.append("发现usb设备，正在打开设备\n");
        if (mUsbDevicesManagerUtil.openDevice()) {
            if (mUsbDevicesManagerUtil != null) {
                Log.d(TAG, "onCreate123: 连接到usb设备成功");
                mUsbDevicesManagerUtil.receiveBulkMessageToPoint();
                mUsbDevicesManagerUtil.setOnFrameListener(new usbDevicesManagerUtil.OnFrameListener() {
                    @Override
                    public void OnTsFrame(List<byte[]>  buffer) {
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

                        byte[]  bytes = cameraFrameBufferQueue.getReadyJpgData();
                        int len = 16;
                        String str2 = String.format("start%01$-" + len + "s", String.valueOf(bytes.length));

                        mUsbDevicesManagerUtil.sendBulkMessageToPoint(str2.getBytes());
                        mUsbDevicesManagerUtil.sendBulkMessageToPoint(bytes);
                        cameraFrameBufferQueue.draw();
                        cameraFrameBufferQueue.calculateDetectFps();
                        sleep(1);

                    }
                });

                cameraFrameBufferQueue.setOnFrameDataListener(new CameraFrameBufferQueue.onFrameData() {
                    @Override
                    public void newFrameData(byte[] data) {
                        byte[]  bytes = cameraFrameBufferQueue.getReadyJpgData();
                        int len = 16;
                        String str2 = String.format("start%01$-" + len + "s", String.valueOf(bytes.length));

                        mUsbDevicesManagerUtil.sendBulkMessageToPoint(str2.getBytes());
                        mUsbDevicesManagerUtil.sendBulkMessageToPoint(bytes);
//                        mUsbDevicesManagerUtil.sendBulkMessageToPoint(data);
                    }
                });
                initCamera();
            }
        } else {
            Log.d(TAG, "onCreate123: 无法连接到usb设备！！！");
            Toast.makeText(MainActivity.this, "无法连接到usb设备！！！", Toast.LENGTH_LONG).show();
            finish();
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
        if (mUsbDevicesManagerUtil != null)
            mUsbDevicesManagerUtil.onDestroy();
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
                case PermissionUtils.CODE_CAMERA:
//                    Toast.makeText(MainActivity.this, "Result Permission Grant CODE_CAMERA", Toast.LENGTH_SHORT).show();
                    break;
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
