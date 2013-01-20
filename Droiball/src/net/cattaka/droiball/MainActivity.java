
package net.cattaka.droiball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting event listeners
        findViewById(R.id.startButton).setOnClickListener(this);
        findViewById(R.id.editButton).setOnClickListener(this);
        findViewById(R.id.twitterSettingButton).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            Intent intent = new Intent(this, AppActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.editButton) {
            Intent intent = new Intent(this, PoseEditActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.twitterSettingButton) {
            Intent intent = new Intent(this, AuthTwitterActivity.class);
            startActivity(intent);
        }
    }
}
