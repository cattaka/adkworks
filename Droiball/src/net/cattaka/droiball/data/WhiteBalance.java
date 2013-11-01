
package net.cattaka.droiball.data;

import org.opencv.highgui.Highgui;

public enum WhiteBalance {
    AUTO(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_AUTO), //
    CLOUDY_DAYLIGHT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_CLOUDY_DAYLIGHT), //
    DAYLIGHT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_DAYLIGHT), //
    FLUORESCENT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_FLUORESCENT), //
    INCANDESCENT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_INCANDESCENT), //
    SHADE(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_SHADE), //
    TWILIGHT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_TWILIGHT), //
    WARM_FLUORESCENT(Highgui.CV_CAP_ANDROID_WHITE_BALANCE_WARM_FLUORESCENT) //
    ;
    public final int value;

    private WhiteBalance(int value) {
        this.value = value;
    }

}
