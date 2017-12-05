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

public class MainActivity extends AppCompatActivity {
    final String subURl = "http://52.90.79.130:8080/Groups/api/get/subscriptions";
    final OkHttpClient client = new OkHttpClient();
    SharedPreferences shpr = null;
    ArrayList<Channel> channels = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        String loginAPI = shpr.getString("login_api_key","");
        Request request = new Request.Builder()
                .url(subURl)
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

                String data = response.body().string();
                try {
                    JSONObject jb = new JSONObject(data);
                    if(jb.getString("message").contains("Success")){
                        channels = new ArrayList<Channel>();
                        JSONArray jArr = jb.getJSONArray("data");
                        for (int i =0; i<jArr.length();i++){
                            Channel ch = new Channel();
                            JSONObject jObj = jArr.getJSONObject(i);
                            JSONObject jObj1 = jObj.getJSONObject("channel");
                            ch.setId(jObj1.getString("channel_id"));
                            ch.setName(jObj1.getString("channel_name"));
                            channels.add(ch);
                        }
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setData();
                            }
                        });


                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    public void setData(){
        ListView lv = (ListView)findViewById(R.id.mainList);
        ChannelAdapter cAda = new ChannelAdapter(MainActivity.this, R.layout.channel_item,channels,"m");
        lv.setAdapter(cAda);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout){
            SharedPreferences.Editor editor = shpr.edit();
            editor.clear();
            editor.commit();
        }
        return true;
    }

    public void goToChat(String id){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id",id);
        startActivity(intent);
    }

    public void addMore(View view){
        Intent intent = new Intent(this,ChannelsActivity.class);
        startActivity(intent);
    }

}
