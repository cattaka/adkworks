
package net.cattaka.android.humitemp4pd;

import java.text.DecimalFormat;

import net.cattaka.android.humitemp4pd.data.MyPacket;
import net.cattaka.android.humitemp4pd.data.OpCode;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final int EVENT_ON_RECEIVE_PACKET = 1;

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;

    private static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Object[] objs = (Object[])msg.obj;
            MainActivity target = (MainActivity)objs[0];
            if (msg.what == EVENT_ON_RECEIVE_PACKET) {
                target.onReceivePacket((MyPacket)objs[1]);
            }
        };
    };

    private MainActivity me = this;

    private IHumiTempService mService;

    private TextView mHumidityText;

    private TextView mTemperatureText;

    private ServiceConnectionEx mServiceConnection = new ServiceConnectionEx();

    class ServiceConnectionEx implements ServiceConnection {
        private BluetoothDevice mPendingAddDevice;

        @Override
        public void onServiceDisconnected(ComponentName paramComponentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = HumiTempService.Stub.asInterface(binder);
            if (mPendingAddDevice != null) {
                try {
                    mService.addDevice(mPendingAddDevice);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                mPendingAddDevice = null;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHumidityText = (TextView)findViewById(R.id.humidityText);
        mTemperatureText = (TextView)findViewById(R.id.temperatureText);

        // Setting event listeners
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.showGraphButton).setOnClickListener(this);
        findViewById(R.id.selectDeviceButton).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, HumiTempService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SELECT_DEVICE == requestCode) {
            if (resultCode == RESULT_OK) {
                mServiceConnection.mPendingAddDevice = (BluetoothDevice)data
                        .getParcelableExtra(SelectDeviceActivity.EXTRA_BLUETOOTH_DEVICE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            try {
                mService.updateSensorValues();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            // TODO
        } else if (v.getId() == R.id.showGraphButton) {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.selectDeviceButton) {
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE);
        }
    }

    private void onReceivePacket(MyPacket packet) {
        if (packet.getOpCode() == OpCode.RESPONSE) {
            if (packet.getDataLen() == 8) {
                byte[] data = packet.getData();
                int humidity = (((0xFF & data[0]) << 24) | ((0xFF & data[1]) << 16)
                        | ((0xFF & data[2]) << 8) | ((0xFF & data[3]) << 0));
                int temperature = (((0xFF & data[4]) << 24) | ((0xFF & data[5]) << 16)
                        | ((0xFF & data[6]) << 8) | ((0xFF & data[7]) << 0));
                DecimalFormat df = new DecimalFormat("####0.00");
                mHumidityText.setText(df.format(humidity / 100f));
                mTemperatureText.setText(df.format(temperature / 100f));
            }
        }
    }
}
