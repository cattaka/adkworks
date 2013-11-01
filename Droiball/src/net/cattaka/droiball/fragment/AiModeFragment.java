
package net.cattaka.droiball.fragment;

import java.util.Random;

import net.cattaka.droiball.Constants;
import net.cattaka.droiball.R;
import net.cattaka.droiball.data.MyPacket;
import net.cattaka.droiball.data.OpCode;
import net.cattaka.droiball.entity.ActionModel;
import net.cattaka.droiball.entity.PoseModel;
import net.cattaka.droiball.entity.Vector3s;
import net.cattaka.droiball.task.PlayPoseTask;
import net.cattaka.droiball.util.MyPreference;
import net.cattaka.droiball.view.MyNativeCameraView;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.samples.fd.DetectionBasedTracker;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AiModeFragment extends BaseFragment implements View.OnClickListener,
        PlayPoseTask.PlayPoseTaskListener {
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
                try {
                    MyPacket packet = new MyPacket(OpCode.REQ_ACCEL, 0, new byte[0]);
                    getGeppaService().sendPacket(new PacketWrapper(packet));
                } catch (RemoteException e) {
                    // TODO
                    Log.w(Constants.TAG, e.getMessage(), e);
                }
                mHandler.sendEmptyMessageDelayed(EVENT_GET_ACCEL, INTERVAL_GET_ACCEL);
            } else if (msg.what == EVENT_ACTION_RANDOM) {
                if (isEnableAi() && mPlayPoseTask == null) {
                    int n = mRandom.nextInt(Constants.ACTION_NAMES_RANDOM.length);
                    String actionName = Constants.ACTION_NAMES_RANDOM[n];
                    ActionModel actionModel = getDroiballDatabase().findActionModel(actionName,
                            true);

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

    private TextView mAccelXText;

    private TextView mAccelYText;

    private TextView mAccelZText;

    private TextView mStandStateText;

    private Vector3s mCurrentAccel = new Vector3s();

    private Vector3s mOffsetAccel;

    private ToggleButton mEnableAiToggle;

    private MyNativeCameraView mOpenCvCameraView;

    private MyPreference mPreference;

    private StandState mStandState = StandState.STAND;

    private int mStandStateCount = 0;

    private PlayPoseTask mPlayPoseTask;

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
        View view = inflater.inflate(R.layout.ai_mode, null);

        mAccelXText = (TextView)view.findViewById(R.id.accelXText);
        mAccelYText = (TextView)view.findViewById(R.id.accelYText);
        mAccelZText = (TextView)view.findViewById(R.id.accelZText);
        mStandStateText = (TextView)view.findViewById(R.id.standStateText);
        mEnableAiToggle = (ToggleButton)view.findViewById(R.id.enableAiToggle);

        view.findViewById(R.id.resetAccelButton).setOnClickListener(this);

        mPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(getActivity()));
        mOffsetAccel = mPreference.getOffsetAccel();

        mOpenCvCameraView = (MyNativeCameraView)view.findViewById(R.id.cameraView);
        mOpenCvCameraView.setCvCameraViewListener(mCvCameraViewListener);
        mOpenCvCameraView.setCameraId(1);

        mWakelock = ((PowerManager)getActivity().getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Constants.TAG);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mGray = new Mat();
        mRgba = new Mat();

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
    public void onClick(View v) {
        if (v.getId() == R.id.resetAccelButton) {
            resetAccel();
        }
    }

    private void resetAccel() {
        mOffsetAccel.set(mCurrentAccel);
        mPreference.edit();
        mPreference.putOffsetAccel(mOffsetAccel);
        mPreference.commit();
    }

    @Override
    public void onPageSelected() {
        MyPreference pref = getMyPreference();

        mEnableAiToggle.setChecked(false);
        mWakelock.acquire();
        mHandler.sendEmptyMessageDelayed(EVENT_GET_ACCEL, INTERVAL_GET_ACCEL);
        mHandler.sendEmptyMessageDelayed(EVENT_ACTION_RANDOM, INTERVAL_ACTION_RANDOM);
        mOpenCvCameraView.enableView();

        VideoCapture capture = mOpenCvCameraView.getVideoCapture();
        capture.set(Highgui.CV_CAP_PROP_ANDROID_WHITE_BALANCE, pref.getWhiteBalance().value);
        capture.set(Highgui.CV_CAP_PROP_ANDROID_FOCUS_MODE, Highgui.CV_CAP_ANDROID_FOCUS_MODE_AUTO);
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
    public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceDisconnected(ComponentName paramComponentName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReceivePacket(PacketWrapper packetWrapper) {
        MyPacket packet = (MyPacket)packetWrapper.getPacket();
        if (packet.getOpCode() == OpCode.RES_ACCEL) {
            mCurrentAccel.read16u(packet.getData(), 0);
            mCurrentAccel.setX((short)-mCurrentAccel.getX());
            mCurrentAccel.setY((short)-mCurrentAccel.getY());
            mAccelXText.setText(String.valueOf(mCurrentAccel.getX() - mOffsetAccel.getX()));
            mAccelYText.setText(String.valueOf(mCurrentAccel.getY() - mOffsetAccel.getY()));
            mAccelZText.setText(String.valueOf(mCurrentAccel.getZ() - mOffsetAccel.getZ()));

            StandState ss;
            if (mCurrentAccel.getX() - mOffsetAccel.getX() > 80) {
                ss = StandState.FACE_DOWN;
            } else if (mCurrentAccel.getX() - mOffsetAccel.getX() < -80) {
                ss = StandState.BACK;
            } else {
                ss = StandState.STAND;
            }
            if (ss != mStandState) {
                mStandStateCount = 1;
                mStandState = ss;
            } else {
                mStandStateCount++;
            }
            mStandStateText.setText(mStandState.name() + " : " + mStandStateCount);

            if (isEnableAi() && mPlayPoseTask == null) {
                checkWakeup();
            }
        }
    }

    private void checkWakeup() {
        if (mStandState == StandState.FACE_DOWN && mStandStateCount > 3) {
            ActionModel model = getDroiballDatabase().findActionModel(
                    Constants.ACTION_NAME_WAKE_FRONT, true);
            if (model != null) {
                mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
                mPlayPoseTask.execute(model);
            }
        } else if (mStandState == StandState.BACK && mStandStateCount > 3) {
            ActionModel model = getDroiballDatabase().findActionModel(
                    Constants.ACTION_NAME_WAKE_BACK, true);
            if (model != null) {
                mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
                mPlayPoseTask.execute(model);
            }

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
        if (index < length / 2) {
            actionModel = getDroiballDatabase().findActionModel(Constants.ACTION_NAME_WAVE_LEFT,
                    true);
        } else {
            actionModel = getDroiballDatabase().findActionModel(Constants.ACTION_NAME_WAVE_RIGHT,
                    true);
        }
        { // 頭の方向を変える
            PoseModel headPoseModel = new PoseModel();
            headPoseModel.setHead((byte)(0x30 + (0xA0 * index / length)));
            headPoseModel.setTime(500);
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEyeLeft(true);
                pm.setEyeRight(true);
                pm.setEarLeft((byte)0xFF);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(0, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEyeLeft(false);
                pm.setEyeRight(false);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(1, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEyeLeft(true);
                pm.setEyeRight(true);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x0);
                actionModel.getPoseModels().add(2, pm);
            }
            {
                PoseModel pm = new PoseModel(headPoseModel);
                pm.setEyeLeft(false);
                pm.setEyeRight(false);
                pm.setEarLeft((byte)0x7F);
                pm.setEarRight((byte)0x7F);
                actionModel.getPoseModels().add(3, pm);
            }
            {
                PoseModel pm = new PoseModel();
                pm.setEyeLeft(true);
                pm.setEyeRight(true);
                pm.makeStandPose();
                pm.setTime(500);
                actionModel.getPoseModels().add(pm);
            }
        }
        mPlayPoseTask = new PlayPoseTask(getAppStub(), this);
        mPlayPoseTask.execute(actionModel);
    }

    @Override
    public void onPlayPoseTaskFinish() {
        mPlayPoseTask = null;
    }

    private boolean isEnableAi() {
        return mEnableAiToggle.isChecked();
    }
}
