
package net.cattaka.android.humitemp4ble;

import net.cattaka.android.humitemp4ble.entity.HumiTempModel;
import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import android.bluetooth.BluetoothDevice;


interface IHumiTempServiceListener {
    void onDeviceModelUpdated(in DeviceModel model);
    
    void onWebEvent(int webEvent);
}
