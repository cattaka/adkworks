
package net.cattaka.android.foxkehrobo.activity;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.foxkehrobo.FoxkehRoboService;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.core.IAppStub;
import net.cattaka.android.foxkehrobo.core.MyPreference;
import net.cattaka.android.foxkehrobo.core.ServiceWrapper;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.db.FoxkehRoboDatabase;
import net.cattaka.android.foxkehrobo.fragment.ConnectFragment;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.adapter.IDeviceAdapterListener;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity implements IAppStub {
    private static final int EVENT_ON_DEVICE_STATE_CHANGED = 1;

    private static final int EVENT_ON_RECEIVE_PACKET = 2;

    private MainActivity me = this;

    private ServiceWrapper mServiceWrapper;

    private List<IDeviceAdapterListener<FrPacket>> mDeviceAdapterListeners;

    private int mServiceListenerSeq = -1;

    private MyPreference mPreference;

    private FoxkehRoboDatabase mDbhelper;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IActiveGeppaService service = IActiveGeppaService.Stub.asInterface(binder);
            mServiceWrapper = new ServiceWrapper(service);
            if (mServiceListenerSeq < 0) {
                try {
                    mServiceListenerSeq = service.registerServiceListener(mServiceListener);
                } catch (RemoteException e) {
                    // Nothing to do
                    throw new RuntimeException(e);
                }
            }
        }
    };

    private IActiveGeppaServiceListener mServiceListener = new IActiveGeppaServiceListener.Stub() {
        @Override
        public void onReceivePacket(PacketWrapper packet) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_RECEIVE_PACKET, new Object[] {
                    me, packet.getPacket()
            }).sendToTarget();
        }

        public void onDeviceStateChanged(final DeviceState state, final DeviceEventCode code,
                final DeviceInfo deviceInfo) throws RemoteException {
            sHandler.obtainMessage(EVENT_ON_DEVICE_STATE_CHANGED, new Object[] {
                    me, state, code, deviceInfo
            }).sendToTarget();
        }
    };

    private static Handler sHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Object[] objs = (Object[])msg.obj;
            MainActivity target = (MainActivity)objs[0];
            if (msg.what == EVENT_ON_RECEIVE_PACKET) {
                for (IDeviceAdapterListener<FrPacket> listener : target.mDeviceAdapterListeners) {
                    listener.onReceivePacket((FrPacket)objs[1]);
                }
            } else if (msg.what == EVENT_ON_DEVICE_STATE_CHANGED) {
                for (IDeviceAdapterListener<FrPacket> listener : target.mDeviceAdapterListeners) {
                    listener.onDeviceStateChanged((DeviceState)objs[1], (DeviceEventCode)objs[2],
                            (DeviceInfo)objs[3]);
                }
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        mDeviceAdapterListeners = new ArrayList<IDeviceAdapterListener<FrPacket>>();

        if (getFragmentManager().findFragmentById(R.id.primaryFragment) == null) {
            ConnectFragment fragment = new ConnectFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            ft.add(R.id.primaryFragment, fragment);

            // トランザクションをコミットする
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this, FoxkehRoboService.class);
        startService(service);
        bindService(service, mServiceConnection, 0);
        mPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(this));
        mDbhelper = new FoxkehRoboDatabase(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceListenerSeq >= 0) {
            try {
                mServiceWrapper.getService().unregisterServiceListener(mServiceListenerSeq);
                mServiceListenerSeq = -1;
            } catch (RemoteException e) {
                // Nothing to do
                throw new RuntimeException(e);
            }
        }
        unbindService(mServiceConnection);
        if (mDbhelper != null) {
            mDbhelper.close();
            mDbhelper = null;
        }
    }

    @Override
    public void replacePrimaryFragment(Fragment fragment, boolean withBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (withBackStack) {
            ft.addToBackStack(null);
        }
        ft.replace(R.id.primaryFragment, fragment);
        ft.commit();
    }

    @Override
    public ServiceWrapper getServiceWrapper() {
        return mServiceWrapper;
    }

    @Override
    public boolean registerDeviceAdapterListener(IDeviceAdapterListener<FrPacket> listener) {
        if (!mDeviceAdapterListeners.contains(listener)) {
            mDeviceAdapterListeners.add(listener);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unregisterDeviceAdapterListener(IDeviceAdapterListener<FrPacket> listener) {
        return mDeviceAdapterListeners.remove(listener);
    }

    @Override
    public void setKeepScreen(boolean flag) {
        if (flag) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public FoxkehRoboDatabase getDroiballDatabase() {
        return mDbhelper;
    }

    @Override
    public MyPreference getMyPreference() {
        return mPreference;
    }

}
