
package jp.co.kayo.android.droiddancermotionwriter;

import java.util.Random;

public class MotionItem {
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
            if (intValue < 0) {
                return REVERSE;
            } else if (intValue > 0) {
                return FORWARD;
            }
            return STOP;
        }
    }

    private static Random _rand = new Random(System.nanoTime());

    private long uid;

    private boolean led;

    private int armleft;

    private int armright;

    private MotorDir rotleft = MotorDir.STOP;

    private MotorDir rotright = MotorDir.STOP;

    private int time;

    public MotionItem() {
        uid = _rand.nextLong();
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public boolean isLed() {
        return led;
    }

    public void setLed(boolean led) {
        this.led = led;
    }

    public int getArmleft() {
        return armleft;
    }

    public void setArmleft(int armleft) {
        this.armleft = armleft;
    }

    public int getArmright() {
        return armright;
    }

    public void setArmright(int armright) {
        this.armright = armright;
    }

    public MotorDir getRotleft() {
        return rotleft;
    }

    public void setRotleft(MotorDir rotleft) {
        this.rotleft = rotleft;
    }

    public MotorDir getRotright() {
        return rotright;
    }

    public void setRotright(MotorDir rotright) {
        this.rotright = rotright;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
