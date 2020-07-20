package com.streetlity.client;

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

import com.streetlity.client.Chat.Chat;
import com.streetlity.client.Chat.ChatObject;
import com.streetlity.client.RealtimeService.Information;
import com.streetlity.client.RealtimeService.InformationListener;
import com.streetlity.client.RealtimeService.Listener;
import com.streetlity.client.RealtimeService.MaintenanceOrder;
import com.streetlity.client.RealtimeService.MessageListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MaintainerLocation extends AppCompatActivity implements OnMapReadyCallback, android.location.LocationListener {
    /*
    Same as repairman direction but focus more on showing where the repair man is to the user
     */
    GoogleMap mMap;
    
    MaintenanceOrder socket;

    double currLat;

    double currLon;

    Marker currMarker;

    boolean firstMove = false;

    String phone;

    Information infomation;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintainer_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        startActivityForResult(new Intent(MaintainerLocation.this, Chat.class), 1);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton btnCall = findViewById(R.id.btn_call);
        ImageButton btnChat = findViewById(R.id.btn_chat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MaintainerLocation.this, Chat.class), 1);
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
                        socket.decline(edtReason.getText().toString());
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
                                        //Log.e("", "onResponse: " + jsonObject.toString());
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
                alertDialog.setTitle(getString(R.string.mark_complete)+"?");
//                alertDialog.setMessage(getString(R.string.mark_complete));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                socket.complete();
                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call = tour.completeOrder(Integer.parseInt(getSharedPreferences("activeOrder",MODE_PRIVATE)
                                        .getString("activeOrder","")));
                                //need Id
                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.code() == 200) {
                                            final JSONObject jsonObject;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                //Log.e("", "onResponse: " + jsonObject.toString());
                                                if(jsonObject.getBoolean("Status")){
                                                    getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                                                    finish();
                                                    dialog.dismiss();
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

        if (ContextCompat.checkSelfPermission(MaintainerLocation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MaintainerLocation.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager)
                    MaintainerLocation.this.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
        }

        socket = MaintenanceOrder.getInstance();

        if(socket == null){
            return;
        }

        socket.LocationListener = new com.streetlity.client.RealtimeService.LocationListener<MaintenanceOrder>() {
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


        String phone2 = "";
        if (getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone2 = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        if (phone2.equals("")) {
            phone2 = MyApplication.getInstance().getPhone();
        }

        final TextView tvPhone = findViewById(R.id.tv_phone);
        final TextView tvName = findViewById(R.id.tv_name);

        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infomation = info;
                        //Log.e("", "onReceived: " + info.toString());
                        phone = info.Phone;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPhone.setText(info.Phone);
                                tvName.setText(info.Username);
                            }
                        });
                    }
                });

            }
        };

        Information myInfo = new Information(MyApplication.getInstance().getUsername(), phone2);
        socket.sendInformation(myInfo);
        socket.pullInformation();
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

        if(!firstMove){
            firstMove = false;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
        }

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


    public void onStop(){
        super.onStop();

        locationManager.removeUpdates(this);
        socket.close();
    }

    @Override
    public void onResume(){
        super.onResume();


        socket = MaintenanceOrder.getInstance();
        if(socket == null){
            return;
        }
        //socket.join();
        final TextView tvPhone = findViewById(R.id.tv_phone);
        final TextView tvName = findViewById(R.id.tv_name);
        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infomation = info;
                        //Log.e("", "onReceived: " + info.toString());
                        phone = info.Phone;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvPhone.setText(info.Phone);
                                tvName.setText(info.Username);
                            }
                        });
                    }
                });

            }
        };

        String phone2 = "";
        if (getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone2 = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        if (phone2.equals("")) {
            phone2 = MyApplication.getInstance().getPhone();
        }
        Information myInfo = new Information(MyApplication.getInstance().getUsername(), phone2);
        socket.sendInformation(myInfo);
        socket.pullInformation();

        socket.MessageListener = new MessageListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, ChatObject message) {
                //Log.e("", "onReceived:  this is america");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.img_chat_new).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.LocationListener = new com.streetlity.client.RealtimeService.LocationListener<MaintenanceOrder>() {
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

        socket.CompleteListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.layout_complete).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.DeclineListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                findViewById(R.id.btn_finish_denu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.layout_denied).setVisibility(View.VISIBLE);
                    }
                });
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            finish();
        }
    }
}
