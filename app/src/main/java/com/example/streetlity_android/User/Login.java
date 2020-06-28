package com.example.streetlity_android.User;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import com.example.streetlity_android.AboutUs;
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
import com.example.streetlity_android.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.json.JSONObject;

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
        Retrofit retro = new Retrofit.Builder().baseUrl("http://35.240.232.218/")
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

                                if(!jsonObject1.getString("Avatar").equals("")){
                                    getAvatar(jsonObject1.getString("Avatar"));
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


                                setResult(RESULT_OK);
                                finish();
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
        Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getDriverURL())
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

