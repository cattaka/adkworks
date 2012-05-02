package net.cattaka.droidrobo01.robo;

import net.cattaka.droidrobo01.RobotUtil.MotorState;

public class RoboPauseInfo {
    private boolean eyeLight;
    private MotorState motorState;
    private int ducation;
    private float armLeftAngle;
    private float armRightAngle;

    public RoboPauseInfo() {
    }

    public RoboPauseInfo(RoboPauseInfo src) {
        super();
        this.eyeLight = src.isEyeLight();
        this.motorState = src.getMotorState();
        this.ducation = src.getDucation();
        this.armLeftAngle = src.getArmLeftAngle();
        this.armRightAngle = src.getArmRightAngle();
    }
    
    public RoboPauseInfo(boolean eyeLight, MotorState motorState, int ducation, float armLeftAngle,
            float armRightAngle) {
        super();
        this.eyeLight = eyeLight;
        this.motorState = motorState;
        this.ducation = ducation;
        this.armLeftAngle = armLeftAngle;
        this.armRightAngle = armRightAngle;
    }

    public byte[] toUpperBytes() {
        byte[] bs = new byte[5];
        {   // EyeLight
            bs[0] = (byte)((eyeLight) ? 1:0);
        }
        {   // Motor
            switch (motorState) {
                case TURN_LEFT:
                    bs[1] = 1;
                    bs[2] = 0;
                    bs[3] = 0;
                    bs[4] = 1;
                    break;
                case TURN_RIGHT:
                    bs[1] = 0;
                    bs[2] = 1;
                    bs[3] = 1;
                    bs[4] = 0;
                    break;
                default:
                case NONE:
                    bs[1] = 0;
                    bs[2] = 0;
                    bs[3] = 0;
                    bs[4] = 0;
                    break;
            }
        }
        return bs;
    }
    
    public byte[] toLowerBytes() {
        byte[] bs = new byte[4];
        {   // Arm
            int lValue = (int) (0xFFFF * (armLeftAngle));
            int rValue = (int) (0xFFFF * (1.0f-armRightAngle));
            bs[0] = (byte)((lValue >> 8) & 0xFF);
            bs[1] = (byte)(lValue & 0xFF);
            bs[2] = (byte)((rValue >> 8) & 0xFF);
            bs[3] = (byte)(rValue & 0xFF);
        }
        return bs;
    }
    
    public boolean isEyeLight() {
        return eyeLight;
    }

    public void setEyeLight(boolean eyeLight) {
        this.eyeLight = eyeLight;
    }

    public MotorState getMotorState() {
        return motorState;
    }

    public void setMotorState(MotorState motorState) {
        this.motorState = motorState;
    }

    public int getDucation() {
        return ducation;
    }

    public void setDucation(int ducation) {
        this.ducation = ducation;
    }

    public float getArmLeftAngle() {
        return armLeftAngle;
    }

    public void setArmLeftAngle(float armLeftAngle) {
        this.armLeftAngle = armLeftAngle;
    }

    public float getArmRightAngle() {
        return armRightAngle;
    }

    public void setArmRightAngle(float armRightAngle) {
        this.armRightAngle = armRightAngle;
    }
}
