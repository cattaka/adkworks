
package net.cattaka.droiball.util;

import net.cattaka.droiball.entity.Vector3s;
import android.content.SharedPreferences;

public class MyPreference {
    private static final String OFFSET_ACCEL = "offsetAccel";

    private static final String ACCESS_TOKEN = "accessToken";

    private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";

    private static final String TRACK_WORDS = "trakWords";

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
}
