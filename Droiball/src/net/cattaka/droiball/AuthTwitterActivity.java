
package net.cattaka.droiball;

import java.io.IOException;

import net.cattaka.droiball.util.MyPreference;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AuthTwitterActivity extends Activity implements View.OnClickListener {
    private AuthTwitterActivity me = this;

    class GetRequestTokenTask extends AsyncTask<Void, Void, RequestToken> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(me);
            mProgressDialog.show();
        }

        @Override
        protected RequestToken doInBackground(Void... params) {
            try {
                RequestToken requestToken = getRequestToken();
                return requestToken;
            } catch (TwitterException e) {
                Log.e("Debug", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(RequestToken result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            if (result != null) {
                mRequestToken = result;
                String uriString = mRequestToken.getAuthenticationURL();
                Uri url = Uri.parse(uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                startActivity(intent);
            }
        }
    }

    class GetAccessTokenTask extends AsyncTask<Void, Void, AccessToken> {
        private ProgressDialog mProgressDialog;

        private String mPin;

        public GetAccessTokenTask(String mPin) {
            super();
            this.mPin = mPin;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(me);
            mProgressDialog.show();
        }

        @Override
        protected AccessToken doInBackground(Void... params) {
            try {
                AccessToken accessToken = getAccessToken(mRequestToken, mPin);
                return accessToken;
            } catch (TwitterException e) {
                Log.e("Debug", e.getMessage(), e);
            } catch (IOException e) {
                Log.e("Debug", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(AccessToken result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            if (result != null) {
                storeAccessToken(result);
                Toast.makeText(AuthTwitterActivity.this, "AccessToken Ready!", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    }

    private RequestToken mRequestToken;

    private MyPreference mMyPreference;

    private EditText mTrackWordsEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_twitter);

        mTrackWordsEdit = (EditText)findViewById(R.id.trackWordsText);

        findViewById(R.id.GetPinButton).setOnClickListener(this);
        findViewById(R.id.AuthenticateButton).setOnClickListener(this);
        findViewById(R.id.saveButton).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMyPreference = new MyPreference(PreferenceManager.getDefaultSharedPreferences(this));

        mTrackWordsEdit.setText(mMyPreference.getTrackWords());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.GetPinButton) {
            GetRequestTokenTask task = new GetRequestTokenTask();
            task.execute();
        } else if (v.getId() == R.id.AuthenticateButton) {
            EditText pinEdit = (EditText)findViewById(R.id.PinEdit);
            String pin = String.valueOf(pinEdit.getText());

            GetAccessTokenTask task = new GetAccessTokenTask(pin);
            task.execute();
        } else if (v.getId() == R.id.saveButton) {
            mMyPreference.edit();
            mMyPreference.putTrackWords(String.valueOf(mTrackWordsEdit.getText()));
            mMyPreference.commit();
            finish();
        }
    }

    private RequestToken getRequestToken() throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Constants.getTwConsumerKey(this),
                Constants.getTwConsumerSecret(this));
        RequestToken requestToken = twitter.getOAuthRequestToken();
        return requestToken;
    }

    private AccessToken getAccessToken(RequestToken requestToken, String pin)
            throws TwitterException, IOException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Constants.getTwConsumerKey(this),
                Constants.getTwConsumerSecret(this));

        AccessToken accessToken = null;
        try {
            if (pin.length() > 0) {
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            } else {
                accessToken = twitter.getOAuthAccessToken();
            }
        } catch (TwitterException te) {
            if (401 == te.getStatusCode()) {
                System.out.println("Unable to get the access token.");
            } else {
                te.printStackTrace();
            }
        }

        return accessToken;
    }

    private void storeAccessToken(AccessToken accessToken) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        MyPreference myPreference = new MyPreference(pref);
        myPreference.edit();
        myPreference.putAccessToken(accessToken.getToken());
        myPreference.putAccessTokenSecret(accessToken.getTokenSecret());
        myPreference.commit();
    }

}
