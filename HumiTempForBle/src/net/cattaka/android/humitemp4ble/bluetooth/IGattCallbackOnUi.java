
package net.cattaka.android.humitemp4ble.bluetooth;

import net.cattaka.util.genasyncif.GenAsyncInterface;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

@GenAsyncInterface
public interface IGattCallbackOnUi {
    public void onConnectionStateChangeOnUi(BluetoothGatt gatt, int status, int newState);

    public void onReadRemoteRssiOnUi(BluetoothGatt gatt, int rssi, int status);

    public void onServicesDiscoveredOnUi(BluetoothGatt gatt, int status);

    public void onCharacteristicReadOnUi(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic, int status);

    public void onCharacteristicChangedOnUi(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic);
}
