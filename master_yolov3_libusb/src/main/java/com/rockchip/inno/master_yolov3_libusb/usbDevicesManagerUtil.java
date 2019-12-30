package com.rockchip.inno.master_yolov3_libusb;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.rockchip.inno.util_library.bytesConversionTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.hardware.usb.UsbConstants.USB_DIR_IN;

/**
 * Created by cxh on 2018/3/12
 * E-mail: shon.chen@rock-chips.com
 */

public class usbDevicesManagerUtil {

    private static String TAG = "usbDevicesManagerUtil";

    private Context mContext;
    private static boolean DEBUG = false;
    private static int TARGET_PRODUCT_ID = 0x0018;
    private static int TARGET_VENDOR_ID = 0x2207;

    //设备列表
    private UsbManager usbManager;
    //满足的设备
    private UsbDevice myUsbDevice;
    //usb接口
    private UsbInterface usbInterface;
    //块输出端点_0
    private UsbEndpoint epBulkOut_0;
    private UsbEndpoint epBulkIn_0;
    //控制端点
    private UsbEndpoint epControl;
    //中断端点
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epIntEndpointIn;

    private final static int LIBUSB_CLASS_1808_InterfaceSubclass = 0x08;
    private final static int LIBUSB_CLASS_1808_InterfaceProtocol = 0x02;
    // 连接
    private UsbDeviceConnection myDeviceConnection;

    public usbDevicesManagerUtil(Context context) {
        mContext = context;
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }


    /**
     * 枚举设备
     */
    public String enumeraterDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        StringBuilder sb = new StringBuilder();
        sb.append("java USB device enumeration\n");

        sb.append(String.format(Locale.getDefault(), "Detected %1$d USB devices : ", deviceList.size()));
        for (UsbDevice device : deviceList.values()) {
//            Log.d(TAG, "enumeraterDevices:  " +device.toString());
            sb.append(devicesString(device));
            if (device.getVendorId() == TARGET_VENDOR_ID && device.getProductId() == TARGET_PRODUCT_ID) {
                myUsbDevice = device; // 获取USBDevice
            }
        }
//        if (DEBUG) MyLog(TAG, "enumeraterDevices: " + sb.toString());
        return sb.toString();
    }

    //规定每段显示的长度
    private static int LOG_MAXLENGTH = 2000;

    public static void MyLog(String TAG, String msg) {
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                Log.d(TAG + i, msg.substring(start, end));
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                Log.d(TAG, msg.substring(start, strLength));
                break;
            }
        }
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String devicesString(UsbDevice device) {
        StringBuilder sb = new StringBuilder("\n\nUsbDevice = {");
        sb.append(stringFormat("\n\tName...............: %s", device.getDeviceName()));

        sb.append(stringFormat("\n\tDeviceId...........: %1$d\t(0x%1$04x)", device.getVendorId()));
        sb.append(stringFormat("\n\tVendorId...........: %1$d\t(0x%1$04x)", device.getVendorId()));
        sb.append(stringFormat("\n\tProductId..........: %1$d\t(0x%1$04x)", device.getProductId()));
        LibUSBClassCode usbClassCode = LibUSBClassCode.getCode(device.getDeviceClass());
        sb.append(stringFormat("\n\tClass..............: %1$d - (0x%1$02x - %2$s)", usbClassCode.getValue(), usbClassCode.name()));
        sb.append(stringFormat("\n\tSubclass...........: %1$d\t(0x%1$04x)", device.getDeviceSubclass()));
        sb.append(stringFormat("\n\tProtocol...........: %1$d\t(0x%1$04x)", device.getDeviceProtocol()));
        sb.append(stringFormat("\n\tManufacturerName...: %s", device.getManufacturerName()));
        sb.append(stringFormat("\n\tgetProductName.....: %s", device.getProductName()));
        sb.append(stringFormat("\n\tVersion............: %s", device.getVersion()));
        sb.append(stringFormat("\n\tSerialNumber.......: %s", device.getSerialNumber()));
        sb.append(stringFormat("\n\tDetected %1$d Configuration", device.getConfigurationCount()));
//        Log.d(TAG, "devicesString: " + sb.toString());
        sb.append(configurationsString(device));
        sb.append(stringFormat("\n\tDetected %1$d InterfaceCount", device.getInterfaceCount()));
//        Log.d(TAG, "devicesString: " + stringFormat("\n\tDetected %1$d InterfaceCount", device.getInterfaceCount()));
        sb.append(usbInterfaceString(device));
        sb.append("\n}");
        return sb.toString();
    }

    private String configurationsString(UsbDevice device) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < device.getConfigurationCount(); i++) {
            UsbConfiguration mUsbConfiguration = device.getConfiguration(i);
            sb.append(stringFormat("\n\tUsbConfiguration[%d] = {", i));
            sb.append(stringFormat("\n\t\tId..................: %1$d\t(0x%1$04x)", mUsbConfiguration.getId()));
            sb.append(stringFormat("\n\t\tName................: %s", mUsbConfiguration.getName()));
//            sb.append(stringFormat("\n\t\tMaxPower............: %s", mUsbConfiguration.getMaxPower())); ,mAttributes=192,m
            sb.append(stringFormat("\n\t\tMaxPower............: %1$d\t(0x%1$04x)", mUsbConfiguration.getMaxPower()));
//            sb.append(stringFormat("\n\t\tInterfaceCount......: %d", mUsbConfiguration.get()));
            sb.append("\n\t}");
        }
