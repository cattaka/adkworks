
package net.cattaka.android.humitemp4ble;

import net.cattaka.android.humitemp4ble.data.UserInfo;
import net.cattaka.android.humitemp4ble.entity.DeviceModel;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SyncActivity extends Activity implements OnClickListener, IHumiTempServiceListener {
    private SyncActivity me = this;

    private IHumiTempService mService;

    private ServiceConnectionEx mServiceConnection = new ServiceConnectionEx();

    class ServiceConnectionEx implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName paramComponentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = HumiTempService.Stub.asInterface(binder);
            // try {
            // mService.registerServiceListener(mServiceListener);
            // } catch (RemoteException e) {
            // throw new RuntimeException(e);
            // }
            me.onServiceConnected();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        findViewById(R.id.registerButton).setOnClickListener(this);
        findViewById(R.id.requestSyncButton).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, HumiTempService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_input_username, null);
            final TextView textView = (TextView)view.findViewById(R.id.usernameEdit);
            builder.setView(view);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String username = String.valueOf(textView.getText());
                    if (username.length() > 0) {
                        try {
                            mService.regiterUser(username);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            builder.create().show();
        } else if (v.getId() == R.id.requestSyncButton) {
            try {
                mService.requestUpload();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onServiceConnected() {
        updateUserInfo();
    }

    private void updateUserInfo() {
        try {
            UserInfo userInfo = mService.getUserInfo();
            int v = (userInfo != null) ? View.INVISIBLE : View.VISIBLE;
            findViewById(R.id.registerButton).setVisibility(v);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IBinder asBinder() {
        // not used
        return null;
    }

    @Override
    public void onDeviceModelUpdated(DeviceModel model) {
        // ok
    }

    @Override
    public void onWebEvent(int webEvent) {
        Toast.makeText(me, "onWebEvent:" + webEvent, Toast.LENGTH_SHORT).show();
        updateUserInfo();
    }
}
