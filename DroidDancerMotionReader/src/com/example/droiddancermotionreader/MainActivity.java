package com.example.droiddancermotionreader;

import java.util.List;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {
    private static final String TAG = "DroidDancerMotionReader";
    private NfcAdapter nfcadapter;
    private IntentFilter[] nfcfilters = new IntentFilter[] {
            new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
    };
    private String[][] nfctechLists = new String[][] {
            new String[] {
                    Ndef.class.getName()
            }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcadapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                readNdefMessages(tag, msgs);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcadapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()), 0);
        nfcadapter.enableForegroundDispatch(this, pendingIntent, nfcfilters, nfctechLists);
    }
    
    private MotionData readNdefMessages(Tag tag, NdefMessage[] msgs) {
        MotionData motiondata = new MotionData();
        for(NdefMessage msg : msgs){
            NdefRecord rec = msg.getRecords()[0];
            byte[] data = rec.getPayload();
            motiondata.repeat = Byte.valueOf(data[0]);
            int size = Byte.valueOf(data[1]);
            Log.d(TAG, "repeat="+Byte.valueOf(motiondata.repeat).toString());
            Log.d(TAG, "size"+size);
            int pos=2;
            for(int i=0; i<size; i++){
                MotionItem item = new MotionItem();
                item.setLed(data[pos++]==1);
                item.setArmleft(data[pos++]);
                item.setArmright(data[pos++]);
                item.setRotleft(data[pos++]);
                item.setRotright(data[pos++]);
                item.setTime(data[pos++]);
                motiondata.items.add(item);
                Log.d(TAG, "led="+item.isLed());
                Log.d(TAG, "armleft="+Byte.valueOf(item.getArmleft()).toString());
                Log.d(TAG, "armright="+Byte.valueOf(item.getArmright()).toString());
                Log.d(TAG, "rotleft="+Byte.valueOf(item.getRotleft()).toString());
                Log.d(TAG, "rotright="+Byte.valueOf(item.getRotright()).toString());
                Log.d(TAG, "time="+Byte.valueOf(item.getTime()).toString());
                
            }
        }
        return motiondata;
    }

}
