
package net.cattaka.android.humitemp4ble.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.cattaka.android.humitemp4ble.Constants;
import net.cattaka.android.humitemp4ble.data.RegisterResultInfo;
import net.cattaka.android.humitemp4ble.data.RegisterResultInfoGen;
import net.vvakame.util.jsonpullparser.JsonFormatException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class RegisterTask extends AsyncTask<Void, Void, RegisterResultInfo> {
    private IRegisterTaskListener mListener;

    private String mUsername;

    public interface IRegisterTaskListener {
        public void onRegisterTaskStart();

        public void onRegisterTaskFinish(RegisterResultInfo info);
    }

    public RegisterTask(IRegisterTaskListener listener, String username) {
        super();
        mListener = listener;
        mUsername = username;
    }

    @Override
    protected RegisterResultInfo doInBackground(Void... params) {
        HttpEntity entity;
        try {
            JSONObject obj = new JSONObject();
            obj.put("operation", "registerUser");
            obj.put("username", mUsername);
            String json = obj.toString();
            entity = new StringEntity(json, "UTF-8");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("json", json));
            entity = new UrlEncodedFormEntity(nameValuePairs);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        } catch (JSONException e) {
            throw new RuntimeException();
        }

        DefaultHttpClient client = new DefaultHttpClient();
        new DefaultHttpClient();
        client.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                boolean isRedirect = false;
                isRedirect = super.isRedirectRequested(response, context);
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == 301 || responseCode == 302) {
                        return true;
                    }
                }
                return isRedirect;
            }
        });
        HttpPost post = new HttpPost(Constants.BASE_URL);
        post.setEntity(entity);

        RegisterResultInfo info = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            HttpResponse response = client.execute(post);
            response.getEntity().writeTo(bout);
            String contents = new String(bout.toByteArray());
            if (response.getStatusLine().getStatusCode() == 200) {
                info = RegisterResultInfoGen.get(contents);
            }
        } catch (JsonFormatException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        return info;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onRegisterTaskStart();
    }

    @Override
    protected void onPostExecute(RegisterResultInfo result) {
        super.onPostExecute(result);
        mListener.onRegisterTaskFinish(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mListener.onRegisterTaskFinish(null);
    }
}
