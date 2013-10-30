
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
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
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

    private float mRelativeFaceSize = 0.15f;

    private int mAbsoluteFaceSize = 0;

    private long[] mDetectedFaceTime = new long[10];

    private WakeLock mWakelock;

    private int mLastHeadYaw = 0x7F;

    private int mLastHeadPitch = 0x7F;

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
        MyPreference pref = getMyPreference();
        mEnableAiToggle.setChecked(pref.getAiModeOnStart());

        mWakelock.acquire();
        mHandler.sendEmptyMessageDelayed(EVENT_GET_ACCEL, INTERVAL_GET_ACCEL);
        mHandler.sendEmptyMessageDelayed(EVENT_ACTION_RANDOM, INTERVAL_ACTION_RANDOM);

        {
            mOpenCvCameraView.enableView();
            VideoCapture capture = mOpenCvCameraView.getVideoCapture();
            capture.set(Highgui.CV_CAP_PROP_ANDROID_WHITE_BALANCE, pref.getWhiteBalance().value);
            capture.set(Highgui.CV_CAP_PROP_ANDROID_FOCUS_MODE,
                    Highgui.CV_CAP_ANDROID_FOCUS_MODE_AUTO);
        }
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

        {
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Rect face = facesArray[i];
                Core.rectangle(mRgba, face.tl(), face.br(), FACE_RECT_COLOR, 3);
            }
            if (facesArray.length > 0) {
                Rect face = facesArray[mRandom.nextInt(facesArray.length)];
                { // 座標更新
                    int width = inputFrame.width();
                    int height = inputFrame.height();

                    float aspect = (float)height / (float)width;
                    float posX = -(((float)(face.x + face.width / 2) / (float)width) * 2 - 1);
                    float posY = -(((float)(face.y + face.height / 2) / (float)height) * 2 - 1)
                            * aspect;
                    float dist = (float)(1.0 / Math.atan(Math.PI / 6));
                    float deltaYaw = (float)(Math.atan(posX / dist) * 256 / Math.PI);
                    float deltaPitch = (float)(Math.atan(posY / dist) * 256 / Math.PI);
                    mLastHeadYaw += (int)deltaYaw;
                    mLastHeadPitch = (int)deltaPitch;
                    mLastHeadYaw = Math.max(Constants.HEAD.yawMin,
                            Math.min(Constants.HEAD.yawMax, mLastHeadYaw));
                    mLastHeadPitch = Math.max(Constants.HEAD.pitchMin,
                            Math.min(Constants.HEAD.pitchMax, mLastHeadPitch));
                }

                Core.rectangle(mRgba, face.tl(), face.br(), FACE_RECT_COLOR_T, 3);
                onFaceDetected(mLastHeadYaw, mLastHeadPitch);
            }
        }

        return mRgba;
    }

    public boolean onMultiFaceDetected(int detectedCount) {
        Log.d(Constants.TAG, "Multi detect:" + detectedCount);
        ActionModel actionModel;
        actionModel = pickBindedActionRandom(ActionBind.PANIC);
        if (actionModel != null) {
            mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
            mPlayPoseTask.execute(actionModel);
            return true;
        } else {
            return false;
        }
    }

    private void onFaceDetected(int yaw, int pitch) {
        if (!isEnableAi()) {
            return;
        }
        if (mPlayPoseTask != null) {
            return;
        }
        ActionModel actionModel;
        { // 左手か右手かどっちの手を振るか選ぶ
            if (yaw <= 127) {
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
            headPoseModel.setHeadYaw((byte)mLastHeadYaw);
            headPoseModel.setHeadPitch((byte)mLastHeadYaw);
            headPoseModel.setTime(200);
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEarLeft((byte)0xAF);
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
                pm.setEarRight((byte)0x3F);
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

    private void onFaceDetected(Rect rect, int width, int height) {
        float aspect = (float)height / (float)width;
        float posX = -(((float)(rect.x + rect.width / 2) / (float)width) * 2 - 1);
        float posY = -(((float)(rect.y + rect.height / 2) / (float)height) * 2 - 1) * aspect;
        Log.d("Debug", String.format("%.3f,%.3f", posX, posY));

        if (!isEnableAi()) {
            return;
        }
        if (mPlayPoseTask != null) {
            return;
        }
        if (false) {
            { // 新しい頭の向きを計算
                float dist = (float)(1.0 / Math.atan(Math.PI / 6));
                float deltaYaw = (float)(Math.atan(posX / dist) * 256 / Math.PI);
                float deltaPitch = (float)(Math.atan(posY / dist) * 256 / Math.PI);
                mLastHeadYaw += (int)deltaYaw;
                mLastHeadPitch = (int)deltaPitch;
                mLastHeadYaw = Math.max(Constants.HEAD.yawMin,
                        Math.min(Constants.HEAD.yawMax, mLastHeadYaw));
                mLastHeadPitch = Math.max(Constants.HEAD.pitchMin,
                        Math.min(Constants.HEAD.pitchMax, mLastHeadPitch));
            }
            { // 頭の方向を変える。ついでに耳をちょっと動かす
                PoseModel headPoseModel = new PoseModel();
                headPoseModel.setHeadYaw((byte)mLastHeadYaw);
                headPoseModel.setHeadPitch((byte)mLastHeadPitch);
                headPoseModel.setTime(1000);
            }

            ActionModel actionModel;
            { // 左手か右手かどっちの手を振るか選ぶ
              // 見つからない場合はスタンド状態を設定する
                actionModel = new ActionModel();
                actionModel.setPoseModels(new ArrayList<PoseModel>());
                PoseModel poseModel = new PoseModel();
                poseModel.makeStandPose();
                poseModel.setTime(100);
                poseModel.setHeadYaw((byte)mLastHeadYaw);
                poseModel.setHeadPitch((byte)mLastHeadYaw);
                actionModel.getPoseModels().add(poseModel);
            }

            mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
            mPlayPoseTask.execute(actionModel);
        } else {
            ActionModel actionModel;
            { // 左手か右手かどっちの手を振るか選ぶ
                if (posX < 0.5) {
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

            { // 新しい頭の向きを計算
                float dist = (float)(1.0 / Math.atan(Math.PI / 6));
                float deltaYaw = (float)(Math.atan(posX / dist) * 256 / Math.PI);
                float deltaPitch = (float)(Math.atan(posY / dist) * 256 / Math.PI);
                mLastHeadYaw += (int)deltaYaw;
                mLastHeadPitch = (int)deltaPitch;
                mLastHeadYaw = Math.max(Constants.HEAD.yawMin,
                        Math.min(Constants.HEAD.yawMax, mLastHeadYaw));
                mLastHeadPitch = Math.max(Constants.HEAD.pitchMin,
                        Math.min(Constants.HEAD.pitchMax, mLastHeadPitch));
            }

            { // 頭の方向を変える。ついでに耳をちょっと動かす
                PoseModel headPoseModel = new PoseModel();
                headPoseModel.setHeadYaw((byte)mLastHeadYaw);
                headPoseModel.setHeadPitch((byte)mLastHeadYaw);
                headPoseModel.setTime(200);
                {
                    PoseModel pm = new PoseModel(headPoseModel);
                    pm.setEarLeft((byte)0xAF);
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
                    pm.setEarRight((byte)0x3F);
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
