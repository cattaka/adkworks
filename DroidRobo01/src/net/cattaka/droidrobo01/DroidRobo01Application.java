
package net.cattaka.droidrobo01;

import net.cattaka.droidrobo01.R;
import net.cattaka.droidrobo01.service.AdkService;
import android.app.Application;
import android.content.Intent;

public class DroidRobo01Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, AdkService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Intent intent = new Intent(this, AdkService.class);
        stopService(intent);
    }
}
