package com.rockchip.inno.master_yolov3_rndis;

import android.annotation.SuppressLint;

import com.rockchip.inno.master_yolov3_rndis.Util.net.TCPClient.TCPClientConnect;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by cxh on 2019/8/26
 * E-mail: shon.chen@rock-chips.com
 */

public class CameraFrameBufferQueue {
    private static final String TAG = "CameraFrameBufferQueue";
    TCPClientConnect mBaseTcpClient;
    float cameraFps = 0;
    float detectFps = 0;
    int cameraFpsCount = 0;
    int detectFpsCount = 0;
    long lastCameraTime = System.currentTimeMillis();
    long lastDetectTime = System.currentTimeMillis();

    class CameraFrameBuffer {
        public volatile Mat matBuff;
        public volatile byte[] jpgData;
        public volatile List<DetectResult> detectResultList;
    }

    volatile CameraFrameBuffer[] cameraFrameBufferList = new CameraFrameBuffer[4];

    public void calculateCameraFps() {
        cameraFpsCount++;
        if (cameraFpsCount % 10 == 0) {
            cameraFps = 10000.0f / (System.currentTimeMillis() - lastCameraTime);
            lastCameraTime = System.currentTimeMillis();
            cameraFpsCount = 0;
        }
    }

    public void calculateDetectFps() {
        detectFpsCount++;
        if (detectFpsCount % 10 == 0) {
            detectFps = 10000.0f / (System.currentTimeMillis() - lastDetectTime);
            lastDetectTime = System.currentTimeMillis();
            detectFpsCount = 0;
        }
    }

    CameraFrameBufferQueue(TCPClientConnect mBaseTcpClient) {
        this.mBaseTcpClient = mBaseTcpClient;
        for (int i = 0; i < 3; i++) {
            cameraFrameBufferList[i] = new CameraFrameBuffer();
        }
    }

    private byte[] setJpgData(Mat mat) {
        int len = 16;
        byte[] data = mat2Byte(mat, ".jpg");
        String str2 = String.format("%01$-" + len + "s", String.valueOf(data.length));
        byte[] jpgData_t = new byte[str2.getBytes().length + data.length];

        System.arraycopy(str2.getBytes(), 0, jpgData_t, 0, str2.getBytes().length);
        System.arraycopy(data, 0, jpgData_t, str2.getBytes().length, data.length);
        return jpgData_t;
    }

    private static boolean isPutRunning = false;

    public void putNewBuff(Mat newMat) {
        if (!isPutRunning) {
            final Mat matBuff = newMat;
            new Thread() {
                @Override
                public void run() {
                    isPutRunning = true;
                    CameraFrameBuffer cameraFrameBuffer = new CameraFrameBuffer();
                    cameraFrameBuffer.matBuff = matBuff.clone();
                    Mat tmpFrame = cameraFrameBuffer.matBuff.clone();
                    Imgproc.resize(tmpFrame, tmpFrame, new Size(416, 416));
                    cameraFrameBuffer.jpgData = setJpgData(tmpFrame);
                    cameraFrameBufferList[2] = cameraFrameBuffer;
                    if (cameraFrameBufferList[1].matBuff == null) {
                        cameraFrameBufferList[1] = cameraFrameBufferList[2];
                        if (cameraFrameBufferList[0].matBuff == null) {
                            cameraFrameBufferList[0] = cameraFrameBufferList[1];
                        }
                    }
                    if ((System.currentTimeMillis() - lastDetectTime) > 10000) {
                        lastDetectTime = System.currentTimeMillis();
                        mBaseTcpClient.write(getReadyJpgData());
                    }

                    isPutRunning = false;
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public byte[] getReadyJpgData() {
        if (cameraFrameBufferList[1].jpgData == null) {
            return null;
        }
        return cameraFrameBufferList[1].jpgData;
    }

    public void setDetectResult(List<DetectResult> detectResult) {
        cameraFrameBufferList[1].detectResultList = detectResult;
        cameraFrameBufferList[0] = cameraFrameBufferList[1];
        cameraFrameBufferList[1] = cameraFrameBufferList[2];
//        if (cameraFrameBufferList.size()>(READY_BUFFER+1))
//        cameraFrameBufferList.remove(0);
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

    @SuppressLint("DefaultLocale")
    public Mat draw() {
//            Log.d(TAG, "推理帧率 : " + detectFps);
//            Log.d(TAG, String.format("detectFps: %.2f", detectFps));
        if (cameraFrameBufferList[0].matBuff == null) {
            return null;
        }
        if (cameraFrameBufferList[0].detectResultList == null) {
//            Log.d(TAG, "drew: detectResultList==null ");
            return cameraFrameBufferList[0].matBuff;
        }
        new Thread() {
            @Override
            public void run() {
                org.opencv.core.Scalar textColor = new Scalar(255, 0, 0);
                org.opencv.core.Point fpsPoint = new Point();
                fpsPoint.x = 10;
                fpsPoint.y = 40;
                Imgproc.putText(cameraFrameBufferList[0].matBuff,
                        String.format("cameraFps: %.2f", cameraFps),
                        fpsPoint, Core.FONT_HERSHEY_DUPLEX,
                        1, textColor);
                fpsPoint.y = 75;
                Imgproc.putText(cameraFrameBufferList[0].matBuff,
                        String.format("detectFps: %.2f", detectFps),
                        fpsPoint, Core.FONT_HERSHEY_TRIPLEX,
                        1, textColor);

                for (DetectResult detectResult : cameraFrameBufferList[0].detectResultList) {
                    detectResult.initPoint(cameraFrameBufferList[0].matBuff.width(),
                            cameraFrameBufferList[0].matBuff.height());
                    Imgproc.rectangle(cameraFrameBufferList[0].matBuff,
                            detectResult.getPt1(),
                            detectResult.getPt2(),
                            detectResult.getColor(), 2);
                    Imgproc.putText(cameraFrameBufferList[0].matBuff,
                            String.format("%s %.2f", detectResult.getClassesName(), detectResult.getScores()),
                            detectResult.getTextPt(),
                            Core.FONT_HERSHEY_TRIPLEX,
                            1, textColor);
                }
            }
        }.start();
        return cameraFrameBufferList[0].matBuff;
    }

}
