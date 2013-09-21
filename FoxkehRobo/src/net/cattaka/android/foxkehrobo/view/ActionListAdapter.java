
package net.cattaka.android.foxkehrobo.view;

import java.util.List;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.ActionModel;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ActionListAdapter extends ArrayAdapter<ActionModel> {
    public interface ActionListAdapterListener {
        public void onClickEdit(int position);

        public void onClickUp(int position);

        public void onClickDown(int position);

        public void onClickDelete(int position);

        public void onClickCopy(int position);
    }

    public static class ViewHolder {
        int position;

        TextView actionNameText;

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

    private ActionListAdapterListener mListener;

    private boolean mEditMode = false;

    public ActionListAdapter(Context context, List<ActionModel> objects, boolean editMode) {
        super(context, R.layout.action_list_item, objects);
        mEditMode = editMode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActionModel model = getItem(position);
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.action_list_item, null);
            vh = new ViewHolder();
            vh.actionNameText = (TextView)convertView.findViewById(R.id.actionNameText);
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

        vh.position = position;
        vh.actionNameText.setText(model.getName());

        return convertView;
    }

    public void setListener(ActionListAdapterListener listener) {
        mListener = listener;
    }

}
