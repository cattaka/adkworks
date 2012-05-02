package net.cattaka.droidrobo01;

import android.os.RemoteException;
import net.cattaka.droidrobo01.service.IAdkService;

public class RobotUtil {
    public enum MotorState {
        NONE,
        TURN_LEFT,
        TURN_RIGHT,
    }
    
    public static void enableEyeLight(IAdkService adkService, boolean enabled) throws RemoteException {
        byte value = (byte) ((enabled) ? 1 : 0);
        byte[] data = new byte[] {value};
        adkService.sendCommand((byte)0x03, (byte)(0x0), data);
    }
    
    public static void drive(IAdkService adkService, MotorState state) throws RemoteException {
        byte[] data;
        switch (state) {
            case TURN_LEFT:
                data = new byte[]{1,0,0,1};
                break;
            case TURN_RIGHT:
                data = new byte[]{0,1,1,0};
                break;
            default:
            case NONE:
                data = new byte[]{0,0,0,0};
                break;
        }
        adkService.sendCommand((byte)0x03, (byte)(0x01), data);
    }
}
