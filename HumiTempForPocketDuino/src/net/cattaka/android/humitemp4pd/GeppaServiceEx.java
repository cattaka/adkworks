
package net.cattaka.android.humitemp4pd;

import java.util.Date;

import net.cattaka.android.humitemp4pd.data.MyPacket;
import net.cattaka.android.humitemp4pd.data.MyPacketFactory;
import net.cattaka.android.humitemp4pd.data.OpCode;
import net.cattaka.android.humitemp4pd.db.DbHelper;
import net.cattaka.android.humitemp4pd.entity.HumiTempModel;
import net.cattaka.libgeppa.ActiveGeppaService;
import net.cattaka.libgeppa.IActiveGeppaServiceListener;
import net.cattaka.libgeppa.data.BaudRate;
import net.cattaka.libgeppa.data.DeviceEventCode;
import net.cattaka.libgeppa.data.DeviceInfo;
import net.cattaka.libgeppa.data.DeviceState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;

public class GeppaServiceEx extends ActiveGeppaService<MyPacket> {
    private static final int NOTIFICATION_CONNECTED_ID = 1;

    private static final int EVENT_RECORD = 2;

    private static final int RECORD_INTERVAL = 60000;

    private boolean requesting = false;

    private WakeLock mWakeLock;

    private static Handler sHandler = new Handler() {
        public void handleMessage(Message msg) {
            Object[] objs = (Object[])msg.obj;
            GeppaServiceEx target = (GeppaServiceEx)objs[0];
            if (msg.what == EVENT_RECORD) {
                target.scheduleRecord();
                target.requesting = true;
                target.sendRequestPacket();
            }
        };
    };

    private GeppaServiceEx me = this;

    private DbHelper mDbHelper;

    public GeppaServiceEx() {
        super(new MyPacketFactory());
        setBaudRate(BaudRate.BAUD9600);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDbHelper = new DbHelper(this);

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);

        mRestenerSeq = registerConnectionListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sHandler.removeMessages(EVENT_RECORD);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        unregisterConnectionListener(mRestenerSeq);
    }

    private void scheduleRecord() {
        mWakeLock.acquire(RECORD_INTERVAL * 2 / 3);
        sHandler.sendMessageDelayed(sHandler.obtainMessage(EVENT_RECORD, new Object[] {
            this
        }), RECORD_INTERVAL);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void handleConnectedNotification(boolean connected, DeviceInfo deviceInfo) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (connected) {
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentIntent(pIntent);
            builder.setContentTitle(getText(R.string.notification_title_connected));
            builder.setContentText(deviceInfo.getLabel());
            builder.setSmallIcon(R.drawable.ic_launcher);
            @SuppressWarnings("deprecation")
            Notification nortification = builder.getNotification();
            manager.notify(NOTIFICATION_CONNECTED_ID, nortification);
        } else {
            manager.cancel(NOTIFICATION_CONNECTED_ID);
        }
    }

    private int mRestenerSeq;

    private IActiveGeppaServiceListener mListener = new IActiveGeppaServiceListener.Stub() {

        @Override
        public void onReceivePacket(PacketWrapper packet) throws RemoteException {
            me.onReceivePacket((MyPacket)packet.getPacket());
        }

        @Override
        public void onDeviceStateChanged(DeviceState state, DeviceEventCode eventCode,
                DeviceInfo deveiceInfo) throws RemoteException {
            me.onDeviceStateChanged(state, eventCode, deveiceInfo);
        }
    };

    private void sendRequestPacket() {
        byte[] bs = new byte[0];
        MyPacket packet = new MyPacket(OpCode.REQUEST, bs.length, bs);
        sendPacket(packet);
    }

    private void onReceivePacket(MyPacket packet) {
        if (packet.getOpCode() == OpCode.RESPONSE) {
            if (requesting) {
                if (packet.getDataLen() == 8) {
                    byte[] data = packet.getData();
                    int humidity = (((0xFF & data[0]) << 24) | ((0xFF & data[1]) << 16)
                            | ((0xFF & data[2]) << 8) | ((0xFF & data[3]) << 0));
                    int temperature = (((0xFF & data[4]) << 24) | ((0xFF & data[5]) << 16)
                            | ((0xFF & data[6]) << 8) | ((0xFF & data[7]) << 0));

                    HumiTempModel model = new HumiTempModel();
                    model.setDate(new Date((System.currentTimeMillis() / 60000) * 60000));
                    model.setHumidity(humidity / 100f);
                    model.setTemperature(temperature / 100f);
                    mDbHelper.registerHumiTempModel(model);
                    requesting = false;
                }
            }
        }
    }

    private void onDeviceStateChanged(DeviceState state, DeviceEventCode eventCode,
            DeviceInfo deveiceInfo) throws RemoteException {
        if (state == DeviceState.CONNECTED) {
            sHandler.sendMessage(sHandler.obtainMessage(EVENT_RECORD, new Object[] {
                this
            }));
        }
    }
}
