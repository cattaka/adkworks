
package net.cattaka.robotarm01;

import java.util.ArrayList;
import java.util.List;

import net.cattaka.robotarm01.service.AdkService;
import net.cattaka.robotarm01.service.IAdkService;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControllerActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_ARM_ANGLES = "ARM_ANGLES";

    private static final int UPDATE_EVENT_ID = 1;

    private static final int UPDATE_INTERVAL = 100;

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

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == UPDATE_EVENT_ID) {
                sendValues();
                mHandler.sendEmptyMessageDelayed(UPDATE_EVENT_ID, UPDATE_INTERVAL);
                return true;
            }
            return false;
        }
    });

    private class ViewBundle implements SeekBar.OnSeekBarChangeListener {
        SeekBar valueBar;

        TextView valueView;

        int angleMin = -90;

        int angleMax = 90;

        boolean invert;

        public ViewBundle(int valueBarId, int valueViewId, int initialValue, int angleMin,
                int angleMax, boolean invert) {
            valueBar = (SeekBar)findViewById(valueBarId);
            valueView = (TextView)findViewById(valueViewId);
            this.angleMin = angleMin;
            this.angleMax = angleMax;
            this.invert = invert;
            valueBar.setMax(angleMax - angleMin);
            valueBar.setProgress(initialValue - angleMin);
            valueBar.setOnSeekBarChangeListener(this);
        }

        public int getValue() {
            return (valueBar.getProgress() * 0xFFFF) / valueBar.getMax();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDisplay();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        };
    }

    private List<ViewBundle> viewBundles;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        ArmSetting armSetting = new ArmSetting();
        armSetting.loadPreference(pref);

        viewBundles = new ArrayList<ControllerActivity.ViewBundle>();
        viewBundles.add(new ViewBundle(R.id.valueBar1, R.id.valueText1, 0, armSetting
                .getServo1Min(), armSetting.getServo1Max(), true));
        viewBundles.add(new ViewBundle(R.id.valueBar2, R.id.valueText2, 0, armSetting
                .getServo2Min(), armSetting.getServo2Max(), false));
        viewBundles.add(new ViewBundle(R.id.valueBar3, R.id.valueText3, 0, armSetting
                .getServo3Min(), armSetting.getServo3Max(), true));
        viewBundles.add(new ViewBundle(R.id.valueBar4, R.id.valueText4, 0, armSetting
                .getServo4Min(), armSetting.getServo4Max(), true));
        viewBundles.add(new ViewBundle(R.id.valueBar5, R.id.valueText5, 0, armSetting
                .getServo5Min(), armSetting.getServo5Max(), true));
        viewBundles.add(new ViewBundle(R.id.valueBar6, R.id.valueText6, 0, armSetting
                .getServo6Min(), armSetting.getServo6Max(), false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        {
            Intent intent = new Intent(this, AdkService.class);
            bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
            mHandler.sendEmptyMessageDelayed(UPDATE_EVENT_ID, UPDATE_INTERVAL);
        }
        float[] armAngles = getIntent().getFloatArrayExtra(EXTRA_ARM_ANGLES);
        if (armAngles != null) {
            for (int i = 0; i < armAngles.length && i < viewBundles.size(); i++) {
                ViewBundle vb = viewBundles.get(i);
                vb.valueBar.setProgress((int)armAngles[i] - vb.angleMin);
            }
        }

        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        mHandler.removeMessages(UPDATE_EVENT_ID);
    }

    @Override
    public void onClick(View v) {
        // none
    }

    private void updateDisplay() {
        for (int i = 0; i < viewBundles.size(); i++) {
            ViewBundle vb = viewBundles.get(i);
            int progress = vb.valueBar.getProgress() + vb.angleMin;
            String value = String.format("% 5d", progress);
            vb.valueView.setText(value);
        }
    }

    private void sendValues() {
        {
            int start = 0;
            int end = viewBundles.size();
            int step = viewBundles.size() / 2;
            while (start < end) {
                if (end - start < step) {
                    step = end - start;
                }
                byte[] data = new byte[step * 2];
                for (int i = 0; i < step; i++) {
                    ViewBundle vb = viewBundles.get(start + i);
                    int value = vb.getValue();
                    if (vb.invert) {
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
                    finish();
                }
                start += step;
            }
        }
        // {
        // int step = viewBundles.size();
        // byte[] data = new byte[step * 2];
        // for (int i = 0; i < step; i++) {
        // int progress = viewBundles.get(i).valueBar.getProgress();
        // data[i * 2] = (byte)((progress >> 8) & 0xFF);
        // data[i * 2 + 1] = (byte)(progress & 0xFF);
        // }
        // AndyCommand cmd = new AndyCommand((byte)0x03, (byte)(0x01), data);
        // try {
        // mDeviceComponent.sendCommand(cmd);
        // } catch (Exception e) {
        // Log.d("Debug", e.getMessage());
        // finish();
        // }
        // }
    }
}
