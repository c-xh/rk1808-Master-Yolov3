package com.rockchip.inno.master_yolov3_demo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by cxh on 2019/8/21
 * E-mail: shon.chen@rock-chips.com
 */
public class LiveCameraView extends SurfaceView implements SurfaceHolder.Callback {
    private final static String TAG = "WYF LiveCameraView";
    private Camera camera;
//    private TextureView textureView;
    private Surface textureSurface;
    private SurfaceHolder surfaceHolder;
    private boolean startingCamera = false;
    private long lastFpsTime = 0;
    private int drawFps = 0;
//    private Rect surfaceRect;
    private Paint bitmapPaint;
    private boolean isStarted = false;
    // surface 处理线程
    private Handler captureHandler = null;
    private HandlerThread captureHandlerThread;

    /**
     * 相机数据回调
     */
    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] d, Camera camera) {
            Log.w(TAG, "onPreviewFrame");
            if (lastFpsTime == 0) {
                lastFpsTime = System.currentTimeMillis();
            }
            long time1 = System.currentTimeMillis();
            drawFps++;
            int length = d.length;

            byte[] data = new byte[d.length];
            System.arraycopy(d, 0, data, 0, d.length);
            long t2 = System.currentTimeMillis() - time1;

            // 数据转换成bitmap
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            Log.w(TAG, "previewSize = " + previewSize.width + "," + previewSize.height);
            YuvImage yuvimage = new YuvImage(
                    data,
                    ImageFormat.NV21,
                    previewSize.width,
                    previewSize.height,
                    null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 图像质量损失50，提高效率
            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 50
                    , baos);


            long t3 = System.currentTimeMillis() - t2 - time1;
            data = baos.toByteArray();
            // 2的倍数，压缩几倍
            int scaleRate = 2;
            try {
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inSampleSize = scaleRate;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, newOpts);
//                Canvas canvas = textureSurface.lockCanvas(surfaceRect);
//                Matrix matrix = canvas.getMatrix();
//                matrix.postScale(-1, 1, surfaceRect.width()/2, surfaceRect.height()/2);
//                matrix.postRotate(90, surfaceRect.width()/2, surfaceRect.height()/2);
//                canvas.setMatrix(matrix);
//                int width = bitmap.getWidth();
//                int height = bitmap.getHeight();
//
//                // TODO 计算比例
//                canvas.drawBitmap(bitmap, (surfaceRect.right - width/scaleRate)/2
//                        , (surfaceRect.bottom - height/scaleRate)/2, bitmapPaint);
//
//                canvas.scale(5.0f, 5.0f);
//                textureSurface.unlockCanvasAndPost(canvas);
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            long t4 = System.currentTimeMillis() - t3 - t2 - time1;

            Log.w(TAG, "fps = " + (drawFps / ((System.currentTimeMillis() - lastFpsTime) / 1000.f))
                    + ",t = " + (System.currentTimeMillis() - time1) + ", length = " + length);
            Log.w(TAG, "t2=" + t2 + ",t3=" + t3 + ",t4=" + t4);
        }
    };

    public LiveCameraView(Context context) {
        super(context);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    public LiveCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    public LiveCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Start preview display[SURFACE-CREATED]");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null){
            return;
        }
        followScreenOrientation(getContext(), camera);
//        surfaceRect = new Rect(0, 0, textureView.getWidth(), textureView.getHeight());
        Log.d(TAG, "Restart preview display[SURFACE-CHANGED]");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Stop preview display[SURFACE-DESTROYED]");
        stopCamera();
    }

//    /**设置一个textureview 来显示捕获到的图像
//     * @param textureView
//     */
//    public void setTextureView(TextureView textureView) {
//        this.textureView = textureView;
//        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture s, int w, int h) {
//                Log.w(TAG, "onSurfaceTextureAvailable");
//                textureSurface = new Surface(s);
//            }
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int w, int h){
//            }
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                return false;
//            }
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
//        });
//    }

    /**
     * 调用之前必须判断是否启动，启动相机是线程启动，
     * 在surfaceView 启动相机的时候，外部也在启动，会导致错误
     */
    public synchronized void startCamera() {
        if (startingCamera || isStarted) {
            return;
        }
        // 打开一个相机
        startingCamera = true;

        captureHandlerThread = new HandlerThread("camera_preview");
        captureHandlerThread.start();
        // 相机捕获数据处理handler
        captureHandler = new Handler(captureHandlerThread.getLooper());

//        surfaceRect = new Rect(0, 0, textureView.getWidth(), textureView.getHeight());
        bitmapPaint = new Paint();

        try {
            captureHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        camera = Camera.open(1);
                        // TODO 调整这个参数
                        camera.setDisplayOrientation(90);
                        final Camera.Parameters params = camera.getParameters();
                        List<Camera.Size> previewSIzes = params.getSupportedVideoSizes();
                        Log.w("WenYF", "supported sizes: " + cameraSizeToSting(previewSIzes));
                        params.setPreviewSize(1280, 720);
                        Log.w("WenYF", params.flatten());
                        // 生效
                        camera.setParameters(params);

                        camera.setPreviewDisplay(surfaceHolder);
                        camera.setPreviewCallback(callback);
                        camera.startPreview();
                        isStarted = true;
                        startingCamera = true;
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void stopCamera() {
        if (captureHandlerThread != null) {
            captureHandlerThread.quit();
        }
        captureHandlerThread = null;

        isStarted = false;
        startingCamera = false;

        if (camera == null) {
            return;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isStarting() {
        return startingCamera;
    }

    public static void followScreenOrientation(Context context, Camera camera){
        if (camera == null) {
            return;
        }
        final int orientation = context.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        }else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }


    public static String cameraSizeToSting(Iterable<Camera.Size> sizes)
    {
        StringBuilder s = new StringBuilder();
        for (Camera.Size size : sizes)
        {
            if (s.length() != 0)
                s.append(",");
            s.append(size.width).append('x').append(size.height).append("\n");
        }
        return s.toString();
    }

}
