package edu.uncc.chitchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    String getMessURL = "http://52.90.79.130:8080/Groups/api/get/messages?channel_id=";
    final String postMessURL = "http://52.90.79.130:8080/Groups/api/post/message";
    SharedPreferences shpr = null;
    final OkHttpClient client = new OkHttpClient();
    ArrayList<Message> messages = null;
    String ch_id = null;
    String loginAPI;
    LinearLayout rl = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ch_id = getIntent().getStringExtra("id");
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        loginAPI = shpr.getString("login_api_key","");
        rl = (LinearLayout)findViewById(R.id.chatListView);
        Log.d("demo",loginAPI);
        getMessages();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout1){
            SharedPreferences.Editor editor = shpr.edit();
            editor.clear();
            editor.commit();
            finish();
        }else{
            getMessages();
        }
        return true;
    }


    public void getMessages(){
        getMessURL += ""+ch_id;
        Request request = new Request.Builder()
                .url(getMessURL)
                .header("Authorization", "BEARER "+loginAPI)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code: " + response);
                }

                String data = response.body().string();
                try {
                    JSONObject jb = new JSONObject(data);
                    if (jb.getString("message").contains("Success")) {
                        messages = new ArrayList<Message>();
                        JSONArray jArr = jb.getJSONArray("data");
                        for (int i = 0; i < jArr.length(); i++) {
                            Message m = new Message();
                            JSONObject jObj = jArr.getJSONObject(i);
                            m.setM_id(jObj.getString("message_id"));
                            m.setChannel_id(jObj.getString("channel_id"));
                            JSONObject jObj1 = jObj.getJSONObject("user");
                            m.setfName(jObj1.getString("fname"));
                            m.setlName(jObj1.getString("lname"));
                            m.setTime(jObj.getString("msg_time"));
                            m.setText(jObj.getString("messages_text"));
                            messages.add(m);
                        }
                        ChatActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setData2();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setData2(){
        Iterator<Message> it = messages.iterator();

        while(it.hasNext()){
            Message m = it.next();

            LayoutInflater linf =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = linf.inflate(R.layout.message_layout,rl,false);
            ((TextView)view.findViewById(R.id.chat_name)).setText(""+m.getfName()+" "+m.getlName());
            ((TextView)view.findViewById(R.id.chat_txt)).setText(""+m.getText());
            Date expiry = null;
            try{
                 expiry = new Date(Long.parseLong(m.getTime()));
            }catch (Exception e){
                Log.d("demo","time "+m.getTime());
            }

            PrettyTime p = new PrettyTime();
            if(expiry!=null){
                String time = p.format(expiry);
                ((TextView)view.findViewById(R.id.chat_time)).setText(""+time);
            }

            rl.addView(view);

        }
    }


    public void sendMessage(View v){
        final String msg_txt = ((EditText)findViewById(R.id.editText)).getText().toString();
        if("".equals(msg_txt)){
            Toast.makeText(this, "Please enter a text message to send", Toast.LENGTH_SHORT).show();
            return;
        }
        final String time = ""+System.currentTimeMillis();
        FormBody rbody = new FormBody.Builder()
                .add("msg_text",""+msg_txt)
                .add("msg_time",""+time)
                .add("channel_id",""+ch_id)
                .build();
        Request request = new Request.Builder()
                .url(postMessURL)
                .header("Authorization", "BEARER "+loginAPI)
                .header("Content-Type","application/x-www-form-urlencoded")
                .post(rbody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendMsg(msg_txt,time);
                    }
                });
            }
        });
    }

    public void sendMsg(String txt, String time){
        LayoutInflater linf =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = linf.inflate(R.layout.message_layout,rl,false);

        ((TextView)view.findViewById(R.id.chat_name)).setText("Kalyan Chavali");
        ((TextView)view.findViewById(R.id.chat_txt)).setText(""+txt);
        Date expiry = new Date(Long.parseLong(time));
        PrettyTime p = new PrettyTime();
        String time1 = p.format(expiry);
        ((TextView)view.findViewById(R.id.chat_time)).setText(""+time1);
        rl.addView(view);
    }
}
