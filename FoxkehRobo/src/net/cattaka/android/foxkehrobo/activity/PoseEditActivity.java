
package net.cattaka.android.foxkehrobo.activity;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.FoxkehRoboService;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.OpCode;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.libgeppa.IActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PoseEditActivity extends Activity implements View.OnClickListener {
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

    private class SeekBarBundle implements OnSeekBarChangeListener {
        private TextView textView;

        private CheckBox checkBox;

        private SeekBar seekBar;

        private boolean invert;

        public SeekBarBundle(int blockResId, String name, int max, boolean invert) {
            super();
            View block = findViewById(blockResId);
            textView = (TextView)block.findViewById(R.id.blockText);
            seekBar = (SeekBar)block.findViewById(R.id.blockSeek);
            checkBox = (CheckBox)block.findViewById(R.id.blockCheck);
            this.invert = invert;

            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    seekBar.setEnabled(checked);
                }
            });

            checkBox.setText(name);
            checkBox.setChecked(true);
            seekBar.setMax(max);
            seekBar.setProgress(max / 2);
            seekBar.setOnSeekBarChangeListener(this);

            checkBox.setVisibility(mEditMode ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendPose(true);
            updateTextView(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                sendPose(false);
            }
            updateTextView(seekBar);
        }

        private void updateTextView(SeekBar seekBar) {
            int value = seekBar.getProgress();
            if (invert) {
                value = seekBar.getMax() - value;
            }
            textView.setText(String.valueOf(value));
        }

        public void setByte(Byte b) {
            checkBox.setChecked(b != null);
            if (b != null) {
                if (invert) {
                    seekBar.setProgress(seekBar.getMax() - (0xFF & b));
                } else {
                    seekBar.setProgress(0xFF & b);
                }
            }
        }

        public Byte getByte() {
            if (checkBox.isChecked()) {
                int r = (invert) ? seekBar.getMax() - seekBar.getProgress() : seekBar.getProgress();
                return (byte)r;
            } else {
                return null;
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGeppaService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGeppaService = IActiveGeppaService.Stub.asInterface(service);
            sendPose(true);

            try {
                mGeppaServiceListenerSeq = mGeppaService
                        .registerServiceListener(mGeppaServiceListener);
                mConnectionStateText.setText(mGeppaService.getCurrentDeviceInfo().getLabel());
            } catch (RemoteException e) {
                // Impossible, ignore
                Log.w(Constants.TAG, e.getMessage(), e);
            }
        }
    };

    private int mGeppaServiceListenerSeq = -1;

    private IActiveGeppaServiceListener.Stub mGeppaServiceListener = new IActiveGeppaServiceListener.Stub() {
        public void onDeviceStateChanged(DeviceState deviceState, DeviceEventCode deviceEventCode,
                DeviceInfo deviceInfo) throws RemoteException {
            mConnectionStateText.setText(deviceState.name());
        }

        @Override
        public void onReceivePacket(PacketWrapper paramPacketWrapper) throws RemoteException {
            // TODO Auto-generated method stub
        }
    };

    private boolean mEditMode = true;

    private PoseModel mPoseModel;;

    private IActiveGeppaService mGeppaService;

    private long lastSendControl = 0;

    private SeekBarBundle mHeadYaw;

    private SeekBarBundle mHeadPitch;

    private SeekBarBundle mArmLeft;

    private SeekBarBundle mArmRight;

    private SeekBarBundle mFootLeft;

    private SeekBarBundle mFootRight;

    private SeekBarBundle mEarLeft;

    private SeekBarBundle mEarRight;

    private SeekBarBundle mTailYaw;

    private SeekBarBundle mTailPitch;

    private SeekBarBundle mTime;

    private TextView mConnectionStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pose_edit);

        mEditMode = (getIntent().getBooleanExtra(EXTRA_EDIT_MODE, true));
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

        // Pick up views
        mHeadYaw = new SeekBarBundle(R.id.headYawBlock, "Head yaw", Constants.SEEK_MAX_VALUE, true);
        mHeadPitch = new SeekBarBundle(R.id.headPitchBlock, "Head pitch", Constants.SEEK_MAX_VALUE,
                true);
        mArmLeft = new SeekBarBundle(R.id.leftArmBlock, "Left arm", Constants.SEEK_MAX_VALUE, true);
        mArmRight = new SeekBarBundle(R.id.rightArmBlock, "Right arm", Constants.SEEK_MAX_VALUE,
                false);
        mFootLeft = new SeekBarBundle(R.id.leftFootBlock, "Left foot", Constants.SEEK_MAX_VALUE,
                true);
        mFootRight = new SeekBarBundle(R.id.rightFootBlock, "Right foot", Constants.SEEK_MAX_VALUE,
                false);
        mEarLeft = new SeekBarBundle(R.id.leftEarBlock, "Left ear", Constants.SEEK_MAX_VALUE, true);
        mEarRight = new SeekBarBundle(R.id.rightEarBlock, "Right ear", Constants.SEEK_MAX_VALUE,
                false);
        mTailYaw = new SeekBarBundle(R.id.tailYawBlock, "Tail yaw", Constants.SEEK_MAX_VALUE, true);
        mTailPitch = new SeekBarBundle(R.id.tailPitchBlock, "Tail pitch", Constants.SEEK_MAX_VALUE,
                true);
        mTime = new SeekBarBundle(R.id.timeBlock, "Time", 100, false);
        mConnectionStateText = (TextView)findViewById(R.id.connectionStateText);

        mTime.checkBox.setVisibility(View.INVISIBLE);

        // Setting event handler
        findViewById(R.id.finishButton).setOnClickListener(this);

        // Setting initial values
        mConnectionStateText.setText(ConnectionState.UNKNOWN.name());

        loadPoseModel();
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
                mGeppaService.unregisterServiceListener(mGeppaServiceListenerSeq);
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
            savePoseModel();
            Intent data = new Intent();
            data.putExtra(EXTRA_POSE_MODEL, mPoseModel);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void sendPose(boolean forceSend) {
        { // Limit number of times not to send too many times
            long currTime = SystemClock.currentThreadTimeMillis();
            if (!forceSend) {
                if (currTime - lastSendControl <= MINIMUM_INTERVAL_SEND_CONTROL) {
                    return;
                }
            }
            lastSendControl = currTime;
        }
        savePoseModel();
        byte[] data = mPoseModel.toPose();

        FrPacket packet = new FrPacket(OpCode.POSE, data.length, data);
        try {
            if (mGeppaService != null) {
                mGeppaService.sendPacket(new PacketWrapper(packet));
            }
        } catch (RemoteException e) {
            Log.w(Constants.TAG, e.getMessage(), e);
        }
    }

    private void loadPoseModel() {
        mHeadYaw.setByte(mPoseModel.getHeadYaw());
        mHeadPitch.setByte(mPoseModel.getHeadPitch());
        mArmLeft.setByte(mPoseModel.getArmLeft());
        mArmRight.setByte(mPoseModel.getArmRight());
        mFootLeft.setByte(mPoseModel.getFootLeft());
        mFootRight.setByte(mPoseModel.getFootRight());
        mEarLeft.setByte(mPoseModel.getEarLeft());
        mEarRight.setByte(mPoseModel.getEarRight());
        mTailYaw.setByte(mPoseModel.getTailYaw());
        mTailPitch.setByte(mPoseModel.getTailPitch());
        mTime.setByte((byte)(mPoseModel.getTime() / 100));
    }

    private void savePoseModel() {
        mPoseModel.setNonKeyValues( //
                mHeadYaw.getByte(), //
                mHeadPitch.getByte(), //
                mArmLeft.getByte(), //
                mArmRight.getByte(), //
                mFootLeft.getByte(), //
                mFootRight.getByte(), //
                mEarLeft.getByte(), //
                mEarRight.getByte(), //
                mTailYaw.getByte(), //
                mTailPitch.getByte(), //
                mTime.getByte() * 100);
    }
}
