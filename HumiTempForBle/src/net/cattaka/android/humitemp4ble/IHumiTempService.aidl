
package net.cattaka.android.humitemp4ble;

import net.cattaka.android.humitemp4ble.IHumiTempServiceListener;
import net.cattaka.android.humitemp4ble.entity.HumiTempModel;
import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import android.bluetooth.BluetoothDevice;


interface IHumiTempService {

    List<DeviceModel> findDeviceModels();
    DeviceModel findDeviceModel(in String address);
    
    boolean addDevice(in BluetoothDevice device);
    boolean removeDevice(in String address);
    
    List<DeviceModel> updateSensorValues();
    
    int registerServiceListener(IHumiTempServiceListener listener);
    boolean unregisterServiceListener(int seq);
}
