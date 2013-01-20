
package net.cattaka.droiball.task;

import net.cattaka.droiball.Constants;
import net.cattaka.droiball.IAppStub;
import net.cattaka.droiball.data.MyPacket;
import net.cattaka.droiball.data.OpCode;
import net.cattaka.droiball.entity.ActionModel;
import net.cattaka.droiball.entity.PoseModel;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class PlayPoseTask extends AsyncTask<ActionModel, Void, Void> {
    private IAppStub mAppStub;

    private PlayPoseTaskListener mListener;

    public interface PlayPoseTaskListener {
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
                MyPacket packet = new MyPacket(OpCode.POSE, data.length, data);
                try {
                    mAppStub.getGeppaService().sendPacket(new PacketWrapper(packet));
                } catch (RemoteException e) {
                    Log.e(Constants.TAG, e.getMessage(), e);
                }
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
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.onPlayPoseTaskFinish();
        }
    }
}
