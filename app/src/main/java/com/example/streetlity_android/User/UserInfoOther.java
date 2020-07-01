package com.example.streetlity_android.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.streetlity_android.Achievement.Achievement;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfoOther extends AppCompatActivity {

    boolean edtState = false;
    boolean hasImg = false;

    MultipartBody.Part body;
    String fileName = "";
    ImageView imgAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        TextInputEditText edtAddress = findViewById(R.id.edt_address);
        TextInputEditText edtPhone = findViewById(R.id.edt_phone);
        TextInputEditText tvUsername = findViewById(R.id.edt_username);
        TextInputEditText tvEmail = findViewById(R.id.edt_email);

        imgAvatar = findViewById(R.id.img_avatar);

        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);

        fabEdit.hide();
        getInfo(tvUsername, tvEmail, edtPhone, edtAddress);

        Button btnAchiviement = findViewById(R.id.btn_to_achievement);
        btnAchiviement.setText(Html.fromHtml(getString(R.string.underline_achievement)));
        btnAchiviement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserInfoOther.this, Achievement.class));
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void getInfo(TextInputEditText username, TextInputEditText mail, TextInputEditText phone,
                        TextInputEditText address){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();

        final MapAPI tour = retro.create(MapAPI.class);

        Call<ResponseBody> call = tour.userInfo(getIntent().getStringExtra("user"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                final JSONObject jsonObject;
                if(response.code() == 200) {
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        JSONObject jsonObject1 = jsonObject.getJSONObject("Info");
                        username.setText(jsonObject1.getString("Id"));
                        mail.setText(jsonObject1.getString("Email"));
                        phone.setText(jsonObject1.getString("Phone"));
                        address.setText(jsonObject1.getString("Address"));
                        if(!jsonObject1.getString("Avatar").equals("")){

                        }

                    }catch (Exception e){
                    e.printStackTrace();
                    }
                }else{
                    Log.e("", "onResponse: "+ response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
