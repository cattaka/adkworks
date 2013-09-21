
package net.cattaka.android.foxkehrobo.view;

import java.util.List;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class PoseListAdapter extends ArrayAdapter<PoseModel> {
    public interface PoseListAdapterListener {
        public void onClickEdit(int position);

        public void onClickUp(int position);

        public void onClickDown(int position);

        public void onClickDelete(int position);

        public void onClickCopy(int position);
    }

    public static class ViewHolder {
        int position;

        ProgressBar earLeft;

        ProgressBar earRight;

        ProgressBar headYaw;

        ProgressBar headPitch;

        ProgressBar armLeft;

        ProgressBar armRight;

        ProgressBar footLeft;

        ProgressBar footRight;

        ProgressBar tailYaw;

        ProgressBar tailPitch;

        ProgressBar time;

        View editButton;

        View upButton;

        View downButton;

        View deleteButton;

        View copyButton;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) {
                ViewHolder vh = (ViewHolder)v.getTag();
                if (v.getId() == R.id.editButton) {
                    mListener.onClickEdit(vh.position);
                } else if (v.getId() == R.id.upButton) {
                    mListener.onClickUp(vh.position);
                } else if (v.getId() == R.id.downButton) {
                    mListener.onClickDown(vh.position);
                } else if (v.getId() == R.id.deleteButton) {
                    mListener.onClickDelete(vh.position);
                } else if (v.getId() == R.id.copyButton) {
                    mListener.onClickCopy(vh.position);
                }
            }
        }
    };

    private PoseListAdapterListener mListener;

    private boolean mEditMode = false;

    public PoseListAdapter(Context context, List<PoseModel> objects, boolean editMode) {
        super(context, R.layout.pose_list_item, objects);
        mEditMode = editMode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PoseModel model = getItem(position);
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.pose_list_item, null);
            vh = new ViewHolder();
            vh.earLeft = (ProgressBar)convertView.findViewById(R.id.earLeft);
            vh.earRight = (ProgressBar)convertView.findViewById(R.id.earRight);
            vh.headYaw = (ProgressBar)convertView.findViewById(R.id.headYaw);
            vh.headPitch = (ProgressBar)convertView.findViewById(R.id.headPitch);
            vh.armLeft = (ProgressBar)convertView.findViewById(R.id.armLeft);
            vh.armRight = (ProgressBar)convertView.findViewById(R.id.armRight);
            vh.footLeft = (ProgressBar)convertView.findViewById(R.id.footLeft);
            vh.footRight = (ProgressBar)convertView.findViewById(R.id.footRight);
            vh.tailYaw = (ProgressBar)convertView.findViewById(R.id.tailYaw);
            vh.tailPitch = (ProgressBar)convertView.findViewById(R.id.tailPitch);
            vh.time = (ProgressBar)convertView.findViewById(R.id.time);

            vh.earLeft.setMax(Constants.SEEK_MAX_VALUE);
            vh.earRight.setMax(Constants.SEEK_MAX_VALUE);
            vh.headYaw.setMax(Constants.SEEK_MAX_VALUE);
            vh.headPitch.setMax(Constants.SEEK_MAX_VALUE);
            vh.armLeft.setMax(Constants.SEEK_MAX_VALUE);
            vh.armRight.setMax(Constants.SEEK_MAX_VALUE);
            vh.footLeft.setMax(Constants.SEEK_MAX_VALUE);
            vh.footRight.setMax(Constants.SEEK_MAX_VALUE);
            vh.tailYaw.setMax(Constants.SEEK_MAX_VALUE);
            vh.tailPitch.setMax(Constants.SEEK_MAX_VALUE);
            vh.time.setMax(Constants.SEEK_MAX_VALUE);

            vh.editButton = convertView.findViewById(R.id.editButton);
            vh.upButton = convertView.findViewById(R.id.upButton);
            vh.downButton = convertView.findViewById(R.id.downButton);
            vh.deleteButton = convertView.findViewById(R.id.deleteButton);
            vh.copyButton = convertView.findViewById(R.id.copyButton);

            vh.editButton.setOnClickListener(mOnClickListener);
            vh.upButton.setOnClickListener(mOnClickListener);
            vh.downButton.setOnClickListener(mOnClickListener);
            vh.deleteButton.setOnClickListener(mOnClickListener);
            vh.copyButton.setOnClickListener(mOnClickListener);

            if (!mEditMode) {
                vh.editButton.setVisibility(View.GONE);
                vh.upButton.setVisibility(View.GONE);
                vh.downButton.setVisibility(View.GONE);
                vh.deleteButton.setVisibility(View.GONE);
                vh.copyButton.setVisibility(View.GONE);
            }

            convertView.setTag(vh);
            vh.editButton.setTag(vh);
            vh.upButton.setTag(vh);
            vh.downButton.setTag(vh);
            vh.deleteButton.setTag(vh);
            vh.copyButton.setTag(vh);
        } else {
            vh = (ViewHolder)convertView.getTag();
        }
        setValue(vh.earLeft, model.getEarLeft(), true);
        setValue(vh.earRight, model.getEarRight(), false);
        setValue(vh.headYaw, model.getHeadYaw(), true);
        setValue(vh.headPitch, model.getHeadPitch(), true);
        setValue(vh.armLeft, model.getArmLeft(), true);
        setValue(vh.armRight, model.getArmRight(), false);
        setValue(vh.footLeft, model.getFootLeft(), true);
        setValue(vh.footRight, model.getFootRight(), false);
        setValue(vh.tailYaw, model.getTailYaw(), true);
        setValue(vh.tailPitch, model.getTailPitch(), true);
        setValue(vh.time, (byte)(model.getTime() / 100), false);

        vh.position = position;

        return convertView;
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

    // private void setValue(ToggleButton toggle, Boolean value) {
    // toggle.setEnabled(value != null);
    // if (value != null) {
    // toggle.setEnabled(true);
    // toggle.setChecked(value);
    // } else {
    // toggle.setEnabled(false);
    // toggle.setChecked(false);
    // }
    // }

    public void setListener(PoseListAdapterListener listener) {
        mListener = listener;
    }

}
