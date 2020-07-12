package com.example.streetlity_android.User;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import com.example.streetlity_android.AboutUs;
import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.MapAPI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.Option.MaintainerOption;
import com.example.streetlity_android.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        final EditText edtUser = findViewById(R.id.edt_username);
        final TextInputEditText edtPass = findViewById(R.id.edt_password);

        layout = findViewById(R.id.layout_cant_find_loca);


        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUser.getText().toString().equals("")) {
                    Toast toast = Toast.makeText(Login.this, R.string.empty_user_name, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                } else if (edtPass.getText().toString().equals("")) {
                    Toast toast = Toast.makeText(Login.this, R.string.empty_pass, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                } else {
                    login(edtUser.getText().toString(),edtPass.getText().toString());
                }

                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        Button btnSignup = findViewById(R.id.btn_to_signup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                Intent t = new Intent(Login.this, SignUp.class);
                t.putExtra("from", 2);
                startActivity(t);
            }
        });

        Button btnForgot = findViewById(R.id.btn_to_forgot);
        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                Intent t = new Intent(Login.this, ForgotPassword.class);
                startActivityForResult(t, 2);
            }
        });

        btnSignup.setText(Html.fromHtml(getString(R.string.underline_signup)));
        btnForgot.setText(Html.fromHtml(getString(R.string.underline_forgotpass)));

        ImageButton imgAbout = findViewById(R.id.img_about);
        imgAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, AboutUs.class));
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void login(final String username, String password){
        layout.setVisibility(View.VISIBLE);
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.login(username, password, ((MyApplication) getApplication()).getDeviceToken());
        Log.e("", "login: " + ((MyApplication) getApplication()).getDeviceToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject);
                        if(jsonObject.getBoolean("Status")) {

                            ((MyApplication) Login.this.getApplication()).setToken(jsonObject.getString("AccessToken"));
                            //((MyApplication) Login.this.getApplication()).setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTQ0NDUzMzIsImlkIjoibXJYWVpaemFidiJ9.0Hd4SpIELulSuTxGAeuCPl_A33X-KoPUpRmgK4dTphk");
                            ((MyApplication) Login.this.getApplication()).setRefreshToken(jsonObject.getString("RefreshToken"));
                            ((MyApplication) Login.this.getApplication()).setUsername(username);

                            JSONObject jsonObject1 = jsonObject.getJSONObject("User");
                            jsonObject1 = jsonObject1.getJSONObject("Info");

                            ((MyApplication) Login.this.getApplication()).setEmail(jsonObject1.getString("Email"));
                            ((MyApplication) Login.this.getApplication()).setPhone(jsonObject1.getString("Phone"));
                            ((MyApplication) Login.this.getApplication()).setAddress(jsonObject1.getString("Address"));

                            jsonObject = jsonObject.getJSONObject("User");
                            if (jsonObject.getInt("Role") != -1) {
                                ((MyApplication) Login.this.getApplication()).setUserType(jsonObject.getInt("Role"));


                                SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
                                SharedPreferences.Editor e = s.edit();
                                e.clear();
                                e.apply();
                                e.putString("username", username);
                                e.putString("token", ((MyApplication) Login.this.getApplication()).getToken());
                                e.putString("refreshToken", ((MyApplication) Login.this.getApplication()).getRefreshToken());
                                e.putString("email", ((MyApplication) Login.this.getApplication()).getEmail());
                                e.putString("phone", ((MyApplication) Login.this.getApplication()).getPhone());
                                e.putString("address", ((MyApplication) Login.this.getApplication()).getAddress());
                                e.putInt("userType", jsonObject.getInt("Role"));
                                e.putString("avatar", jsonObject1.getString("Avatar"));
                                e.commit();

                                if(MyApplication.getInstance().getUserType() == 7){
                                    if(getSharedPreferences("acceptEmergency", MODE_PRIVATE).contains("acceptEmergency")){
                                        MyApplication.getInstance().setOption(new MaintainerOption(getSharedPreferences("acceptEmergency", MODE_PRIVATE).getBoolean("acceptEmergency", false)));
                                    }
                                    else if(MyApplication.getInstance().getOption() == null){
                                        MyApplication.getInstance().setOption(new MaintainerOption());
                                        MyApplication.getInstance().getOption().setAcceptEmergency(false);
                                    }
                                }

                                if(!jsonObject1.getString("Avatar").equals("")){
                                    getAvatar(jsonObject1.getString("Avatar"));
                                }else{
                                    Log.e("TAG", "onResponse: no avatar" );
                                }

//                                Call<ResponseBody> call2 = tour.addDevice("1.0.0", ((MyApplication) Login.this.getApplication()).getToken(),
//                                        username);
//                                call2.enqueue(new Callback<ResponseBody>() {
//                                    @Override
//                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                        if (response.code() == 200) {
//                                            try {
//                                                JSONObject jsonObject1 = new JSONObject(response.body().string());
//                                                if (jsonObject1.getBoolean("Status")) {
//                                                    Log.e("", "onResponse: " + jsonObject1.toString());
//                                                } else {
//                                                    Log.e("", "onResponse: " + jsonObject1.toString());
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        } else {
//                                            Log.e("", "onResponse: " + response.code());
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                                    }
//                                });

                                Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro2.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.getProgress(username);

                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200){
                                            try{
                                                JSONObject jsonObject2 = new JSONObject(response.body().string());
                                                Log.e("TAG", "onResponse: " +jsonObject2.toString() );
                                                if(jsonObject2.getBoolean("Status")){
                                                    Map<String, Map<String, ActionObject>> upvoteMap = new HashMap<>();
                                                    Map<String, Map<String, ActionObject>> downvoteMap = new HashMap<>();
                                                    Map<String, Map<String, ActionObject>> reviewMap = new HashMap<>();
                                                    Map<String, Map<String, ActionObject>> contributeMap = new HashMap<>();

                                                    if(!jsonObject2.getString("Progress").equals("")) {
                                                        jsonObject2 = new JSONObject(jsonObject2.getString("Progress"));

                                                        JSONObject jsonObject3;
                                                        JSONObject jsonObject4;

                                                        if (!jsonObject2.toString().equals("")) {

                                                            if (jsonObject2.has("Downvoted")) {
                                                                jsonObject3 = jsonObject2.getJSONObject("Downvoted");
                                                                jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Maintenance", insideMap);
                                                                }


                                                                jsonObject4 = jsonObject3.getJSONObject("Atm");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Atm", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Fuel", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Toilet", insideMap);
                                                                }
                                                            }

                                                            if (jsonObject2.has("Upvoted")) {
                                                                jsonObject3 = jsonObject2.getJSONObject("Upvoted");
                                                                jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Maintenance", insideMap);
                                                                }


                                                                jsonObject4 = jsonObject3.getJSONObject("Atm");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Atm", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Fuel", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    upvoteMap.put("Toilet", insideMap);
                                                                }
                                                            }

                                                            if (jsonObject2.has("Reviewed")) {

                                                                jsonObject3 = jsonObject2.getJSONObject("Reviewed");
                                                                jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    reviewMap.put("Maintenance", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Atm");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    reviewMap.put("Atm", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    reviewMap.put("Fuel", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    reviewMap.put("Toilet", insideMap);
                                                                }
                                                            }

                                                            if (jsonObject2.has("Contributed")) {
                                                                jsonObject3 = jsonObject2.getJSONObject("Contributed");
                                                                jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    contributeMap.put("Maintenance", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Atm");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    contributeMap.put("Atm", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    contributeMap.put("Fuel", insideMap);
                                                                }

                                                                jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                                                if (jsonObject4.length() > 0) {
                                                                    Iterator<String> keys = jsonObject4.keys();
                                                                    Map<String, ActionObject> insideMap = new HashMap<>();
                                                                    while (keys.hasNext()) {
                                                                        String key = keys.next();
                                                                        JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                                        ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                                                jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                                        insideMap.put(key, ao);
                                                                    }
                                                                    contributeMap.put("Toilet", insideMap);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    MyApplication.getInstance().setContributeMap(contributeMap);
                                                    MyApplication.getInstance().setReviewedMap(reviewMap);
                                                    MyApplication.getInstance().setUpvoteMap(upvoteMap);
                                                    MyApplication.getInstance().setDownvoteMap(downvoteMap);

                                                    Log.e("TAG", "onResponse: "+contributeMap );
                                                    Log.e("TAG", "onResponse: "+reviewMap );
                                                    Log.e("TAG", "onResponse: "+upvoteMap );
                                                    Log.e("TAG", "onResponse: "+downvoteMap );

                                                    Gson gs = new Gson();

                                                    SharedPreferences s = getSharedPreferences("map", MODE_PRIVATE);
                                                    SharedPreferences.Editor e = s.edit();

                                                    e.putString("review", gs.toJson(reviewMap));
                                                    e.putString("contribute", gs.toJson(contributeMap));
                                                    e.putString("upvote", gs.toJson(upvoteMap));
                                                    e.putString("downvote", gs.toJson(downvoteMap));

                                                    e.apply();

                                                    setResult(RESULT_OK);
                                                    finish();
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                        t.printStackTrace();
                                    }
                                });
                            } else {
                                layout.setVisibility(View.GONE);
                                Toast toast = Toast.makeText(Login.this, R.string.user_wait_approval, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }
                        }
                        else{
                            layout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(Login.this, R.string.user_not_exist, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }


                } else {
                    try {
                        Log.e("", "onResponse: " + response.code());
                        Toast toast = Toast.makeText(Login.this, R.string.user_not_exist, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("", "onFailure: " + t.toString());
            }
        });
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public  void getAvatar(String avatar){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.download(avatar);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        MyApplication.getInstance().setImage(bmp);
                        Log.e("TAG", "onResponse: getAvatar" );
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

}

