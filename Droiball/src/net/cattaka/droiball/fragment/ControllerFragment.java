
package net.cattaka.droiball.fragment;

import net.cattaka.droiball.Constants;
import net.cattaka.droiball.R;
import net.cattaka.droiball.data.MyPacket;
import net.cattaka.droiball.data.OpCode;
import net.cattaka.droiball.entity.PoseModel;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ControllerFragment extends BaseFragment {
    private static final int MINIMUM_INTERVAL_SEND_CONTROL = 200;

    private class ToggleButtonBundle {
        private CheckBox checkBox;

        private ToggleButton toggleButton;

        public ToggleButtonBundle(View view, int toggleResId, int checkResId, String name) {
            checkBox = (CheckBox)view.findViewById(checkResId);
            toggleButton = (ToggleButton)view.findViewById(toggleResId);
            checkBox.setText(name);

            checkBox.setChecked(true);
            toggleButton.setChecked(true);
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    toggleButton.setEnabled(checked);
                }
            });
            toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    sendEyeLed();
                }
            });

            checkBox.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
        }

        public boolean isChecked() {
            return toggleButton.isChecked();
        }

        public void setBoolean(Boolean b) {
            checkBox.setChecked(b != null);
            if (b != null) {
                toggleButton.setChecked(b);
            }
        }

        public Boolean getBoolean() {
            return (checkBox.isChecked()) ? toggleButton.isChecked() : null;
        }
    }

    private class SeekBarBundle implements OnSeekBarChangeListener {
        private TextView textView;

        private CheckBox checkBox;

        private SeekBar seekBar;

        private boolean invert;

        public SeekBarBundle(View view, int blockResId, String name, int max, boolean invert) {
            super();
            View block = view.findViewById(blockResId);
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

            checkBox.setVisibility(mEditMode ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendControl(true);
            updateTextView(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                sendControl(false);
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
                    seekBar.setProgress(seekBar.getMax() - b);
                } else {
                    seekBar.setProgress(b);
                }
            }
        }

        public Byte getByte() {
            if (checkBox.isChecked()) {
                return (byte)((invert) ? seekBar.getMax() - seekBar.getProgress() : seekBar
                        .getProgress());
            } else {
                return null;
            }
        }
    }

    private boolean mEditMode = true;

    private PoseModel mPoseModel;;

    private long lastSendControl = 0;

    private SeekBarBundle mHead;

    private SeekBarBundle mArmLeft;

    private SeekBarBundle mArmRight;

    private SeekBarBundle mFootLeft;

    private SeekBarBundle mFootRight;

    private SeekBarBundle mEarLeft;

    private SeekBarBundle mEarRight;

    private SeekBarBundle mTime;

    private ToggleButtonBundle mEyeLeft;

    private ToggleButtonBundle mEyeRight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pose_fragment, null);

        mEditMode = false;
        {
            Object obj = null; // getIntent().getSerializableExtra(EXTRA_POSE_MODEL);
            if (obj instanceof PoseModel) {
                mPoseModel = (PoseModel)obj;
            } else {
                mPoseModel = new PoseModel();
                mPoseModel.setNonKeyValues( //
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // head
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // armLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // armRight
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // footLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // footRight
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // earLeft
                        (byte)(Constants.SEEK_MAX_VALUE / 2), // earRight
                        true, // eyeLeft
                        true, // eyeRight
                        1000 // time
                        );
            }
        }

        // Pick up views
        mHead = new SeekBarBundle(view, R.id.headBlock, "Head", Constants.SEEK_MAX_VALUE, true);
        mArmLeft = new SeekBarBundle(view, R.id.leftArmBlock, "Left arm", Constants.SEEK_MAX_VALUE,
                true);
        mArmRight = new SeekBarBundle(view, R.id.rightArmBlock, "Right arm",
                Constants.SEEK_MAX_VALUE, false);
        mFootLeft = new SeekBarBundle(view, R.id.leftFootBlock, "Left foot",
                Constants.SEEK_MAX_VALUE, true);
        mFootRight = new SeekBarBundle(view, R.id.rightFootBlock, "Right foot",
                Constants.SEEK_MAX_VALUE, false);
        mEarLeft = new SeekBarBundle(view, R.id.leftEarBlock, "Left ear", Constants.SEEK_MAX_VALUE,
                true);
        mEarRight = new SeekBarBundle(view, R.id.rightEarBlock, "Right ear",
                Constants.SEEK_MAX_VALUE, false);
        mEyeLeft = new ToggleButtonBundle(view, R.id.eyeLeftToggle, R.id.eyeLeftCheck, "Left eye");
        mEyeRight = new ToggleButtonBundle(view, R.id.eyeRightToggle, R.id.eyeRightCheck,
                "Right eye");
        mTime = new SeekBarBundle(view, R.id.timeBlock, "Time", 100, false);

        mTime.checkBox.setVisibility(View.INVISIBLE);

        loadPoseModel();

        return view;
    }

    private void sendControl(boolean forceSend) {
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

        MyPacket packet = new MyPacket(OpCode.POSE, data.length, data);
        try {
            getGeppaService().sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            Log.w(Constants.TAG, e.getMessage(), e);
        }
    }

    private void sendEyeLed() {
        byte[] data = new byte[] {
            (byte)((mEyeLeft.isChecked() ? 1 : 0) | (mEyeRight.isChecked() ? 2 : 0)),
        };

        MyPacket packet = new MyPacket(OpCode.EYE_LEDS, data.length, data);
        try {
            getGeppaService().sendPacket(new PacketWrapper(packet));
        } catch (RemoteException e) {
            Log.w(Constants.TAG, e.getMessage(), e);
        }
    }

    private void loadPoseModel() {
        mHead.setByte(mPoseModel.getHead());
        mArmLeft.setByte(mPoseModel.getArmLeft());
        mArmRight.setByte(mPoseModel.getArmRight());
        mFootLeft.setByte(mPoseModel.getFootLeft());
        mFootRight.setByte(mPoseModel.getFootRight());
        mEarLeft.setByte(mPoseModel.getEarLeft());
        mEarRight.setByte(mPoseModel.getEarRight());
        mEyeLeft.setBoolean(mPoseModel.getEyeLeft());
        mEyeRight.setBoolean(mPoseModel.getEyeRight());
        mTime.setByte((byte)(mPoseModel.getTime() / 100));
    }

    private void savePoseModel() {
        mPoseModel.setNonKeyValues( //
                mHead.getByte(), //
                mArmLeft.getByte(), //
                mArmRight.getByte(), //
                mFootLeft.getByte(), //
                mFootRight.getByte(), //
                mEarLeft.getByte(), //
                mEarRight.getByte(), //
                mEyeLeft.getBoolean(), //
                mEyeRight.getBoolean(), //
                mTime.getByte() * 100);
    }

    @Override
    public void onPageSelected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageDeselected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        // none
    }

    @Override
    public void onReceivePacket(PacketWrapper packetWrapper) {
        // none
    }

    @Override
    public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder) {
        // none
    }

    @Override
    public void onServiceDisconnected(ComponentName paramComponentName) {
        // none
    }

}
