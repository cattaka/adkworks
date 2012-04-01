
package net.cattaka.robotarm01;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class AdjustmentActivity extends PreferenceActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummary(sharedPreferences, key);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                listener);
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        for (String key : sharedPreferences.getAll().keySet()) {
            Preference pref = findPreference(key);
            // if (pref instanceof EditTextPreference) {
            // EditTextPreference prefEditText = (EditTextPreference)pref;
            // prefEditText.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            // }
            updateSummary(sharedPreferences, key);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                listener);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            String val = sharedPreferences.getString(key, "");
            pref.setSummary(val);
        }
    }
}
