
package net.cattaka.android.humitemp4ble.core;

import net.cattaka.android.humitemp4ble.data.UserInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MyPreference {
    private static final String NAME = "humitemp.pref";

    private static final String KEY_USER_INFO = "userInfo";

    private SharedPreferences mPref;

    private Editor mEditor;

    public MyPreference(Context context) {
        mPref = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void edit() {
        mEditor = mPref.edit();
    }

    public void commit() {
        mEditor.commit();
        mEditor = null;
    }

    public UserInfo getUserInfo() {
        int userId = mPref.getInt(KEY_USER_INFO + ".userId", -1);
        String username = mPref.getString(KEY_USER_INFO + ".username", null);
        String token = mPref.getString(KEY_USER_INFO + ".token", null);
        if (username != null && token != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setUsername(username);
            userInfo.setToken(token);
            return userInfo;
        } else {
            return null;
        }
    }

    public void putUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            mEditor.putInt(KEY_USER_INFO + ".userId", userInfo.getUserId());
            mEditor.putString(KEY_USER_INFO + ".username", userInfo.getUsername());
            mEditor.putString(KEY_USER_INFO + ".token", userInfo.getToken());
        } else {
            mEditor.remove(KEY_USER_INFO + ".userId");
            mEditor.remove(KEY_USER_INFO + ".username");
            mEditor.remove(KEY_USER_INFO + ".token");
        }
    }

}
