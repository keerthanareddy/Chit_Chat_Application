package edu.uncc.chitchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChannelsActivity extends AppCompatActivity {
    final String channelURL = "http://52.90.79.130:8080/Groups/api/get/channels";
    ArrayList<Channel> channels = null;
    SharedPreferences shpr = null;
    final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        String loginAPI = shpr.getString("login_api_key","");
        Log.d("demo","token "+loginAPI);
        Request request = new Request.Builder()
                .url(channelURL)
                .header("Authorization", "BEARER "+loginAPI)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()){
                    throw new IOException("Unexpected code: "+response);
                }

                Log.d("demo","inside response");

                String data = response.body().string();
                try {
                    JSONObject jb = new JSONObject(data);
                    if(jb.getString("message").contains("Success")){
                        channels = new ArrayList<Channel>();
                        JSONArray jArr = jb.getJSONArray("data");
                        for (int i =0; i<jArr.length();i++){
                            Channel ch = new Channel();
                            JSONObject jObj = jArr.getJSONObject(i);

                            ch.setId(jObj.getString("channel_id"));
                            ch.setName(jObj.getString("channel_name"));
                            channels.add(ch);
                        }
                        Log.d("demo","parsing complete"+channels.size());
                        ChannelsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setData1();
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }


    public void setData1(){
        if(channels!=null){
            ListView lv = (ListView)ChannelsActivity.this.findViewById(R.id.chnList);
            ChannelAdapter cAda = new ChannelAdapter(ChannelsActivity.this, R.layout.channel_item,channels, "c");
            lv.setAdapter(cAda);
        }

    }

    public void done(View v){
        finish();
    }

    public void goToChat(String id){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id",id);
        startActivity(intent);
    }
}
