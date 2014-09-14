
package net.cattaka.android.humitemp4ble;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cattaka.android.humitemp4ble.binder.IHumiTempServiceWrapper;
import net.cattaka.android.humitemp4ble.bluetooth.MyBtListener;
import net.cattaka.android.humitemp4ble.bluetooth.MyGattCallback;
import net.cattaka.android.humitemp4ble.core.MyPreference;
import net.cattaka.android.humitemp4ble.data.HttpResultInfo;
import net.cattaka.android.humitemp4ble.data.RegisterResultInfo;
import net.cattaka.android.humitemp4ble.data.UserInfo;
import net.cattaka.android.humitemp4ble.db.DbHelper;
import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import net.cattaka.android.humitemp4ble.entity.HumiTempModel;
import net.cattaka.android.humitemp4ble.task.RegisterTask;
import net.cattaka.android.humitemp4ble.task.RegisterTask.IRegisterTaskListener;
import net.cattaka.android.humitemp4ble.task.UploadTask;
import net.cattaka.android.humitemp4ble.task.UploadTask.IUploadTaskListener;
import net.cattaka.android.humitemp4ble.util.AidlUtil;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.SparseArray;

public class HumiTempService extends Service implements IHumiTempServiceWrapper {
    private static final int EVENT_UPDATE_SENSOR_VALUES = 1;

    private static final int EVENT_UPLOAD = 2;

    private static final long INTERVAL_UPDATE_SENSOR_VALUES = 60000 * 5;

    private static final long INTERVAL_UPLOAD = 60000 * 60;
    
    private static final int NUM_OF_UPLOAD_ONCE = 300;

    private static class DeviceBundle {

        BluetoothDevice bluetoothDevice;

        BluetoothGatt gatt;

        DeviceModel deviceModel;

        MyGattCallbackEx myGattCallback;
        
        long lastRecordTime = 0;

        private DeviceBundle(BluetoothDevice bluetoothDevice, BluetoothGatt gatt,
                DeviceModel deviceModel, MyGattCallbackEx myGattCallback) {
            super();
            this.bluetoothDevice = bluetoothDevice;
            this.gatt = gatt;
            this.deviceModel = deviceModel;
            this.myGattCallback = myGattCallback;
            this.myGattCallback.deviceBundle = this;
        }

    }

    private class MyGattCallbackEx extends MyGattCallback implements MyBtListener {
        DeviceBundle deviceBundle;

        public MyGattCallbackEx() {
            super();
            this.setListener(this);
        }

        @Override
        public void onDisconnected() {
            mAddress2DeviceBundle.remove(deviceBundle.bluetoothDevice.getAddress());
        }

        public void onReceivePacket(int temperature, int humidity) {
            float t = (float)temperature / 100f;
            float h = (float)humidity / 100f;
            me.onReceivePacket(deviceBundle, t, h);
            // Toast.makeText(me, String.format("T=%f, H=%f", t, h),
            // Toast.LENGTH_SHORT).show();
        };

        @Override
        public void onRssi(int rssi) {
            // TODO Auto-generated method stub

        }
    }

