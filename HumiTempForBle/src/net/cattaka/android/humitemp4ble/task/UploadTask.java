
package net.cattaka.android.humitemp4ble.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cattaka.android.humitemp4ble.Constants;
import net.cattaka.android.humitemp4ble.data.HttpResultInfo;
import net.cattaka.android.humitemp4ble.data.HttpResultInfoGen;
import net.cattaka.android.humitemp4ble.data.UserInfo;
import net.cattaka.android.humitemp4ble.entity.HumiTempModel;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class UploadTask extends AsyncTask<HumiTempModel, Void, HttpResultInfo> {
    private IUploadTaskListener mListener;

    private UserInfo mUserInfo;

    public interface IUploadTaskListener {
        public void onUploadTaskStart();

        public void onUploadTaskFinish(HttpResultInfo info);
    }

    public UploadTask(IUploadTaskListener listener, UserInfo userInfo) {
        super();
        mListener = listener;
        mUserInfo = userInfo;
    }

    @Override
    protected HttpResultInfo doInBackground(HumiTempModel... params) {
        Map<Long, List<HumiTempModel>> modelsMap = new HashMap<Long, List<HumiTempModel>>();
        List<Long> results = new ArrayList<Long>();
        for (HumiTempModel model : params) {
            if (model.getId() != null) {
                results.add(model.getId());
            }

            List<HumiTempModel> models = modelsMap.get(model.getDeviceId());
            if (models == null) {
                models = new ArrayList<HumiTempModel>();
                modelsMap.put(model.getDeviceId(), models);
            }
            models.add(model);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HttpEntity entity;
        try {
            JSONObject obj = new JSONObject();
            obj.put("operation", "registerData");
            obj.put("username", mUserInfo.getUsername());
            obj.put("token", mUserInfo.getToken());
            JSONObject values = new JSONObject();
            for (Map.Entry<Long, List<HumiTempModel>> entry : modelsMap.entrySet()) {
                Long deviceId = entry.getKey();
                JSONArray items = new JSONArray();
                for (HumiTempModel model : entry.getValue()) {
                    JSONArray item = new JSONArray();
                    if (model.getDate() != null && model.getTemperature() != null
                            && model.getHumidity() != null) {
                        item.put(df.format(model.getDate()));
                        item.put(model.getTemperature());
                        item.put(model.getHumidity());
                    }
                    items.put(item);
                }
                values.put(String.valueOf(deviceId), items);
            }
            obj.put("values", values);
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

        HttpResultInfo info = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            HttpResponse response = client.execute(post);
            response.getEntity().writeTo(bout);
            String contents = new String(bout.toByteArray());
            if (response.getStatusLine().getStatusCode() == 200) {
                info = HttpResultInfoGen.get(contents);
            }
        } catch (JsonFormatException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        if (info != null) {
            info.setExtra(results);
        }
        return info;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onUploadTaskStart();
    }

    @Override
    protected void onPostExecute(HttpResultInfo result) {
        super.onPostExecute(result);
        mListener.onUploadTaskFinish(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mListener.onUploadTaskFinish(null);
    }
}
