
package net.cattaka.android.foxkehrobo.task;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.core.IAppStub;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.OpCode;
import net.cattaka.android.foxkehrobo.entity.ActionModel;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class PlayPoseTask extends AsyncTask<ActionModel, PoseModel, Void> {
    private IAppStub mAppStub;

    private PlayPoseTaskListener mListener;

    public interface PlayPoseTaskListener {
        public void onPlayPoseTaskUpdate(PoseModel model);

        public void onPlayPoseTaskFinish();
    }

    public PlayPoseTask(IAppStub appStub, PlayPoseTaskListener listener) {
        super();
        mAppStub = appStub;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(ActionModel... actionModels) {
        outer: for (ActionModel actionModel : actionModels) {
            if (actionModel.getPoseModels() == null) {
                continue;
            }
            for (PoseModel poseModel : actionModel.getPoseModels()) {
                if (isCancelled()) {
                    break outer;
                }
                byte[] data = poseModel.toPose();
                publishProgress(poseModel);
                FrPacket packet = new FrPacket(OpCode.POSE, data.length, data);
                mAppStub.getServiceWrapper().sendPacket(packet);
                try {
                    long limit = (poseModel.getTime() != null) ? poseModel.getTime() : 0;
                    long time = SystemClock.elapsedRealtime();
                    do {
                        Thread.sleep(100);
                    } while (((SystemClock.elapsedRealtime() - time) < limit));
                } catch (InterruptedException e) {
                    Log.e(Constants.TAG, e.getMessage(), e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(PoseModel... values) {
        super.onProgressUpdate(values);
        if (mListener != null) {
            mListener.onPlayPoseTaskUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onPlayPoseTaskFinish();
        }
    }
}
