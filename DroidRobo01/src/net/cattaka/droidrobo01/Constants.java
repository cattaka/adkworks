package net.cattaka.droidrobo01;

import android.content.Context;

public class Constants {
    public static String getTwConsumerKey(Context context) {
        return context.getResources().getString(R.string.tw_consumer_key);
    }
    public static String getTwConsumerSecret(Context context) {
        return context.getResources().getString(R.string.tw_consumer_secret);
    }
}
