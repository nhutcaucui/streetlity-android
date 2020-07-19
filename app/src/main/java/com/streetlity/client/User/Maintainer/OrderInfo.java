package com.streetlity.client.User.Maintainer;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.streetlity.client.MaintainerDirection;
import com.streetlity.client.MapAPI;
import com.streetlity.client.MyApplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.streetlity.client.R;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NormalOrderObject item = new NormalOrderObject();
        int from = getIntent().getIntExtra("from",0);

        getSupportActionBar().setTitle("");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        if(isTaskRoot()){
//            Intent t = new Intent(this, MainNavigationHolder.class);
//            t.putExtra("isRoot", 1);
//            startActivity(t);
//            t = new Intent(this, Works.class);
//            t.putExtra("isRoot", 1);
//            startActivity(t);
//        }

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvPhone = findViewById(R.id.tv_phone);
        TextView tvReason = findViewById(R.id.tv_reason);
        TextView tvNote = findViewById(R.id.tv_note);

//        tvName.setText(item.getName());
//        tvPhone.setText(item.getPhone());
//        tvNote.setText(item.getNote());
//        tvReason.setText(item.getReason());

        Button btnGoto = findViewById(R.id.btn_goto);
        Button btnAccept = findViewById(R.id.btn_accept);
        Button btnDecline = findViewById(R.id.btn_decline);

        LinearLayout layoutGo = findViewById(R.id.layout_goto);
        LinearLayout layoutAccept = findViewById(R.id.layout_accept_decline);

        try {
            if (from == 0) {
                item = new NormalOrderObject(Integer.parseInt(getIntent().getStringExtra("id")), getIntent().getStringExtra("common_user"),
                        getIntent().getStringExtra("reason"));
                item.setNote(getIntent().getStringExtra("note"));
               // item.setPhone(getIntent().getStringExtra("phone"));
                tvName.setText(item.getName());
                //tvPhone.setText(item.getPhone());
                tvPhone.setText("");
                tvNote.setText(item.getNote());
                tvReason.setText(item.getReason());
                //Log.e("", "onCreate: " + item.getName() +item.getReason()+item.getId() );
            }else if(from == 1){
                item  = (NormalOrderObject) getIntent().getSerializableExtra("item");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(from == 2){
            item  = (NormalOrderObject) getIntent().getSerializableExtra("item");
            layoutGo.setVisibility(View.VISIBLE);
            btnGoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            layoutAccept.setVisibility(View.GONE);
        }
        else if (from == 1 || from == 0){
            //item  = (NormalOrderObject) getIntent().getSerializableExtra("item");

            final NormalOrderObject item1 = item;

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                            .addConverterFactory(GsonConverterFactory.create()).build();
                    final MapAPI tour = retro.create(MapAPI.class);
                    Call<ResponseBody> call = tour.acceptOrder(MyApplication.getInstance().getUsername(), item1.getId());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.e("", "onResponse: "+ response.code());
                            final JSONObject jsonObject;
                            if(response.code()==200) {
                                try {
                                    jsonObject = new JSONObject(response.body().string());
                                    //Log.e("", "onResponse: " + jsonObject.toString());
                                    if (jsonObject.getBoolean("Status")) {
                                        finish();
                                        startActivity(new Intent(OrderInfo.this, MaintainerDirection.class));
                                        //startActivity(new Intent( OrderInfo.this, Chat.class));
                                        SharedPreferences s = getSharedPreferences("Room", MODE_PRIVATE);
                                        s.edit().putString("room",Integer.toString(item1.getId())).apply();

                                        getSharedPreferences("activeOrder",MODE_PRIVATE).edit().putString("activeOrder", Integer.toString(item1.getId())).apply();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Toast toast = Toast.makeText(OrderInfo.this, R.string.something_wrong, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //Log.e("", "onFailure: " +t );
                        }
                    });
                }
            });

            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialogDecline = new Dialog(OrderInfo.this);

                    final LayoutInflater inflater = LayoutInflater.from(OrderInfo.this);

                    final View dialogView = View.inflate(OrderInfo.this ,R.layout.dialog_decline, null);

                    EditText edtReason = dialogView.findViewById(R.id.edt_reason);

                    Button btnConfirm = dialogView.findViewById(R.id.btn_decline);

                    Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogDecline.cancel();
                        }
                    });

                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                                    .addConverterFactory(GsonConverterFactory.create()).build();
                            final MapAPI tour = retro.create(MapAPI.class);
                            Call<ResponseBody> call = tour.denyOrder(item1.getId(), 2, edtReason.getText().toString());
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        final JSONObject jsonObject;
                                        try {
                                            jsonObject = new JSONObject(response.body().string());
                                            //Log.e("", "onResponse: " + jsonObject.toString());
                                            if(jsonObject.getBoolean("Status")){

                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                    else{
                                        Toast toast = Toast.makeText(OrderInfo.this, R.string.something_wrong, Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
                        }
                    });

                    dialogDecline.setContentView(dialogView);

                    dialogDecline.show();
                }
            });
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
