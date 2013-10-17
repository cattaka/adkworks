
package net.cattaka.android.foxkehrobo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.cattaka.android.foxkehrobo.Constants;
import net.cattaka.android.foxkehrobo.R;
import net.cattaka.android.foxkehrobo.core.MyPreference;
import net.cattaka.android.foxkehrobo.data.ActionBind;
import net.cattaka.android.foxkehrobo.data.FrPacket;
import net.cattaka.android.foxkehrobo.data.OpCode;
import net.cattaka.android.foxkehrobo.entity.ActionModel;
import net.cattaka.android.foxkehrobo.entity.PoseModel;
import net.cattaka.android.foxkehrobo.opencv.DetectionBasedTracker;
import net.cattaka.android.foxkehrobo.opencv.MyNativeCameraView;
import net.cattaka.android.foxkehrobo.task.PlayPoseTask;
import net.cattaka.android.foxkehrobo.view.PoseView;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AiModeFragment extends BaseFragment implements View.OnClickListener,
        PlayPoseTask.PlayPoseTaskListener {
    // public static final String ACTION_NAME_WAKE_FRONT = "Wake-front";
    //
    // public static final String ACTION_NAME_WAKE_BACK = "Wake-back";
    //
    // public static final String ACTION_NAME_WAVE_LEFT = "Wave-left";
    //
    // public static final String ACTION_NAME_WAVE_RIGHT = "Wave-right";
    //
    // public static final String[] ACTION_NAMES_RANDOM = new String[] {
    // "Head-dance", "Ear-dance", "Arm-dance", "Ear-cast-down"
    // };
    //
    private static final int EVENT_GET_ACCEL = 1;

    private static final int EVENT_ACTION_RANDOM = 2;

    private static final int INTERVAL_GET_ACCEL = 1000;

    private static final int INTERVAL_ACTION_RANDOM = 15000;

    private static final Scalar FACE_RECT_COLOR_T = new Scalar(0, 255, 255, 255);

    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    enum StandState {
        STAND, FACE_DOWN, BACK
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == EVENT_GET_ACCEL) {
                FrPacket packet = new FrPacket(OpCode.REQ_ACCEL, 0, new byte[0]);
                getServiceWrapper().sendPacket(packet);
                mHandler.sendEmptyMessageDelayed(EVENT_GET_ACCEL, INTERVAL_GET_ACCEL);
            } else if (msg.what == EVENT_ACTION_RANDOM) {
                if (isEnableAi() && mPlayPoseTask == null) {
                    ActionModel actionModel = pickBindedActionRandom(ActionBind.RANDOM);

                    if (actionModel != null) {
                        mPlayPoseTask = new PlayPoseTask(getAppStub(), me);
                        mPlayPoseTask.execute(actionModel);
                    }
                } else {
                    // ignore
                }
                mHandler.sendEmptyMessageDelayed(EVENT_ACTION_RANDOM, INTERVAL_ACTION_RANDOM);
            }
        };
    };

    private AiModeFragment me = this;

    private Random mRandom = new Random();

    private ToggleButton mEnableAiToggle;

    private MyNativeCameraView mOpenCvCameraView;

    private MyPreference mPreference;

    private StandState mStandState = StandState.STAND;

    private int mStandStateCount = 0;

    private PlayPoseTask mPlayPoseTask;

    private TextView mActionStateText;

    private PoseView mPoseView;

    private Map<ActionBind, List<ActionModel>> mBindedActions;

    // Setting
    private Mat mRgba;

    private Mat mGray;

    private float mRelativeFaceSize = 0.2f;

    private int mAbsoluteFaceSize = 0;

    private long[] mDetectedFaceTime = new long[10];

    private WakeLock mWakelock;

    private CvCameraViewListener mCvCameraViewListener = new CvCameraViewListener() {

        @Override
        public void onCameraViewStopped() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onCameraViewStarted(int width, int height) {
            // TODO Auto-generated method stub
        }

        @Override
        public Mat onCameraFrame(Mat inputFrame) {
            return detectFace(inputFrame);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_mode, null);

        mEnableAiToggle = (ToggleButton)view.findViewById(R.id.enableAiToggle);
        mPoseView = (PoseView)view.findViewById(R.id.poseView);
        mActionStateText = (TextView)view.findViewById(R.id.actionStateText);

        mPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(getActivity()));

        mBindedActions = new HashMap<ActionBind, List<ActionModel>>();

        mOpenCvCameraView = (MyNativeCameraView)view.findViewById(R.id.cameraView);
        mOpenCvCameraView.setCvCameraViewListener(mCvCameraViewListener);
        mOpenCvCameraView.setCameraIndex(0);

        mWakelock = ((PowerManager)getActivity().getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Constants.TAG);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mGray = new Mat();
        mRgba = new Mat();

        { // バインドされたアクションのロード
            mBindedActions.clear();
            for (ActionBind bind : ActionBind.values()) {
                List<ActionModel> models = getDroiballDatabase().findBindedActions(bind, true);
                mBindedActions.put(bind, models);
            }
        }

        org.opencv.core.Size size = getMyPreference().getPreviewSizeAsSize();
        mOpenCvCameraView.setPreviewSize(size);
    }

    @Override
    public void onPause() {
        super.onPause();
        mOpenCvCameraView.disableView();
        mGray.release();
        mRgba.release();
        if (mPlayPoseTask != null) {
            mPlayPoseTask.cancel(false);
            mPlayPoseTask = null;
        }
    }

    @Override
    public void onPageSelected() {
        mEnableAiToggle.setChecked(false);

        mWakelock.acquire();
        mHandler.sendEmptyMessageDelayed(EVENT_GET_ACCEL, INTERVAL_GET_ACCEL);
        mHandler.sendEmptyMessageDelayed(EVENT_ACTION_RANDOM, INTERVAL_ACTION_RANDOM);
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onPageDeselected() {
        mEnableAiToggle.setChecked(false);
        mWakelock.release();
        mHandler.removeMessages(EVENT_GET_ACCEL);
        mHandler.removeMessages(EVENT_ACTION_RANDOM);
        mOpenCvCameraView.disableView();
        if (mPlayPoseTask != null) {
            mPlayPoseTask.cancel(false);
            mPlayPoseTask = null;
        }
    }

    @Override
    public void onReceivePacket(FrPacket packet) {
        // none
    }

    @Override
    public void onClick(View v) {
        // none
    }

    private void checkWakeup() {
        if (mStandState == StandState.FACE_DOWN && mStandStateCount > 3) {
            ActionModel model = pickBindedActionRandom(ActionBind.WAKE_FRONT);
            if (model != null) {
                mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
                mPlayPoseTask.execute(model);
            }
        } else if (mStandState == StandState.BACK && mStandStateCount > 3) {
            ActionModel model = pickBindedActionRandom(ActionBind.WAKE_BACK);
            if (model != null) {
                mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
                mPlayPoseTask.execute(model);
            }

        }
    }

    private ActionModel pickBindedActionRandom(ActionBind bind) {
        List<ActionModel> models = mBindedActions.get(bind);
        if (models != null && models.size() > 0) {
            return new ActionModel(models.get(mRandom.nextInt(models.size())));
        } else {
            return null;
        }
    }

    private Mat detectFace(Mat inputFrame) {
        DetectionBasedTracker nativeDetector = getAppStub().getNativeDetector();

        inputFrame.copyTo(mRgba);
        Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            nativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (nativeDetector != null) {
            nativeDetector.detect(mGray, faces);
        }

        int width = inputFrame.width();
        long currTime = SystemClock.elapsedRealtime();
        Rect[] facesArray = faces.toArray();
        boolean[] detectedIndexs = new boolean[mDetectedFaceTime.length];
        boolean[] fixedIndexs = new boolean[mDetectedFaceTime.length];
        for (int i = 0; i < facesArray.length; i++) {
            int center = facesArray[i].x + facesArray[i].width / 2;
            int index = center * 10 / width;
            if (width / facesArray[i].width >= 8) {
                // 画像に対して8分の１より小さい場合は無視
                continue;
            }
            if (0 <= index && index < detectedIndexs.length) {
                detectedIndexs[index] = true;
                if (mDetectedFaceTime[index] > 0 && currTime - mDetectedFaceTime[index] >= 1000) {
                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR,
                            3);
                    fixedIndexs[index] = true;
                } else {
                    Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                            FACE_RECT_COLOR_T, 3);
                }
            }
        }
        for (int i = 0; i < mDetectedFaceTime.length; i++) {
            if (detectedIndexs[i]) {
                mDetectedFaceTime[i] = (mDetectedFaceTime[i] == 0) ? currTime : Math.min(
                        mDetectedFaceTime[i], currTime);
            } else {
                mDetectedFaceTime[i] = 0;
            }
        }
        for (int i = 0; i < detectedIndexs.length; i++) {
            if (detectedIndexs[i]) {
                onFaceDetected(i, detectedIndexs.length);
                break;
            }
        }

        return mRgba;
    }

    public void onFaceDetected(int index, int length) {
        if (!isEnableAi()) {
            return;
        }
        if (mPlayPoseTask != null) {
            return;
        }
        ActionModel actionModel;
        { // 左手か右手かどっちの手を振るか選ぶ
            if (index < length / 2) {
                actionModel = pickBindedActionRandom(ActionBind.WAVE_LEFT);
            } else {
                actionModel = pickBindedActionRandom(ActionBind.WAVE_RIGHT);
            }
            if (actionModel == null) {
                // 見つからない場合はスタンド状態を設定する
                actionModel = new ActionModel();
                actionModel.setPoseModels(new ArrayList<PoseModel>());
                PoseModel poseModel = new PoseModel();
                poseModel.makeStandPose();
                poseModel.setTime(10);
                actionModel.getPoseModels().add(poseModel);
            }
        }
        { // 頭の方向を変える。ついでに耳をちょっと動かす
            PoseModel headPoseModel = new PoseModel();
            headPoseModel.setHeadYaw((byte)(0x30 + (0xA0 * index / length)));
            headPoseModel.setTime(200);
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEarLeft((byte)0xFF);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(0, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(1, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x0);
                actionModel.getPoseModels().add(2, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(3, pm);
            }
        }
        mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
        mPlayPoseTask.execute(actionModel);
    }

    @Override
    public void onPlayPoseTaskUpdate(String name, PoseModel model, int pos, int num) {
        mActionStateText.setText(name + " : " + (pos + 1) + "/" + num);
        mPoseView.setValues(model);
    }

    @Override
    public void onPlayPoseTaskFinish() {
        mActionStateText.setText("---");
        mPlayPoseTask = null;
    }

    private boolean isEnableAi() {
        return mEnableAiToggle.isChecked();
    }
}
