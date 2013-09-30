
package net.cattaka.android.foxkehrobo.fragment;

import java.util.List;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.activity.ActionListEditActivity;
import net.cattaka.android.foxkehrobo.entity.ActionModel;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.android.foxkehrobo.task.PlayPoseTask;
import net.cattaka.android.foxkehrobo.task.PlayPoseTask.PlayPoseTaskListener;
import net.cattaka.android.foxkehrobo.view.ActionListAdapter;
import net.cattaka.android.foxkehrobo.view.PoseView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ActionListFragment extends BaseFragment implements View.OnClickListener,
        OnItemClickListener, PlayPoseTaskListener {

    private ListView mActionListView;

    private ActionListAdapter mAdapter;

    private PlayPoseTask mPlayPoseTask;

    private TextView mActionStateText;

    private PoseView mPoseView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_list, null);
        mActionListView = (ListView)view.findViewById(R.id.actionList);
        mPoseView = (PoseView)view.findViewById(R.id.poseView);
        mActionStateText = (TextView)view.findViewById(R.id.actionStateText);

        view.findViewById(R.id.editButton).setOnClickListener(this);

        mActionListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        List<ActionModel> actionModels = getDroiballDatabase().findActions(true);
        mAdapter = new ActionListAdapter(getActivity(), actionModels, false);
        mActionListView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayPoseTask != null) {
            mPlayPoseTask.cancel(false);
            mPlayPoseTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.editButton) {
            Intent intent = new Intent(getActivity(), ActionListEditActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
        if (mPlayPoseTask != null) {
            mPlayPoseTask.cancel(false);
            mPlayPoseTask = null;
        }
        ActionModel actionModel = mAdapter.getItem(position);
        mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
        mPlayPoseTask.execute(actionModel);
    }

    @Override
    public void onPlayPoseTaskFinish() {
        mActionStateText.setText("---");
        mPlayPoseTask = null;
    }

    @Override
    public void onPlayPoseTaskUpdate(String name, PoseModel model, int pos, int num) {
        mActionStateText.setText(name + " : " + (pos + 1) + "/" + num);
        mPoseView.setValues(model);
    }
}
