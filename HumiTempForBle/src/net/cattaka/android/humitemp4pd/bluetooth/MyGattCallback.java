
package net.cattaka.android.humitemp4pd.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.cattaka.android.humitemp4pd.bluetooth.async.MyBtListenerAsync;
import net.cattaka.android.humitemp4pd.data.MyPacket;
import net.cattaka.android.humitemp4pd.data.MyPacketFactory;
import net.cattaka.android.humitemp4pd.data.OpCode;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

public abstract class MyGattCallback extends BluetoothGattCallback {
    private static final byte[] BS_ZERO = new byte[0];

    public final static UUID UUID_BLE_SHIELD_TX = UUID.fromString(RBLGattAttributes.BLE_SHIELD_TX);

    public final static UUID UUID_BLE_SHIELD_RX = UUID.fromString(RBLGattAttributes.BLE_SHIELD_RX);

    public final static UUID UUID_BLE_SHIELD_SERVICE = UUID
            .fromString(RBLGattAttributes.BLE_SHIELD_SERVICE);

    private BluetoothGattCharacteristic mCharacteristicTx;

    private BluetoothGattCharacteristic mCharacteristicRx;

    private BluetoothGatt mBluetoothGatt;

    private MyBtListenerAsync mListener;

    public MyGattCallback() {
        super();
    }

    public void setListener(MyBtListener listener) {
        mListener = new MyBtListenerAsync(listener);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mBluetoothGatt = gatt;
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mBluetoothGatt = null;
            mListener.onDisconnected();
        }
    }

    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mListener.onRssi(rssi);
        }
    };

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService gattService = gatt.getService(UUID_BLE_SHIELD_SERVICE);

            mCharacteristicTx = gattService.getCharacteristic(UUID_BLE_SHIELD_TX);

            mCharacteristicRx = gattService.getCharacteristic(UUID_BLE_SHIELD_RX);
            gatt.setCharacteristicNotification(mCharacteristicRx, true);

            if (UUID_BLE_SHIELD_RX.equals(mCharacteristicRx.getUuid())) {
                BluetoothGattDescriptor descriptor = mCharacteristicRx.getDescriptor(UUID
                        .fromString(RBLGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            onInnerReceivePacket(gatt, characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic) {
        gatt.disconnect();
        onInnerReceivePacket(gatt, characteristic.getValue());
    }

    public void sendRequest() {
        if (mBluetoothGatt != null) {
            sendRequest(mBluetoothGatt);
        }
    }

    private void sendRequest(BluetoothGatt gatt) {
        byte[] tx;
        MyPacketFactory factory = new MyPacketFactory();
        MyPacket myPacket = new MyPacket(OpCode.REQUEST, 0, BS_ZERO);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            factory.writePacket(bout, myPacket);
            tx = bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mCharacteristicTx.setValue(tx);
        gatt.writeCharacteristic(mCharacteristicTx);
    }

    private void onInnerReceivePacket(BluetoothGatt gatt, byte[] byteArray) {
        if (byteArray == null) {
            return;
        }
        MyPacketFactory factory = new MyPacketFactory();
        MyPacket myPacket;
        try {
            myPacket = factory.readPacket(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] data = myPacket.getData();
        if (myPacket == null || myPacket.getData() == null && myPacket.getDataLen() < 8) {
            return;
        }
        int humidity = (((0xFF & data[0]) << 24) | ((0xFF & data[1]) << 16)
                | ((0xFF & data[2]) << 8) | ((0xFF & data[3]) << 0));
        int temperature = (((0xFF & data[4]) << 24) | ((0xFF & data[5]) << 16)
                | ((0xFF & data[6]) << 8) | ((0xFF & data[7]) << 0));

        mListener.onReceivePacket(temperature, humidity);

        gatt.close();
        mBluetoothGatt = null;
        mListener.onDisconnected();
    }

    public boolean isConnected() {
        return mBluetoothGatt != null;
    }

}
