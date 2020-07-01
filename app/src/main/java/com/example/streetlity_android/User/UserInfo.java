package com.example.streetlity_android.User;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.example.streetlity_android.Achievement.Achievement;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.Util.ImageFilePath;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streetlity_android.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfo extends AppCompatActivity {

    boolean edtState = false;
    boolean hasImg = false;

    MultipartBody.Part body;
    String fileName = "";
    ImageView imgAvatar;

    String avatar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        EditText edtAddress = findViewById(R.id.edt_address);
        EditText edtPhone = findViewById(R.id.edt_phone);

        edtAddress.setText(MyApplication.getInstance().getAddress());
        edtPhone.setText(MyApplication.getInstance().getPhone());

        EditText tvUsername = findViewById(R.id.edt_username);
        tvUsername.setText((MyApplication.getInstance().getUsername()));

        EditText tvEmail = findViewById(R.id.edt_email);
        tvEmail.setText((MyApplication.getInstance().getEmail()));

        imgAvatar = findViewById(R.id.img_avatar);
        ImageView imgEditable = findViewById(R.id.img_editable);
        LinearLayout preventClick = findViewById(R.id.prevent_click);

        if(MyApplication.getInstance().getImage()!= null){
            imgAvatar.setImageBitmap(MyApplication.getInstance().getImage());
        }

        Button btnAchiviement = findViewById(R.id.btn_to_achievement);
        btnAchiviement.setText(Html.fromHtml(getString(R.string.underline_achievement)));
        btnAchiviement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserInfo.this, Achievement.class));
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);

                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtState){
                    edtState = true;
                    imgEditable.setVisibility(View.VISIBLE);
                    preventClick.setVisibility(View.GONE);
                    fabEdit.setImageResource(R.drawable.checkmark_black);

                    edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);

                    edtPhone.setFocusable(true);
                    edtPhone.setFocusableInTouchMode(true);

                    edtAddress.setInputType(InputType.TYPE_CLASS_TEXT);
                    edtPhone.setInputType(InputType.TYPE_CLASS_NUMBER);

                    edtAddress.setLongClickable(true);
                    edtPhone.setLongClickable(true);

                    edtAddress.requestFocus();

                    InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }else {
                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }

                    edtState = false;
                    imgEditable.setVisibility(View.GONE);
                    preventClick.setVisibility(View.VISIBLE);

                    fabEdit.setImageResource(R.drawable.edit);

                    edtAddress.setFocusable(false);
                    edtAddress.setFocusableInTouchMode(false);

                    edtPhone.setFocusable(false);
                    edtAddress.setFocusableInTouchMode(false);

                    edtAddress.setInputType(InputType.TYPE_NULL);
                    edtPhone.setInputType(InputType.TYPE_NULL);

                    edtAddress.setLongClickable(false);
                    edtPhone.setLongClickable(false);

                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }

                    updateInfo(edtPhone.getText().toString(), edtAddress.getText().toString());
                }
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && data != null) {
                int leftLimit = 48; // letter 'a'
                int rightLimit = 122; // letter 'z'
                int targetStringLength = 10;
                if (data.getData() != null) {

                    String path = ImageFilePath.getPath(UserInfo.this, data.getData());

                    File file = new File(path);

                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                    imgAvatar.setImageBitmap(myBitmap);

                    hasImg = true;

                    String extension = path.substring(path.lastIndexOf("."));

                    String generatedString = MyApplication.getInstance().getUsername()+extension;

                    RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part mBody =
                            MultipartBody.Part.createFormData(generatedString, file.getName(), fbody);

                    body=mBody;

                    fileName = generatedString;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateInfo(String phone, String address){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        final MapAPI tour2 = retro2.create(MapAPI.class);

        String token = MyApplication.getInstance().getToken();

        if(hasImg){

            String[] f = new String[1];
            f[0] = fileName;

            List<MultipartBody.Part> list = new ArrayList<>();
            list.add(body);

            Call<ResponseBody> call2 = tour2.uploadWithType(f, list,1);
            call2.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        final JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "onResponse: " + jsonObject.toString());

                            if (jsonObject.getBoolean("Status")) {
                                JSONObject jsonObject1 = jsonObject.getJSONObject("Paths");
                                    JSONObject jsonObject2 = jsonObject1.getJSONObject(fileName);
                                    avatar = jsonObject2.getString("Message");


                                Call<ResponseBody> call1 = tour.updateInfo(MyApplication.getInstance().getUsername(),"", address, phone, avatar);

                                call1.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200) {
                                            final JSONObject jsonObject;
                                            JSONArray jsonArray;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                Log.e("", "onResponse: " + jsonObject.toString());

                                                Toast toast = Toast.makeText(UserInfo.this, R.string.update_successfully, Toast.LENGTH_LONG);

                                                toast.show();

                                                MyApplication.getInstance().setAddress(address);
                                                MyApplication.getInstance().setPhone(phone);
                                                MyApplication.getInstance().setImage(((BitmapDrawable)imgAvatar.getDrawable()).getBitmap());

                                                SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor e = s.edit();
                                                e.putString("phone", MyApplication.getInstance().getPhone());
                                                e.putString("address", MyApplication.getInstance().getAddress());
                                                e.putString("avatar", avatar);
                                                e.apply();

                                                //finish();
                                            } catch (Exception e){
                                                e.printStackTrace();
                                                Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                        else{
                                            try {
                                                Log.e(", ",""+response.code());
                                                Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Log.e("", "onFailure: " + t.toString());
                                        Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();

                                    }
                                });
                            } else {
                                Toast toast = Toast.makeText(UserInfo.this, R.string.error_upload, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("", "onFailure: " + t.toString());
                    Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                }
            });
        }
        else{
            Call<ResponseBody> call1 = tour.updateInfoWithoutAvatar(MyApplication.getInstance().getUsername(),"", address, phone);

            call1.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.code() == 200) {
                        final JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "onResponse: " + jsonObject.toString());

                            Toast toast = Toast.makeText(UserInfo.this, R.string.update_successfully, Toast.LENGTH_LONG);

                            toast.show();

                            MyApplication.getInstance().setAddress(address);
                            MyApplication.getInstance().setPhone(phone);

                            SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor e = s.edit();
                            e.putString("phone", MyApplication.getInstance().getPhone());
                            e.putString("address", MyApplication.getInstance().getAddress());
                            e.apply();

                            //finish();
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                    else{
                        try {
                            Log.e(", ",response.errorBody().toString());
                            Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast toast = Toast.makeText(UserInfo.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("", "onFailure: " + t.toString());
                }
            });
        }
    }
}
