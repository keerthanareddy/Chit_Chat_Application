package edu.uncc.chitchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    final OkHttpClient client = new OkHttpClient();
    final String loginAPIURL = "http://52.90.79.130:8080/Groups/api/login";
    final String signUpURL = "http://52.90.79.130:8080/Groups/api/signUp";
    SharedPreferences shpr = null;
    SharedPreferences.Editor editor = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shpr =  getSharedPreferences("login",MODE_PRIVATE);
        setContentView(R.layout.activity_login);
        boolean flag = false;
        String loginAPI = shpr.getString("login_api_key","");
        if(loginAPI!=null) {
            if ("".equals(loginAPI)) {
                flag = true;
            }
        }else{
            flag = true;
        }
        if(!flag){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void login(View v){


        String email = ((EditText)findViewById(R.id.loginEmail)).getText().toString();
        if ("".equals(email)) {
            Toast.makeText(this, "Please enter email for login", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = ((EditText)findViewById(R.id.loginPass)).getText().toString();
        if ("".equals(password)) {
            Toast.makeText(this, "Please enter Password for login", Toast.LENGTH_SHORT).show();
            return;
        }

        FormBody rbody = new FormBody.Builder()
                .add("email",""+email)
                .add("password",""+password).build();

        Request request = new Request.Builder()
                .url(loginAPIURL)
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

                String message = "";
                String api_key = "";
                String data = response.body().string();
                try {
                    JSONObject jb = new JSONObject(data);
                    api_key = jb.getString("data");
                    message = jb.getString("message");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(message.contains("Success")){
                    editor = shpr.edit();
                    editor.putString("login_api_key",api_key);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });

    }
    public void signUp(View v){
        Log.d("demo","button clicked");
        String email = ((EditText)findViewById(R.id.signUpEmail)).getText().toString();
        if ("".equals(email)) {
            Toast.makeText(this, "Please enter email for login", Toast.LENGTH_SHORT).show();
            return;
        }
        String fname = ((EditText)findViewById(R.id.signUpFName)).getText().toString();
        if ("".equals(fname)) {
            Toast.makeText(this, "Please enter email for login", Toast.LENGTH_SHORT).show();
            return;
        }
        String lname = ((EditText)findViewById(R.id.signUpLName)).getText().toString();
        if ("".equals(lname)) {
            Toast.makeText(this, "Please enter email for login", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = ((EditText)findViewById(R.id.signPass)).getText().toString();
        if ("".equals(password)) {
            Toast.makeText(this, "Please enter Password for login", Toast.LENGTH_SHORT).show();
            return;
        }

        FormBody rbody = new FormBody.Builder()
                .add("email",""+email)
                .add("password",""+password)
                .add("fname",""+fname)
                .add("lname",""+lname)
                .build();

        Request request = new Request.Builder()
                .url(signUpURL)
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
                String message = "";
                String api_key = "";
                String data = response.body().string();
                try {
                    JSONObject jb = new JSONObject(data);
                    api_key = jb.getString("data");
                    message = jb.getString("message");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(message.contains("Success")){
                    editor = shpr.edit();
                    editor.putString("login_api_key",api_key);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
