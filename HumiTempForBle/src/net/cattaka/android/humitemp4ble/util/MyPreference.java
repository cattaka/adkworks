
package net.cattaka.android.humitemp4ble.util;

import android.content.SharedPreferences;

public class MyPreference {

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

}
