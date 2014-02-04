package net.cattaka.android.humitemp4ble;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cattaka.android.humitemp4ble.binder.IHumiTempServiceWrapper;
import net.cattaka.android.humitemp4ble.bluetooth.MyBtListener;
import net.cattaka.android.humitemp4ble.bluetooth.MyGattCallback;
import net.cattaka.android.humitemp4ble.db.DbHelper;
import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import net.cattaka.android.humitemp4ble.entity.HumiTempModel;
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
import android.os.SystemClock;
import android.util.SparseArray;

public class HumiTempService extends Service implements IHumiTempServiceWrapper {
	private static final int EVENT_UPDATE_SENSOR_VALUES = 1;

	private static final int INTERVALUPDATE_SENSOR_VALUES = 30000;

	private static class DeviceBundle {
		BluetoothDevice bluetoothDevice;

		BluetoothGatt gatt;

		DeviceModel deviceModel;

		MyGattCallbackEx myGattCallback;

		private DeviceBundle(BluetoothDevice bluetoothDevice,
				BluetoothGatt gatt, DeviceModel deviceModel,
				MyGattCallbackEx myGattCallback) {
			super();
			this.bluetoothDevice = bluetoothDevice;
			this.gatt = gatt;
			this.deviceModel = deviceModel;
			this.myGattCallback = myGattCallback;
			this.myGattCallback.deviceBundle = this;
		}
	}

	private class MyGattCallbackEx extends MyGattCallback implements
			MyBtListener {
		DeviceBundle deviceBundle;
		long lastReceiveTime = 0;

		public MyGattCallbackEx() {
			super();
			this.setListener(this);
		}

		@Override
		public void onDisconnected() {
		}

		public void onReceivePacket(int temperature, int humidity) {
			long currtime = SystemClock.elapsedRealtime();
			if (currtime - lastReceiveTime > INTERVALUPDATE_SENSOR_VALUES) {
				lastReceiveTime = currtime;
				float t = (float) temperature / 100f;
				float h = (float) humidity / 100f;
				me.onReceivePacket(deviceBundle, t, h);
			}
		};

		@Override
		public void onRssi(int rssi) {
			// TODO Auto-generated method stub

		}
	}

	private static final Handler sHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			HumiTempService target = (HumiTempService) msg.obj;
			if (msg.what == EVENT_UPDATE_SENSOR_VALUES) {
				target.updateSensorValues();

				Message nextMessage = sHandler.obtainMessage(
						EVENT_UPDATE_SENSOR_VALUES, target);
				sendMessageDelayed(nextMessage, INTERVALUPDATE_SENSOR_VALUES);
			}
		};
	};

	private HumiTempService me = this;

	private DbHelper mDbHelper;

	private BluetoothAdapter mBluetoothAdapter;

	private Map<String, DeviceBundle> mAddress2DeviceBundle;

	private int mServiceListenersSeq = 0;

	private SparseArray<IHumiTempServiceListener> mServiceListeners;

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

		public boolean unregisterServiceListener(int seq)
				throws RemoteException {
			return me.unregisterServiceListener(seq);
		};
	};

	public void onCreate() {
		super.onCreate();
		mDbHelper = new DbHelper(this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mServiceListeners = new SparseArray<IHumiTempServiceListener>();

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
	public int registerServiceListener(IHumiTempServiceListener listener)
			throws RemoteException {
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
			DeviceBundle bundle = mAddress2DeviceBundle.get(deviceModel
					.getAddress());
			if (bundle != null) {
				if (bundle.myGattCallback.isConnected()) {
					// OK
				} else {
					bundle.gatt.connect();
				}
			} else {
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(deviceModel.getAddress());
				MyGattCallbackEx callback = new MyGattCallbackEx();
				BluetoothGatt gatt = device.connectGatt(this, false, callback);
				bundle = new DeviceBundle(device, gatt, deviceModel, callback);
				mAddress2DeviceBundle.put(deviceModel.getAddress(), bundle);
			}
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

	public void onReceivePacket(DeviceBundle bundle, float temperature,
			float humidity) {
		// Toast.makeText(me, String.format("T=%f, H=%f", temperature,
		// humidity),
		// Toast.LENGTH_SHORT).show();
		Date date = new Date();
		final DeviceModel deviceModel = bundle.deviceModel;
		{
			deviceModel.setLastHumidity(humidity);
			deviceModel.setLastTemplature(temperature);
			deviceModel.setLastUpdate(date);
			mDbHelper.registerDeviceModel(deviceModel);
		}

		{
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
					public boolean run(IHumiTempServiceListener item)
							throws RemoteException {
						item.onDeviceModelUpdated(deviceModel);
						return true;
					}
				});
	}
}
