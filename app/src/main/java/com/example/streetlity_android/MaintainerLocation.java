package com.example.streetlity_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.streetlity_android.Chat.Chat;
import com.example.streetlity_android.RealtimeService.LocationListener;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.example.streetlity_android.User.SignUp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MaintainerLocation extends AppCompatActivity implements OnMapReadyCallback, android.location.LocationListener {

    GoogleMap mMap;
    
    MaintenanceOrder socket;

    double currLat;

    double currLon;

    Marker currMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintainer_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton btnCall = findViewById(R.id.btn_call);
        ImageButton btnChat = findViewById(R.id.btn_chat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MaintainerLocation.this, Chat.class));
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0123456789"));
                startActivity(intent);
            }
        });

        ImageButton btnCancel = findViewById(R.id.btn_cancel_order);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogDecline = new Dialog(MaintainerLocation.this);

                final LayoutInflater inflater = LayoutInflater.from(MaintainerLocation.this);

                final View dialogView = View.inflate(MaintainerLocation.this ,R.layout.dialog_decline, null);

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
                        Call<ResponseBody> call = tour.denyOrder(0, 2, edtReason.getText().toString());
                        //need Id
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    final JSONObject jsonObject;
                                    try {
                                        jsonObject = new JSONObject(response.body().string());
                                        Log.e("", "onResponse: " + jsonObject.toString());
                                        if(jsonObject.getBoolean("Status")){

                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    Toast toast = Toast.makeText(MaintainerLocation.this, R.string.something_wrong, Toast.LENGTH_LONG);
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

        ImageButton imgComplete = findViewById(R.id.btn_done);
        imgComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MaintainerLocation.this).create();
                alertDialog.setTitle(getString(R.string.remove)+"?");
                alertDialog.setMessage(getString(R.string.remove_pic));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        socket = MaintenanceOrder.getInstance();

        socket.LocationListener = new com.example.streetlity_android.RealtimeService.LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                if(currMarker != null){
                    currMarker.remove();
                }

                MarkerOptions options = new MarkerOptions();
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.cursor));
                options.position(new LatLng(lat,lon));

                currMarker = mMap.addMarker(options);
            }
        };

        if (ContextCompat.checkSelfPermission(MaintainerLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MaintainerLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager)
                    MaintainerLocation.this.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
        }

    }

    public void onLocationChanged(Location location) {
        if(currMarker!= null) {
            currMarker.remove();
        }
        MarkerOptions currOption = new MarkerOptions();
        currOption.position(new LatLng(location.getLatitude(),location.getLongitude()));
        currOption.title(getString(R.string.you_r_here));
        currOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
        currMarker = mMap.addMarker(currOption);

        if(socket != null){
            socket.updateLocation(location.getLatitude(),location.getLongitude(),location.getBearing());
        }
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

    public  void onResume(){
        super.onResume();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onStop(){
        super.onStop();

        socket.close();
    }
}
