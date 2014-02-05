
package net.cattaka.android.humitemp4ble;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener, OnItemClickListener {
    private static final int REQUEST_CODE_SELECT_DEVICE = 1;

    private static class DeviceModelBundle {
    	SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        DeviceModel model;

        String date;

        private DeviceModelBundle(DeviceModel model) {
            super();
            setModel(model);
        }
        
        public void setModel(DeviceModel model) {
			this.model = model;
			this.date = (model.getLastUpdate() != null) ?df.format(model.getLastUpdate()) : "";
		}



		@Override
        public String toString() {
            return String.format(Locale.ROOT, "%d %s Temp=%01.2f[deg],Humi=%01.2f[%%] %s",
                    model.getId(), model.getAddress(), model.getLastTemplature(),
                    model.getLastHumidity(), date);
        }
    }

    private MainActivity me = this;

    private IHumiTempService mService;

    private ListView mDeviceList;

    private ServiceConnectionEx mServiceConnection = new ServiceConnectionEx();

    class ServiceConnectionEx implements ServiceConnection {
        private BluetoothDevice mPendingAddDevice;

        @Override
        public void onServiceDisconnected(ComponentName paramComponentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = HumiTempService.Stub.asInterface(binder);
            try {
                mService.registerServiceListener(mServiceListener);
                if (mPendingAddDevice != null) {
                    mService.addDevice(mPendingAddDevice);
                    mPendingAddDevice = null;
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            me.onServiceConnected();
        }
    };

    private int mServiceListenerSeq = -1;

    private IHumiTempServiceListener mServiceListener = new IHumiTempServiceListener.Stub() {
        @Override
        public void onDeviceModelUpdated(DeviceModel model) throws RemoteException {
            me.onDeviceModelUpdated(model);
        }

        public void onWebEvent(int webEvent) throws RemoteException {
            Toast.makeText(me, "onWebEvent:" + webEvent, Toast.LENGTH_SHORT).show();
        };
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceList = (ListView)findViewById(R.id.deviceList);

        // Setting event listeners
        findViewById(R.id.updateButton).setOnClickListener(this);
        findViewById(R.id.selectDeviceButton).setOnClickListener(this);
        findViewById(R.id.syncButton).setOnClickListener(this);

        ArrayAdapter<DeviceModelBundle> adapter = new ArrayAdapter<DeviceModelBundle>(this,
                android.R.layout.simple_list_item_1, new ArrayList<DeviceModelBundle>());
        mDeviceList.setAdapter(adapter);
        mDeviceList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, HumiTempService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mServiceListenerSeq >= 0) {
            try {
                mService.unregisterServiceListener(mServiceListenerSeq);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            mServiceListenerSeq = -1;
        }
        unbindService(mServiceConnection);
    }

    private void onServiceConnected() {
        try {
            @SuppressWarnings("unchecked")
            ArrayAdapter<DeviceModelBundle> adapter = (ArrayAdapter<DeviceModelBundle>)mDeviceList
                    .getAdapter();
            adapter.clear();

            List<DeviceModel> models = mService.findDeviceModels();
            for (DeviceModel model : models) {
                adapter.add(new DeviceModelBundle(model));
            }
            adapter.notifyDataSetInvalidated();
        } catch (RemoteException e) {
            // ignore
        }
    }

    public void onDeviceModelUpdated(DeviceModel model) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<DeviceModelBundle> adapter = (ArrayAdapter<DeviceModelBundle>)mDeviceList
                .getAdapter();
        boolean changed = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            DeviceModelBundle t = adapter.getItem(i);
            if (t.model.getId().longValue() == model.getId().longValue()) {
                t.setModel(model);
                changed = true;
                break;
            }
        }
        if (changed) {
            adapter.notifyDataSetInvalidated();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SELECT_DEVICE == requestCode) {
            if (resultCode == RESULT_OK) {
                mServiceConnection.mPendingAddDevice = (BluetoothDevice)data
                        .getParcelableExtra(SelectDeviceActivity.EXTRA_BLUETOOTH_DEVICE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.updateButton) {
            try {
                mService.updateSensorValues();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else if (v.getId() == R.id.selectDeviceButton) {
            Intent serviceIntent = new Intent(this,HumiTempService.class);
            stopService(serviceIntent);
            
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE);
        } else if (v.getId() == R.id.syncButton) {
            Intent intent = new Intent(this, SyncActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.deviceList) {
            DeviceModelBundle bundle = (DeviceModelBundle)parent.getItemAtPosition(position);
            if (bundle != null) {
                Intent intent = new Intent(this, GraphActivity.class);
                intent.putExtra(GraphActivity.EXTRA_DEVICE_ID, bundle.model.getId());
                startActivity(intent);
            }
        }

    }
}
