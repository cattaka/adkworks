
package net.cattaka.android.foxkehrobo.fragment;

import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.android.foxkehrobo.fragment.PoseEditFragment.IPoseEditFragmentListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControllerFragment extends BaseFragment implements IPoseEditFragmentListener {
    private PoseEditFragment mPoseEditFragment;

    private static final int MINIMUM_INTERVAL_SEND_CONTROL = 200;

    private PoseModel mPoseModel;

    private long lastSendControl = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPoseModel = new PoseModel();
        mPoseModel.makeStandPose();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, null);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Note: Fragment#restoreHierarchyState sets wrong values. To avoid this
        // problem, mPoseEditFragment is created every time.
        // It seems that The reason of this problem is PoseEditFragment contains
        // same view id.
        mPoseEditFragment = new PoseEditFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.poseEditFragment, mPoseEditFragment).commit();
        mPoseEditFragment.setListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPoseEditFragment.loadPoseModel(mPoseModel);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPoseEditFragment.savePoseModel(mPoseModel);
    }

    @Override
    public void onPoseChanged(boolean operationFinished) {
        { // Limit number of times not to send too many times
            long currTime = SystemClock.currentThreadTimeMillis();
            if (!operationFinished) {
                if (currTime - lastSendControl <= MINIMUM_INTERVAL_SEND_CONTROL) {
                    return;
                }
            }
            lastSendControl = currTime;
        }

        mPoseEditFragment.savePoseModel(mPoseModel);
        getServiceWrapper().sendPose(mPoseModel);
    }
}
