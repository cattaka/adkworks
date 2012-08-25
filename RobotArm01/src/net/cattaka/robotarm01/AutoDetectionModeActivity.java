
package net.cattaka.robotarm01;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.cattaka.positiondetector.OnPositionDetectorListener;
import net.cattaka.positiondetector.TagEvent;
import net.cattaka.positiondetector.TagState;
import net.cattaka.positiondetector.v4.PositionDetectorFragment;
import net.cattaka.robotarm01.math.ArmAngleUtil;
import net.cattaka.robotarm01.math.ArmAngleUtil.AngleBundle;
import net.cattaka.robotarm01.math.CtkMath;
import net.cattaka.robotarm01.service.AdkService;
import net.cattaka.robotarm01.service.IAdkService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AutoDetectionModeActivity extends FragmentActivity implements
        OnPositionDetectorListener, View.OnClickListener {

    private static final int EVENT_UPDATE_ARM_STATE = 1;

    private enum ArmStateType {
        WAITING, //
        READY_TO_AUTO_MODE, //
        MOVE_ABOVE_OBJECT, //
        MOVE_TO_OBJECT, //
        GRAB_OBJECT, //
        MOVE_ABOVE_TARGET, //
        MOVE_TO_TARGET, //
        RELEASE_OBJECT, //
        MOVE_ABOVE_RELEASED_OBJECT, //
    };

    private enum ErrorType {
        NO_ERROR, //
        OBJECT_NOT_DETECTED, TARGET_NOT_DETECTED, BAD_OBJECT_POSITION, //
        BAD_TARGET_POSITION, //
    };

    private class ArmState {
        public ArmStateType armStateType;

        public float[] exPosition;

        public float[] armAngles;

        public long time;

        public ArmState(ArmState src) {
            super();
            this.armStateType = src.armStateType;
            this.armAngles = new float[src.armAngles.length];
            System.arraycopy(src.armAngles, 0, armAngles, 0, armAngles.length);
            this.time = src.time;
        }

        public ArmState(ArmStateType armStateType, float[] armAngles, long time) {
            super();
            this.armStateType = armStateType;
            this.armAngles = armAngles;
            this.time = time;
        }

    }

    private static class PositionBundle {
        boolean detected;

        float[] position = CtkMath.createVector3f();

        float[] xVec = CtkMath.createVector3f();

        float[] yVec = CtkMath.createVector3f();

        float[] zVec = CtkMath.createVector3f();

        public void reset() {
            detected = false;
            CtkMath.set(position, 0, 0, 0);
            CtkMath.set(xVec, 1, 0, 0);
            CtkMath.set(yVec, 0, 1, 0);
            CtkMath.set(zVec, 0, 0, 1);
        }
    }

    /** オブジェクトの一片の長さ(mm) */
    private static final float OBJECT_SIZE = 25;

    /**
     * オブジェクト上のマージン30mm
     */
    private static final float UPPER_MARGIN = 30;

    private static final float EXTRA_MARGIN_RELEASE = 5;

    /** 自動動作モード時の動作対象とするターゲットとの距離 */
    private static final float AUTO_MODE_DISTANCE = 40;

    /** 自動動作モード時の動いていないと判定するための距離 */
    private static final float AUTO_MODE_DIFF = 10;

    private static final long DELAY_WAIT = 1000;

    private static final long DELAY_MOVE = 3000;

    private static final long DELAY_READY_TO_AUTO_MODE = 3000;

    private static final float[][] ANCHOR_POS_OFFSET = new float[][] {
            CtkMath.createVector3f(0, -75, 0), //
            CtkMath.createVector3f(55f, -75, 0), //
            CtkMath.createVector3f(-55f, -75, 0), //
            CtkMath.createVector3f(0, 75, 0), //
            CtkMath.createVector3f(55f, 75, 0), //
            CtkMath.createVector3f(-55f, 75, 0), //
    };

    /** オブジェクトのタグIDXは10〜15 */
    private static final int OBJECT_IDX_OFFSET = 10;

    /**
     * オブジェクトの各面から見た中心へのオフセット
     */
    private static final float[][] OBJECT_POS_OFFSET = new float[][] {
            CtkMath.createVector3f(0, 0, -12.5f), // 10
            CtkMath.createVector3f(0, 0, -12.5f), // 11
            CtkMath.createVector3f(0, 0, -12.5f), // 12
            CtkMath.createVector3f(0, 0, -12.5f), // 13
            CtkMath.createVector3f(0, 0, -12.5f), // 14
            CtkMath.createVector3f(0, 0, -12.5f), // 15
    };

    /**
     * オブジェクトの各面から見た左の方向
     */
    private static final float[][] OBJECT_X_VEC = new float[][] {
            CtkMath.createVector3f(1, 0, 0), // 10
            CtkMath.createVector3f(0, 0, 1), // 11
            CtkMath.createVector3f(-1, 0, 0), // 12
            CtkMath.createVector3f(0, 0, -1), // 13
            CtkMath.createVector3f(1, 0, 0), // 14
            CtkMath.createVector3f(1, 0, 0), // 15
    };

    /**
     * オブジェクトの各面から見た上の方向
     */
    private static final float[][] OBJECT_Y_VEC = new float[][] {
            CtkMath.createVector3f(0, 1, 0), // 10
            CtkMath.createVector3f(0, 1, 0), // 11
            CtkMath.createVector3f(0, 1, 0), // 12
            CtkMath.createVector3f(0, 1, 0), // 13
            CtkMath.createVector3f(0, 0, 1), // 14
            CtkMath.createVector3f(0, 0, -1), // 15
    };

    /**
     * オブジェクトの各面から見た前の方向
     */
    private static final float[][] OBJECT_Z_VEC = new float[][] {
            CtkMath.createVector3f(0, 0, 1), // 4
            CtkMath.createVector3f(-1, 0, 0), // 5
            CtkMath.createVector3f(0, 0, -1), // 6
            CtkMath.createVector3f(1, 0, 0), // 7
            CtkMath.createVector3f(0, -1, 0), // 8
            CtkMath.createVector3f(0, 1, 0), // 9
    };

    private static final int TARGET_IDX_OFFSET = 6;

    private Map<Integer, TagState> mTagStateMap = new HashMap<Integer, TagState>();

    private PositionDetectorFragment mPositionDetectorFragment;

    private ArmSetting mArmSetting = new ArmSetting();

    private PositionBundle mObjectBundle = new PositionBundle();

    private PositionBundle[] mTargetBundles = new PositionBundle[] {
            new PositionBundle(), new PositionBundle(), new PositionBundle(), new PositionBundle(),
    };

    private float[] tAnchorMat = CtkMath.createMatrix4f();

    private float[] tInvAnchorMat = CtkMath.createMatrix4f();

    private float[] tTmpMat = CtkMath.createMatrix4f();

    private float[] tTmpMat2 = CtkMath.createMatrix4f();

    private float[] tTmpVec = CtkMath.createVector3f();

    private ArmState mWaitingArmState = new ArmState(ArmStateType.WAITING, new float[] { //
                    0, //
                    -(float)(Math.PI / 4), //
                    (float)(Math.PI / 2), //
                    (float)(Math.PI / 2), //
                    0, //
                    0, //
                    0
            }, DELAY_WAIT);

    private ArmState mCurrentArmState;

    private Queue<ArmState> mArmStateQueue = new LinkedList<AutoDetectionModeActivity.ArmState>();

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == EVENT_UPDATE_ARM_STATE) {
                if (mArmStateQueue.size() > 0) {
                    mCurrentArmState = mArmStateQueue.remove();
                } else {
                    ToggleButton autoModeToggle = (ToggleButton)findViewById(R.id.autoModeToggle);
                    // 自動モードならサーチを試みる
                    if (autoModeToggle.isChecked() && startAutoGrabAction()) {
                        // OK
                    }
                    // 単に待つ
                    else {
                        mCurrentArmState = mWaitingArmState;
                    }
                }
                if (mCurrentArmState.armStateType == ArmStateType.READY_TO_AUTO_MODE) {
                    if (mObjectBundle.detected) {
                        float distance = CtkMath.distance(mCurrentArmState.exPosition,
                                mObjectBundle.position);
                        // オブジェクトが動いてないなら掴みにかかる
                        if (distance <= AUTO_MODE_DIFF) {
                            startGrabAction(false);
                        }
                    }
                }

                TextView armStateText = (TextView)findViewById(R.id.armStateText);
                armStateText.setText(mCurrentArmState.armStateType.name());
                this.sendEmptyMessageDelayed(EVENT_UPDATE_ARM_STATE, mCurrentArmState.time);

                sendValues(mCurrentArmState.armAngles);
            }
        };
    };

    private IAdkService mAdkService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAdkService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAdkService = IAdkService.Stub.asInterface(service);
            try {
                mAdkService.connectDevice();
            } catch (RemoteException e) {
                Log.d("Debug", e.toString(), e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.auto_detection_mode);

        findViewById(R.id.grubObjectButton).setOnClickListener(this);
        findViewById(R.id.autofocusButton).setOnClickListener(this);

        mPositionDetectorFragment = (PositionDetectorFragment)getSupportFragmentManager()
                .findFragmentByTag("PositionDetector");
        mPositionDetectorFragment.setOnPositionDetectorListener(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mArmSetting.loadPreference(pref);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, AdkService.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        mHandler.sendEmptyMessage(EVENT_UPDATE_ARM_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        mHandler.removeMessages(EVENT_UPDATE_ARM_STATE);
    }

    @Override
    public void onPositionDetectEvent(TagState tagState) {
        { // 認識済み座標として登録
            if (tagState.getTagEvent() != TagEvent.DISAPPEAR) {
                mTagStateMap.put(tagState.getIndex(), tagState);
            } else {
                mTagStateMap.remove(tagState.getIndex());
            }
        }
        float[] goodFrontVec = null;
        {
            goodFrontVec = getGoodFrontVec();
            if (goodFrontVec == null) {
                goodFrontVec = CtkMath.createVector3f();
            }
        }
        { // オブジェクトとターゲットの座標を表示する
            calcObjectPosition();
            calcTargetPosition();
            TextView textView = (TextView)findViewById(R.id.positionOutText);
            String str = String
                    .format("Pos=(%.3f,%.3f,%.3f)\nLeft=(%.3f,%.3f,%.3f)\nUp=(%.3f,%.3f,%.3f)\nFront=(%.3f,%.3f,%.3f)\nGoodFront=(%.3f,%.3f,%.3f)\nTargetPos=(%.3f,%.3f,%.3f)", //
                            mObjectBundle.position[0],
                            mObjectBundle.position[1],
                            mObjectBundle.position[2], //
                            mObjectBundle.xVec[0], mObjectBundle.xVec[1],
                            mObjectBundle.xVec[2], //
                            mObjectBundle.yVec[0], mObjectBundle.yVec[1],
                            mObjectBundle.yVec[2], //
                            mObjectBundle.zVec[0], mObjectBundle.zVec[1],
                            mObjectBundle.zVec[2], //
                            goodFrontVec[0], goodFrontVec[1],
                            goodFrontVec[2], //
                            mTargetBundles[0].position[0], mTargetBundles[0].position[1],
                            mTargetBundles[0].position[2] //
                    );
            textView.setText(str);
        }
    }

    private void calcObjectPosition() {
        int count = 0;
        {
            // 値をリセット
            mObjectBundle.reset();
            CtkMath.set(mObjectBundle.xVec, 0, 0, 0);
            CtkMath.set(mObjectBundle.yVec, 0, 0, 0);
            CtkMath.set(mObjectBundle.zVec, 0, 0, 0);
        }
        for (int j = 0; j < ANCHOR_POS_OFFSET.length; j++) {
            { // アンカーの行列を割り出す
                TagState tagState = mTagStateMap.get(j);
                if (tagState != null && tagState.getTagEvent() != TagEvent.DISAPPEAR) {
                    CtkMath.copyMatrix4f(tAnchorMat, tagState.getPoseMats());
                    if (!CtkMath.inverseMatrix4f(tInvAnchorMat, tAnchorMat)) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            { // アンカーの行列を使ってブロックの座標を割り出す
                for (int i = OBJECT_IDX_OFFSET; i < OBJECT_IDX_OFFSET + OBJECT_POS_OFFSET.length; i++) {
                    TagState tagState = mTagStateMap.get(i);
                    if (tagState != null && tagState.getTagEvent() != TagEvent.DISAPPEAR) {
                        Matrix.multiplyMM(tTmpMat, 0, tInvAnchorMat, 0, tagState.getPoseMats(), 0);
                        { // 座標の算出
                            mObjectBundle.position[0] += tTmpMat[12]
                                    + OBJECT_POS_OFFSET[i - OBJECT_IDX_OFFSET][0]
                                    + ANCHOR_POS_OFFSET[j][0];
                            mObjectBundle.position[1] += tTmpMat[13]
                                    + OBJECT_POS_OFFSET[i - OBJECT_IDX_OFFSET][1]
                                    + ANCHOR_POS_OFFSET[j][1];
                            mObjectBundle.position[2] += tTmpMat[14]
                                    + OBJECT_POS_OFFSET[i - OBJECT_IDX_OFFSET][2]
                                    + ANCHOR_POS_OFFSET[j][2];
                            count++;
                        }
                        { // X方向、Y方向、Z方向の算出
                            tTmpMat[12] = 0;
                            tTmpMat[13] = 0;
                            tTmpMat[14] = 0;
                            CtkMath.inverseMatrix4f(tTmpMat2, tTmpMat);
                            CtkMath.transpose3F(tTmpVec, OBJECT_X_VEC[i - OBJECT_IDX_OFFSET],
                                    tTmpMat2);
                            CtkMath.addEq3F(mObjectBundle.xVec, tTmpVec);
                            CtkMath.transpose3F(tTmpVec, OBJECT_Y_VEC[i - OBJECT_IDX_OFFSET],
                                    tTmpMat2);
                            CtkMath.addEq3F(mObjectBundle.yVec, tTmpVec);
                            CtkMath.transpose3F(tTmpVec, OBJECT_Z_VEC[i - OBJECT_IDX_OFFSET],
                                    tTmpMat2);
                            CtkMath.addEq3F(mObjectBundle.zVec, tTmpVec);
                        }
                    }
                }
            }
        }
        if (count > 0) {
            float scale = 1.0f / (float)count;
            mObjectBundle.detected = true;
            CtkMath.scaleEq3F(mObjectBundle.position, scale);
            CtkMath.normalizeEq3F(mObjectBundle.xVec);
            CtkMath.normalizeEq3F(mObjectBundle.yVec);
            CtkMath.normalizeEq3F(mObjectBundle.zVec);
        } else {
            mObjectBundle.detected = false;
        }
    }

    private void calcTargetPosition() {
        int count = 0;
        for (int j = 0; j < ANCHOR_POS_OFFSET.length; j++) {
            { // アンカーの行列を割り出す
                TagState tagState = mTagStateMap.get(j);
                if (tagState != null && tagState.getTagEvent() != TagEvent.DISAPPEAR) {
                    CtkMath.copyMatrix4f(tAnchorMat, tagState.getPoseMats());
                    if (!CtkMath.inverseMatrix4f(tInvAnchorMat, tAnchorMat)) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            { // アンカーの行列を使ってターゲットの座標を割り出す
                for (int i = 0; i < mTargetBundles.length; i++) {
                    TagState tagState = mTagStateMap.get(TARGET_IDX_OFFSET + i);
                    if (tagState != null && tagState.getTagEvent() != TagEvent.DISAPPEAR) {
                        mTargetBundles[i].detected = true;
                        Matrix.multiplyMM(tTmpMat, 0, tInvAnchorMat, 0, tagState.getPoseMats(), 0);
                        { // 座標の算出
                            mTargetBundles[i].position[0] = tTmpMat[12] + ANCHOR_POS_OFFSET[j][0];
                            mTargetBundles[i].position[1] = tTmpMat[13] + ANCHOR_POS_OFFSET[j][1];
                            mTargetBundles[i].position[2] = tTmpMat[14] + ANCHOR_POS_OFFSET[j][2];
                            count++;
                        }
                        { // X方向、Y方向、Z方向の算出
                            CtkMath.set(mTargetBundles[i].xVec, 1, 0, 0);
                            CtkMath.set(mTargetBundles[i].yVec, 0, 1, 0);
                            CtkMath.set(mTargetBundles[i].zVec, 0, 0, 1);
                            tTmpMat[12] = 0;
                            tTmpMat[13] = 0;
                            tTmpMat[14] = 0;
                            CtkMath.inverseMatrix4f(tTmpMat2, tTmpMat);
                            CtkMath.transposeEq3F(mTargetBundles[i].xVec, tTmpMat2);
                            CtkMath.transposeEq3F(mTargetBundles[i].yVec, tTmpMat2);
                            CtkMath.transposeEq3F(mTargetBundles[i].zVec, tTmpMat2);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.grubObjectButton) {
            startGrabAction(true);
        } else if (v.getId() == R.id.autofocusButton) {
            mPositionDetectorFragment.startAutoFocus();
        }
    }

    private void startGrabAction(boolean sendEvent) {
        TextView messageText = (TextView)findViewById(R.id.messageText);
        List<ArmState> armStates = new ArrayList<AutoDetectionModeActivity.ArmState>();
        ErrorType errorType = createArmState(armStates);
        if (errorType == ErrorType.NO_ERROR) {
            mArmStateQueue.clear();
            mArmStateQueue.addAll(armStates);
            messageText.setText("Starting grab action started.");
            if (sendEvent) {
                mHandler.removeMessages(EVENT_UPDATE_ARM_STATE);
                mHandler.sendEmptyMessage(EVENT_UPDATE_ARM_STATE);
            }
        } else {
            messageText.setText("Starting grab action failed:" + errorType);
        }
    }

    private boolean startAutoGrabAction() {
        PositionBundle targetBundle = mTargetBundles[0];
        if (mObjectBundle.detected && targetBundle.detected) {
            float distance = CtkMath.distance(mObjectBundle.position, targetBundle.position);
            if (distance >= AUTO_MODE_DISTANCE) {
                ArmState armState = new ArmState(mWaitingArmState);
                armState.armStateType = ArmStateType.READY_TO_AUTO_MODE;
                armState.time = DELAY_READY_TO_AUTO_MODE;
                armState.exPosition = CtkMath.createVector3f(mObjectBundle.position);
                mArmStateQueue.clear();
                mArmStateQueue.add(armState);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public ErrorType createArmState(List<ArmState> armStates) {
        PositionBundle targetBundle = mTargetBundles[0];
        if (!mObjectBundle.detected) {
            return ErrorType.OBJECT_NOT_DETECTED;
        }
        if (!targetBundle.detected) {
            return ErrorType.TARGET_NOT_DETECTED;
        }
        float[] lengths = new float[] {
                mArmSetting.getArmLength1(), mArmSetting.getArmLength2(),
                mArmSetting.getArmLength3(), mArmSetting.getArmLength4()
        };

        float[] targetPosition = CtkMath.createVector3f(ANCHOR_POS_OFFSET[0]);
        targetPosition[2] += 20;

        float[] goodFrontVec = getGoodFrontVec();
        if (goodFrontVec == null) {
            goodFrontVec = CtkMath.createVector3f(0, 0, -1);
        }

        ArmState moveToObjectState;
        ArmState moveToTargetState;
        int index;
        { // MOVE_ABOVE_OBJECT
            float[] position = CtkMath.createVector3f(-mObjectBundle.position[1],
                    mObjectBundle.position[0], mObjectBundle.position[2] + UPPER_MARGIN);
            float[] frontVec = CtkMath.createVector3f(0, 0, -1);
            float[] upVec = CtkMath.createVector3f(goodFrontVec[1], -goodFrontVec[0],
                    goodFrontVec[2]);
            AngleBundle[] angleBundles = ArmAngleUtil
                    .calcAngles(lengths, position, frontVec, upVec);
            index = ArmAngleUtil.pickAvailableIndex(angleBundles, mArmSetting);
            if (index < 0) {
                return ErrorType.BAD_OBJECT_POSITION;
            }
            float[] armAngles = angleBundles[index].toAnglesFloatArray();
            armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Min());
            armStates.add(new ArmState(ArmStateType.MOVE_ABOVE_OBJECT, armAngles, DELAY_MOVE));
        }
        { // MOVE_TO_OBJECT
            float[] position = CtkMath.createVector3f(-mObjectBundle.position[1],
                    mObjectBundle.position[0], mObjectBundle.position[2]);
            float[] frontVec = CtkMath.createVector3f(0, 0, -1);
            float[] upVec = CtkMath.createVector3f(goodFrontVec[1], -goodFrontVec[0],
                    goodFrontVec[2]);
            AngleBundle[] angleBundles = ArmAngleUtil
                    .calcAngles(lengths, position, frontVec, upVec);
            if (!ArmAngleUtil.isAvailableIndex(angleBundles, mArmSetting, index)) {
                // 駄目じゃん！
                return ErrorType.BAD_OBJECT_POSITION;
            }
            float[] armAngles = angleBundles[index].toAnglesFloatArray();
            armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Min());
            moveToObjectState = new ArmState(ArmStateType.MOVE_TO_OBJECT, armAngles, DELAY_WAIT);
            armStates.add(moveToObjectState);
        }
        { // GRAB_OBJECT
            ArmState armState = new ArmState(moveToObjectState);
            armState.armStateType = ArmStateType.GRAB_OBJECT;
            armState.armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Max() / 3);
            armState.time = DELAY_WAIT;
            armStates.add(armState);
        }
        int tIndex = -1;
        { // MOVE_ABOVE_TARGET
            float[] position = CtkMath.createVector3f(-targetBundle.position[1],
                    targetBundle.position[0], targetBundle.position[2] + UPPER_MARGIN + OBJECT_SIZE
                            / 2);
            float[] frontVec = CtkMath.createVector3f(0, 0, -1);
            float[] upVec = CtkMath.createVector3f(1, 0, 0);
            AngleBundle[] angleBundles = ArmAngleUtil
                    .calcAngles(lengths, position, frontVec, upVec);
            tIndex = ArmAngleUtil.pickAvailableIndex(angleBundles, mArmSetting);
            if (tIndex == -1) {
                // 駄目じゃん！
                return ErrorType.BAD_TARGET_POSITION;
            }
            float[] armAngles = angleBundles[tIndex].toAnglesFloatArray();
            armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Max() / 3);
            moveToTargetState = new ArmState(ArmStateType.MOVE_ABOVE_TARGET, armAngles, DELAY_MOVE);
            armStates.add(moveToTargetState);
        }
        { // MOVE_TO_TARGET
            float[] position = CtkMath.createVector3f(-targetBundle.position[1],
                    targetBundle.position[0], targetBundle.position[2] + OBJECT_SIZE / 2
                            + EXTRA_MARGIN_RELEASE);
            float[] frontVec = CtkMath.createVector3f(0, 0, -1);
            float[] upVec = CtkMath.createVector3f(1, 0, 0);
            AngleBundle[] angleBundles = ArmAngleUtil
                    .calcAngles(lengths, position, frontVec, upVec);
            tIndex = ArmAngleUtil.pickAvailableIndex(angleBundles, mArmSetting);
            if (!ArmAngleUtil.isAvailableIndex(angleBundles, mArmSetting, tIndex)) {
                // 駄目じゃん！
                return ErrorType.BAD_TARGET_POSITION;
            }
            float[] armAngles = angleBundles[tIndex].toAnglesFloatArray();
            armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Max() / 3);
            moveToTargetState = new ArmState(ArmStateType.MOVE_TO_TARGET, armAngles, DELAY_WAIT);
            armStates.add(moveToTargetState);
        }
        { // RELEASE_OBJECT
            ArmState armState = new ArmState(moveToTargetState);
            armState.armStateType = ArmStateType.RELEASE_OBJECT;
            armState.armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Min());
            armState.time = DELAY_WAIT;
            armStates.add(armState);
        }
        { // MOVE_TO_RELEASED_OBJECT
            float[] position = CtkMath.createVector3f(-targetBundle.position[1],
                    targetBundle.position[0], targetBundle.position[2] + UPPER_MARGIN + OBJECT_SIZE
                            / 2);
            float[] frontVec = CtkMath.createVector3f(0, 0, -1);
            float[] upVec = CtkMath.createVector3f(1, 0, 0);
            AngleBundle[] angleBundles = ArmAngleUtil
                    .calcAngles(lengths, position, frontVec, upVec);
            if (!ArmAngleUtil.isAvailableIndex(angleBundles, mArmSetting, tIndex)) {
                // 駄目じゃん！
                return ErrorType.BAD_TARGET_POSITION;
            }
            float[] armAngles = angleBundles[tIndex].toAnglesFloatArray();
            armAngles[5] = (float)ArmAngleUtil.digToRad(mArmSetting.getServo6Min());
            armStates.add(new ArmState(ArmStateType.MOVE_ABOVE_RELEASED_OBJECT, armAngles,
                    DELAY_WAIT));
        }
        return ErrorType.NO_ERROR;
    }

    private void sendValues(float[] armAngles) {
        int start = 0;
        int end = armAngles.length;
        int step = armAngles.length / 2;
        while (start < end) {
            if (end - start < step) {
                step = end - start;
            }
            byte[] data = new byte[step * 2];
            for (int i = 0; i < step; i++) {
                int idx = start + i;
                float angle = (float)ArmAngleUtil.radToDig(armAngles[idx])
                        + mArmSetting.getAngleOffset(idx);
                float rate = (angle - mArmSetting.getServoMin(idx))
                        / (mArmSetting.getServoMax(idx) - mArmSetting.getServoMin(idx));
                rate = Math.max(Math.min(1, rate), 0);
                int value = (int)(0xFFFF * rate);
                if (mArmSetting.getServoInvert(idx)) {
                    value = 0xffff - value;
                }
                data[i * 2] = (byte)((value >> 8) & 0xFF);
                data[i * 2 + 1] = (byte)(value & 0xFF);
            }
            try {
                if (mAdkService != null) {
                    mAdkService.sendCommand((byte)0x03, (byte)(0x01 + (start * 2)), data);
                }
            } catch (RemoteException e) {
                Log.d("Debug", "RemoteException");
                // finish();
            }
            start += step;
        }
    }

    private float[] getGoodFrontVec() {
        if (mObjectBundle.detected) {
            float[] dir = CtkMath.createVector3f(mObjectBundle.position);
            dir[2] = 0;
            CtkMath.normalizeEq3F(dir);
            float[][] vecs;
            {
                vecs = new float[][] {
                        CtkMath.createVector3f(mObjectBundle.xVec),
                        CtkMath.createVector3f(mObjectBundle.yVec),
                        CtkMath.createVector3f(mObjectBundle.zVec),
                        CtkMath.createVector3f(mObjectBundle.xVec),
                        CtkMath.createVector3f(mObjectBundle.yVec),
                        CtkMath.createVector3f(mObjectBundle.zVec)
                };
                CtkMath.scaleEq3F(vecs[3], -1);
                CtkMath.scaleEq3F(vecs[4], -1);
                CtkMath.scaleEq3F(vecs[5], -1);
            }
            float maxDot = 0;
            float[] result = null;
            for (float[] vec : vecs) {
                float dot = CtkMath.dot3F(dir, vec);
                if (dot >= maxDot) {
                    maxDot = dot;
                    result = vec;
                }
            }
            return result;
        } else {
            return null;
        }
    }
}