//        Log.d(TAG, "configurationsString: " + sb.toString());
        return sb.toString();
    }

    private String usbInterfaceString(UsbDevice device) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            StringBuilder sb2 = new StringBuilder();
            UsbInterface mUsbInterface = device.getInterface(i);
            sb.append(stringFormat("\n\tUsbInterface[%d] = {", i));
            sb.append(stringFormat("\n\t\tId..................: %1$d\t(0x%1$04x)", mUsbInterface.getId()));
            sb.append(stringFormat("\n\t\tAlternateSetting....: %1$d\t(0x%1$04x)", mUsbInterface.getAlternateSetting()));
            sb.append(stringFormat("\n\t\tName................: %s", mUsbInterface.getName()));

            LibUSBClassCode usbClassCode = LibUSBClassCode.getCode(mUsbInterface.getInterfaceClass());
            sb.append(stringFormat("\n\t\tClass...............: %1$d - (0x%1$02x - %2$s)", usbClassCode.getValue(), usbClassCode.name()));
            sb.append(stringFormat("\n\t\tInterfaceSubclass...: %1$d", mUsbInterface.getInterfaceSubclass()));
            sb.append(stringFormat("\n\t\tInterfaceProtocol...: %1$d", mUsbInterface.getInterfaceProtocol()));
            sb.append(stringFormat("\n\t\tDetected %1$d EndpointCount", mUsbInterface.getEndpointCount()));

//            sb2.append(stringFormat("\n\tUsbInterface[%d] = {", i));
//            sb2.append(stringFormat("\n\t\tId..................: %1$d\t(0x%1$04x)", mUsbInterface.getId()));
//            sb2.append(stringFormat("\n\t\tAlternateSetting....: %1$d\t(0x%1$04x)", mUsbInterface.getAlternateSetting()));
//            sb2.append(stringFormat("\n\t\tName................: %s", mUsbInterface.getName()));
//
//
//            sb2.append(stringFormat("\n\t\tClass...............: %1$d - (0x%1$02x - %2$s)", usbClassCode.getValue(), usbClassCode.name()));
//            sb2.append(stringFormat("\n\t\tInterfaceSubclas....: %1$d", mUsbInterface.getInterfaceSubclass()));
//            sb2.append(stringFormat("\n\t\tInterfaceProtocol...: %1$d", mUsbInterface.getInterfaceProtocol()));
//            sb2.append(stringFormat("\n\t\tDetected %1$d EndpointCount", mUsbInterface.getEndpointCount()));
//            Log.d(TAG, "usbInterfaceString: " + sb2.toString());
            sb.append(usbEndpointString(mUsbInterface));
            sb.append("\n\t}");
        }
        return sb.toString();
    }

    private String usbEndpointString(UsbInterface mUsbInterface) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = mUsbInterface.getEndpoint(i);
            sb.append(stringFormat("\n\t\tUsbEndpoint[%d] = {", i));
            sb.append(stringFormat("\n\t\t\tDirection............: %1$d\t(0x%1$02x) (%2$s)", ep.getDirection(), getDirectionName(ep)));
            sb.append(stringFormat("\n\t\t\tType.................: %1$d\t(0x%1$02x) (%2$s)", ep.getType(), getTypeName(ep)));
            sb.append(stringFormat("\n\t\t\tAddress..............: %1$d\t(0x%1$02x) (0b%2$s)", ep.getAddress(), lpad(Integer.toBinaryString(ep.getAddress()), 8, "0")));
            sb.append(stringFormat("\n\t\t\tAttributes...........: %1$d\t(0x%1$02x) (0b%2$s)", ep.getAttributes(), lpad(Integer.toBinaryString(ep.getAttributes()), 8, "0")));
            sb.append(stringFormat("\n\t\t\tMaxPacketSize........: %1$d", ep.getMaxPacketSize()));
            sb.append(stringFormat("\n\t\t\tInterval.............: %1$d", ep.getInterval()));
            sb.append(stringFormat("\n\t\t\tEndpointNumber.......: %1$d", ep.getEndpointNumber()));
