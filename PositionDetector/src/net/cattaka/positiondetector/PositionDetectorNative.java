
package net.cattaka.positiondetector;

public class PositionDetectorNative {
    static {
        System.loadLibrary("QCAR");
        System.loadLibrary("PositionDetector");
    }

    public static native void setActivityPortraitMode(boolean isPortrait);

    public static native boolean getTrakerPose(int idx, float[] dst);

    public static native int renderFrame();

    public static native void initApplicationNative(int width, int height, int numOfTags);

    public static native void deinitApplicationNative();

    public static native void startCamera();

    public static native void stopCamera();

    public static native boolean setFlash(boolean enable);

    public static native boolean isFlash();

    public static native boolean startAutoFocus();

    public static native boolean setFocusMode(int mode);

    public static native void initRendering();

    public static native void updateRendering(int width, int height);
}
