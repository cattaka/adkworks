
package net.cattaka.android.foxkehrobo.view;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TableLayout;

public class PoseView extends TableLayout {
    private ProgressBar earLeft;

    private ProgressBar earRight;

    private ProgressBar headYaw;

    private ProgressBar headPitch;

    private ProgressBar armLeft;

    private ProgressBar armRight;

    private ProgressBar footLeft;

    private ProgressBar footRight;

    private ProgressBar tailYaw;

    private ProgressBar tailPitch;

    private ProgressBar time;

    public PoseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PoseView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_pose, this);

        earLeft = (ProgressBar)findViewById(R.id.earLeft);
        earRight = (ProgressBar)findViewById(R.id.earRight);
        headYaw = (ProgressBar)findViewById(R.id.headYaw);
        headPitch = (ProgressBar)findViewById(R.id.headPitch);
        armLeft = (ProgressBar)findViewById(R.id.armLeft);
        armRight = (ProgressBar)findViewById(R.id.armRight);
        footLeft = (ProgressBar)findViewById(R.id.footLeft);
        footRight = (ProgressBar)findViewById(R.id.footRight);
        tailYaw = (ProgressBar)findViewById(R.id.tailYaw);
        tailPitch = (ProgressBar)findViewById(R.id.tailPitch);
        time = (ProgressBar)findViewById(R.id.time);

        earLeft.setMax(Constants.SEEK_MAX_VALUE);
        earRight.setMax(Constants.SEEK_MAX_VALUE);
        headYaw.setMax(Constants.SEEK_MAX_VALUE);
        headPitch.setMax(Constants.SEEK_MAX_VALUE);
        armLeft.setMax(Constants.SEEK_MAX_VALUE);
        armRight.setMax(Constants.SEEK_MAX_VALUE);
        footLeft.setMax(Constants.SEEK_MAX_VALUE);
        footRight.setMax(Constants.SEEK_MAX_VALUE);
        tailYaw.setMax(Constants.SEEK_MAX_VALUE);
        tailPitch.setMax(Constants.SEEK_MAX_VALUE);
        time.setMax(Constants.SEEK_MAX_VALUE);
    }

    private void setValue(ProgressBar bar, Byte value, boolean invert) {
        bar.setEnabled(value != null);
        if (value != null) {
            bar.setEnabled(true);
            if (invert) {
                bar.setProgress(0xFF - (0xFF & value));
            } else {
                bar.setProgress(0xFF & value);
            }
        } else {
            bar.setEnabled(false);
            bar.setProgress(bar.getMax() / 2);
        }
    }

    public void setValues(PoseModel model) {
        setValue(earLeft, model.getEarLeft(), true);
        setValue(earRight, model.getEarRight(), false);
        setValue(headYaw, model.getHeadYaw(), true);
        setValue(headPitch, model.getHeadPitch(), true);
        setValue(armLeft, model.getArmLeft(), true);
        setValue(armRight, model.getArmRight(), false);
        setValue(footLeft, model.getFootLeft(), true);
        setValue(footRight, model.getFootRight(), false);
        setValue(tailYaw, model.getTailYaw(), true);
        setValue(tailPitch, model.getTailPitch(), true);
        setValue(time, (byte)(model.getTime() / 100), false);
    }
}