//            sb.append(stringFormat("\n\t\t\tDirection............: %1$d\t(0x%1$04x)", ep.getDirection()));
            sb.append("\n\t\t}");
        }
//        if (DEBUG)
//            Log.d(TAG, "usbEndpointString: " + sb.toString());
        return sb.toString();
    }

    private String getDirectionName(UsbEndpoint endPoint) {
        switch (endPoint.getDirection()) {
            case UsbConstants.USB_DIR_IN:
                return "device to host";
            case UsbConstants.USB_DIR_OUT:
                return "host to device";
            default:
                return "unknown";
        }
    }

    private String getTypeName(UsbEndpoint endPoint) {
        switch (endPoint.getType()) {
            case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                return "control";
            case UsbConstants.USB_ENDPOINT_XFER_ISOC:
                return "isochronous";
            case UsbConstants.USB_ENDPOINT_XFER_BULK:
                return "bulk";
            case UsbConstants.USB_ENDPOINT_XFER_INT:
                return "interrupt";
            default:
                return "unknown";
        }
    }

    private String lpad(String string, int size, String character) {
        if (string.length() > size) {
            return string.substring(0, size - 3) + "...";
        }
        StringBuilder pad = new StringBuilder(size);
        for (int c = string.length(); c < size; ++c) {
            pad.append(character);
        }
        pad.append(string);
        return pad.toString();
    }

    private String stringFormat(String format, Object... args) {
        return String.format(Locale.getDefault(), format, args);
    }

    /**
     * 获取设备的接口
     * 分配端点，IN | OUT，即输入输出；可以通过判断
     */
    private boolean assignEndpoint() {
        if (myUsbDevice == null) {
            return false;
        }

        for (int interfaceIndex = 0; interfaceIndex < myUsbDevice.getInterfaceCount(); interfaceIndex++) {
            UsbInterface mUsbInterface = myUsbDevice.getInterface(interfaceIndex);
            LibUSBClassCode usbClassCode = LibUSBClassCode.getCode(mUsbInterface.getInterfaceClass());
            if (usbClassCode.getValue() == LibUSBClassCode.LIBUSB_CLASS_1808_NPU.getValue()
                    && mUsbInterface.getInterfaceSubclass() == LIBUSB_CLASS_1808_InterfaceSubclass
                    && mUsbInterface.getInterfaceProtocol() == LIBUSB_CLASS_1808_InterfaceProtocol) {

                usbInterface = myUsbDevice.getInterface(interfaceIndex);
                for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = usbInterface.getEndpoint(i);
                    switch (ep.getType()) {
                        case UsbConstants.USB_ENDPOINT_XFER_BULK://块
                            if (UsbConstants.USB_DIR_IN == ep.getDirection()) {
                                epBulkIn_0 = ep;
                                Log.d(TAG, "Find the BulkEndpointIn:" + "\tindex:" + i + ",\t使用端点号：" + epBulkIn_0.getEndpointNumber() + ",\tAddress:" + ep.getAddress());
                            } else {//输出
                                epBulkOut_0 = ep;
                                if (DEBUG)
                                    Log.d(TAG, "Find the BulkEndpointOut," + "\tindex:" + i + ",\t使用端点号：" + epBulkOut_0.getEndpointNumber() + ",\tAddress:" + ep.getAddress());
                            }
                            break;
                        case UsbConstants.USB_ENDPOINT_XFER_CONTROL://控制
                            epControl = ep;
                            if (DEBUG)
                                Log.d(TAG, "Find the ControlEndPoint:" + "index:" + i + "," + epControl.getEndpointNumber());
                            break;
                        case UsbConstants.USB_ENDPOINT_XFER_INT://中断
                            if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                                epIntEndpointOut = ep;
                                if (DEBUG)
                                    Log.d(TAG, "Find the InterruptEndpointOut:" + "index:" + i + "," + epIntEndpointOut.getEndpointNumber());
                            }
                            if (ep.getDirection() == USB_DIR_IN) {
                                epIntEndpointIn = ep;
                                if (DEBUG)
                                    Log.d(TAG, "Find the InterruptEndpointIn:" + "index:" + i + "," + epIntEndpointIn.getEndpointNumber());
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            }
        }
        return true;
    }

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    /**
     * 连接设备
     */
    public boolean openDevice() {
//        if (DEBUG) Log.d(TAG, "openUsbDevice: \n" + usbInterface.toString());
//        if (usbInterface != null) {//接口是否为null
//            openDevice(myUsbDevice);
//        }
        if (assignEndpoint()) {
            return openDevice(myUsbDevice);
        }
        return false;
    }

    public boolean openDevice(UsbDevice usbDevice) {
        // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
        UsbDeviceConnection conn = null;
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if (usbManager.hasPermission(usbDevice)) {//有权限，那么打开
            conn = usbManager.openDevice(usbDevice);
        } else {
            usbManager.requestPermission(usbDevice, mPermissionIntent);
        }
        if (null == conn) {
            Log.d(TAG, "openUsbDevice: 不能连接到设备");
            return false;
        }
        //打开设备
        if (usbInterface == null) return false;
        if (conn.claimInterface(usbInterface, true)) {
            myDeviceConnection = conn;
            MyLog(TAG, "open设备成功" + devicesString(myUsbDevice));
        } else {
            Log.d(TAG, "无法打开连接通道。");
            conn.close();
            return false;
        }
        return true;
    }


    /**
     * 发送控制数据
     *
     * @param buffer
     */
    public boolean sendControlMessageToPoint(byte[] buffer) {
        int ret = myDeviceConnection.controlTransfer(1 << 5 | 1, 0x1f, 0, 0, buffer, buffer.length, 0);
        if (ret >= 0) {
            return true;
        } else {
            return false;
        }
    }

    private final int MAX_BULK_TRANSFER_BUFF = 16000;
    // 安卓bulkTransfer接口有16k的收发限制所以我们在使用bulkTransfer时需要注意数据的长度不能够超过16k
    // 且接收时接收的长度不能小于设备发送的长度，否则会出错这里我们采用统一接收最大长度，
    // 然后判断实际接收长度的方法来接收数据

    private static boolean isSend = true;

    /**
     * 发送块数据
     *
     * @param buffer
     */
    public void sendBulkMessageToPoint(final byte[] buffer) {
        if (isSend) {
            new Thread() {
                @Override
                public void run() {
                    isSend = false;
//                    Log.d(TAG, "sendBulkMessageToPoint: " + bytesConversionTool.byteArray2HexString(buffer, 21));
//                    Log.d(TAG, "sendBulkMessageToPoint: " + bytesConversionTool.byteArray2Str(buffer).substring(0, 21));
////                    if (DEBUG) Log.d(TAG, "usbdata sendMessageToPoint: " + epBulkOut_0.toString());
                    myDeviceConnection.controlTransfer(1 << 5 | 1, 0x1f, 0, 0, buffer, buffer.length, 0);

                    int len = 16;
                    String str2 = String.format("start%01$-" + len + "s", String.valueOf(buffer.length));

                    if (DEBUG) Log.d(TAG, "sendBulkMessageToPoint 准备发送数据头 " + str2);
                    if (myDeviceConnection.bulkTransfer(epBulkOut_0, str2.getBytes(),  str2.getBytes().length, -1) >= 0) {
                        if (DEBUG)
                            Log.d(TAG, "sendBulkMessageToPoint sendMessageToPoint: 发送成功" + buffer.length);//0 或者正数表示成功
                    } else {
                        if (DEBUG) Log.d(TAG, "sendBulkMessageToPoint send error!!!");
                        return;
                    }

                    int count = 0;
                    while (count < buffer.length) {
                        int sendLen = 0;
                        if ((buffer.length - count) > MAX_BULK_TRANSFER_BUFF) {
                            sendLen = MAX_BULK_TRANSFER_BUFF;
                        } else {
                            sendLen = buffer.length - count;
                        }
                        if (DEBUG) Log.d(TAG, "sendBulkMessageToPoint 准备发送数据 " + sendLen);
                        if (myDeviceConnection.bulkTransfer(epBulkOut_0, buffer, count, sendLen, -1) >= 0) {
                            if (DEBUG)
                                Log.d(TAG, "sendBulkMessageToPoint sendMessageToPoint: 发送成功" + buffer.length);//0 或者正数表示成功
                        } else {
                            if (DEBUG) Log.d(TAG, "sendBulkMessageToPoint send error!!!");
                        }
                        count += sendLen;
                    }

                    isSend = true;
                }
            }.start();
        }

    }

    boolean isReceiveBulkMessage_0 = false;
    /**
     * 接收块数据
     */


    private byte[] buffer = new byte[MAX_BULK_TRANSFER_BUFF * 100];// 缓冲区字节数组，信息不能大于此缓冲区

    public void receiveBulkMessageToPoint() {

        if (isReceiveBulkMessage_0) return;
        isReceiveBulkMessage_0 = true;
        new Thread() {
            @Override
            public void run() {
                while (isReceiveBulkMessage_0) {
                    try {
                        Log.d(TAG, "receiveBulkMessageToPoint: 开始接收~~~~~~~~~");
                        int ret = myDeviceConnection.bulkTransfer(epBulkIn_0, buffer, MAX_BULK_TRANSFER_BUFF, 5000);
                        if (ret > 0) {
                            String str = bytesConversionTool.byteArray2UTF8Str(buffer);
                            Log.d(TAG, "receiveBulkMessageToPoint: 收到长度头" + bytesConversionTool.byteArray2UTF8Str(buffer));
                            if (!"start".equals(str.substring(0, 5))) {
                                continue;
                            }
                            ret = Integer.parseInt(str.substring(5, 21).replace(" ", ""));// 缓冲区字节数组，信息不能大于此缓冲区
                            int count = 0;
                            Log.d(TAG, "receiveBulkMessageToPoint: 即将接收数据长度 = " + ret);
                            while (count < ret) {
                                int recLen = 0;
                                if ((ret - count) > MAX_BULK_TRANSFER_BUFF) {
                                    recLen = MAX_BULK_TRANSFER_BUFF;
                                } else {
                                    recLen = ret - count;
                                }
//                            Log.d(TAG, "run: count = " + count + "   recLen = " + recLen);
                                int ret_t = myDeviceConnection.bulkTransfer(epBulkIn_0, buffer, count, MAX_BULK_TRANSFER_BUFF, 1000);
                                if (recLen != ret_t) {
                                    continue;
                                }
                                count += recLen;
                            }
                            Log.d(TAG, "receiveBulkMessageToPoint: 收到数据 str【" + bytesConversionTool.byteArray2Str(buffer).substring(0, ret) + "】");

                            Log.d(TAG, "receiveBulkMessageToPoint: 收到数据 hex【" + bytesConversionTool.byteArray2HexString(buffer, ret) + "】");

                            int use = 0;
                            byte[] countArr = new byte[8];
                            System.arraycopy(buffer, 0, countArr, 0, countArr.length);
                            use += countArr.length;
                            Log.d(TAG, "receiveBulkMessageToPoint: " + bytesConversionTool.byteArray2HexString(countArr));
                            int[] buf = new int[Integer.parseInt(bytesConversionTool.byteArray2UTF8Str(countArr).replace(" ", ""))];// 缓冲区字节数组，信息不能大于此缓冲区

                            for (int i = 0; i < buf.length; i++) {
//                            Log.d(TAG, "run:  i = " + i);
                                byte[] buf_t = new byte[4];
                                buf_t[0] = buffer[use + i * 4];
                                buf_t[1] = buffer[use + i * 4 + 1];
                                buf_t[2] = buffer[use + i * 4 + 2];
                                buf_t[3] = buffer[use + i * 4 + 3];
                                buf[i] = bytesConversionTool.byteArray2Int(buf_t);
                                Log.d(TAG, "receiveBulkMessageToPoint read: + buf[" + i + "] = " + buf[i]);
                            }
                            use += buf.length * 4;
                            List<byte[]> receiveList = new ArrayList<>();
                            for (int i1 : buf) {
                                byte[] tmp = new byte[i1];
                                System.arraycopy(buffer, use, tmp, 0, i1);
                                use += i1;
                                Log.d(TAG, "receiveBulkMessageToPoint receiveList: " + bytesConversionTool.byteArray2HexString(tmp));
                                receiveList.add(tmp);
                            }
                            listener.OnTsFrame(receiveList);
                        } else {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "receiveBulkMessageToPoint: 接收错误" + ret);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "receiveBulkMessageToPoint error: " + e.toString());

                    }
                }
                Log.e(TAG, "receiveBulkMessageToPoint 已退出");
            }
        }.start();
    }


    public void stopRecBulkMessage() {
        isReceiveBulkMessage_0 = false;
    }

    public void onDestroy() {
        stopRecBulkMessage();
    }

    private OnFrameListener listener;

    public void setOnFrameListener(OnFrameListener listener) {
        this.listener = listener;
    }

    public interface OnFrameListener {
        void OnTsFrame(List<byte[]> buffer);
    }

}
