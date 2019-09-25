package com.rockchip.inno.util_library;

import java.io.UnsupportedEncodingException;

public class bytesConversionTool {

    public synchronized static String byteArray2HexString(byte[] src) {
        return byteArray2HexString(src, src.length);
    }

    public synchronized static String byteArray2HexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length && i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (i > 0) {
                stringBuilder.append(" ");
            }
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public synchronized static String byteArray2Str(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray);
        return str;
    }

    public synchronized static String byteArray2UnicodeStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = null;
        try {
            str = new String(byteArray, "Unicode");
        } catch (UnsupportedEncodingException e) {
            str = "";
            e.printStackTrace();
        }
        return str;
    }

    public synchronized static String byteArray2UTF8Str(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = null;
        try {
            str = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            str = "";
            e.printStackTrace();
        }
        return str;
    }

    //byte与int转换
    public static byte int2Byte(int x) {
        return (byte) x;
    }

    public static int byte2Int(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    //byte[]与int转换
    public static int byteArray2Int(byte[] b) {
        return b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 24) & 0xFF)};
    }
    //byte[]与int转换
    public static long byteArray2Int64(byte[] b, int index) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (b[index+i] & 0xff)) << (8 * i);
        }
        return value;
    }

    /**
     * 浮点转换为字节
     *
     * @param f
     * @return
     */
    public static byte[] float2byte(float f) {
        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param index 开始位置
     * @return
     */
    public static float byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }
    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param index 开始位置
     * @return
     */
    public static float[] byte2floatArr(byte[] b, int index) {
        float[] floats = new float[(b.length-index)/4];
        for (int i=index;i<(b.length-4);i = i+4){
            floats[i/4] = byte2float(b,i);
        }
        return floats;
    }

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

}
