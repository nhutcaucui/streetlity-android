package com.example.streetlity_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.streetlity_android.User.SignupAsMaintainer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .NETWORK_PROVIDER);
            currLat = (float) location.getLatitude();
            currLon = (float) location.getLongitude();
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
                //denyOrder();
            }
        });
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
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange("1.0.0", (float) lat, (float) lon, (float) 0.1);
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
                                        && !jsonObject1.getString("Owner").equals("")) {
                                    idList.add(jsonObject1.getInt("Id"));
                                    maintenanceList.add(jsonObject1.getString("Owner"));
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

                            Call<ResponseBody> call2 = tour.broadcast("1.0.0", MyApplication.getInstance().getUsername()
                                    , reason, phone, note, maintenance, id);
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
}
