
package net.cattaka.droidrobo01.robo;

public class RoboPauseInfo {
    public enum MotorDir {
        STOP(0), FORWARD(1), REVERSE(2);

        private int intValue;

        private MotorDir(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public static MotorDir parse(int intValue) {
            for (MotorDir item : values()) {
                if (item.getIntValue() == intValue) {
                    return item;
                }
            }
            return STOP;
        }
    }

    private boolean eyeLight;

    private MotorDir motorDirLeft = MotorDir.STOP;

    private MotorDir motorDirRight = MotorDir.STOP;

    private int ducation;

    private float armLeftAngle;

    private float armRightAngle;

    public RoboPauseInfo() {
    }

    public RoboPauseInfo(RoboPauseInfo src) {
        super();
        this.eyeLight = src.isEyeLight();
        this.motorDirLeft = src.getMotorDirLeft();
        this.motorDirRight = src.getMotorDirRight();
        this.ducation = src.getDucation();
        this.armLeftAngle = src.getArmLeftAngle();
        this.armRightAngle = src.getArmRightAngle();
    }

    public RoboPauseInfo(boolean eyeLight, MotorDir motorDirLeft, MotorDir motorDirRight,
            int ducation, float armLeftAngle, float armRightAngle) {
        super();
        this.eyeLight = eyeLight;
        this.motorDirLeft = motorDirLeft;
        this.motorDirRight = motorDirRight;
        this.ducation = ducation;
        this.armLeftAngle = armLeftAngle;
        this.armRightAngle = armRightAngle;
    }

    public byte[] toUpperBytes() {
        byte[] bs = new byte[5];
        { // EyeLight
            bs[0] = (byte)((eyeLight) ? 1 : 0);
        }
        { // Motor
            switch (motorDirLeft) {
                case FORWARD:
                    bs[1] = 0;
                    bs[2] = 1;
                    break;
                case REVERSE:
                    bs[1] = 1;
                    bs[2] = 0;
                    break;
                default:
                    bs[1] = 0;
                    bs[2] = 0;
                    break;
            }
            switch (motorDirRight) {
                case FORWARD:
                    bs[3] = 0;
                    bs[4] = 1;
                    break;
                case REVERSE:
                    bs[3] = 1;
                    bs[4] = 0;
                    break;
                default:
                    bs[3] = 0;
                    bs[4] = 0;
                    break;
            }
        }
        return bs;
    }

    public byte[] toLowerBytes() {
        byte[] bs = new byte[4];
        { // Arm
            int lValue = (int)(0xFFFF * (armLeftAngle));
            int rValue = (int)(0xFFFF * (1.0f - armRightAngle));
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

    public int getDucation() {
        return ducation;
    }

    public void setDucation(int ducation) {
        this.ducation = ducation;
    }

    public MotorDir getMotorDirLeft() {
        return motorDirLeft;
    }

    public void setMotorDirLeft(MotorDir motorDirLeft) {
        this.motorDirLeft = motorDirLeft;
    }

    public MotorDir getMotorDirRight() {
        return motorDirRight;
    }

    public void setMotorDirRight(MotorDir motorDirRight) {
        this.motorDirRight = motorDirRight;
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
