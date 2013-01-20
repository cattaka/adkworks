
package net.cattaka.droiball.fragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.cattaka.droiball.Constants;
import net.cattaka.droiball.R;
import net.cattaka.droiball.entity.ActionModel;
import net.cattaka.droiball.task.PlayPoseTask;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.data.PacketWrapper;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

public class MonitorTwitterFragment extends BaseFragment implements View.OnClickListener,
        PlayPoseTask.PlayPoseTaskListener {
    private static final int EVENT_ADD_MESSAGE = 1;

    private static final int EVENT_DRIVE = 2;

    private Random mRandom = new Random();

    private MonitorTwitterFragment me = this;

    private ListView mTweetListView;

    private TwitterStream mTwitterStream;

    private PlayPoseTask mPlayPoseTask;

    private WakeLock mWakelock;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == EVENT_ADD_MESSAGE) {
                if (mTweetListView.getAdapter() instanceof ArrayAdapter<?>) {
                    if (msg.obj instanceof String) {
                        addMessage((String)msg.obj);
                    }
                }
            } else if (msg.what == EVENT_DRIVE) {
                startRandomAction();
            }
        };
    };

    public View onCreateView(android.view.LayoutInflater inflater,
            android.view.ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.monitor_twitter, null);

        view.findViewById(R.id.DanceButton).setOnClickListener(this);

        mTweetListView = (ListView)view.findViewById(R.id.TweetListView);

        mWakelock = ((PowerManager)getActivity().getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, Constants.TAG);
        return view;
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayPoseTask != null) {
            mPlayPoseTask.cancel(false);
            mPlayPoseTask = null;
        }
    }

    @Override
    public void onPageSelected() {
        mWakelock.acquire();
        startMonitor();
    }

    @Override
    public void onPageDeselected() {
        mWakelock.release();
        stopMonitor();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.DanceButton) {
            addMessageAsync(null);
        }
    }

    private void startMonitor() {
        stopMonitor();

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthAccessToken(getMyPreference().getAccessToken());
        builder.setOAuthAccessTokenSecret(getMyPreference().getAccessTokenSecret());
        builder.setOAuthConsumerKey(Constants.getTwConsumerKey(getActivity()));
        builder.setOAuthConsumerSecret(Constants.getTwConsumerSecret(getActivity()));
        mTwitterStream = new TwitterStreamFactory(builder.build()).getInstance();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);
        mTweetListView.setAdapter(adapter);
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                DateFormat df = android.text.format.DateFormat.getTimeFormat(getActivity());
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
                addMessageAsync("Got a status deletion notice id:"
                        + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                addMessageAsync("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                addMessageAsync("Got scrub_geo event userId:" + userId + " upToStatusId:"
                        + upToStatusId);
            }

            public void onException(Exception ex) {
                Log.e("Debug", ex.getMessage(), ex);
            }
        };
        mTwitterStream.addListener(listener);

        List<Long> follow = new ArrayList<Long>();
        List<String> track = new ArrayList<String>();
        {
            String trackWords = getMyPreference().getTrackWords();
            if (trackWords == null || trackWords.length() == 0) {
                trackWords = "droidrobo";
            }
            String[] tracks = trackWords.split("\\s+");
            for (String str : tracks) {
                track.add(str);
            }
        }

        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        mTwitterStream.filter(new FilterQuery(0, followArray, trackArray));

        // mTwitterStream.sample();
    }

    private void addMessage(String str) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)mTweetListView.getAdapter();
        adapter.insert(str, 0);
        if (adapter.getCount() >= 20) {
            // コメントが溜まり過ぎてたら消す
            while (adapter.getCount() > 10) {
                adapter.remove(adapter.getItem(10));
            }
        }

        startRandomAction();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onReceivePacket(PacketWrapper packetWrapper) {
        // none
    }

    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        // none
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // none
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // none
    }

    private void startRandomAction() {
        if (isEnableAction() && mPlayPoseTask == null) {
            int n = mRandom.nextInt(Constants.ACTION_NAMES_RANDOM.length);
            String actionName = Constants.ACTION_NAMES_RANDOM[n];
            ActionModel actionModel = getDroiballDatabase().findActionModel(actionName, true);

            if (actionModel != null) {
                mPlayPoseTask = new PlayPoseTask(getAppStub(), me);
                mPlayPoseTask.execute(actionModel);
            }
        } else {
            // ignore
        }
    }

    @Override
    public void onPlayPoseTaskFinish() {
        mPlayPoseTask = null;
    }

    private boolean isEnableAction() {
        ToggleButton button = (ToggleButton)getView().findViewById(R.id.enableActionToggle);
        return button.isChecked();
    }

    private void addMessageAsync(String str) {
        if (str != null) {
            Message msg = new Message();
            msg.what = EVENT_ADD_MESSAGE;
            msg.obj = str;
            mHandler.sendMessage(msg);
        }
    }

    private void stopMonitor() {
        if (mTwitterStream != null) {
            mTwitterStream.shutdown();
            mTwitterStream = null;
        }
    }

}
