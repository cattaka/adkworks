
package net.cattaka.android.humitemp4pd.bluetooth;

import net.cattaka.util.genasyncif.GenAsyncInterface;

@GenAsyncInterface
public interface MyBtListener {
    public void onReceivePacket(int temperature, int humidity);

    public void onDisconnected();

    public void onRssi(int rssi);
}
