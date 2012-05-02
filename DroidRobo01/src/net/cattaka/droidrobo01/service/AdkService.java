
package net.cattaka.droidrobo01.service;

import java.io.IOException;

import jp.co.kayo.ykmjuku.andylib.cmd.AndyCommand;
import jp.co.kayo.ykmjuku.andylib.component.DeviceComponent.OnMessageReceiver;
import jp.co.kayo.ykmjuku.andylib.net.CommandMessage;
import jp.co.kayo.ykmjuku.andylib.net.USBConnection;
import jp.co.kayo.ykmjuku.andylib.tools.Logger;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AdkService extends Service {
    private static final String ACTION_USB_PERMISSION = "net.cattaka.robotarm01.action.USB_PERMISSION";

    private boolean mPermissionRequestPending;

    private PendingIntent mPermissionIntent;

    private UsbAccessory mAccessory;

    private UsbManager mUsbManager;

    private USBConnection mUSBCon;

    private IAdkService.Stub mAdkService = new IAdkService.Stub() {
        @Override
        public boolean sendCommand(byte cmd, byte addr, byte[] data) throws RemoteException {
            if (!mRunning) {
                return false;
            }
            try {
                AndyCommand msg = new AndyCommand(cmd, addr, data);
                return mUSBCon.sendCommand(msg);
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public boolean isConnected() throws RemoteException {
            return mRunning;
        }

        @Override
        public boolean connectDevice() throws RemoteException {
            return AdkService.this.connectDevice();
        }
    };

    private boolean mRunning;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    Logger.d("onReceive accessory:" + accessory);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Logger.d("permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    };

    private OnMessageReceiver mMessageReceiver;

    private Handler mUsbReceiveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mMessageReceiver != null) {
                mMessageReceiver.onReceive((CommandMessage)msg.obj);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        {
            try {
                mUsbManager = UsbManager.getInstance(this);
            } catch (Exception e) {
                Logger.e(
                        "<uses-library android:name=\"com.android.future.usb.accessory\" />をマニフェストに追加してますか？",
                        e);
            }
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
            registerReceiver(mUsbReceiver, filter);
        }
        {
            mUSBCon = new USBConnection();
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        connectDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAccessory();
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAdkService;
    }

    private boolean connectDevice() {
        if (mRunning) {
            return true;
        } else {
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            UsbAccessory accessory = (accessories == null ? null : accessories[0]);
            Logger.d("onResume accessory:" + accessory);
            if (accessory != null) {
                if (mUsbManager.hasPermission(accessory)) {
                    Logger.d("onResume openAccessory START");
                    openAccessory(accessory);
                } else {
                    Logger.d("permission ERROR?");
                    synchronized (mUsbManager) {
                        if (!mPermissionRequestPending) {
                            mUsbManager.requestPermission(accessory, mPermissionIntent);
                            mPermissionRequestPending = true;
                        }
                    }
                }
                return true;
            } else {
                Logger.d("not attachd USB.");
                return false;
            }
        }
    }

    public boolean sendCommand(CommandMessage msg) throws IOException {
        return mUSBCon.sendCommand(msg);
    }

    private void openAccessory(UsbAccessory accessory) {
        Logger.d("openAccessory  accessory:" + accessory.toString());
        ParcelFileDescriptor fd = mUsbManager.openAccessory(accessory);
        Logger.d("openAccessory  fd:" + fd);
        if (fd != null) {
            mAccessory = accessory;
            mUSBCon.start(fd, mUsbReceiveHandler);
            mRunning = true;
        } else {
            Logger.d("accessory open fail");
        }
    }

    private void closeAccessory() {
        try {
            mRunning = false;
            mUSBCon.stop();
        } finally {
            // mAccessory = null;
        }
    }

}
