package com.rockchip.inno.master_yolov3_libusb;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by cxh on 2019/1/21
 * E-mail: shon.chen@rock-chips.com
 */
public class usbStateReceiver extends BroadcastReceiver {
    private static String TAG = "usbStateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
//        context.unregisterReceiver(this);

        String action = intent.getAction();
        //USB连接上手机时会发送广播android.hardware.usb.action.USB_STATE"及UsbManager.ACTION_USB_DEVICE_ATTACHED
        if (action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED) | action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {//判断其中一个就可以了
//            Toast.makeText(MainActivity.this, "USB已经连接！", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "USB已经连接！");

            ComponentName component = new ComponentName("com.rockchip.inno.master_yolov3_libusb", "com.rockchip.inno.master_yolov3_libusb.MainActivity");
            Intent startActivityIntent = new Intent();
            startActivityIntent.setComponent(component);
//            Intent startActivityIntent = new Intent(context, MainActivity.class);
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startActivityIntent);
        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {//USB被拔出
//            Toast.makeText(MainActivity.this, "USB连接断开！", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "USB连接断开！");
            android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
            System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
//            finish();
        }

        if (action.equals(usbDevicesManagerUtil.ACTION_USB_PERMISSION)) {
            synchronized (this) {
                UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //user choose YES for your previously popup window asking for grant perssion for this usb device
//                    if (null != usbDevice) {
//                        if (mUsbDevicesManagerUtil != null) {
//                            mUsbDevicesManagerUtil.openDevice(usbDevice);
//                            Log.d(TAG, "onReceive: " + String.valueOf("Got permission for usb device: " + usbDevice));
//                            Log.d(TAG, "onReceive: " + String.valueOf("Found USB device: VID=" + usbDevice.getVendorId()
//                                    + " PID=" + usbDevice.getProductId()));
//                            Toast.makeText(context, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
//                            Toast.makeText(context, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId()
//                                    + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();
//                        }
//                    }
                } else {
                    //user choose NO for your previously popup window asking for grant perssion for this usb device
                    Log.d(TAG, String.valueOf("Permission denied for device" + usbDevice));
                    Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
