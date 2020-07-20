package com.streetlity.client;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MaintainerDirection extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
/*
common user find repairman's location on the map
 */
    GoogleMap mMap;

    Marker currMarker;
    Marker userMaker;

    double currLat;
    double currLon;

    MaintenanceOrder socket;

    boolean initUserDestination = true;

    Information infomation;

    String phone;

    String room;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintainer_direction);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        /*
        open the chat as the default screen
         */
        startActivityForResult(new Intent(MaintainerDirection.this, Chat.class), 1);

        /*
        get map
         */
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton btnCall = findViewById(R.id.btn_call);
        ImageButton btnChat = findViewById(R.id.btn_chat);

        /*
        get the chat room id
         */
        room = getSharedPreferences("Room", MODE_PRIVATE).getString("room", "");

        /*
        go to chat
         */
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MaintainerDirection.this, Chat.class),1);
                findViewById(R.id.img_chat_new).setVisibility(View.GONE);
            }
        });

        /*
        go to phone call screen with repairman's phone number
         */
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+phone));
                startActivity(intent);
            }
        });

        /*
        cancel the order and give reason
         */
        ImageButton btnCancel = findViewById(R.id.btn_cancel_order);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogDecline = new Dialog(MaintainerDirection.this);

                //final LayoutInflater inflater = LayoutInflater.from(MaintainerDirection.this);

                final View dialogView = View.inflate(MaintainerDirection.this ,R.layout.dialog_decline, null);

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
                                            getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    Toast toast = Toast.makeText(MaintainerDirection.this, R.string.something_wrong, Toast.LENGTH_LONG);
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

    /*
    close activity when tap back arrow
     */
    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    /*
    trigger once the map is ready to use
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        request location update
         */
        if (ContextCompat.checkSelfPermission(MaintainerDirection.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MaintainerDirection.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager)
                    MaintainerDirection.this.getSystemService(Context.LOCATION_SERVICE);
//            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            currLat = location.getLatitude();
//            currLon = location.getLongitude();
//
//            MarkerOptions option = new MarkerOptions();
//            option.icon(BitmapDescriptorFactory.fromResource(R.drawable.cursor));
//            option.position(new LatLng(location.getLatitude(),location.getLongitude()));
//            option.rotation(location.getBearing() - 45);
//            currMarker = mMap.addMarker(option);
//
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
        }

        socket = MaintenanceOrder.getInstance();
    }

    /*
    Trigger when the location listener detect location changes (after a MIN_TIME or a MIN_DISTANCE)
    But since the GPS take a while to actually start, the actual time may take longer than the MIN_TIME
     */
    public void onLocationChanged(Location location) {
        if(currMarker!= null) {
            currMarker.remove();
        }
        MarkerOptions currOption = new MarkerOptions();
        currOption.position(new LatLng(location.getLatitude(),location.getLongitude()));
        currOption.title(getString(R.string.you_r_here));
        currOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.cursor));
        currOption.rotation(location.getBearing() - 45);
        currMarker = mMap.addMarker(currOption);

        if(socket != null){
            socket.updateLocation(location.getLatitude(),location.getLongitude(),location.getBearing());
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),15f));
    }

    /*
   Below are methods required to implement the LocationListener
    */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /*
    close socket and remove location update when close this screen
     */
    @Override
    public void onStop(){
        super.onStop();

        locationManager.removeUpdates(this);
        socket.close();
    }

    /*
    when go back from the chat screen for the first time
     */
    @Override
    public void onResume(){
        super.onResume();


        /*
        connect to socket and override the listeners
         */
        socket = MaintenanceOrder.getInstance();

        if(socket == null){
            return;
        }

        final TextView tvPhone = findViewById(R.id.tv_phone);
        final TextView tvName = findViewById(R.id.tv_name);

        /*
        when receive information, set the text view with username and phone of repairman
         */
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

        /*
        if user doesn't provide the phone number, the default phone number when sign up will be use
         */
        String phone2 = "";
        if (getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone2 = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        else if (phone2.equals("")) {
            phone2 = MyApplication.getInstance().getPhone();
        }

        /*
        send this information to the repairman and request the same information
         */
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

        /*
        when receive a location update from the user, if it's the first time, find the direction to the user
        else update the user's marker
         */
        socket.LocationListener = new com.streetlity.client.RealtimeService.LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                if(initUserDestination){
                    initUserDestination = false;
                    MarkerOptions option = new MarkerOptions();
                    option.icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
                    option.position(new LatLng(lat,lon));

                    String serverKey = "AIzaSyB56CeF7ccQ9ZeMn0O4QkwlAQVX7K97-Ss";
                    LatLng origin = new LatLng(currLat, currLon);
                    LatLng destination = new LatLng(lat, lon);
                    GoogleDirection.withServerKey(serverKey)
                            .from(origin)
                            .to(destination)
                            .execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction) {

                                    String status = direction.getStatus();
                                    //Log.e("", "onDirectionSuccess: " + status);
                                    if(status.equals(RequestResult.OK)) {
                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);
                                        ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                        PolylineOptions polylineOptions = DirectionConverter.createPolyline(MaintainerDirection.this, directionPositionList, 5, Color.GREEN);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.addPolyline(polylineOptions);
                                            }
                                        });

                                    } else if(status.equals(RequestResult.NOT_FOUND)) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast toast = Toast.makeText(MaintainerDirection.this, R.string.cant_go, Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    //Log.e("", "onDirectionFailure: ");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(MaintainerDirection.this, R.string.something_wrong_direction, Toast.LENGTH_LONG);
                                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                            tv.setTextColor(Color.RED);

                                            toast.show();
                                        }
                                    });
                                }
                            });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap.addMarker(option);
                        }
                    });
                }else{
                    if(userMaker!=null) {
                        userMaker.remove();
                    }
                    MarkerOptions option = new MarkerOptions();
                    option.icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
                    option.position(new LatLng(lat,lon));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userMaker = mMap.addMarker(option);
                        }
                    });
                }
            }
        };

        /*
        when receive a complete call, show dialog saying this order is completed
         */
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

        /*
        when receive a cancel call, show dialog saying this order is canceled with reason
         */
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

    /*
    close in case order is complete and the user is in chat screen
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            finish();
        }
    }
}
