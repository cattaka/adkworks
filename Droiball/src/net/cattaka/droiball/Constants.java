
package net.cattaka.droiball;

import android.content.Context;

public class Constants {
    public static String TAG = "Droiball";

    public static final int SEEK_MAX_VALUE = 0xFF;

    public static final String DB_NAME = "droiball.db";

    public static final String ACTION_NAME_WAKE_FRONT = "Wake-front";

    public static final String ACTION_NAME_WAKE_BACK = "Wake-back";

    public static final String ACTION_NAME_WAVE_LEFT = "Wave-left";

    public static final String ACTION_NAME_WAVE_RIGHT = "Wave-right";

    public static final String[] ACTION_NAMES_RANDOM = new String[] {
            "Head-dance", "Ear-dance", "Arm-dance", "Ear-cast-down"
    };

    public static String getTwConsumerKey(Context context) {
        return context.getResources().getString(R.string.tw_consumer_key);
    }

    public static String getTwConsumerSecret(Context context) {
        return context.getResources().getString(R.string.tw_consumer_secret);
    }
}
