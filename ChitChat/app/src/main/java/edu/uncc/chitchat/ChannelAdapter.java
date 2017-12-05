package edu.uncc.chitchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kalyan on 3/27/2017.
 */

public class ChannelAdapter extends ArrayAdapter<Channel> {

    ArrayList<Channel> channels;
    Context context;
    int mresource;
    final OkHttpClient client = new OkHttpClient();
    MainActivity activity = null;
    ChannelsActivity cactivity = null;
    boolean setFlag = false;
    String c = null;
    final String subchanURL = "http://52.90.79.130:8080/Groups/api/subscribe/channel";
    public ChannelAdapter(Context context, int resource, ArrayList<Channel> objects, String c) {
        super(context, resource, objects);
        this.channels = objects;
        this.context = context;
        this.mresource = resource;
        this.c = c;
        if("c".equals(c)){
            this.cactivity = (ChannelsActivity)context;
        }else{
            this.activity = (MainActivity)context;
        }

    }

    public View getView(final int position, View convertView, ViewGroup parent){
        final SharedPreferences shpr = getContext().getSharedPreferences("login",getContext().MODE_PRIVATE);
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(mresource,parent,false);
        }

        final Channel ch = channels.get(position);
        ((TextView)convertView.findViewById(R.id.channelName)).setText(ch.getName());
        final Button btn = (Button)convertView.findViewById(R.id.channelView);
        if("m".equals(c)){
            btn.setText("View");
        }
        String txt = btn.getText().toString();
        if("View".equalsIgnoreCase(txt)){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = ch.getId();
                    if(activity!=null){
                        activity.goToChat(id);
                    }else if(cactivity!=null){
                        cactivity.goToChat(id);
                    }
                }
            });
        }else{
            final String loginAPI = shpr.getString("login_api_key","");

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FormBody rbody = new FormBody.Builder()
                            .add("channel_id",""+ch.getId())
                            .build();

                    Request request = new Request.Builder()
                            .url(subchanURL)
                            .header("Authorization", "BEARER "+loginAPI)
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .post(rbody)
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
                                    setFlag =true;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            if(setFlag){
                btn.setText("View");
            }

        }
        return convertView;
    }



}
