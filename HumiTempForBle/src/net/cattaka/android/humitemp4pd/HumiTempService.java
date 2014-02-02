
package net.cattaka.android.humitemp4pd;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cattaka.android.humitemp4pd.binder.IHumiTempServiceWrapper;
import net.cattaka.android.humitemp4pd.bluetooth.MyBtListener;
import net.cattaka.android.humitemp4pd.bluetooth.MyGattCallback;
import net.cattaka.android.humitemp4pd.db.DbHelper;
import net.cattaka.android.humitemp4pd.entity.DeviceModel;
import net.cattaka.android.humitemp4pd.entity.HumiTempModel;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;

public class HumiTempService extends Service implements IHumiTempServiceWrapper {
    private static final int EVENT_UPDATE_SENSOR_VALUES = 1;

    private static final int INTERVALUPDATE_SENSOR_VALUES = 60000;

    private static class DeviceBundle {
        boolean needRecord = false;

        BluetoothDevice bluetoothDevice;

        BluetoothGatt gatt;

        DeviceModel deviceModel;

        MyGattCallbackEx myGattCallback;

        private DeviceBundle(BluetoothDevice bluetoothDevice, BluetoothGatt gatt,
                DeviceModel deviceModel, MyGattCallbackEx myGattCallback, boolean needRecord) {
            super();
            this.bluetoothDevice = bluetoothDevice;
            this.gatt = gatt;
            this.deviceModel = deviceModel;
            this.myGattCallback = myGattCallback;
            this.myGattCallback.deviceBundle = this;
            this.needRecord = needRecord;
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
            me.onReceivePacket(deviceBundle, temperature, humidity);
            Toast.makeText(me, String.format("T=%d, H=%d", temperature, humidity),
                    Toast.LENGTH_SHORT).show();
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
                target.updateSensorValues(true, true);

                Message nextMessage = sHandler.obtainMessage(EVENT_UPDATE_SENSOR_VALUES, target);
                sendMessageDelayed(nextMessage, INTERVALUPDATE_SENSOR_VALUES);
            }
        };
    };

    private HumiTempService me = this;

    private DbHelper mDbHelper;

    private BluetoothAdapter mBluetoothAdapter;

    private Map<String, DeviceBundle> mAddress2DeviceBundle;

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
    };

    public void onCreate() {
        super.onCreate();
        mDbHelper = new DbHelper(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mAddress2DeviceBundle = new HashMap<String, HumiTempService.DeviceBundle>();

        sHandler.obtainMessage(EVENT_UPDATE_SENSOR_VALUES, this).sendToTarget();

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
    public IBinder asBinder() {
        // not used
        return null;
    }

    @Override
    public List<DeviceModel> updateSensorValues() {
        return updateSensorValues(false, false);
    }

    public List<DeviceModel> updateSensorValues(boolean resetAll, boolean needRecord) {
        if (resetAll) {
            stopAll();
        }

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
            bundle = new DeviceBundle(device, gatt, deviceModel, callback, needRecord);
            mAddress2DeviceBundle.put(deviceModel.getAddress(), bundle);
        }
        return result;
    }

    private void stopAll() {
        for (DeviceBundle bundle : mAddress2DeviceBundle.values()) {
            bundle.gatt.close();
        }
        mAddress2DeviceBundle.clear();
    }

    public void onReceivePacket(DeviceBundle bundle, float temperature, float humidity) {
        Date date = new Date();
        {
            DeviceModel model = bundle.deviceModel;
            model.setLastHumidity((float)humidity / 100f);
            model.setLastTemplature((float)temperature / 100f);
            model.setLastUpdate(date);
            mDbHelper.registerDeviceModel(model);
        }

        if (bundle.needRecord) {
            HumiTempModel model = new HumiTempModel();
            model.setDeviceId(bundle.deviceModel.getId());
            model.setDate(date);
            model.setHumidity(humidity);
            model.setTemperature(temperature);
            mDbHelper.registerHumiTempModel(model);
        }
    }
}
