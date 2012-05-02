package net.cattaka.droidrobo01;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.cattaka.droidrobo01.RobotUtil.MotorState;
import net.cattaka.droidrobo01.robo.RoboPauseInfo;
import net.cattaka.droidrobo01.service.AdkService;
import net.cattaka.droidrobo01.service.IAdkService;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MonitorTwitterActivity extends Activity {
    private static final int EVENT_ADD_MESSAGE = 1;
    private static final int EVENT_DRIVE = 2;
    
    private MonitorTwitterActivity me = this;
    private IAdkService mAdkService;
    private ListView mTweetListView;
    private TwitterStream mTwitterStream;
    private ArmSetting mSetting;
    private Queue<RoboPauseInfo> mPauseQueue = new LinkedList<RoboPauseInfo>();
    
    private RoboPauseInfo ROBO_PAUSE_STOP = new RoboPauseInfo(false, MotorState.NONE, 500, 0.25f, 0.25f);
    
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
            if (msg.what == EVENT_ADD_MESSAGE) {
                if (mTweetListView.getAdapter() instanceof ArrayAdapter<?>) {
                    if (msg.obj instanceof String) {
                        addMessage((String) msg.obj);
                    }
                }
            } else if (msg.what == EVENT_DRIVE) {
                startDriveRobo();
            }
        };
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_twitter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);;
        
        mTweetListView = (ListView) findViewById(R.id.TweetListView);
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
        
        startMonitor();
        startDriveRobo();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopMonitor();
        stopDriveRobo();
    }
    
    private void startMonitor() {
        stopMonitor();
        
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthAccessToken(mSetting.getAccessToken());
        builder.setOAuthAccessTokenSecret(mSetting.getAccessTokenSecret());
        builder.setOAuthConsumerKey(Constants.getTwConsumerKey(this));
        builder.setOAuthConsumerSecret(Constants.getTwConsumerSecret(this));
        mTwitterStream = new TwitterStreamFactory(builder.build()).getInstance();
        
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mTweetListView.setAdapter(adapter);
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                DateFormat df = android.text.format.DateFormat.getTimeFormat(me);
                StringBuffer sb = new StringBuffer();
                sb.append("@" + status.getUser().getScreenName() + " - " + status.getText());
                if (status.getCreatedAt() != null) {
                    sb.append("(");
                    sb.append(df.format(status.getCreatedAt()));
                    sb.append(")");
                }
                addMessageAsync(sb.toString());
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                addMessageAsync("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                addMessageAsync("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                addMessageAsync("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onException(Exception ex) {
                Log.e("Debug", ex.getMessage(), ex);
            }
        };
        mTwitterStream.addListener(listener);
        
        List<Long> follow = new ArrayList<Long>();
        List<String> track = new ArrayList<String>();
        track.add("nyaruko");
        
        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        mTwitterStream.filter(new FilterQuery(0, followArray, trackArray));
        
        //mTwitterStream.sample();
    }
    private void addMessage(String str) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String> )mTweetListView.getAdapter();
        adapter.insert(str, 0);
        if (adapter.getCount() >= 20) {
            while (adapter.getCount() > 10) {
                adapter.remove(adapter.getItem(10));
            }
        }
        
        adapter.notifyDataSetChanged();
    }
    private void addMessageAsync(String str) {
        Message msg = new Message();
        msg.what = EVENT_ADD_MESSAGE;
        msg.obj = str;
        mHandler.sendMessage(msg);
        if (mPauseQueue.size() == 0) {
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 0.25f, 0.25f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorState.NONE, 500, 0.25f, 0.25f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 0.25f, 0.25f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorState.NONE, 500, 0.25f, 0.25f));
            
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 1000, 0.75f, 0.75f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 1.00f, 0.50f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 0.50f, 1.00f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 1.00f, 0.50f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 0.50f, 1.00f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 1.00f, 0.50f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 500, 0.50f, 1.00f));
            mPauseQueue.add(new RoboPauseInfo(false, MotorState.NONE, 500, 0.75f, 0.75f));

            mPauseQueue.add(new RoboPauseInfo(true, MotorState.TURN_LEFT, 200, 0.75f, 0.75f));
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.TURN_RIGHT, 200, 0.75f, 0.75f));
            
            mPauseQueue.add(new RoboPauseInfo(true, MotorState.NONE, 1000, 1.0f, 1.0f));
            
        }
    }
    
    private void stopMonitor() {
        if (mTwitterStream != null) {
            mTwitterStream.shutdown();
            mTwitterStream = null;
        }
    }
    
    private void startDriveRobo() {
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
                    Log.e("Debug", e.getMessage(),e);
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(EVENT_DRIVE, rpInfo.getDucation());
    }
    private void stopDriveRobo() {
        mHandler.removeMessages(EVENT_DRIVE);
    }
}
