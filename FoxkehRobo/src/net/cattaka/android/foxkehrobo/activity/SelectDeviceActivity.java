
package net.cattaka.android.foxkehrobo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.cattaka.android.foxkehrobo.FoxkehRoboService;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.db.FoxkehRoboDatabase;
import net.cattaka.android.foxkehrobo.dialog.EditAddrresDialog;
import net.cattaka.android.foxkehrobo.dialog.EditAddrresDialog.IEditAddrresDialogListener;
import net.cattaka.android.foxkehrobo.entity.MySocketAddress;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import net.cattaka.libgeppa.net.UsbClass;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectDeviceActivity extends Activity implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener, IEditAddrresDialogListener {
    protected static final String EXTRA_USB_DEVICE_KEY = "usbDevicekey";

    private static class ListItem {
        String label;

        DeviceInfo deviceInfo;

        public ListItem(String label, DeviceInfo deviceInfo) {
            super();
            this.label = label;
            this.deviceInfo = deviceInfo;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                refleshUsbDeviceList();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                refleshUsbDeviceList();
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IActiveGeppaService.Stub.asInterface(service);
            if (mServiceListenerSeq < 0) {
                try {
                    mServiceListenerSeq = mService.registerServiceListener(mServiceListener);
                    DeviceInfo deviceInfo = mService.getCurrentDeviceInfo();
                    updateSelectedUsbDevice(deviceInfo);
                } catch (RemoteException e) {
                    // Nothing to do
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private SelectDeviceActivity me = this;

    private FoxkehRoboDatabase mDbHelper;

    private EditAddrresDialog mEditAddrresDialog;

    private ListView mSocketAddressList;

    private int mServiceListenerSeq = -1;

    private IActiveGeppaServiceListener mServiceListener = new IActiveGeppaServiceListener.Stub() {
        private ProgressDialog mProgressDialog;

        @Override
        public void onReceivePacket(PacketWrapper packet) throws RemoteException {
            // ignore
        }

        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final DeviceInfo deviceInfo) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == DeviceState.CONNECTING) {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        mProgressDialog = new ProgressDialog(me);
                        mProgressDialog.setMessage(getString(R.string.msg_now_connecting));
                        mProgressDialog.show();
                    } else {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                    }
                    updateSelectedUsbDevice(deviceInfo);
                    if (state == DeviceState.CONNECTED && code != DeviceEventCode.ON_REGISTER) {
                        finish();
                    }
                }
            });
        }
    };

    private IActiveGeppaService mService;

    private ListView mUsbDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_local_device);

        // Pickup view
        mUsbDeviceList = (ListView)findViewById(R.id.usbDeviceList);
        mSocketAddressList = (ListView)findViewById(R.id.socketAddressList);

        // Binds event listener
        mUsbDeviceList.setOnItemClickListener(this);
        findViewById(R.id.addSocketAddressButton).setOnClickListener(this);
        mSocketAddressList.setOnItemClickListener(this);
        mSocketAddressList.setOnItemLongClickListener(this);

        mUsbDeviceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mSocketAddressList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mEditAddrresDialog = EditAddrresDialog.createEditAddrresDialog(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this, FoxkehRoboService.class);
        startService(service);
        bindService(service, mServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceListenerSeq >= 0) {
            try {
                mService.unregisterServiceListener(mServiceListenerSeq);
                mServiceListenerSeq = -1;
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbHelper = new FoxkehRoboDatabase(this);

        refleshUsbDeviceList();
        refleshSocketAddressList();

        { // Registers receiver for USB attach
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbReceiver, filter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        { // Unregisters receiver for USB attach
            unregisterReceiver(mUsbReceiver);
        }
        mDbHelper.close();
        mDbHelper = null;
    }

    private void refleshUsbDeviceList() {
        UsbManager usbman = (UsbManager)getSystemService(Context.USB_SERVICE);

        List<ListItem> items = new ArrayList<SelectDeviceActivity.ListItem>();
        { // Add special item
            items.add(new ListItem(getString(R.string.item_disconnect), null));
        }
        { // Add an item of dummy
            items.add(new ListItem(getString(R.string.item_dummy), DeviceInfo.createDummy(false)));
        }
        { // Creates list items
            HashMap<String, UsbDevice> deviceMap = usbman.getDeviceList();
            for (Entry<String, UsbDevice> entry : deviceMap.entrySet()) {
                UsbDevice d = entry.getValue();
                String name = UsbClass.parce(d.getDeviceClass()).name();
                String label = String.format("%s(%04X:%04X)", name, d.getVendorId(),
                        d.getProductId());
                items.add(new ListItem(label, DeviceInfo.createUsb(entry.getKey(), false)));
            }
        }

        ArrayAdapter<ListItem> adapter = new ArrayAdapter<SelectDeviceActivity.ListItem>(this,
                android.R.layout.simple_list_item_single_choice, items);
        mUsbDeviceList.setAdapter(adapter);
    }

    private void refleshSocketAddressList() {
        List<MySocketAddress> items = mDbHelper.findMySocketAddresses();
        ArrayAdapter<MySocketAddress> adapter = new ArrayAdapter<MySocketAddress>(this,
                android.R.layout.simple_list_item_1, items);
        mSocketAddressList.setAdapter(adapter);
    }

    private void updateSelectedUsbDevice(DeviceInfo deviceInfo) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<ListItem> adapter = (ArrayAdapter<SelectDeviceActivity.ListItem>)mUsbDeviceList
                .getAdapter();
        if (deviceInfo != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                ListItem item = adapter.getItem(i);
                mUsbDeviceList.setItemChecked(i, equalsDeviceInfo(item.deviceInfo, deviceInfo));
            }
        }
    }

    private boolean equalsDeviceInfo(DeviceInfo s1, DeviceInfo s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addSocketAddressButton) {
            mEditAddrresDialog.show(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.usbDeviceList) {
            ListItem item = (ListItem)mUsbDeviceList.getItemAtPosition(position);
            onSelectItem(item.deviceInfo);
        } else if (parent.getId() == R.id.socketAddressList) {
            MySocketAddress item = (MySocketAddress)parent.getItemAtPosition(position);
            onSelectItem(item.toDeviceInfo());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.socketAddressList) {
            MySocketAddress item = (MySocketAddress)parent.getItemAtPosition(position);
            mEditAddrresDialog.show(item);
            return true;
        } else {
            return false;
        }
    }

    private void onSelectItem(DeviceInfo deviceInfo) {
        try {
            if (deviceInfo != null) {
                mService.connect(deviceInfo);
            } else {
                mService.disconnect();
                finish();
            }
        } catch (RemoteException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    /**
     * @see EditAddrresDialog.IEditAddrresDialogListener
     */
    @Override
    public void onEditAddrresDialogFinished(MySocketAddress result) {
        mDbHelper.registerMySocketAddress(result);
        refleshSocketAddressList();
    };

    /**
     * @see EditAddrresDialog.IEditAddrresDialogListener
     */
    @Override
    public void onEditAddrresDialogCanceled() {
        // none
    }

    @Override
    public void onEditAddrresDialogDelete(Long id) {
        if (id != null) {
            mDbHelper.deleteMySocketAddress(id);
            refleshSocketAddressList();
        }
    }
}
