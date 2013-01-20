
package net.cattaka.robotarm01;

import android.content.SharedPreferences;

public class ArmSetting {
    private int servo1Min;

    private int servo1Max;

    private int servo2Min;

    private int servo2Max;

    private int servo3Min;

    private int servo3Max;

    private int servo4Min;

    private int servo4Max;

    private int servo5Min;

    private int servo5Max;

    private int servo6Min;

    private int servo6Max;

    private int armLength1;

    private int armLength2;

    private int armLength3;

    private int armLength4;

    private boolean servo1Invert = true;

    private boolean servo2Invert = false;

    private boolean servo3Invert = true;

    private boolean servo4Invert = true;

    private boolean servo5Invert = true;

    private boolean servo6Invert = false;

    private int angleOffset1;

    private int angleOffset2;

    private int angleOffset3;

    private int angleOffset4;

    private int angleOffset5;

    private int angleOffset6;

    public void loadPreference(SharedPreferences pref) {
        servo1Min = loadInt(pref, "servo1_min", -80);
        servo1Max = loadInt(pref, "servo1_max", 80);
        servo2Min = loadInt(pref, "servo2_min", -80);
        servo2Max = loadInt(pref, "servo2_max", 80);
        servo3Min = loadInt(pref, "servo3_min", -110);
        servo3Max = loadInt(pref, "servo3_max", 110);
        servo4Min = loadInt(pref, "servo4_min", -100);
        servo4Max = loadInt(pref, "servo4_max", 105);
        servo5Min = loadInt(pref, "servo5_min", -90);
        servo5Max = loadInt(pref, "servo5_max", 90);
        servo6Min = loadInt(pref, "servo6_min", -90);
        servo6Max = loadInt(pref, "servo6_max", 90);
        armLength1 = loadInt(pref, "arm_length_1", 127);
        armLength2 = loadInt(pref, "arm_length_2", 114);
        armLength3 = loadInt(pref, "arm_length_3", 117);
        armLength4 = loadInt(pref, "arm_length_4", 143);
        angleOffset1 = loadInt(pref, "angle_offset_1", 4);
        angleOffset2 = loadInt(pref, "angle_offset_2", 0);
        angleOffset3 = loadInt(pref, "angle_offset_3", 0);
        angleOffset4 = loadInt(pref, "angle_offset_4", 0);
        angleOffset5 = loadInt(pref, "angle_offset_5", 0);
        angleOffset6 = loadInt(pref, "angle_offset_6", 0);
    }

    private static int loadInt(SharedPreferences pref, String key, int defaultValue) {
        try {
            String str = pref.getString(key, null);
            if (str != null) {
                return Integer.parseInt(str);
            } else {
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getServoMin(int index) {
        switch (index) {
            case 0:
                return servo1Min;
            case 1:
                return servo2Min;
            case 2:
                return servo3Min;
            case 3:
                return servo4Min;
            case 4:
                return servo5Min;
            case 5:
                return servo6Min;
            default:
                return 0;
        }
    }

    public int getServoMax(int index) {
        switch (index) {
            case 0:
                return servo1Max;
            case 1:
                return servo2Max;
            case 2:
                return servo3Max;
            case 3:
                return servo4Max;
            case 4:
                return servo5Max;
            case 5:
                return servo6Max;
            default:
                return 0;
        }
    }

    public int getAngleOffset(int index) {
        switch (index) {
            case 0:
                return angleOffset1;
            case 1:
                return angleOffset2;
            case 2:
                return angleOffset3;
            case 3:
                return angleOffset4;
            case 4:
                return angleOffset5;
            case 5:
                return angleOffset6;
            default:
                return 0;
        }
    }

    public boolean getServoInvert(int index) {
        switch (index) {
            case 0:
                return servo1Invert;
            case 1:
                return servo2Invert;
            case 2:
                return servo3Invert;
            case 3:
                return servo4Invert;
            case 4:
                return servo5Invert;
            case 5:
                return servo6Invert;
            default:
                return false;
        }
    }

    public int getServo1Min() {
        return servo1Min;
    }

    public void setServo1Min(int servo1Min) {
        this.servo1Min = servo1Min;
    }

    public int getServo1Max() {
        return servo1Max;
    }

    public void setServo1Max(int servo1Max) {
        this.servo1Max = servo1Max;
    }

    public int getServo2Min() {
        return servo2Min;
    }

    public void setServo2Min(int servo2Min) {
        this.servo2Min = servo2Min;
    }

    public int getServo2Max() {
        return servo2Max;
    }

    public void setServo2Max(int servo2Max) {
        this.servo2Max = servo2Max;
    }

    public int getServo3Min() {
        return servo3Min;
    }

    public void setServo3Min(int servo3Min) {
        this.servo3Min = servo3Min;
    }

    public int getServo3Max() {
        return servo3Max;
    }

    public void setServo3Max(int servo3Max) {
        this.servo3Max = servo3Max;
    }

    public int getServo4Min() {
        return servo4Min;
    }

    public void setServo4Min(int servo4Min) {
        this.servo4Min = servo4Min;
    }

    public int getServo4Max() {
        return servo4Max;
    }

    public void setServo4Max(int servo4Max) {
        this.servo4Max = servo4Max;
    }

    public int getServo5Min() {
        return servo5Min;
    }

    public void setServo5Min(int servo5Min) {
        this.servo5Min = servo5Min;
    }

    public int getServo5Max() {
        return servo5Max;
    }

    public void setServo5Max(int servo5Max) {
        this.servo5Max = servo5Max;
    }

    public int getServo6Min() {
        return servo6Min;
    }

    public void setServo6Min(int servo6Min) {
        this.servo6Min = servo6Min;
    }

    public int getServo6Max() {
        return servo6Max;
    }

    public void setServo6Max(int servo6Max) {
        this.servo6Max = servo6Max;
    }

    public int getArmLength1() {
        return armLength1;
    }

    public void setArmLength1(int armLength1) {
        this.armLength1 = armLength1;
    }

    public int getArmLength2() {
        return armLength2;
    }

    public void setArmLength2(int armLength2) {
        this.armLength2 = armLength2;
    }

    public int getArmLength3() {
        return armLength3;
    }

    public void setArmLength3(int armLength3) {
        this.armLength3 = armLength3;
    }

    public int getArmLength4() {
        return armLength4;
    }

    public void setArmLength4(int armLength4) {
        this.armLength4 = armLength4;
    }

}
