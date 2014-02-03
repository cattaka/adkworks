
package net.cattaka.android.humitemp4ble.bluetooth;


public interface MyBtListener {
    public void onReceivePacket(int temperature, int humidity);

    public void onDisconnected();

    public void onRssi(int rssi);
}
