
package net.cattaka.android.foxkehrobo;

import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.FrPacketFactory;
import net.cattaka.libgeppa.ActiveGeppaService;
import net.cattaka.libgeppa.data.BaudRate;
import net.cattaka.libgeppa.data.DeviceInfo;

public class FoxkehRoboService extends ActiveGeppaService<FrPacket> {
    public FoxkehRoboService() {
        super(new FrPacketFactory());
        setBaudRate(BaudRate.BAUD38400);
    }

    @Override
    protected void handleConnectedNotification(boolean connected, DeviceInfo deviceInfo) {
        // TODO Auto-generated method stub

    }

}
