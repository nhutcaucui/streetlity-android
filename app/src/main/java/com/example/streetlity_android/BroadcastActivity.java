package com.example.streetlity_android;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.example.streetlity_android.Chat.Chat;
import com.example.streetlity_android.User.Login;
import com.example.streetlity_android.User.SignUp;
import com.example.streetlity_android.User.SignupAsMaintainer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BroadcastActivity extends AppCompatActivity {

    float currLat;
    float currLon;

    int broadcastId=-1;

    boolean isOther = false;

    boolean notFound = false;

    int broadcastCount = 0;

    boolean isActive = true;

    boolean stopThread = false;

    CountDownTimer countdown;

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        LocationManager locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        final Dialog dialog;



            dialog = new Dialog(this);

            final View dialogView = View.inflate(this ,R.layout.dialog_instruction_broadcast, null);

            Button btnUnderstand = dialogView.findViewById(R.id.btn_understand);

            btnUnderstand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.hide();
                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

            dialog.setContentView(dialogView);

        if(!getSharedPreferences("first",MODE_PRIVATE).getBoolean("firstBroadcast", false)){
            getSharedPreferences("first",MODE_PRIVATE).edit().putBoolean("firstBroadcast", true).apply();

            dialog.show();
        }

        ImageView imgHelp = findViewById(R.id.img_help);
        imgHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .GPS_PROVIDER);
            if(location != null) {

                currLat = (float) location.getLatitude();
                currLon = (float) location.getLongitude();
            }
        }
        Log.e("", "onMapReady: " + currLat + " , " + currLon);


        final com.google.android.material.textfield.TextInputEditText edtPhone = findViewById(R.id.edt_phone);
        final Spinner spnReason = findViewById(R.id.spn_reason);
        final com.google.android.material.textfield.TextInputEditText edtReason = findViewById(R.id.edt_reason);
        final com.google.android.material.textfield.TextInputEditText edtNote = findViewById(R.id.edt_note);

        edtPhone.setText(MyApplication.getInstance().getPhone());

        ArrayList<String> arrReason = new ArrayList<>();

        arrReason.add(getString(R.string.select_reason_spinner));
        SharedPreferences s = getSharedPreferences("broadcastReason", Context.MODE_PRIVATE);
        if (s.contains("size")) {
            for (int i = 0; i < s.getInt("size", 0); i++) {
                arrReason.add(s.getString("reason" + (i + 1), ""));
            }
        }
        arrReason.add(getString(R.string.other));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item_broadcast, arrReason);

        spnReason.setAdapter(adapter);

        LinearLayout layoutOther = findViewById(R.id.layout_other_reason);

        ImageButton imgAdd = findViewById(R.id.img_add);
        ImageButton imgRemove = findViewById(R.id.img_remove);

        spnReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnReason.getSelectedItem().toString().equals(getString(R.string.other))) {
                    isOther = true;
                    layoutOther.setVisibility(View.VISIBLE);
                } else {
                    isOther = false;
                    layoutOther.setVisibility(View.GONE);
                }

                if (spnReason.getSelectedItemPosition() == 0 || spnReason.getSelectedItemPosition() == arrReason.size() - 1) {
                    imgRemove.setVisibility(View.GONE);
                } else {
                    imgRemove.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnConfirm = findViewById(R.id.btn_broadcast);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (isOther) {
                    if (edtReason.getText().toString().equals("")) {
                        Toast toast = Toast.makeText(BroadcastActivity.this, R.string.please_select_reason, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else {
                        sendBroadcast(edtReason.getText().toString(),
                                edtPhone.getText().toString(), edtNote.getText().toString(), currLat, currLon);
                    }
                } else {
                    if (spnReason.getSelectedItemPosition() == 0) {
                        Toast toast = Toast.makeText(BroadcastActivity.this, R.string.please_select_reason, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else {
                        sendBroadcast(spnReason.getSelectedItem().toString(),
                                edtPhone.getText().toString(), edtNote.getText().toString(), currLat, currLon);
                    }
                }
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtReason.getText().equals("")) {
                    arrReason.set(arrReason.size() - 1, edtReason.getText().toString());
                    arrReason.add(getString(R.string.other));
                    imgRemove.setVisibility(View.VISIBLE);
                    layoutOther.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    SharedPreferences s = getSharedPreferences("broadcastReason", Context.MODE_PRIVATE);
                    SharedPreferences.Editor e = s.edit();
                    e.clear();
                    e.apply();
                    e.putInt("size", arrReason.size() - 2);
                    for (int i = 1; i < arrReason.size() - 1; i++) {
                        e.putString("reason" + i, arrReason.get(i));
                    }
                    e.commit();
                    edtReason.setText("");
                    isOther = false;
                } else {
                    Toast toast = Toast.makeText(BroadcastActivity.this, R.string.empty_reason, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                }
            }
        });

        imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrReason.remove(spnReason.getSelectedItemPosition());
                adapter.notifyDataSetChanged();
                if (spnReason.getSelectedItemPosition() == 0 || spnReason.getSelectedItemPosition() == arrReason.size() - 1) {
                    imgRemove.setVisibility(View.GONE);
                }
                if (spnReason.getSelectedItemPosition() == arrReason.size() - 1) {
                    layoutOther.setVisibility(View.VISIBLE);
                }
                SharedPreferences s = getSharedPreferences("broadcastReason", Context.MODE_PRIVATE);
                SharedPreferences.Editor e = s.edit();
                e.clear();
                e.apply();
                if (arrReason.size() - 2 > 0) {
                    e.putInt("size", arrReason.size() - 2);
                    for (int i = 1; i < arrReason.size() - 1; i++) {
                        e.putString("reason" + i, arrReason.get(i));
                    }
                    e.commit();
                }
            }
        });

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout broadcasting = findViewById(R.id.layout_broadcasting);
                broadcasting.setVisibility(View.GONE);
                TextView tvBroadcastTo = findViewById(R.id.tv_broadcast_to);
                tvBroadcastTo.setText("");
                broadcastCount++;
                if(broadcastCount>=3){

                    setResult(RESULT_OK);
                    finish();
                }

                countdown.cancel();

                stopThread = true;
                //denyOrder();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MaintenanceAcceptNotification")
        );
    }

    public void denyOrder(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.denyOrder(broadcastId, 1, "");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if(jsonObject.getBoolean("Status")){
                            RelativeLayout broadcasting = findViewById(R.id.layout_broadcasting);
                            broadcasting.setVisibility(View.GONE);

                            getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    Toast toast = Toast.makeText(BroadcastActivity.this, R.string.no_available, Toast.LENGTH_LONG);
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

    public void sendBroadcast(final String reason, final String phone, final String note, double lat, double lon) {
        RelativeLayout broadcasting = findViewById(R.id.layout_broadcasting);
        broadcasting.setVisibility(View.VISIBLE);

        Drawable background = broadcasting.getBackground();

        if(Build.VERSION.SDK_INT >= 21 && background instanceof RippleDrawable)
        {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            stopThread = false;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(!stopThread) {
                            Thread.sleep(1000);
                            if (isActive) {
                                isActive = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleDrawable.setState(new int[]{});
                                    }
                                });
                            } else {
                                isActive = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
                                    }
                                });

                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange(MyApplication.getInstance().getVersion(), (float) lat, (float) lon, (float) 0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    final JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        jsonArray = jsonObject.getJSONArray("Services");

                        final ArrayList<Integer> idList = new ArrayList<>();
                        final ArrayList<String> maintenanceList = new ArrayList<>();

                        int count = 0;
                        int range = 1000;

                        int[] id;
                        String[] maintenance;

                        while (count < 3) {
                            count = 0;
                            idList.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                if (distance(lat, lon, jsonObject1.getDouble("Lat"), jsonObject1.getDouble("Lon")) < range
                                        && !jsonObject1.getString("Maintainer").equals("")) {
                                    idList.add(jsonObject1.getInt("Id"));
                                    maintenanceList.add(jsonObject1.getString("Maintainer"));
                                    count++;
                                }
                            }

                            range += 1000;
                            if (range > 10000) {
                                range -= 1000;
                                break;
                            }
                        }

                        id = new int[idList.size()];
                        maintenance = new String[maintenanceList.size()];

                        for (int i = 0; i < idList.size(); i++) {
                            id[i] = idList.get(i);
                            maintenance[i] = maintenanceList.get(i);
                        }

                        final int fRange = range;

                        if (count > 0) {

                            Call<ResponseBody> call2 = tour.broadcast(MyApplication.getInstance().getVersion(), MyApplication.getInstance().getUsername()
                                    , reason, phone, note, maintenance, id, 1);
                            call2.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    JSONObject jsonObject;
                                    if (response.code() == 200) {
                                        Log.e("", "onResponse: " + response.raw().request());
                                        try {
                                            jsonObject = new JSONObject(response.body().string());
                                            Log.e("", "onResponse: " + jsonObject.toString());
                                            if (jsonObject.getBoolean("Status")) {
//                                                Intent data = new Intent();
//                                                data.putExtra("numStore", idList.size());
//                                                data.putExtra("range", fRange);
//                                                setResult(RESULT_OK, data);
//                                                finish();
                                                getSharedPreferences("broadcastPhone", MODE_PRIVATE).edit()
                                                        .putString("phone", phone).apply();

                                                TextView tvBroadcastTo = findViewById(R.id.tv_broadcast_to);

                                                String temp = getString(R.string.contacted);
                                                temp += " " + idList.size();
                                                temp += " " + getString(R.string.nearby_repairmen);

                                                if(fRange>=1000) {
                                                    temp += " " + getString(R.string.in_range) + " " + (fRange / 1000) + "km";
                                                }else{
                                                    temp += " " + getString(R.string.in_range) + " " + (fRange) + "m";
                                                }

                                                tvBroadcastTo.setText(temp);

                                                notFound = true;

                                                countdown = new CountDownTimer(20000, 1000) {

                                                    public void onTick(long millisUntilFinished) {
                                                        if (!notFound) {

                                                            cancel();

                                                        }
                                                    }

                                                    public void onFinish() {
                                                        if(notFound) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    broadcasting.setVisibility(View.GONE);
                                                                    Toast toast = Toast.makeText(BroadcastActivity.this, R.string.no_available, Toast.LENGTH_LONG);
                                                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                                    tv.setTextColor(Color.RED);

                                                                    toast.show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                }.start();

//                                                Thread thread = new Thread(new Runnable () {
//                                                    @Override
//                                                    public void run() {
//
//                                                        for (int i = 300 ; i >= 0; i--) {
//                                                            try{
//                                                                Thread.sleep(1000);
//                                                            }catch (InterruptedException e) {
//                                                                e.printStackTrace();
//                                                            }
//
//                                                        }
//                                                        if(notFound) {
//                                                            runOnUiThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    broadcasting.setVisibility(View.GONE);
//                                                                    Toast toast = Toast.makeText(BroadcastActivity.this, R.string.no_available, Toast.LENGTH_LONG);
//                                                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
//                                                                    tv.setTextColor(Color.RED);
//
//                                                                    toast.show();
//                                                                }
//                                                            });
//                                                        }
//                                                    }
//                                                });
                                                //thread.start();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            ;
                                            Log.e("", "onResponse: " + response.code());
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
                        } else {
                            broadcasting.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(BroadcastActivity.this, R.string.no_available, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.e(", ", response.errorBody().toString() + response.code());
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


    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (earthRadius * c);

        return dist;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void foundAMaintainer(Intent intent){
        notFound = false;

        getSharedPreferences("activeOrder",MODE_PRIVATE).edit().putString("activeOrder", intent.getStringExtra("id")).apply();

        RelativeLayout broadcasting = findViewById(R.id.layout_broadcasting);

        broadcasting.setVisibility(View.GONE);

        RelativeLayout found = findViewById(R.id.layout_found);
        found.setVisibility(View.VISIBLE);

        TextView tvMaintainer= findViewById(R.id.tv_maintainer_name);
        tvMaintainer.setText(intent.getStringExtra("maintenance_user"));

        Button btnGoChat = findViewById(R.id.btn_go_chat);
        btnGoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t1 = new Intent(BroadcastActivity.this, MaintainerLocation.class);
                startActivity(t1);

                SharedPreferences s = getSharedPreferences("Room", MODE_PRIVATE);
                s.edit().putString("room",intent.getStringExtra("id")).apply();

                //startActivity(intent);

                finish();
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           foundAMaintainer(intent);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(countdown != null) {
            countdown.cancel();
        }

        stopThread = true;
    }
}
