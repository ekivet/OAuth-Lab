package com.example.erickivet.oauthlab;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    ListView mListView;
    Button mButton;
    EditText mEditText;
    private static final String TAG= "Main Activity";
    private static String mToken;
    //public String screenname;

    final ArrayList <String> tweets = new ArrayList<>();
    ArrayAdapter <String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listview0);
        mEditText = (EditText) findViewById(R.id.edittext0);
        mButton = (Button) findViewById(R.id.button0);


        byte[] concatArray = (TwitterAccess.CONSUMER_KEY + ":" + TwitterAccess.CONSUMER_SECRET)
                .getBytes();
        TwitterAccess.KEY_BASE64 = Base64.encodeToString(concatArray, Base64.DEFAULT);

        mButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (mEditText.toString().length() > 0){
                    tweets.clear();
                    getJson(getAccessToken(), mEditText.getText().toString());
                }
            }
        });

        adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,tweets);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();



    }



    private String getAccessToken(){
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_id",TwitterAccess.CONSUMER_KEY)
                .add("client_secret",TwitterAccess.CONSUMER_SECRET)
                .add("redirect_uri",TwitterAccess.CALLBACK_URL)
                .add("Authorization",TwitterAccess.KEY_BASE64)
                .build();

        final Request request = new Request.Builder()
                .url("https://api.twitter.com/oauth2/token")
                .addHeader("Authorization", TwitterAccess.KEY_BASE64)
                .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"onFailure: request failed",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                }

                String responseString = response.body().string();
                Log.i(TAG, "onResponse: " + responseString);

                try {
                    JSONObject object = new JSONObject(responseString);
                    mToken = object.getString("access_token");
                    Log.i(TAG, "onResponse: access token " + mToken);

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        });
        return mToken;


    }

    public void getJson(String token, String screenname) {

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("screen_name", screenname)
                .add("count", "30")
                .build();

        Request request = new Request.Builder()
                .url("https://api.twitter.com/1.1/statuses/user_timeline.json")
                .addHeader("Authorization", token)
                .put(formBody)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getJson.onFailure: request failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected code " + response);
                }
                String responseString = response.body().string();
                Log.i(TAG, "getJson.onResponse: " + responseString);

                try{
                    JSONArray array = new JSONArray(responseString);
                    for (int i = 0; i < array.length(); i++){
                        JSONObject tweet = array.getJSONObject(i);
                        tweets.add(tweet.getString("text"));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }


        });

    }


}
