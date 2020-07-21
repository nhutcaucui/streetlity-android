package com.streetlity.client.Option;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.streetlity.client.MapAPI;
import com.streetlity.client.MyApplication;
import com.streetlity.client.R;
import com.streetlity.client.UpdateLocationService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        if(MyApplication.getInstance().getUserType() == 7) {
            Switch switchEmergency = findViewById(R.id.emergency_switch);
            switchEmergency.setChecked(MyApplication.getInstance().getOption().isAcceptEmergency());
            switchEmergency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    boolean isNull = false;

                    findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
                    MyApplication.getInstance().getOption().setAcceptEmergency(isChecked);


                    getSharedPreferences("acceptEmergency", MODE_PRIVATE).edit().putBoolean("acceptEmergency", isChecked).apply();
                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                            .addConverterFactory(GsonConverterFactory.create()).build();
                    final MapAPI tour = retro.create(MapAPI.class);
                    Call<ResponseBody> call = tour.removeEmergency(MyApplication.getInstance().getUsername());
                    if (isChecked) {
                        LocationManager locationManager = (LocationManager)
                                Options.this.getSystemService(Context.LOCATION_SERVICE);
                        if (ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            Location location = locationManager.getLastKnownLocation(locationManager
                                    .GPS_PROVIDER);
                            if (location == null) {
                                final Call<ResponseBody> call2 = tour.createEmergency(MyApplication.getInstance().getUsername(), (float) location.getLatitude(), (float) location.getLongitude());
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        call2.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (response.code() == 200) {
                                                    try {
                                                        Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
                                                        findViewById(R.id.layout_loading).setVisibility(View.GONE);
                                                        if (isChecked) {
//                                                            MyApplication.getInstance().setThread();
//                                                            MyApplication.getInstance().getThread().scheduleAtFixedRate(new TimerTask() {
//                                                                @Override
//                                                                public void run() {
//                                                                    LocationManager locationManager = (LocationManager)
//                                                                            getSystemService(Context.LOCATION_SERVICE);
//
//                                                                    if (ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                                                                            ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                                                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
//                                                                            @Override
//                                                                            public void onLocationChanged(Location location) {
//                                                                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
//                                                                                        .addConverterFactory(GsonConverterFactory.create()).build();
//                                                                                final MapAPI tour = retro.create(MapAPI.class);
//                                                                                Call<ResponseBody> call2 = tour.updateLocation(MyApplication.getInstance().getUsername(),
//                                                                                        (float)location.getLatitude(), (float)location.getLongitude());
//                                                                                call2.enqueue(new Callback<ResponseBody>() {
//                                                                                    @Override
//                                                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                                                                        if (response.code() == 200) {
//                                                                                            try {
//                                                                                                Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
//                                                                                            }catch (Exception e){
//                                                                                                e.printStackTrace();}
//                                                                                        }else{
//                                                                                            Log.e("tag", "onResponse: " + response.code());
//                                                                                        }
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                                                                                    }
//                                                                                });
//                                                                                locationManager.removeUpdates(this);
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onProviderEnabled(String provider) {
//
//                                                                            }
//
//                                                                            @Override
//                                                                            public void onProviderDisabled(String provider) {
//
//                                                                            }
//                                                                        });
//                                                                    }
//
//                                                                }
//                                                            }, 5000, 600000);
//                                                            MyApplication.getInstance().setThread();
////                                                            MyApplication.getInstance().getThread().scheduleAtFixedRate(new TimerTask() {
////                                                                @Override
////                                                                public void run() {
////                                                                    LocationManager locationManager = (LocationManager)
////                                                                            getSystemService(Context.LOCATION_SERVICE);
////
////                                                                    if (ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
////                                                                            ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
////                                                                        Location location = locationManager.getLastKnownLocation(locationManager
////                                                                                .GPS_PROVIDER);
////                                                                        if(location != null){
////                                                                            Call<ResponseBody> call2 = tour.updateLocation(MyApplication.getInstance().getUsername(),
////                                                                                    (float)location.getLatitude(), (float)location.getLongitude());
////                                                                            call2.enqueue(new Callback<ResponseBody>() {
////                                                                                @Override
////                                                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
////                                                                                    if (response.code() == 200) {
////                                                                                        try {
////                                                                                            Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
////                                                                                        }catch (Exception e){
////                                                                                            e.printStackTrace();}
////                                                                                    }else{
////                                                                                        Log.e("tag", "onResponse: " + response.code());
////                                                                                    }
////                                                                                }
////
////                                                                                @Override
////                                                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
////
////                                                                                }
////                                                                            });
////                                                                        }
////                                                                    }
////
////                                                                }
////                                                            }, 5000, 600000);
                                                            Intent t= new Intent(Options.this, UpdateLocationService.class);
                                                            t.setAction("start");
                                                            startService(t);
                                                        }
                                                        else{
                                                            Intent t= new Intent(Options.this, UpdateLocationService.class);
                                                            t.setAction("stop");
                                                            startService(t);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    Log.e("tag", "onResponse: " + response.code());
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                                            }
                                        });
                                        locationManager.removeUpdates(this);
                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {

                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {

                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {

                                    }
                                });
                                isNull = true;
                            } else {
                                call = tour.createEmergency(MyApplication.getInstance().getUsername(), (float) location.getLatitude(), (float) location.getLongitude());
                            }
                        }

                    } else {
                        call = tour.removeEmergency(MyApplication.getInstance().getUsername());
                    }

                    if (!isNull) {
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    try {
                                        Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
                                        findViewById(R.id.layout_loading).setVisibility(View.GONE);
                                        if (isChecked) {
//                                            MyApplication.getInstance().setThread();
//                                            MyApplication.getInstance().getThread().scheduleAtFixedRate(new TimerTask() {
//                                                @Override
//                                                public void run() {
//                                                    LocationManager locationManager = (LocationManager)
//                                                            getSystemService(Context.LOCATION_SERVICE);
//
//                                                    if (ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                                                            ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                                                        Location location = locationManager.getLastKnownLocation(locationManager
//                                                                .GPS_PROVIDER);
//                                                        if(location != null){
//                                                            Call<ResponseBody> call2 = tour.updateLocation(MyApplication.getInstance().getUsername(),
//                                                                    (float)location.getLatitude(), (float)location.getLongitude());
//                                                            call2.enqueue(new Callback<ResponseBody>() {
//                                                                @Override
//                                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                                                    if (response.code() == 200) {
//                                                                        try {
//                                                                            Log.e("tag", "onResponse: " + new JSONObject(response.body().string()));
//                                                                        }catch (Exception e){
//                                                                        e.printStackTrace();}
//                                                                    }else{
//                                                                        Log.e("tag", "onResponse: " + response.code());
//                                                                    }
//                                                                }
//
//                                                                @Override
//                                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                                                                }
//                                                            });
//                                                        }
//                                                    }
//
//                                                }
//                                            }, 5000, 600000);
                                            Intent t= new Intent(Options.this, UpdateLocationService.class);
                                            t.setAction("start");
                                            startService(t);
                                        }else{
                                            Intent t= new Intent(Options.this, UpdateLocationService.class);
                                            t.setAction("stop");
                                            startService(t);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.e("tag", "onResponse: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
                    }
                }
            });
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}