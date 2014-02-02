
package net.cattaka.android.humitemp4pd;

import net.cattaka.android.humitemp4pd.entity.HumiTempModel;
import net.cattaka.android.humitemp4pd.entity.DeviceModel;
import android.bluetooth.BluetoothDevice;


interface IHumiTempService {

    List<DeviceModel> findDeviceModels();
    DeviceModel findDeviceModel(in String address);
    
    boolean addDevice(in BluetoothDevice device);
    boolean removeDevice(in String address);
    
    List<DeviceModel> updateSensorValues();
}
