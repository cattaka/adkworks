
package net.cattaka.android.humitemp4ble;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.cattaka.android.humitemp4ble.db.DbHelper;
import net.cattaka.android.humitemp4ble.util.MyGraphDrawer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class GraphActivity extends Activity implements OnClickListener {
    public static final String EXTRA_DEVICE_ID = "deviceId";

    private ImageView mGraphImage;

    private DbHelper mDbHelper;

    private MyGraphDrawer mGraphDrawer;

    private GregorianCalendar mCalendar = new GregorianCalendar();

    private long mDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        findViewById(R.id.prevButton).setOnClickListener(this);
        findViewById(R.id.nextButton).setOnClickListener(this);

        mGraphImage = (ImageView)findViewById(R.id.graphImage);

        mDeviceId = (getIntent().getLongExtra(EXTRA_DEVICE_ID, -1));
        if (mDeviceId == -1) {
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDbHelper = new DbHelper(this);
        mGraphDrawer = new MyGraphDrawer(this, mDbHelper, mDeviceId);

        refleshView();
    }

    protected void onStop() {
        super.onStop();
        mGraphDrawer = null;
        mDbHelper.close();
        mDbHelper = null;
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.prevButton) {
            mCalendar.add(Calendar.DATE, -1);
            refleshView();
        } else if (v.getId() == R.id.nextButton) {
            mCalendar.add(Calendar.DATE, 1);
            refleshView();
        }
    }

    private void refleshView() {
        Bitmap bitmap = mGraphDrawer.createImageDay10Min(mCalendar);
        mGraphImage.setImageBitmap(bitmap);
    }
}
