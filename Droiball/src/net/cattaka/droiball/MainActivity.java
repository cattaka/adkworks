
package net.cattaka.droiball;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.droiball.data.FaceDetectionAlgorism;
import net.cattaka.droiball.data.WhiteBalance;
import net.cattaka.droiball.util.MyPreference;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends Activity implements View.OnClickListener, OnItemSelectedListener {

    private Spinner mPreviewSizeView;

    private Spinner mWhiteBalanceView;

    private Spinner mFaceDetectionAlgorismView;

    private MyPreference mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting event listeners
        findViewById(R.id.startButton).setOnClickListener(this);
        findViewById(R.id.editButton).setOnClickListener(this);
        findViewById(R.id.twitterSettingButton).setOnClickListener(this);

        mPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(this));

        mPreviewSizeView = (Spinner)findViewById(R.id.previewSizeSpinner);
        {
            List<String> sizeStrs = new ArrayList<String>();
            {
                VideoCapture mCapture = new VideoCapture();
                if (mCapture.open(0)) {
                    List<Size> sizes = mCapture.getSupportedPreviewSizes();
                    mCapture.release();
                    for (Size size : sizes) {
                        sizeStrs.add((int)size.width + "x" + (int)size.height);
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, sizeStrs);
            mPreviewSizeView.setAdapter(adapter);
            mPreviewSizeView.setOnItemSelectedListener(this);
            setSelection(mPreviewSizeView, getMyPreference().getPreviewSize());
        }

        mWhiteBalanceView = (Spinner)findViewById(R.id.whiteBalanceSpinner);
        {
            ArrayAdapter<WhiteBalance> adapter = new ArrayAdapter<WhiteBalance>(this,
                    android.R.layout.simple_list_item_1, WhiteBalance.values());
            mWhiteBalanceView.setAdapter(adapter);
            mWhiteBalanceView.setOnItemSelectedListener(this);
            setSelection(mWhiteBalanceView, getMyPreference().getWhiteBalance());
        }

        mFaceDetectionAlgorismView = (Spinner)findViewById(R.id.facedetectionAlgorismSpinner);
        {
            ArrayAdapter<FaceDetectionAlgorism> adapter = new ArrayAdapter<FaceDetectionAlgorism>(
                    this, android.R.layout.simple_list_item_1, FaceDetectionAlgorism.values());
            mFaceDetectionAlgorismView.setAdapter(adapter);
            mFaceDetectionAlgorismView.setOnItemSelectedListener(this);
            setSelection(mFaceDetectionAlgorismView, getMyPreference().getFaceDetectionAlgorism());
        }
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

    public void setSelection(AdapterView<?> view, Object obj) {
        if (obj != null) {
            for (int i = 0; i < view.getCount(); i++) {
                if (obj.equals(view.getItemAtPosition(i))) {
                    view.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.previewSizeSpinner) {
            String item = (String)parent.getItemAtPosition(position);
            getMyPreference().edit();
            getMyPreference().putPreviewSize(item);
            getMyPreference().commit();
        } else if (parent.getId() == R.id.whiteBalanceSpinner) {
            WhiteBalance item = (WhiteBalance)parent.getItemAtPosition(position);
            getMyPreference().edit();
            getMyPreference().putWhiteBalance(item);
            getMyPreference().commit();
        } else if (parent.getId() == R.id.facedetectionAlgorismSpinner) {
            FaceDetectionAlgorism item = (FaceDetectionAlgorism)parent.getItemAtPosition(position);
            getMyPreference().edit();
            getMyPreference().putFaceDetectionAlgorism(item);
            getMyPreference().commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // none
    }

    public MyPreference getMyPreference() {
        return mPreference;
    }
}
