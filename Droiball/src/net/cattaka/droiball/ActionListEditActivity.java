
package net.cattaka.droiball;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.droiball.db.DroiballDatabase;
import net.cattaka.droiball.entity.ActionModel;
import net.cattaka.droiball.entity.PoseModel;
import net.cattaka.droiball.view.ActionListAdapter;
import net.cattaka.droiball.view.ActionListAdapter.ActionListAdapterListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ActionListEditActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CODE_ACTION_EDIT = 1;

    private ActionListEditActivity me = this;

    private ListView mActionListView;

    private ActionListAdapterListener mActionListAdapterListener = new ActionListAdapterListener() {

        @Override
        public void onClickUp(int position) {
            if (position > 0) {
                replace(position - 1);
            }
        }

        @Override
        public void onClickEdit(int position) {
            ActionModel model = mAdapter.getItem(position);
            model = mDroiballDatabase.findActionModel(model.getName(), true);
            Intent intent = new Intent(me, ActionEditActivity.class);
            intent.putExtra(ActionEditActivity.EXTRA_ACTION_MODEL, model);
            startActivityForResult(intent, REQUEST_CODE_ACTION_EDIT);
        }

        @Override
        public void onClickDown(int position) {
            if (position < mAdapter.getCount() - 1) {
                replace(position);
            }
        }

        @Override
        public void onClickDelete(int position) {
            ActionModel model = mAdapter.getItem(position);
            mDroiballDatabase.deleteActionModel(model.getId());
            mAdapter.remove(model);
        }

        @Override
        public void onClickCopy(int position) {
            ActionModel model = mAdapter.getItem(position);
            ActionModel newModel = new ActionModel();
            newModel.set(model);
            addActionModel(newModel, position + 1);
        }

        private void replace(int idx) {
            ActionModel m1 = mAdapter.getItem(idx);
            ActionModel m2 = mAdapter.getItem(idx + 1);
            long sort = m2.getSort();
            m2.setSort(m1.getSort());
            m1.setSort(sort);

            mDroiballDatabase.deleteActionModel(m1.getId());
            mDroiballDatabase.deleteActionModel(m2.getId());
            m1.setId(null);
            m2.setId(null);
            mDroiballDatabase.registerActionModel(m1, true);
            mDroiballDatabase.registerActionModel(m2, true);

            mAdapter.remove(m1);
            mAdapter.remove(m2);
            mAdapter.insert(m1, idx);
            mAdapter.insert(m2, idx);
        }
    };

    private DroiballDatabase mDroiballDatabase;

    private ActionListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_list_edit);
        mActionListView = (ListView)findViewById(R.id.actionList);

        findViewById(R.id.addButton).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        { // opening DB
            if (mDroiballDatabase != null) {
                mDroiballDatabase.close();
                ;
            }
            if (mDroiballDatabase == null) {
                mDroiballDatabase = new DroiballDatabase(this);
            }
        }

        List<ActionModel> actionModels = mDroiballDatabase.findActions(true);
        mAdapter = new ActionListAdapter(this, actionModels, true);
        mAdapter.setListener(mActionListAdapterListener);
        mActionListView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        { // closing DB
            mDroiballDatabase.close();
            mDroiballDatabase = null;
            ;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACTION_EDIT) {
            if (resultCode == RESULT_OK) {
                // OK
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addButton) {
            addActionModel(null, -1);
        }
    }

    private void addActionModel(ActionModel model, int idx) {
        if (model == null) {
            String name = findUnusedName("Action");
            model = new ActionModel();
            model.setName(name);
            model.setPoseModels(new ArrayList<PoseModel>());
        } else {
            String name = findUnusedName(model.getName());
            model.setName(name);
        }
        model.setId(null);
        if (0 <= idx && idx < mAdapter.getCount()) {
            long sortNo = mAdapter.getItem(idx).getSort();
            for (int i = idx; i < mAdapter.getCount(); i++) {
                ActionModel am = mAdapter.getItem(i);
                am.setSort(am.getSort() + 1);
                mDroiballDatabase.registerActionModel(am, false);
            }
            model.setSort(sortNo);
            mDroiballDatabase.registerActionModel(model, true);
            mAdapter.insert(model, idx);
        } else {
            model.setSort(getNextSort());
            mDroiballDatabase.registerActionModel(model, true);
            mAdapter.add(model);
        }

        mAdapter.notifyDataSetChanged();
    }

    private Long getNextSort() {
        Long nextSort = mDroiballDatabase.findMaxActionSort();
        { // finding sort
            if (nextSort == null) {
                nextSort = 1L;
            } else {
                nextSort += 1;
            }
        }
        return nextSort;
    }

    private String findUnusedName(String base) {
        String name;
        { // finding unused name
            name = base;
            int n = 1;
            do {
                if (mDroiballDatabase.findActionModel(name, false) == null) {
                    break;
                }

                name = base + " " + n;
                n++;
            } while (true);
        }
        return name;
    }
}
