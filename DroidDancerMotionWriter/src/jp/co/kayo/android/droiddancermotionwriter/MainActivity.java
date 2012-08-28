
package jp.co.kayo.android.droiddancermotionwriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.kayo.android.droiddancermotionwriter.MotionItem.MotorDir;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "DroidDancerMotionWriter";

    private TextView textView1;

    private SeekBar seekBar1;

    private ListView listView1;

    private MotionListAdapter adapter;

    private Button button1;

    private List<MotionItem> items = new ArrayList<MotionItem>();

    private NfcAdapter nfcadapter;

    private IntentFilter[] nfcfilters = new IntentFilter[] {
        new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
    };

    private String[][] nfctechLists = new String[][] {
            new String[] {
                Ndef.class.getName()
            }, new String[] {
                NdefFormatable.class.getName()
            }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcadapter = NfcAdapter.getDefaultAdapter(this);

        listView1 = (ListView)findViewById(R.id.listView1);
        textView1 = (TextView)findViewById(R.id.textView1);
        seekBar1 = (SeekBar)findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView1.setText(String.format("%2d", progress));

            }
        });
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MotionItem item = new MotionItem();
                item.setLed(false);
                item.setArmleft((byte)0);
                item.setArmright((byte)0);
                item.setRotleft(MotorDir.STOP);
                item.setRotright(MotorDir.STOP);
                item.setTime((byte)1);
                items.add(item);
                adapter.setData(items);
                adapter.notifyDataSetChanged();
            }
        });

        adapter = new MotionListAdapter(this);
        listView1.setAdapter(adapter);
        listView1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                MotionItem item = adapter.getItem(arg2);

                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("uid", item.getUid());
                intent.putExtra("led", item.isLed());
                intent.putExtra("armleft", item.getArmleft());
                intent.putExtra("armright", item.getArmright());
                intent.putExtra("rotleft", item.getRotleft());
                intent.putExtra("rotright", item.getRotright());
                intent.putExtra("time", item.getTime());

                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100 && data != null) {
            long uid = data.getLongExtra("uid", -1);
            if (uid != -1) {
                for (MotionItem item : items) {
                    if (item.getUid() == uid) {
                        item.setLed(data.getBooleanExtra("led", false));
                        item.setArmleft(data.getIntExtra("armleft", 0));
                        item.setArmright(data.getIntExtra("armright", 0));
                        item.setRotleft(MotorDir.parse(data.getIntExtra("rotleft", 0)));
                        item.setRotright(MotorDir.parse(data.getIntExtra("rotright", 0)));
                        item.setTime(data.getIntExtra("time", 0));
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeNdefMotionTag(tag);
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

    private void writeNdefMotionTag(Tag tag) {
        try {
            if (Arrays.asList(tag.getTechList()).contains(NdefFormatable.class.getName())) {
                NdefFormatable ndef = NdefFormatable.get(tag);
                try {
                    if (!ndef.isConnected()) {
                        ndef.connect();
                    }
                    ndef.format(createNdefMessage());
                    Toast.makeText(this, "Write Success.", Toast.LENGTH_SHORT).show();
                } finally {
                    ndef.close();
                }
            } else if (Arrays.asList(tag.getTechList()).contains(Ndef.class.getName())) {
                Ndef ndef = Ndef.get(tag);
                try {
                    if (!ndef.isConnected()) {
                        ndef.connect();
                    }
                    if (ndef.isWritable()) {
                        ndef.writeNdefMessage(createNdefMessage());
                        Toast.makeText(this, "Write Success.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Writing is not supported", Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    ndef.close();
                }
            }

        } catch (FormatException e) {
            Log.e(TAG, "FormatException", e);
            Toast.makeText(this, "Writing Error", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            Toast.makeText(this, "Writing Error", Toast.LENGTH_SHORT).show();
        }

    }

    private NdefMessage createNdefMessage() throws IOException {
        byte[] mimeBytes = "application/jp.co.kayo.android.droiddancermotionwriter"
                .getBytes(Charset.forName("US-ASCII"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] header = new byte[2];
        header[0] = (byte)(seekBar1.getProgress());
        header[1] = (byte)(items.size());
        bytes.write(header);
        for (MotionItem item : items) {
            byte[] data = new byte[6];
            data[0] = (byte)(item.isLed() ? 1 : 0);
            data[1] = (byte)(item.getArmleft());
            data[2] = (byte)(item.getArmright());
            data[3] = (byte)(item.getRotleft().getIntValue());
            data[4] = (byte)(item.getRotright().getIntValue());
            data[5] = (byte)(item.getTime());
            bytes.write(data);
        }

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0],
                bytes.toByteArray());

        return new NdefMessage(new NdefRecord[] {
            record
        });
    }

}
