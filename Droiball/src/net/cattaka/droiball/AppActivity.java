
package net.cattaka.droiball;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.cattaka.droiball.db.DroiballDatabase;
import net.cattaka.droiball.fragment.ActionListFragment;
import net.cattaka.droiball.fragment.AiModeFragment;
import net.cattaka.droiball.fragment.ControllerFragment;
import net.cattaka.droiball.fragment.MonitorTwitterFragment;
import net.cattaka.droiball.util.MyPreference;
import net.cattaka.libgeppa.IGeppaService;
import net.cattaka.libgeppa.IGeppaServiceListener;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.samples.fd.DetectionBasedTracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AppActivity extends FragmentActivity implements IAppStub, View.OnClickListener {
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeppaService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGeppaService = IGeppaService.Stub.asInterface(service);
            getAppListener().onServiceConnected(name, service);

            try {
                mGeppaServiceListenerSeq = mGeppaService
                        .registerGeppaServiceListener(mGeppaServiceListener);
                mConnectionStateText.setText(mGeppaService.getConnectionState().name());
            } catch (RemoteException e) {
                // Impossible, ignore
                Log.w(Constants.TAG, e.getMessage(), e);
            }
        }
    };

    class FragmentPagerAdapterEx extends FragmentPagerAdapter {

        public FragmentPagerAdapterEx(FragmentManager fm) {
            super(fm);
        }

        private Fragment[] fragments = new Fragment[] {
                new ControllerFragment(), new ActionListFragment(), new MonitorTwitterFragment(),
                new AiModeFragment()
        };

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public Fragment getItem(int idx) {
            return fragments[idx];
        }
    };

    private IGeppaServiceListener.Stub mGeppaServiceListener = new IGeppaServiceListener.Stub() {
        @Override
        public void onConnectionStateChanged(ConnectionState connectionState)
                throws RemoteException {
            mConnectionStateText.setText(connectionState.name());
        }

        @Override
        public void onReceivePacket(PacketWrapper packetWrapper) throws RemoteException {
            getAppListener().onReceivePacket(packetWrapper);
        }
    };

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        private int lastPosition = -1;

        @Override
        public void onPageSelected(int position) {
            if (lastPosition != position) {
                if (lastPosition >= 0) {
                    getAppListener(lastPosition).onPageDeselected();
                }
                if (position >= 0) {
                    getAppListener(position).onPageSelected();
                }
                lastPosition = position;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(Constants.TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(
                                R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(),
                                0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(Constants.TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    private File mCascadeFile;

    private DetectionBasedTracker mNativeDetector;

    private int mGeppaServiceListenerSeq = -1;

    private IGeppaService mGeppaService;

    private TextView mConnectionStateText;

    private DroiballDatabase mDroiballDatabase;

    private ViewPager mBodyPager;

    private MyPreference mMyPreference;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.app);

        mConnectionStateText = (TextView)findViewById(R.id.connectionStateText);

        // Setting initial values
        mConnectionStateText.setText(ConnectionState.UNKNOWN.name());

        {
            mBodyPager = (ViewPager)findViewById(R.id.bodyPager);
            FragmentPagerAdapter adapter = new FragmentPagerAdapterEx(getSupportFragmentManager());
            mBodyPager.setAdapter(adapter);

            mBodyPager.setOnPageChangeListener(mOnPageChangeListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

        { // opening DB
            if (mDroiballDatabase != null) {
                mDroiballDatabase.close();
                ;
            }
            if (mDroiballDatabase == null) {
                mDroiballDatabase = new DroiballDatabase(this);
            }
        }

        Intent service = new Intent(this, GeppaServiceEx.class);
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);

        mOnPageChangeListener.onPageSelected(mBodyPager.getCurrentItem());

        mMyPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onPause() {
        mOnPageChangeListener.onPageSelected(-1);
        super.onPause();
        { // closing DB
            mDroiballDatabase.close();
            mDroiballDatabase = null;
            ;
        }

        if (mGeppaServiceListenerSeq != -1) {
            try {
                mGeppaService.unregisterGeppaServiceListener(mGeppaServiceListenerSeq);
            } catch (RemoteException e) {
                // impossible, ignore
                Log.w(Constants.TAG, e.getMessage(), e);
            }
            mGeppaServiceListenerSeq = -1;
        }

        mMyPreference = null;

        unbindService(mServiceConnection);
    }

    public IAppListener getAppListener() {
        return (IAppListener)((FragmentPagerAdapter)mBodyPager.getAdapter()).getItem(mBodyPager
                .getCurrentItem());
    }

    public IAppListener getAppListener(int position) {
        return (IAppListener)((FragmentPagerAdapter)mBodyPager.getAdapter()).getItem(position);
    }

    @Override
    public IGeppaService getGeppaService() {
        return mGeppaService;
    }

    @Override
    public DroiballDatabase getDroiballDatabase() {
        return mDroiballDatabase;
    }

    @Override
    public DetectionBasedTracker getNativeDetector() {
        return mNativeDetector;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public MyPreference getMyPreference() {
        return mMyPreference;
    }
}
