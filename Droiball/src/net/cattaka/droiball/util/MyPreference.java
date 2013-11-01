
package net.cattaka.droiball.util;

import net.cattaka.droiball.data.FaceDetectionAlgorism;
import net.cattaka.droiball.data.WhiteBalance;
import net.cattaka.droiball.entity.Vector3s;

import org.opencv.core.Size;

import android.content.SharedPreferences;

public class MyPreference {
    private static final String OFFSET_ACCEL = "offsetAccel";

    private static final String ACCESS_TOKEN = "accessToken";

    private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";

    private static final String TRACK_WORDS = "trakWords";

    private static String KEY_PREVIEW_SIZE = "PreviewSize";

    private static String KEY_WHITE_BALANCE = "WhiteBalance";

    private static String KEY_FACE_DETECTION_ALGORISM = "FaceDetectionAlgorism";

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    public MyPreference(SharedPreferences pref) {
        super();
        this.pref = pref;
    }

    public void edit() {
        editor = pref.edit();
    }

    public void commit() {
        editor.commit();
        editor = null;
    }

    public Vector3s getOffsetAccel() {
        Vector3s val = new Vector3s();
        val.setX((short)pref.getInt(OFFSET_ACCEL + "_X", 0));
        val.setY((short)pref.getInt(OFFSET_ACCEL + "_Y", 0));
        val.setZ((short)pref.getInt(OFFSET_ACCEL + "_Z", 0));
        return val;
    }

    public void putOffsetAccel(Vector3s val) {
        editor.putInt(OFFSET_ACCEL + "_X", val.getX());
        editor.putInt(OFFSET_ACCEL + "_Y", val.getY());
        editor.putInt(OFFSET_ACCEL + "_Z", val.getZ());
    }

    public String getAccessToken() {
        return pref.getString(ACCESS_TOKEN, null);
    }

    public void putAccessToken(String accessToken) {
        editor.putString(ACCESS_TOKEN, accessToken);
    }

    public String getAccessTokenSecret() {
        return pref.getString(ACCESS_TOKEN_SECRET, null);
    }

    public void putAccessTokenSecret(String accessTokenSecret) {
        editor.putString(ACCESS_TOKEN_SECRET, accessTokenSecret);
    }

    public String getTrackWords() {
        return pref.getString(TRACK_WORDS, null);
    }

    public void putTrackWords(String accessTokenSecret) {
        editor.putString(TRACK_WORDS, accessTokenSecret);
    }

    public String getPreviewSize() {
        return pref.getString(KEY_PREVIEW_SIZE, "800x600");
    }

    public void putPreviewSize(String previewSize) {
        editor.putString(KEY_PREVIEW_SIZE, previewSize);
    }

    public Size getPreviewSizeAsSize() {
        Size result = null;
        String str = getPreviewSize();
        if (str.indexOf('x') >= 0) {
            String[] ts = str.split("x");
            if (ts.length >= 2) {
                try {
                    double w = Double.parseDouble(ts[0]);
                    double h = Double.parseDouble(ts[1]);
                    result = new Size(w, h);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        if (result == null) {
            result = new Size(800, 600);
        }
        return result;
    }

    public WhiteBalance getWhiteBalance() {
        String name = pref.getString(KEY_WHITE_BALANCE, WhiteBalance.AUTO.name());
        try {
            return WhiteBalance.valueOf(name);
        } catch (IllegalArgumentException e) {
            return WhiteBalance.AUTO;
        }
    }

    public void putWhiteBalance(WhiteBalance whiteBalance) {
        if (whiteBalance != null) {
            editor.putString(KEY_WHITE_BALANCE, whiteBalance.name());
        } else {
            editor.putString(KEY_WHITE_BALANCE, null);
        }
    }

    public FaceDetectionAlgorism getFaceDetectionAlgorism() {
        String name = pref.getString(KEY_FACE_DETECTION_ALGORISM,
                FaceDetectionAlgorism.HAARCASCADE_FRONTALFACE_ALT.name());
        try {
            return FaceDetectionAlgorism.valueOf(name);
        } catch (IllegalArgumentException e) {
            return FaceDetectionAlgorism.HAARCASCADE_FRONTALFACE_ALT;
        }
    }

    public void putFaceDetectionAlgorism(FaceDetectionAlgorism FaceDetectionAlgorism) {
        if (FaceDetectionAlgorism != null) {
            editor.putString(KEY_FACE_DETECTION_ALGORISM, FaceDetectionAlgorism.name());
        } else {
            editor.putString(KEY_FACE_DETECTION_ALGORISM, null);
        }
    }
}
