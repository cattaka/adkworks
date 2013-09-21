
package net.cattaka.android.foxkehrobo.activity;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.db.FoxkehRoboDatabase;
import net.cattaka.android.foxkehrobo.entity.ActionModel;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.android.foxkehrobo.view.PoseListAdapter;
import net.cattaka.android.foxkehrobo.view.PoseListAdapter.PoseListAdapterListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ActionEditActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_ACTION_MODEL = "actionModel";

    private static final int REQUEST_CODE_POSE_EDIT = 2;

    private ActionEditActivity me = this;

    private ListView mPoseListView;

    private ActionModel mActionModel;

    private EditText mNameEdit;

    private PoseListAdapterListener mPoseListAdapterListener = new PoseListAdapterListener() {

        @Override
        public void onClickUp(int position) {
            if (position > 0) {
                replace(position - 1);
            }
        }

        @Override
        public void onClickEdit(int position) {
            PoseModel model = mAdapter.getItem(position);
            Intent intent = new Intent(me, PoseEditActivity.class);
            intent.putExtra(PoseEditActivity.EXTRA_POSE_MODEL, model);
            intent.putExtra(PoseEditActivity.EXTRA_EDIT_MODE, true);
            startActivityForResult(intent, REQUEST_CODE_POSE_EDIT);
        }

        @Override
        public void onClickDown(int position) {
            if (position < mAdapter.getCount() - 1) {
                replace(position);
            }
        }

        @Override
        public void onClickDelete(int position) {
            PoseModel model = mAdapter.getItem(position);
            mActionModel.getPoseModels().remove(position);
            mAdapter.remove(model);
        }

        private void replace(int idx) {
            PoseModel m1 = mAdapter.getItem(idx);
            PoseModel m2 = mAdapter.getItem(idx + 1);
            long sort = m2.getSort();
            m2.setSort(m1.getSort());
            m1.setSort(sort);

            mActionModel.getPoseModels().remove(m1);
            mActionModel.getPoseModels().remove(m2);
            mActionModel.getPoseModels().add(idx, m1);
            mActionModel.getPoseModels().add(idx, m2);

            mAdapter.remove(m1);
            mAdapter.remove(m2);
            mAdapter.insert(m1, idx);
            mAdapter.insert(m2, idx);
        }

        @Override
        public void onClickCopy(int position) {
            PoseModel model = mAdapter.getItem(position);
            PoseModel newModel = new PoseModel();
            newModel.set(model);
            addPoseModel(newModel, position + 1);
        }
    };

    private PoseListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.action_edit);
        mPoseListView = (ListView)findViewById(R.id.poseList);

        findViewById(R.id.addButton).setOnClickListener(this);
        findViewById(R.id.finishButton).setOnClickListener(this);
        mNameEdit = (EditText)findViewById(R.id.nameEdit);

        mActionModel = (ActionModel)getIntent().getSerializableExtra(EXTRA_ACTION_MODEL);
    }

    @Override
    public void onResume() {
        super.onResume();

        List<PoseModel> poseModels = new ArrayList<PoseModel>(mActionModel.getPoseModels());
        mAdapter = new PoseListAdapter(this, poseModels, true);
        mAdapter.setListener(mPoseListAdapterListener);
        mPoseListView.setAdapter(mAdapter);

        mNameEdit.setText(mActionModel.getName());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addButton) {
            addPoseModel(null, -1);
        } else if (v.getId() == R.id.finishButton) {
            FoxkehRoboDatabase database = new FoxkehRoboDatabase(this);

            mActionModel.setName(String.valueOf(mNameEdit.getText()));

            {
                ActionModel am = (database.findActionModel(mActionModel.getName(), false));
                if (am != null && !am.getId().equals(mActionModel.getId())) {
                    Toast.makeText(this, "There are same name.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Intent data = new Intent();
            data.putExtra(EXTRA_ACTION_MODEL, mActionModel);
            setResult(RESULT_OK, data);

            database.registerActionModel(mActionModel, true);

            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_POSE_EDIT) {
            if (resultCode == RESULT_OK) {
                PoseModel poseModel = (PoseModel)data
                        .getSerializableExtra(PoseEditActivity.EXTRA_POSE_MODEL);
                for (int i = 0; i < mActionModel.getPoseModels().size(); i++) {
                    PoseModel t = mActionModel.getPoseModels().get(i);
                    if (t.getSort().equals(poseModel.getSort())) {
                        mAdapter.remove(t);
                        mActionModel.getPoseModels().remove(i);
                        mAdapter.insert(poseModel, i);
                        mActionModel.getPoseModels().add(i, poseModel);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    private void addPoseModel(PoseModel model, int idx) {
        if (model == null) {
            model = new PoseModel();
            model.setTime(1000);
        }
        model.setId(null);

        if (0 <= idx && idx < mAdapter.getCount()) {
            long sortNo = mAdapter.getItem(idx).getSort();
            for (int i = idx; i < mAdapter.getCount(); i++) {
                PoseModel pm = mAdapter.getItem(i);
                pm.setSort(pm.getSort() + 1);
            }
            model.setSort(sortNo);
            mActionModel.getPoseModels().add(idx, model);
            mAdapter.insert(model, idx);
        } else {
            model.setSort(getNextSort());
            mActionModel.getPoseModels().add(model);
            mAdapter.add(model);
        }
        mAdapter.notifyDataSetChanged();
    }

    private Long getNextSort() {
        Long nextSort = 1L;
        { // finding sort
            for (PoseModel model : mActionModel.getPoseModels()) {
                if (nextSort < model.getSort()) {
                    nextSort = model.getSort();
                }
            }
            nextSort += 1;
        }
        return nextSort;
    }
}
