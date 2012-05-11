
package net.cattaka.droidrobo01;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import net.cattaka.droidrobo01.R;

public class ArmSetting {
    private int servo1Min;

    private int servo1Max;

    private int servo2Min;

    private int servo2Max;

    private boolean servo1Invert = true;

    private boolean servo2Invert = true;
    
    private String accessToken;
    
    private String accessTokenSecret;
    
    private String trackWords;

    public void loadPreference(SharedPreferences pref) {
        servo1Min = loadInt(pref, "servo1_min", -80);
        servo1Max = loadInt(pref, "servo1_max", 80);
        servo2Min = loadInt(pref, "servo2_min", -80);
        servo2Max = loadInt(pref, "servo2_max", 80);
        accessToken = pref.getString("accessToken", "");
        accessTokenSecret = pref.getString("accessTokenSecret", "");
        trackWords = pref.getString("trackWords", "droidrobo");
    }
    
    public void savePreference(SharedPreferences pref) {
        Editor editor = pref.edit();
        editor.putString("servo1_min", ""+servo1Min);
        editor.putString("servo1_max", ""+servo1Max);
        editor.putString("servo2_min", ""+servo2Min);
        editor.putString("servo2_max", ""+servo2Max);
        editor.putString("accessToken", accessToken);
        editor.putString("accessTokenSecret", accessTokenSecret);
        editor.putString("trackWords", trackWords);

        editor.commit();
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getTrackWords() {
        return trackWords;
    }

    public void setTrackWords(String trackWords) {
        this.trackWords = trackWords;
    }
    
    
}
