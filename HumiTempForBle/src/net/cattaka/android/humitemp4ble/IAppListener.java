
package net.cattaka.android.humitemp4ble;

import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.content.ServiceConnection;

public interface IAppListener extends ServiceConnection {

    public void onConnectionStateChanged(ConnectionState state);

    public void onReceivePacket(PacketWrapper packetWrapper);

    public void onPageSelected();

    public void onPageDeselected();
}
