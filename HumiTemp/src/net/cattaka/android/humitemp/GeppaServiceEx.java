
package net.cattaka.android.humitemp;

import java.util.Date;

import net.cattaka.android.humitemp.data.MyPacket;
import net.cattaka.android.humitemp.data.MyPacketFactory;
import net.cattaka.android.humitemp.data.OpCode;
import net.cattaka.android.humitemp.db.DbHelper;
import net.cattaka.android.humitemp.entity.HumiTempModel;
import net.cattaka.libgeppa.AdkPassiveGeppaService;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class GeppaServiceEx extends AdkPassiveGeppaService<MyPacket> {
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
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDbHelper = new DbHelper(this);

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sHandler.removeMessages(EVENT_RECORD);
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
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

    private void sendRequestPacket() {
        byte[] bs = new byte[0];
        MyPacket packet = new MyPacket(OpCode.REQUEST, bs.length, bs);
        sendPacket(new PacketWrapper(packet));
    }

    @Override
    protected void onReceivePacket(MyPacket packet) {
        super.onReceivePacket(packet);

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

    @Override
    protected void onConnectionStateChanged(ConnectionState state) {
        super.onConnectionStateChanged(state);
        if (state == ConnectionState.CONNECTED) {
            sHandler.sendMessage(sHandler.obtainMessage(EVENT_RECORD, new Object[] {
                this
            }));
        }
    }
}
