
package net.cattaka.droidrobo01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.main);
        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonAuthTwitter).setOnClickListener(this);
        findViewById(R.id.buttonAdjustment).setOnClickListener(this);
        findViewById(R.id.buttonMonitorTwitter).setOnClickListener(this);
        findViewById(R.id.buttonMonitorNfc).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonStart) {
            Intent intent = new Intent(this, ControllerActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonAuthTwitter) {
            Intent intent = new Intent(this, AuthTwitterActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonMonitorTwitter) {
            Intent intent = new Intent(this, MonitorTwitterActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonMonitorNfc) {
            Intent intent = new Intent(this, MonitorNfcActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonAdjustment) {
            Intent intent = new Intent(this, AdjustmentActivity.class);
            startActivity(intent);
        }
    }
}
