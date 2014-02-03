
package net.cattaka.android.humitemp4ble;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.humitemp4ble.R;
import net.cattaka.android.humitemp4ble.bluetooth.LeScanCallbackEx;
import net.cattaka.android.humitemp4ble.bluetooth.async.LeScanCallbackExAsync;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectDeviceActivity extends Activity implements OnClickListener, OnItemClickListener,
        LeScanCallbackEx {
    public static final String EXTRA_BLUETOOTH_DEVICE = "bluetoothDevice";

    private ListView mBleDeviceList;

    private LeScanCallbackExAsync mLeScanCallback = new LeScanCallbackExAsync(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);

        // Pickup view
        mBleDeviceList = (ListView)findViewById(R.id.bleDeviceList);

        // Binds event listener
        findViewById(R.id.scanBleDevice).setOnClickListener(this);
        mBleDeviceList.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startScanBleDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScanBleDevice();
    }

    @SuppressLint("NewApi")
    private void startScanBleDevice() {
        {
            List<BluetoothDevice> items = new ArrayList<BluetoothDevice>();
            ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter<BluetoothDevice>(this,
                    android.R.layout.simple_list_item_single_choice, items);
            mBleDeviceList.setAdapter(adapter);
        }

        if (Build.VERSION.SDK_INT < 18) {
            Toast.makeText(this, "BLE is not supported on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && btAdapter.isEnabled()) {
            if (!btAdapter.isDiscovering()) {
                stopScanBleDevice();
                btAdapter.startLeScan(mLeScanCallback);
            } else {
                Toast.makeText(this, "Discovering BLE now..", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (btAdapter == null) {
                Toast.makeText(this, "Bluetooth is not supported on this device.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NewApi")
    private void stopScanBleDevice() {
        if (Build.VERSION.SDK_INT < 18) {
            return;
        }
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && btAdapter.isEnabled()) {
            btAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanBleDevice) {
            startScanBleDevice();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.bleDeviceList) {
            BluetoothDevice item = (BluetoothDevice)mBleDeviceList.getItemAtPosition(position);
            Intent data = new Intent();
            data.putExtra(EXTRA_BLUETOOTH_DEVICE, item);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<BluetoothDevice> adapter = (ArrayAdapter<BluetoothDevice>)mBleDeviceList
                .getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            BluetoothDevice oldDevice = adapter.getItem(i);
            if (oldDevice.getAddress().equals(device.getAddress())) {
                return;
            }
        }
        adapter.add(device);
    }
}
