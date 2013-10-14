
package net.cattaka.android.foxkehrobo.activity;

import net.cattaka.android.foxkehrobo.Constants;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class EntryActivity extends Activity {
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(Constants.TAG, "OpenCV loaded successfully");
                    onLoaded();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    };

    private void onLoaded() {
        Intent intent = new Intent(this, AppActivity.class);
        startActivity(intent);
        finish();
    }
}
