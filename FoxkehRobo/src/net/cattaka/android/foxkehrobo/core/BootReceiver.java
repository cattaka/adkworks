
package net.cattaka.android.foxkehrobo.core;

import net.cattaka.android.foxkehrobo.activity.EntryActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyPreference pref = new MyPreference(PreferenceManager.getDefaultSharedPreferences(context));
        if (pref.getStartupOnBoot()) {
            Intent activityIntent = new Intent(context, EntryActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}
