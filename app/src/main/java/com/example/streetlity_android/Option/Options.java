package com.example.streetlity_android.Option;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.RealtimeService.LocationListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import okhttp3.ResponseBody;
import retrofit2.Call;
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
                    MyApplication.getInstance().getOption().setAcceptEmergency(isChecked);
                    getSharedPreferences("acceptEmergency", MODE_PRIVATE).edit().putBoolean("acceptEmergency", isChecked).apply();
                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                            .addConverterFactory(GsonConverterFactory.create()).build();
                    final MapAPI tour = retro.create(MapAPI.class);
                    Call<ResponseBody> call;
                    if(isChecked){
                        LocationManager locationManager =  (LocationManager)
                                Options.this.getSystemService(Context.LOCATION_SERVICE);
                        if (ContextCompat.checkSelfPermission(Options.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(Options.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            Location location = locationManager.getLastKnownLocation(locationManager
                                    .NETWORK_PROVIDER);
                            if(location == null){
                                Log.e("", "onMapReady: MULL");
                            }else{
                                call = tour.createEmergency(MyApplication.getInstance().getUsername(), (float)location.getLatitude(), (float)location.getLongitude());
                            }
                        }

                    }else{
                        call = tour.removeEmergency(MyApplication.getInstance().getUsername());
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