    private static final Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            HumiTempService target = (HumiTempService)msg.obj;
            if (msg.what == EVENT_UPDATE_SENSOR_VALUES) {
                target.updateSensorValues();

                Message nextMessage = sHandler.obtainMessage(EVENT_UPDATE_SENSOR_VALUES, target);
                sendMessageDelayed(nextMessage, INTERVAL_UPDATE_SENSOR_VALUES);
            } else if (msg.what == EVENT_UPLOAD) {
                target.requestUpload();

                Message nextMessage = sHandler.obtainMessage(EVENT_UPLOAD, target);
                sendMessageDelayed(nextMessage, INTERVAL_UPLOAD);
            }
        };
    };

    private HumiTempService me = this;

    private DbHelper mDbHelper;

    private BluetoothAdapter mBluetoothAdapter;

    private Map<String, DeviceBundle> mAddress2DeviceBundle;

    private int mServiceListenersSeq = 0;

    private SparseArray<IHumiTempServiceListener> mServiceListeners;

    private MyPreference mPreference;

    private IBinder mBinder = new IHumiTempService.Stub() {

        @Override
        public List<DeviceModel> findDeviceModels() throws RemoteException {
            return me.findDeviceModels();
        }

        @Override
        public DeviceModel findDeviceModel(String uuid) throws RemoteException {
            return me.findDeviceModel(uuid);
        }

        @Override
        public boolean addDevice(BluetoothDevice device) throws RemoteException {
            return me.addDevice(device);
        }

        @Override
        public boolean removeDevice(String uuid) throws RemoteException {
            return me.removeDevice(uuid);
        }

        @Override
        public List<DeviceModel> updateSensorValues() throws RemoteException {
            return me.updateSensorValues();
        }

        @Override
        public int registerServiceListener(IHumiTempServiceListener listener)
                throws RemoteException {
            return me.registerServiceListener(listener);
        }

        @Override
        public boolean unregisterServiceListener(int seq) throws RemoteException {
            return me.unregisterServiceListener(seq);
        };

        @Override
        public boolean regiterUser(String username) throws RemoteException {
            return me.regiterUser(username);
        }

        @Override
        public boolean requestUpload() throws RemoteException {
            return me.requestUpload();
        }

        @Override
        public UserInfo getUserInfo() throws RemoteException {
            return me.getUserInfo();
        }
    };

    public void onCreate() {
        super.onCreate();
        mDbHelper = new DbHelper(this);
        mPreference = new MyPreference(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mServiceListeners = new SparseArray<IHumiTempServiceListener>();

        mAddress2DeviceBundle = new HashMap<String, HumiTempService.DeviceBundle>();

        sHandler.obtainMessage(EVENT_UPDATE_SENSOR_VALUES, this).sendToTarget();
        sHandler.obtainMessage(EVENT_UPLOAD, this).sendToTarget();

        {
            Intent serviceIntent = new Intent(this, TelnetSqliteService.class);
            startService(serviceIntent);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAll();

        sHandler.removeMessages(EVENT_UPDATE_SENSOR_VALUES, this);
        sHandler.removeMessages(EVENT_UPLOAD, this);

        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public List<DeviceModel> findDeviceModels() throws RemoteException {
        return mDbHelper.findDeviceModels();
    }

    @Override
    public DeviceModel findDeviceModel(String address) throws RemoteException {
        return mDbHelper.findDeviceModel(address);
    }

    @Override
    public boolean addDevice(BluetoothDevice device) throws RemoteException {
        String uuid = device.getAddress();
        DeviceModel oldDevice = mDbHelper.findDeviceModel(uuid);
        if (oldDevice != null) {
            return false;
        }
        DeviceModel model = new DeviceModel();
        model.setAddress(uuid);
        model.setName(uuid);
        return mDbHelper.registerDeviceModel(model);

    }

    @Override
    public boolean removeDevice(String uuid) throws RemoteException {
        return mDbHelper.deleteDeviceModel(uuid);
    }

    @Override
    public int registerServiceListener(IHumiTempServiceListener listener) throws RemoteException {
        mServiceListeners.put(++mServiceListenersSeq, listener);
        return mServiceListenersSeq;
    }

    public boolean unregisterServiceListener(int seq) throws RemoteException {
        boolean result = (mServiceListeners.get(seq) != null);
        mServiceListeners.remove(seq);
        return result;
    };

    @Override
    public IBinder asBinder() {
        // not used
        return null;
    }

    @Override
    public List<DeviceModel> updateSensorValues() {
        List<DeviceModel> result = new ArrayList<DeviceModel>();
        List<DeviceModel> deviceModels = mDbHelper.findDeviceModels();
        for (DeviceModel deviceModel : deviceModels) {
            DeviceBundle bundle = mAddress2DeviceBundle.get(deviceModel.getAddress());
            if (bundle != null) {
                continue;
            }
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceModel.getAddress());
            MyGattCallbackEx callback = new MyGattCallbackEx();
            BluetoothGatt gatt = device.connectGatt(this, false, callback);
            bundle = new DeviceBundle(device, gatt, deviceModel, callback);
            mAddress2DeviceBundle.put(deviceModel.getAddress(), bundle);
        }
        return result;
    }

    private void stopAll() {
        for (DeviceBundle bundle : mAddress2DeviceBundle.values()) {
            bundle.gatt.disconnect();
            bundle.gatt.close();
        }
        mAddress2DeviceBundle.clear();
    }

    public void onReceivePacket(DeviceBundle bundle, float temperature, float humidity) {
        Date date = new Date();
        final DeviceModel deviceModel = bundle.deviceModel;
        {
            deviceModel.setLastHumidity(humidity);
            deviceModel.setLastTemplature(temperature);
            deviceModel.setLastUpdate(date);
            mDbHelper.registerDeviceModel(deviceModel);
        }

        if (date.getTime() - bundle.lastRecordTime > INTERVAL_UPDATE_SENSOR_VALUES) {
        	bundle.lastRecordTime = date.getTime();
            HumiTempModel model = new HumiTempModel();
            model.setDeviceId(bundle.deviceModel.getId());
            model.setDate(date);
            model.setHumidity(humidity);
            model.setTemperature(temperature);
            mDbHelper.registerHumiTempModel(model);
        }

        AidlUtil.callMethods(mServiceListeners,
                new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                    @Override
                    public boolean run(IHumiTempServiceListener item) throws RemoteException {
                        item.onDeviceModelUpdated(deviceModel);
                        return true;
                    }
                });
    }

    private RegisterTask mRunningTask;

    private UploadTask mUploadTask;

    public boolean regiterUser(String username) {
        UserInfo userInfo = mPreference.getUserInfo();
        if (userInfo != null) {
            return false;
        }
        if (mRunningTask != null) {
            return false;
        }
        mRunningTask = new RegisterTask(new IRegisterTaskListener() {
            @Override
            public void onRegisterTaskStart() {
                AidlUtil.callMethods(mServiceListeners,
                        new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                            public boolean run(IHumiTempServiceListener item)
                                    throws RemoteException {
                                item.onWebEvent(Constants.WEB_EVENT_REGISTERING_BEGIN);
                                return true;
                            };
                        });
            }

            @Override
            public void onRegisterTaskFinish(RegisterResultInfo info) {
                mRunningTask = null;
                if (info != null && info.isResult()) {
                    mPreference.edit();
                    mPreference.putUserInfo(info.toUserInfo());
                    mPreference.commit();
                    AidlUtil.callMethods(mServiceListeners,
                            new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                                public boolean run(IHumiTempServiceListener item)
                                        throws RemoteException {
                                    item.onWebEvent(Constants.WEB_EVENT_REGISTERING_FINISHED);
                                    return true;
                                };
                            });
                } else {
                    AidlUtil.callMethods(mServiceListeners,
                            new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                                public boolean run(IHumiTempServiceListener item)
                                        throws RemoteException {
                                    item.onWebEvent(Constants.WEB_EVENT_REGISTERING_FAILED);
                                    return true;
                                };
                            });
                }
            }
        }, username);
        mRunningTask.execute();
        return true;
    }

    public boolean requestUpload() {
        UserInfo userInfo = mPreference.getUserInfo();
        if (userInfo == null) {
            return false;
        }
        if (mUploadTask != null) {
            return false;
        }
        final List<HumiTempModel> models = mDbHelper.findHumiTempModelBySendFlag(false, NUM_OF_UPLOAD_ONCE);
        if (models.size() == 0) {
            return false;
        }

        mUploadTask = new UploadTask(new IUploadTaskListener() {

            @Override
            public void onUploadTaskStart() {
                AidlUtil.callMethods(mServiceListeners,
                        new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                            public boolean run(IHumiTempServiceListener item)
                                    throws RemoteException {
                                item.onWebEvent(Constants.WEB_EVENT_UPLOADING_BEGIN);
                                return true;
                            };
                        });
            }

            @Override
            public void onUploadTaskFinish(HttpResultInfo info) {
                mUploadTask = null;
                if (info != null && info.isResult()) {
                    if (info.getExtra() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Long> ids = (List<Long>)info.getExtra();
                        mDbHelper.updateHumiTempModelSendFlag(true, ids);
                    }

                    AidlUtil.callMethods(mServiceListeners,
                            new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                                public boolean run(IHumiTempServiceListener item)
                                        throws RemoteException {
                                    item.onWebEvent(Constants.WEB_EVENT_UPLOADING_FINISHED);
                                    return true;
                                };
                            });
                    if (models.size() == NUM_OF_UPLOAD_ONCE) {
                    	me.requestUpload();
                    }
                } else {
                    AidlUtil.callMethods(mServiceListeners,
                            new AidlUtil.CallFunction<IHumiTempServiceListener>() {
                                public boolean run(IHumiTempServiceListener item)
                                        throws RemoteException {
                                    item.onWebEvent(Constants.WEB_EVENT_UPLOADING_FAILED);
                                    return true;
                                };
                            });
                }
            }
        }, userInfo);

        HumiTempModel[] args = models.toArray(new HumiTempModel[models.size()]);
        mUploadTask.execute(args);

        return true;
    }

    @Override
    public UserInfo getUserInfo() throws RemoteException {
        return mPreference.getUserInfo();
    }
}
