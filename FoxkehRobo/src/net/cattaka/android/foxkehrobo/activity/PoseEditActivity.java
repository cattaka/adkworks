
package net.cattaka.android.foxkehrobo.activity;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.FoxkehRoboService;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.core.ServiceWrapper;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.android.foxkehrobo.fragment.PoseEditFragment;
import net.cattaka.android.foxkehrobo.fragment.PoseEditFragment.IPoseEditFragmentListener;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PoseEditActivity extends FragmentActivity implements View.OnClickListener,
        IPoseEditFragmentListener {
    public static final String EXTRA_EDIT_MODE = "editMode";

    public static final String EXTRA_POSE_MODEL = "poseModel";

    private static final int MINIMUM_INTERVAL_SEND_CONTROL = 200;

    /*
     * private class ToggleButtonBundle { private CheckBox checkBox; private
     * ToggleButton toggleButton; public ToggleButtonBundle(int toggleResId, int
     * checkResId, String name) { checkBox = (CheckBox)findViewById(checkResId);
     * toggleButton = (ToggleButton)findViewById(toggleResId);
     * checkBox.setText(name); checkBox.setChecked(true);
     * toggleButton.setChecked(true); checkBox.setOnClickListener(new
     * View.OnClickListener() {
     * @Override public void onClick(View paramView) {
     * toggleButton.setEnabled(checkBox.isChecked()); } });
     * toggleButton.setOnClickListener(new View.OnClickListener() {
     * @Override public void onClick(View paramView) { sendPose(true); } });
     * checkBox.setVisibility(mEditMode ? View.VISIBLE : View.INVISIBLE); }
     * public boolean isChecked() { return toggleButton.isChecked(); } public
     * void setBoolean(Boolean b) { checkBox.setChecked(b != null); if (b !=
     * null) { toggleButton.setChecked(b); } } public Boolean getBoolean() {
     * return (checkBox.isChecked()) ? toggleButton.isChecked() : null; } }
     */

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IActiveGeppaService service = IActiveGeppaService.Stub.asInterface(binder);
            mServiceWrapper = new ServiceWrapper(service);
            mPoseEditFragment.savePoseModel(mPoseModel);
            mServiceWrapper.sendPose(mPoseModel);

            try {
                mGeppaServiceListenerSeq = service.registerServiceListener(mGeppaServiceListener);
                mConnectionStateText.setText(String.valueOf(service.getCurrentDeviceInfo()));
            } catch (RemoteException e) {
                // Impossible, ignore
                Log.w(Constants.TAG, e.getMessage(), e);
            }
        }
    };

    private int mGeppaServiceListenerSeq = -1;

    private PoseModel mPoseModel;

    private IActiveGeppaServiceListener.Stub mGeppaServiceListener = new IActiveGeppaServiceListener.Stub() {
        public void onDeviceStateChanged(DeviceState deviceState, DeviceEventCode deviceEventCode,
                DeviceInfo deviceInfo) throws RemoteException {
            mConnectionStateText.setText(String.valueOf(deviceState));
        }

        @Override
        public void onReceivePacket(PacketWrapper paramPacketWrapper) throws RemoteException {
            // none
        }
    };

    private ServiceWrapper mServiceWrapper;

    private long lastSendControl = 0;

    private TextView mConnectionStateText;

    private PoseEditFragment mPoseEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pose_edit);

        FragmentManager fr = getSupportFragmentManager();
        mPoseEditFragment = (PoseEditFragment)fr.findFragmentById(R.id.poseEditFragment);

        {
            Object obj = getIntent().getSerializableExtra(EXTRA_POSE_MODEL);
            if (obj instanceof PoseModel) {
                mPoseModel = (PoseModel)obj;
            } else {
                mPoseModel = new PoseModel();
                mPoseModel.setNonKeyValues( //
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // headYaw
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // headPitch
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // armLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // armRight
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // footLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // footRight
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // earLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // earRight
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // tailYaw
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // tailPitch
                        1000 // time
                        );
            }
        }

        mConnectionStateText = (TextView)findViewById(R.id.connectionStateText);

        // Setting event handler
        findViewById(R.id.finishButton).setOnClickListener(this);

        // Setting initial values
        mConnectionStateText.setText(ConnectionState.UNKNOWN.name());

        mPoseEditFragment.setListener(this);
        mPoseEditFragment.loadPoseModel(mPoseModel);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent service = new Intent(this, FoxkehRoboService.class);
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGeppaServiceListenerSeq != -1) {
            try {
                mServiceWrapper.getService().unregisterServiceListener(mGeppaServiceListenerSeq);
            } catch (RemoteException e) {
                // impossible, ignore
                Log.w(Constants.TAG, e.getMessage(), e);
            }
            mGeppaServiceListenerSeq = -1;
        }

        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finishButton) {
            mPoseEditFragment.savePoseModel(mPoseModel);
            Intent data = new Intent();
            data.putExtra(EXTRA_POSE_MODEL, mPoseModel);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onPoseChanged(boolean operationFinished) {
        { // Limit number of times not to send too many times
            long currTime = SystemClock.currentThreadTimeMillis();
            if (!operationFinished) {
                if (currTime - lastSendControl <= MINIMUM_INTERVAL_SEND_CONTROL) {
                    return;
                }
            }
            lastSendControl = currTime;
        }

        mPoseEditFragment.savePoseModel(mPoseModel);
        mServiceWrapper.sendPose(mPoseModel);
    }
}
