
package net.cattaka.droidrobo01;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.cattaka.droidrobo01.robo.RoboPauseInfo;
import net.cattaka.droidrobo01.robo.RoboPauseInfo.MotorDir;
import net.cattaka.droidrobo01.service.AdkService;
import net.cattaka.droidrobo01.service.IAdkService;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class MonitorNfcActivity extends Activity {
    private static final int EVENT_DRIVE = 2;

    private MonitorNfcActivity me = this;

    private TextView mMotionCountText;

    private IAdkService mAdkService;

    private NfcAdapter mNfcAdapter;

    private IntentFilter[] mNfcFilters = new IntentFilter[] {
        new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
    };

    private String[][] mNfcTechLists = new String[][] {
        new String[] {
            Ndef.class.getName()
        }
    };

    private PendingIntent mPendingIntent;

    private ArmSetting mSetting;

    private Queue<RoboPauseInfo> mPauseQueue = new LinkedList<RoboPauseInfo>();

    private RoboPauseInfo ROBO_PAUSE_STOP = new RoboPauseInfo(false, MotorDir.STOP, MotorDir.STOP,
            500, 0.25f, 0.25f);

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

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == EVENT_DRIVE) {
                startDriveRobo();
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_nfc);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMotionCountText = (TextView)findViewById(R.id.motionCountText);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        { // enable
            Intent intent = new Intent(this, getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mPendingIntent = PendingIntent.getActivity(this, 1, intent, 0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage[] msgs;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage)rawMsgs[i];
                }

                List<RoboPauseInfo> infos = readNdefMessages(tag, msgs);
                if (infos.size() > 0) {
                    mPauseQueue.clear();
                    mPauseQueue.addAll(infos);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        {
            Intent intent = new Intent(this, AdkService.class);
            bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        {
            mSetting = new ArmSetting();
            mSetting.loadPreference(pref);
        }

        startDriveRobo();

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mNfcFilters, mNfcTechLists);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDriveRobo();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void addMessageAsync(String str) {
        if (mPauseQueue.size() == 0) {
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 0.25f, 0.25f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorDir.STOP, MotorDir.STOP, 500, 0.25f,
                    0.25f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 0.25f, 0.25f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorDir.STOP, MotorDir.STOP, 500, 0.25f,
                    0.25f));

            mPauseQueue.add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 1000, 0.75f,
                    0.75f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 1.00f, 0.50f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 0.50f, 1.00f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 1.00f, 0.50f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 0.50f, 1.00f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 1.00f, 0.50f));
            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 500, 0.50f, 1.00f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorDir.STOP, MotorDir.STOP, 500, 0.75f,
                    0.75f));

            mPauseQueue.add(new RoboPauseInfo(true, MotorDir.FORWARD, MotorDir.REVERSE, 200, 0.75f,
                    0.75f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorDir.REVERSE, MotorDir.FORWARD, 200, 0.75f,
                    0.75f));

            mPauseQueue
                    .add(new RoboPauseInfo(true, MotorDir.STOP, MotorDir.STOP, 1000, 1.0f, 1.0f));

        }
    }

    private List<RoboPauseInfo> readNdefMessages(Tag tag, NdefMessage[] msgs) {
        List<RoboPauseInfo> infos = new ArrayList<RoboPauseInfo>();
        for (NdefMessage msg : msgs) {
            NdefRecord rec = msg.getRecords()[0];
            byte[] data = rec.getPayload();
            int repeat = data[0];
            int size = data[1];
            int pos = 2;
            List<RoboPauseInfo> tmpList = new ArrayList<RoboPauseInfo>(infos);
            for (int i = 0; i < size; i++) {
                RoboPauseInfo info = new RoboPauseInfo();
                info.setEyeLight(data[pos++] != 0);
                info.setArmLeftAngle((float)(0xFF & data[pos++]) / (float)0xFF);
                info.setArmRightAngle((float)(0xFF & data[pos++]) / (float)0xFF);
                info.setMotorDirLeft(MotorDir.parse(data[pos++]));
                info.setMotorDirRight(MotorDir.parse(data[pos++]));
                info.setDucation(data[pos++] * 100);
                tmpList.add(info);
            }
            for (int i = 0; i < repeat; i++) {
                infos.addAll(tmpList);
            }
        }
        return infos;
    }

    private void startDriveRobo() {
        mMotionCountText.setText(String.valueOf(mPauseQueue.size()));

        RoboPauseInfo rpInfo = mPauseQueue.poll();
        if (rpInfo == null) {
            rpInfo = ROBO_PAUSE_STOP;
        }
        {
            if (mAdkService != null) {
                try {
                    mAdkService.sendCommand((byte)0x03, (byte)(0x0), rpInfo.toUpperBytes());
                    mAdkService.sendCommand((byte)0x03, (byte)(0x5), rpInfo.toLowerBytes());
                } catch (RemoteException e) {
                    Log.e("Debug", e.getMessage(), e);
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(EVENT_DRIVE, rpInfo.getDucation());
    }

    private void stopDriveRobo() {
        mHandler.removeMessages(EVENT_DRIVE);
    }
}
