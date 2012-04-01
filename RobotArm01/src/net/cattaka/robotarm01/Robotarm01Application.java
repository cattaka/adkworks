
package net.cattaka.robotarm01;

import net.cattaka.robotarm01.service.AdkService;
import android.app.Application;
import android.content.Intent;

public class Robotarm01Application extends Application {
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
