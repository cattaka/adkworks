
package net.cattaka.android.foxkehrobo.fragment;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PoseEditFragment extends Fragment {
    public static final String EXTRA_POSE_MODEL = "poseModel";

    public interface IPoseEditFragmentListener {
        public void onPoseChanged(boolean operationFinished);
    }

    private class SeekBarBundle implements OnSeekBarChangeListener {
        private TextView textView;

        private CheckBox checkBox;

        private SeekBar seekBar;

        private boolean invert;

        public SeekBarBundle(View parent, int blockResId, String name, int max, boolean invert) {
            super();
            View block = parent.findViewById(blockResId);
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
            if (mListener != null) {
                mListener.onPoseChanged(true);
            }
            updateTextView(seekBar);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (mListener != null) {
                    mListener.onPoseChanged(false);
                }
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

    private boolean mEditMode = true;

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

    private IPoseEditFragmentListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pose_edit, null);
        { // Pick up views
            mHeadYaw = new SeekBarBundle(view, R.id.headYawBlock, "Head yaw",
                    Constants.SEEK_MAX_VALUE, true);
            mHeadPitch = new SeekBarBundle(view, R.id.headPitchBlock, "Head pitch",
                    Constants.SEEK_MAX_VALUE, true);
            mArmLeft = new SeekBarBundle(view, R.id.leftArmBlock, "Left arm",
                    Constants.SEEK_MAX_VALUE, true);
            mArmRight = new SeekBarBundle(view, R.id.rightArmBlock, "Right arm",
                    Constants.SEEK_MAX_VALUE, false);
            mFootLeft = new SeekBarBundle(view, R.id.leftFootBlock, "Left foot",
                    Constants.SEEK_MAX_VALUE, true);
            mFootRight = new SeekBarBundle(view, R.id.rightFootBlock, "Right foot",
                    Constants.SEEK_MAX_VALUE, false);
            mEarLeft = new SeekBarBundle(view, R.id.leftEarBlock, "Left ear",
                    Constants.SEEK_MAX_VALUE, true);
            mEarRight = new SeekBarBundle(view, R.id.rightEarBlock, "Right ear",
                    Constants.SEEK_MAX_VALUE, false);
            mTailYaw = new SeekBarBundle(view, R.id.tailYawBlock, "Tail yaw",
                    Constants.SEEK_MAX_VALUE, true);
            mTailPitch = new SeekBarBundle(view, R.id.tailPitchBlock, "Tail pitch",
                    Constants.SEEK_MAX_VALUE, true);
            mTime = new SeekBarBundle(view, R.id.timeBlock, "Time", 100, false);

            mTime.checkBox.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void loadPoseModel(PoseModel model) {
        mHeadYaw.setByte(model.getHeadYaw());
        mHeadPitch.setByte(model.getHeadPitch());
        mArmLeft.setByte(model.getArmLeft());
        mArmRight.setByte(model.getArmRight());
        mFootLeft.setByte(model.getFootLeft());
        mFootRight.setByte(model.getFootRight());
        mEarLeft.setByte(model.getEarLeft());
        mEarRight.setByte(model.getEarRight());
        mTailYaw.setByte(model.getTailYaw());
        mTailPitch.setByte(model.getTailPitch());
        mTime.setByte((byte)(model.getTime() / 100));
    }

    public void savePoseModel(PoseModel dst) {
        dst.setNonKeyValues( //
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

    public void setListener(IPoseEditFragmentListener listener) {
        mListener = listener;
    }

}
