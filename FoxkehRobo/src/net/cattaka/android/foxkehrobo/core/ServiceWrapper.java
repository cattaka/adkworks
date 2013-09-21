
package net.cattaka.android.foxkehrobo.core;

import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.OpCode;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.os.RemoteException;

public class ServiceWrapper {
    private byte[] mBuffer = new byte[0x100];

    private IActiveGeppaService mService;

    public ServiceWrapper(IActiveGeppaService service) {
        super();
        mService = service;
    }

    public IActiveGeppaService getService() {
        return mService;
    }

    public boolean sendEcho(byte[] data) {
        FrPacket packet = new FrPacket(OpCode.ECHO, data.length, data);
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean sendPacket(FrPacket packet) {
        try {
            return mService.sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            return false;
        }
    }

    public DeviceInfo getCurrentDeviceInfo() {
        try {
            return mService.getCurrentDeviceInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

}
