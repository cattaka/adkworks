
package net.cattaka.android.foxkehrobo.core;

import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.OpCode;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.IPacket;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.os.RemoteException;

public class ServiceWrapper {
    private IActiveGeppaService mService;

    private PacketWrapper mPacketWrapperCache;

    public ServiceWrapper(IActiveGeppaService service) {
        super();
        mService = service;
        mPacketWrapperCache = new PacketWrapper((IPacket)null);
    }

    public IActiveGeppaService getService() {
        return mService;
    }

    public boolean sendEcho(byte[] data) {
        FrPacket packet = new FrPacket(OpCode.ECHO, data.length, data);
        return sendPacket(packet);
    }

    public void sendPose(PoseModel model) {
        byte[] data = model.toPose();

        FrPacket packet = new FrPacket(OpCode.POSE, data.length, data);
        sendPacket(packet);
    }

    public synchronized boolean sendPacket(FrPacket packet) {
        try {
            mPacketWrapperCache.setPacket(packet);
            return mService.sendPacket(mPacketWrapperCache);
        } catch (RemoteException e) {
            return false;
        } finally {
            mPacketWrapperCache.setPacket(null);
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
