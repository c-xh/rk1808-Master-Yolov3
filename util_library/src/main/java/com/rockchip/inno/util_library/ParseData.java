package com.rockchip.inno.util_library;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * Created by cxh on 2019/8/21
 * E-mail: shon.chen@rock-chips.com
 */
public class ParseData {
    private static final String TAG = "ParseData";
    public static boolean PARSE_DEBUG = false;

    public synchronized static String getTpye(byte[] data) {
        byte[] typeName = new byte[8];
        System.arraycopy(data, 0, typeName, 0, typeName.length);
        String typeNameStr = bytesConversionTool.byteArray2UTF8Str(typeName).replace(" ", "");

        if (PARSE_DEBUG) if (PARSE_DEBUG) Log.d(TAG, "getTpye: " + typeNameStr);
        return typeNameStr;
    }

    public synchronized static int getShapeLen(byte[] data) {
        byte[] shapeLenArr = new byte[8];
        System.arraycopy(data, 8, shapeLenArr, 0, shapeLenArr.length);
        int shapeLen = Integer.parseInt(bytesConversionTool.byteArray2UTF8Str(shapeLenArr).replace(" ", ""));
        return shapeLen;
    }

    public synchronized static int getDataLen(byte[] data) {
        byte[] dataLenArr = new byte[8];
        System.arraycopy(data, 8 * 2, dataLenArr, 0, dataLenArr.length);
        int dataLen = Integer.parseInt(bytesConversionTool.byteArray2UTF8Str(dataLenArr).replace(" ", ""));
        return dataLen;
    }

    public synchronized static int[] getShape(byte[] data, int shapeLen, boolean littleEndian) {
        return parseInt32Arr(data, 8 * 3, shapeLen, littleEndian);
    }

    public static final int SHAPE = 0;
    public static final int DATA_ARRAY = 1;
    public static final boolean LITTLE_ENDIAN = true;
    public static final boolean BIG_ENDIAN = false;

    public synchronized static Object[] parse(byte[] data, boolean littleEndian) {
        String typeNameStr = getTpye(data);
        if (typeNameStr.equals("None")) return null;
        int shapeLen = getShapeLen(data);
        int dataLen = getDataLen(data);
        Object[] obj = new Object[2];

        if (PARSE_DEBUG)
            Log.d(TAG, "parse typeNameStr: " + typeNameStr + "\tshapeLen: " + shapeLen + "\tdataLen: " + dataLen);

        obj[SHAPE] = getShape(data, shapeLen, LITTLE_ENDIAN);
        ByteBuffer buffer = ByteBuffer.wrap(data, 8 * 3 + shapeLen, dataLen);
        if (littleEndian)
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // ByteBuffer 默认为大端(BIG_ENDIAN)模式
        switch (typeNameStr) {
            case "int32":
                obj[DATA_ARRAY] = parseInt32Arr(buffer);
                break;
            case "int64":
                obj[DATA_ARRAY] = parseInt64Arr(buffer);
                break;
            case "float32":
                obj[DATA_ARRAY] = parseFloat32Arr(buffer);
                break;
            case "float64":
                obj[DATA_ARRAY] = parseFloat64Arr(buffer);
                break;
        }
        return obj;
    }

    public synchronized static ByteBuffer loaderByteBuffer(byte[] data, int offset, int length, boolean littleEndian) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, length);
        if (littleEndian)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);  // ByteBuffer 默认为大端(BIG_ENDIAN)模式
        return byteBuffer;
    }

    /**********int32**********/
    public synchronized static int[] parseInt32Arr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseInt32Arr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static int[] parseInt32Arr(ByteBuffer buffer) {
        IntBuffer intBuffer = buffer.asIntBuffer();
        int i = 0;
        int[] intArr = new int[intBuffer.limit()];
        while (intBuffer.hasRemaining()) {
            if (PARSE_DEBUG) Log.d(TAG, "parse: doubleBuffer: " + intBuffer.position()
                    + " -> " + intBuffer.get(intBuffer.position()));
            intArr[i++] = intBuffer.get();
        }
        return intArr;
    }

    /**********int64**********/
    public synchronized static long[] parseLongArr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseLongArr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static long[] parseLongArr(ByteBuffer buffer) {
        return parseInt64Arr(buffer);
    }

    public synchronized static long[] parseInt64Arr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseInt64Arr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static long[] parseInt64Arr(ByteBuffer buffer) {
        LongBuffer longBuffer = buffer.asLongBuffer();
        int i = 0;
        long[] longArr = new long[longBuffer.limit()];
        while (longBuffer.hasRemaining()) {
            if (PARSE_DEBUG) Log.d(TAG, "parse: doubleBuffer: " + longBuffer.position()
                    + " -> " + longBuffer.get(longBuffer.position()));
            longArr[i++] = longBuffer.get();
        }
        return longArr;
    }


    public synchronized static float[] parseFloat32Arr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseFloat32Arr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static float[] parseFloat32Arr(ByteBuffer buffer) {
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        int i = 0;
        float[] floatArr = new float[floatBuffer.limit()];
        while (floatBuffer.hasRemaining()) {
            if (PARSE_DEBUG) Log.d(TAG, "parse: doubleBuffer: " + floatBuffer.position()
                    + " -> " + floatBuffer.get(floatBuffer.position()));
            floatArr[i++] = floatBuffer.get();
        }
        return floatArr;
    }

    public synchronized static double[] parseFloat64Arr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseDoubleArr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static double[] parseFloat64Arr(ByteBuffer buffer) {
        return parseDoubleArr(buffer);
    }

    public synchronized static double[] parseDoubleArr(byte[] data, int offset, int length, boolean littleEndian) {
        return parseDoubleArr(loaderByteBuffer(data, offset, length, littleEndian));
    }

    public synchronized static double[] parseDoubleArr(ByteBuffer buffer) {
        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
        int i = 0;
        double[] doubleArr = new double[doubleBuffer.limit()];
        while (doubleBuffer.hasRemaining()) {
            if (PARSE_DEBUG) Log.d(TAG, "parse: doubleBuffer: " + doubleBuffer.position()
                    + " -> " + doubleBuffer.get(doubleBuffer.position()));
            doubleArr[i++] = doubleBuffer.get();
        }
        return doubleArr;
    }

}
