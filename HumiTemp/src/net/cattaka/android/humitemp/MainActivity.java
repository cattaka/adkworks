
package net.cattaka.android.humitemp;

import java.text.DecimalFormat;

import net.cattaka.android.humitemp.data.MyPacket;
import net.cattaka.android.humitemp.data.OpCode;
import net.cattaka.libgeppa.IGeppaService;
import net.cattaka.libgeppa.IGeppaServiceListener;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.app.Activity;
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

    private IGeppaService mService;

    private TextView mHumidityText;

    private TextView mTemperatureText;

    private IGeppaServiceListener mGeppaServiceListener = new IGeppaServiceListener.Stub() {
        public void onConnectionStateChanged(net.cattaka.libgeppa.data.ConnectionState arg0)
                throws android.os.RemoteException {
        };

        public void onReceivePacket(PacketWrapper packetWrapper) throws android.os.RemoteException {
            sHandler.obtainMessage(EVENT_ON_RECEIVE_PACKET, new Object[] {
                    me, packetWrapper.getPacket()
            }).sendToTarget();
        };
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName paramComponentName) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IGeppaService.Stub.asInterface(binder);
            try {
                mService.registerGeppaServiceListener(mGeppaServiceListener);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, GeppaServiceEx.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            byte[] bs = new byte[0];
            MyPacket packet = new MyPacket(OpCode.REQUEST, bs.length, bs);
            try {
                mService.sendPacket(new PacketWrapper(packet));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else if (v.getId() == R.id.showGraphButton) {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
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
