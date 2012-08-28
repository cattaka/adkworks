package jp.co.kayo.android.droiddancermotionwriter;

import java.util.Random;

public class MotionItem {
    private static Random _rand = new Random(System.nanoTime());
    private long uid;
    private boolean led;
    private byte armleft;
    private byte armright;
    private byte rotleft;
    private byte rotright;
    private byte time;
    
    public MotionItem(){
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
    public byte getArmleft() {
        return armleft;
    }
    public void setArmleft(byte armleft) {
        this.armleft = armleft;
    }
    public byte getArmright() {
        return armright;
    }
    public void setArmright(byte armright) {
        this.armright = armright;
    }
    public byte getRotleft() {
        return rotleft;
    }
    public void setRotleft(byte rotleft) {
        this.rotleft = rotleft;
    }
    public byte getRotright() {
        return rotright;
    }
    public void setRotright(byte rotright) {
        this.rotright = rotright;
    }
    public byte getTime() {
        return time;
    }
    public void setTime(byte time) {
        this.time = time;
    }
    
    
}
