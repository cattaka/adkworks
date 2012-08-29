
package jp.co.kayo.android.droiddancermotionwriter;

import jp.co.kayo.android.droiddancermotionwriter.MotionItem.MotorDir;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class EditActivity extends Activity {
    public static final int RESULT_DELETE = RESULT_FIRST_USER + 1;

    static class OnSeekBarChangeListenerImpl implements OnSeekBarChangeListener {
        private TextView mTextView;

        private OnSeekBarChangeListenerImpl(TextView textView) {
            super();
            mTextView = textView;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTextView.setText(String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // none
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // none
        }
    }

    private long uid;

    private SeekBar mTimeSeek;

    private TextView mTimeText;

    private ToggleButton mEyeLightToggle;

    private SeekBar mArmLeftSeek;

    private SeekBar mArmRightSeek;

    private RadioGroup mMotorDirLeft;

    private RadioGroup mMotorDirRight;

    private TextView mArmLeftText;

    private TextView mArmRightText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mTimeSeek = (SeekBar)findViewById(R.id.TimeSeek);
        mTimeText = (TextView)findViewById(R.id.TimeText);
        mEyeLightToggle = (ToggleButton)findViewById(R.id.EyeLightToggle);
        mArmLeftSeek = (SeekBar)findViewById(R.id.ArmLeftSeek);
        mArmRightSeek = (SeekBar)findViewById(R.id.ArmRightSeek);
        mMotorDirLeft = (RadioGroup)findViewById(R.id.motorDirLeft);
        mMotorDirRight = (RadioGroup)findViewById(R.id.motorDirRight);
        mArmLeftText = (TextView)findViewById(R.id.ArmLeftValue);
        mArmRightText = (TextView)findViewById(R.id.ArmRightValue);

        mTimeSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl(mTimeText));
        mArmLeftSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl(mArmLeftText));
        mArmRightSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl(mArmRightText));

        findViewById(R.id.button0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("uid", uid);
                setResult(RESULT_DELETE, data);
                finish();
            }
        });

        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MotorDir rotleft = MotorDir.STOP;
                MotorDir rotright = MotorDir.STOP;
                if (((RadioButton)mMotorDirLeft.findViewById(R.id.forwardItem)).isChecked()) {
                    rotleft = MotorDir.FORWARD;
                } else if (((RadioButton)mMotorDirLeft.findViewById(R.id.reverseItem)).isChecked()) {
                    rotleft = MotorDir.REVERSE;
                }
                if (((RadioButton)mMotorDirRight.findViewById(R.id.forwardItem)).isChecked()) {
                    rotright = MotorDir.FORWARD;
                } else if (((RadioButton)mMotorDirRight.findViewById(R.id.reverseItem)).isChecked()) {
                    rotright = MotorDir.REVERSE;
                }

                Intent data = new Intent();
                data.putExtra("uid", uid);
                data.putExtra("led", mEyeLightToggle.isChecked());
                data.putExtra("armleft", mArmLeftSeek.getProgress());
                data.putExtra("armright", mArmRightSeek.getProgress());
                data.putExtra("rotleft", rotleft);
                data.putExtra("rotright", rotright);
                data.putExtra("time", mTimeSeek.getProgress());
                setResult(RESULT_OK, data);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            uid = intent.getLongExtra("uid", 0);
            mEyeLightToggle.setChecked(intent.getBooleanExtra("led", false));
            mArmLeftSeek.setProgress(intent.getIntExtra("armleft", 0));
            mArmRightSeek.setProgress(intent.getIntExtra("armright", 0));
            switch ((MotorDir)intent.getSerializableExtra("rotleft")) {
                case FORWARD:
                    ((RadioButton)mMotorDirLeft.findViewById(R.id.forwardItem)).setChecked(true);
                    break;
                case REVERSE:
                    ((RadioButton)mMotorDirLeft.findViewById(R.id.reverseItem)).setChecked(true);
                    break;
                default:
                    ((RadioButton)mMotorDirLeft.findViewById(R.id.stopItem)).setChecked(true);
                    break;
            }
            switch ((MotorDir)intent.getSerializableExtra("rotright")) {
                case FORWARD:
                    ((RadioButton)mMotorDirRight.findViewById(R.id.forwardItem)).setChecked(true);
                    break;
                case REVERSE:
                    ((RadioButton)mMotorDirRight.findViewById(R.id.reverseItem)).setChecked(true);
                    break;
                default:
                    ((RadioButton)mMotorDirRight.findViewById(R.id.stopItem)).setChecked(true);
                    break;
            }
            mTimeSeek.setProgress(intent.getIntExtra("time", 0));
        }

    }
}
