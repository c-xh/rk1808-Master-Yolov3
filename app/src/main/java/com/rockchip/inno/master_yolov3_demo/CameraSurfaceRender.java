package com.rockchip.inno.master_yolov3_demo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import com.rockchip.gdapc.demo.glhelper.LineProgram;
import com.rockchip.gdapc.demo.glhelper.TextureProgram;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glFinish;
import static android.opengl.GLES20.glViewport;

/**
 * Created by cxh on 2019/8/22
 * E-mail: shon.chen@rock-chips.com
 */
public class CameraSurfaceRender implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraSurfaceRender";


    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;
    private Object cameraLock = new Object();

    private TextureProgram mTextureProgram;     // Draw texture2D (include camera texture (GL_TEXTURE_EXTERNAL_OES) and normal GL_TEXTURE_2D texture)
    private LineProgram mLineProgram;           // Draw detection result
    private GLSurfaceView mGLSurfaceView;
    private int mOESTextureId = -1;    //camera texture ID


    private int mWidth;    //surface width
    private int mHeight;    //surface height

    public CameraSurfaceRender(GLSurfaceView glSurfaceView, Handler handler) {
        mGLSurfaceView = glSurfaceView;
//        mMainHandler = handler;
//        fileDirPath = mGLSurfaceView.getContext().getCacheDir().getAbsolutePath();
//        createFile(mModelName, R.raw.ssd);
//
//        try {
//            mInferenceResult.init(mGLSurfaceView.getContext().getAssets());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        startCamera();
        startTrack();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        if (mStopInference) {
//            return;
//        }
//
//        ImageBufferQueue.ImageBuffer imageBuffer = mImageBufferQueue.getFreeBuffer();
//
//        if (imageBuffer == null) {
//            return;
//        }
//
//        // render to offscreen
//        glBindFramebuffer(GL_FRAMEBUFFER, imageBuffer.mFramebuffer);
//        glViewport(0, 0, imageBuffer.mWidth, imageBuffer.mHeight);
//        mTextureProgram.drawFeatureMap(mOESTextureId);
//        glFinish();
//        mImageBufferQueue.postBuffer(imageBuffer);

        // main screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, mWidth, mHeight);
        mTextureProgram.draw(mOESTextureId);

//        mLineProgram.draw(recognitions);

        mSurfaceTexture.updateTexImage();

        // update main screen
        // draw track result
//        updateMainUI(1, 0);
    }

    private void startTrack() {
//        mInferenceResult.reset();
//        mImageBufferQueue = new ImageBufferQueue(3, INPUT_SIZE, INPUT_SIZE);
        mOESTextureId = TextureProgram.createOESTextureObject();
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mTextureProgram = new TextureProgram(mGLSurfaceView.getContext());
//        mLineProgram = new LineProgram(mGLSurfaceView.getContext());

        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mGLSurfaceView.requestRender();
            }
        });

        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        mStopInference = false;
//        mInferenceThread = new Thread(mInferenceRunnable);
//        mInferenceThread.start();
    }

    private void stopTrack() {

//        mStopInference = true;
//        try {
//            mInferenceThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if (mSurfaceTexture != null) {
            int[] t = {mOESTextureId};
            GLES20.glDeleteTextures(1, t, 0);

            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (mTextureProgram != null) {
            mTextureProgram.release();
            mTextureProgram = null;
        }

//        if (mLineProgram != null) {
//            mLineProgram.release();
//            mLineProgram = null;
//        }

//        if (mImageBufferQueue != null) {
//            mImageBufferQueue.release();
//            mImageBufferQueue = null;
//        }
    }

    private void startCamera() {
        if (mCamera != null) {
            return;
        }

        synchronized (cameraLock) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();

            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, camInfo);
                //if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                break;
                //}
            }

            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            }

            Camera.Parameters camParams = mCamera.getParameters();

            List<Camera.Size> sizes = camParams.getSupportedPreviewSizes();
            for (int i = 0; i < sizes.size(); i++) {
                Camera.Size size = sizes.get(i);
                Log.v(TAG, "Camera Supported Preview Size = " + size.width + "x" + size.height);
            }

            camParams.setPreviewSize(640, 480);
            camParams.setRecordingHint(true);

            mCamera.setParameters(camParams);

            if (mSurfaceTexture != null) {
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mCamera.startPreview();
            }
        }
    }

    private void stopCamera() {
        if (mCamera == null)
            return;

        synchronized (cameraLock) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        Log.i(TAG, "stopped camera");
    }

    public void onPause() {
        stopCamera();
        stopTrack();

    }

    public void onResume() {
        startCamera();
    }
}
