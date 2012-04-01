
package net.cattaka.robotarm01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import net.cattaka.robotarm01.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.main);
        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonAutoDetection).setOnClickListener(this);
        findViewById(R.id.buttonAdjustment).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonStart) {
            Intent intent = new Intent(this, ControllerActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonAutoDetection) {
            Intent intent = new Intent(this, AutoDetectionModeActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.buttonAdjustment) {
            Intent intent = new Intent(this, AdjustmentActivity.class);
            startActivity(intent);
        }
    }
}
