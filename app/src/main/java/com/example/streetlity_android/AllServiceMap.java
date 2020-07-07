package com.example.streetlity_android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.streetlity_android.MainFragment.BankObject;
import com.example.streetlity_android.MainFragment.BankObjectAdapter;
import com.example.streetlity_android.MainFragment.MapObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.streetlity_android.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AllServiceMap extends AppCompatActivity implements GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback, LocationListener {

    GoogleMap mMap;

    Map<String, Marker> mMarkers = new HashMap<>();

    ArrayList<BankObject> arrBank = new ArrayList<BankObject>();

    ArrayList<MapObject> atmArray = new ArrayList<>();
    ArrayList<MapObject> maintenanceArray = new ArrayList<>();
    ArrayList<MapObject> fuelArray = new ArrayList<>();
    ArrayList<MapObject> wcArray = new ArrayList<>();

    ArrayList<Review> reviewItems = new ArrayList<>();
    ArrayList<Review> displayReviewItems = new ArrayList<>();

    ReviewAdapter adapter;

    Marker currentPosition;

    double latitude;
    double longitude;

    boolean isExpanded = false;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_service_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        //ArrayList<MarkerOptions> markList = new ArrayList<MarkerOptions>();

        locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .NETWORK_PROVIDER);
            if(location == null){
                Log.e("", "onMapReady: MULL");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
            }else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.e("", "onMapReady: " + latitude + " , " + longitude);

                adapter = new ReviewAdapter(this, R.layout.review_item, displayReviewItems);

                getBank();

                callMaintenance(latitude, longitude);

                callFuel(latitude, longitude);

                callWC(latitude, longitude);

                MarkerOptions curPositionMark = new MarkerOptions();
                curPositionMark.position(new LatLng(latitude,longitude));
                curPositionMark.title(getString(R.string.you_r_here));

                currentPosition = mMap.addMarker(curPositionMark);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
            }
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        if(!marker.equals(currentPosition)) {

            //final LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());

            final View dialogView = View.inflate(this,R.layout.dialog_point_info, null);

            TextView tvNote = dialogView.findViewById(R.id.tv_note);
            TextView tvName = dialogView.findViewById(R.id.tv_name);
            TextView tvAddress = dialogView.findViewById(R.id.tv_address);
            TextView tvDistance = dialogView.findViewById(R.id.tv_distance);
            RatingBar rb = dialogView.findViewById(R.id.ratingbar_map_object);
            TextView tvRating =dialogView.findViewById(R.id.tv_rating);
            LinearLayout imgHolder = dialogView.findViewById(R.id.img_holder);
            TextView tvNoImg = dialogView.findViewById(R.id.tv_no_img);
            ListView lv = dialogView.findViewById(R.id.lv_review);
            Button btnShowHide = dialogView.findViewById(R.id.btn_show_hide);
            ProgressBar loadingImg = dialogView.findViewById(R.id.loading_img);
            ProgressBar loadingReview = dialogView.findViewById(R.id.loading_review);
            TextView tvNoReview = dialogView.findViewById(R.id.tv_no_review);

            final BottomSheetDialog dialog = new BottomSheetDialog(this, android.R.style.Theme_Black_NoTitleBar);
            //dialog.getWindow().setWindowAnimations(R.style.PauseDialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
            dialog.setContentView(dialogView);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            for (String key : mMarkers.keySet()){
                if(mMarkers.get(key).equals(marker)){
                    Log.e("", "onMarkerClick: found it");
                        String[] split = key.split(";");
                        MapObject item= new MapObject();
                        if(split[0].equals("1")){
                            for(int i = 0; i<atmArray.size();i++){
                                if(atmArray.get(i).getId() == Integer.parseInt(split[1])){
                                    item = atmArray.get(i);
                                    break;
                                }
                            }
                        }
                    else if(split[0].equals("3")){
                        for(int i = 0; i<fuelArray.size();i++){
                            if(fuelArray.get(i).getId() == Integer.parseInt(split[1])){
                                item = fuelArray.get(i);
                                break;
                            }
                        }
                    }
                    else if(split[0].equals("4")){
                        for(int i = 0; i<wcArray.size();i++){
                            if(wcArray.get(i).getId() == Integer.parseInt(split[1])){
                                item = wcArray.get(i);
                                break;
                            }
                        }
                    }
                    else if(split[0].equals("2")){
                        for(int i = 0; i<maintenanceArray.size();i++){
                            if(maintenanceArray.get(i).getId() == Integer.parseInt(split[1])){
                                item = maintenanceArray.get(i);
                                break;
                            }
                        }
                    }
                    if(null != item){
                        lv.setAdapter(adapter);
                        tvNote.setText(item.getNote());
                        tvName.setText(item.getName());
                        tvAddress.setText(item.getAddress());
                        float distance = item.getDistance();
                        String dis = "m";
                        if(distance > 1000){
                            dis = "km";
                            distance = distance / 1000;
                        }
                        DecimalFormat df = new DecimalFormat("#.#");
                        tvDistance.setText("~" + df.format(distance) + dis);
                        loadReviews(item, rb, tvRating,btnShowHide, loadingReview,tvNoReview);
                        addImages(imgHolder, item, tvNoImg, loadingImg);
                    }
                    break;
                }
            }

            dialog.show();
        }

        marker.showInfoWindow();

        return true;
    }

    public void addATMMarkerToList(float lat, float lon, String type , int id){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(type);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));
        option.position(pos);
        Marker marker = mMap.addMarker(option);
        mMarkers.put("1;"+id,marker);
    }

    public void addMaintenanceMarker(float lat, float lon, String name , int id){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(name);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        option.position(pos);
        Marker marker = mMap.addMarker(option);
        mMarkers.put("2;"+id,marker);
    }

    public void addFuelMarker(float lat, float lon, int id){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(getString(R.string.fuel));
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fuel));
        option.position(pos);
        Marker marker = mMap.addMarker(option);
        mMarkers.put("3;"+id,marker);
    }

    public void addWCMarkerTo(float lat, float lon, int id){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(getString(R.string.wc));
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wc));
        option.position(pos);
        Marker marker = mMap.addMarker(option);
        mMarkers.put("4;"+id,marker);
    }

    public void callATM(double lat, double lon){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getATMInRange("1.0.0",(float)lat,(float)lon,(float)0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        jsonArray = jsonObject.getJSONArray("Services");

                        for (int i = 0; i< jsonArray.length();i++){

                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String bankName="";
                            for (int j=0;j<arrBank.size();j++){
                                if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                    bankName = arrBank.get(j).getName();
                                }
                            }

                            Log.e("", "onResponse: " + jsonObject1.toString());
                            addATMMarkerToList((float)jsonObject1.getDouble("Lat"),
                                    (float)jsonObject1.getDouble("Lon"),bankName, jsonObject1.getInt("Id"));

                            MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 3,
                                    jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                    (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                            item.setBankId(jsonObject1.getInt("BankId"));

                            item.setImages(jsonObject1.getString("Images"));

                            float distance = distance(item.getLat(), item.getLon(), (float)latitude, (float)longitude);

                            item.setDistance(distance);

                            atmArray.add(item);
                        }


//                        for (int i = 0; i < mMarkers.size(); i++){
//                            Log.e("", mMarkers.get(i).getTitle());
//                            mMap.addMarker(mMarkers.get(i));
//                        }
                    } catch (Exception e){
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

    public void callMaintenance(double lat, double lon){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange("1.0.0",(float)lat,(float)lon,(float)0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                            jsonArray = jsonObject.getJSONArray("Services");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                Log.e("", "onResponse: " + jsonObject1.toString());
                                MapObject item = new MapObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name"), 3,
                                        jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                        (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 3);

                                float distance = distance(item.getLat(), item.getLon(), (float)latitude, (float)longitude);

                                item.setImages(jsonObject1.getString("Images"));

                                item.setDistance(distance);
                                maintenanceArray.add(item);

                                addMaintenanceMarker((float)jsonObject1.getDouble("Lat"),
                                        (float)jsonObject1.getDouble("Lon"),item.getName(), jsonObject1.getInt("Id"));
                            }
                        }
//                        for (int i = 0; i < mMarkers.size(); i++){
//                            Log.e("", mMarkers.get(i).getTitle());
//                            mMap.addMarker(mMarkers.get(i));
//                        }
                    } catch (Exception e){
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

    public void callFuel(double lat, double lon){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getFuelInRange("1.0.0",(float)lat,(float)lon,(float)0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                            jsonArray = jsonObject.getJSONArray("Services");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                Log.e("", "onResponse: " + jsonObject1.toString());
                                Log.e("", "onResponse: " + jsonObject1.getInt("Id"));
                                MapObject item = new MapObject(jsonObject1.getInt("Id"), getString(R.string.fuel), 3,
                                        jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                        (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 1);

                                float distance = distance(item.getLat(), item.getLon(), (float)latitude, (float)longitude);

                                item.setImages(jsonObject1.getString("Images"));

                                item.setDistance(distance);
                                fuelArray.add(item);

                                addFuelMarker((float)jsonObject1.getDouble("Lat"),
                                        (float)jsonObject1.getDouble("Lon"), jsonObject1.getInt("Id"));
                            }
                        }
//                        for (int i = 0; i < mMarkers.size(); i++){
//                            Log.e("", mMarkers.get(i).getTitle());
//                            mMap.addMarker(mMarkers.get(i));
//                        }
                    } catch (Exception e){
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

    public void callWC(double lat, double lon){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getWCInRange("1.0.0",(float)lat,(float)lon,(float)0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                            jsonArray = jsonObject.getJSONArray("Services");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                Log.e("", "onResponse: " + jsonObject1.toString());
                                Log.e("", "onResponse: " + jsonObject1.getInt("Id"));
                                MapObject item = new MapObject(jsonObject1.getInt("Id"), getString(R.string.fuel), 3,
                                        jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                        (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 2);

                                float distance = distance(item.getLat(), item.getLon(), (float)latitude, (float)longitude);

                                item.setImages(jsonObject1.getString("Images"));

                                item.setDistance(distance);
                                wcArray.add(item);

                                addWCMarkerTo((float)jsonObject1.getDouble("Lat"),
                                        (float)jsonObject1.getDouble("Lon"), jsonObject1.getInt("Id"));
                            }
                        }
//                        for (int i = 0; i < mMarkers.size(); i++){
//                            Log.e("", mMarkers.get(i).getTitle());
//                            mMap.addMarker(mMarkers.get(i));
//                        }
                    } catch (Exception e){
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

    public static float distance(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    public void getBank(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

        String token = MyApplication.getInstance().getToken();

        Call<ResponseBody> call = tour.getBank("1.0.0",token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());

                        if(jsonObject.getBoolean("Status")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("Banks");
                            arrBank.add(new BankObject(0,getString(R.string.all)));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                arrBank.add(new BankObject(jsonObject1.getInt("Id"),jsonObject1.getString("Name")));
                                Log.e("", "onResponse: "+ jsonObject1.getString("Name") + getString(R.string.all) );
                            }

                            callATM(latitude, longitude);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        Log.e(", ",response.errorBody().toString() + response.code());
                    }catch (Exception e){
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

    public void loadReviews(MapObject item, final RatingBar rb, final TextView tvRating, Button btnShowHide,
                            ProgressBar loading, TextView noReview){
        loading.setVisibility(View.VISIBLE);
        reviewItems.clear();
        displayReviewItems.clear();
        // MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getFuelReview("1.0.0", item.getId(), 0,-1);
        switch (item.getType()){
            case 1:{
                call = tour.getFuelReview("1.0.0", item.getId(), 0,-1);
                break;
            }
            case 2:{
                call = tour.getWCReview("1.0.0", item.getId(), 0,-1);
                break;
            }
            case 3:{
                call = tour.getMaintenanceReview("1.0.0", item.getId(), 0,-1);
                break;
            }
            case 4:{
                call = tour.getAtmReview("1.0.0", item.getId(), 0, -1);
                break;
            }
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString() + " reviewload" + item.getId());
                        if (jsonObject.getBoolean("Status")) {

                            JSONArray jsonArray = jsonObject.getJSONArray("Reviews");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                Review review = new Review(jsonObject1.getString("Reviewer"),
                                        jsonObject1.getString("Body"),
                                        (float) jsonObject1.getDouble("Score"));
                                review.setId(jsonObject1.getInt("Id"));
                                reviewItems.add(review);
                            }

                            if (reviewItems.size() <= 0) {
                                noReview.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                            } else {

                                Collections.reverse(reviewItems);

                                int number = 0;
                                if (reviewItems.size() > 3) {
                                    number = reviewItems.size() - 3;

                                }

                                for (int i = reviewItems.size() - 1; i >= number; i--) {
                                    displayReviewItems.add(reviewItems.get(i));
                                }

                                adapter.notifyDataSetChanged();

                                loading.setVisibility(View.GONE);

                                item.setRating(calculateRating(reviewItems));
                                rb.setRating(item.getRating());

                                DecimalFormat df = new DecimalFormat("#.#");

                                tvRating.setText("(" + df.format(item.getRating()) + ")");

                                LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
                                stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                                stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                                stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                            }
                            if (reviewItems.size() <= 3) {
                                btnShowHide.setVisibility(View.GONE);
                            }
                            btnShowHide.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isExpanded) {
                                        isExpanded = false;
                                        btnShowHide.setText(R.string.show_more);
                                        for (int i = 3; i < reviewItems.size(); i++) {
                                            displayReviewItems.remove(3);
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        isExpanded = true;
                                        displayReviewItems.clear();
                                        for (int i = reviewItems.size() - 1; i >= 0; i--) {
                                            displayReviewItems.add(reviewItems.get(i));
                                        }
                                        btnShowHide.setText(R.string.show_less);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Log.e("", "onResponse: " +response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public float calculateRating(ArrayList<Review> list){
        float temp = 0;
        if(list.size()>0) {
            for (int i = 0; i < list.size(); i++) {
                temp += list.get(i).rating;
            }

            temp = temp / list.size();
        }

        return temp;
    }

    public void addImages(LinearLayout imgContainer,MapObject item, TextView tvNoImg, ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        if (!item.getImages().equals("")) {
            String[] split = item.getImages().split(";");
            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                    .addConverterFactory(GsonConverterFactory.create()).build();
            final MapAPI tour = retro.create(MapAPI.class);
            for (int i = 0; i < split.length; i++) {
                Log.e("", "addImages: " + split[i]);
                Call<ResponseBody> call = tour.download(split[i]);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {

                                Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                                ImageView img = new ImageView(AllServiceMap.this);
                                img.setImageBitmap(bmp);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        300,
                                        300
                                );
                                lp.setMargins(5, 0, 5, 0);
                                img.setLayoutParams(lp);
                                img.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                                        new PhotoFullPopupWindow(AllServiceMap.this, R.layout.popup_photo_full, img, "", bitmap);
                                    }
                                });
                                imgContainer.addView(img);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }

            loading.setVisibility(View.GONE);

//        File file = new File("/");
//        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        } else {
            loading.setVisibility(View.GONE);
            tvNoImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public  void onStop(){
        super.onStop();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        adapter = new ReviewAdapter(this, R.layout.review_item, displayReviewItems);

        getBank();

        callMaintenance(latitude, longitude);

        callFuel(latitude, longitude);

        callWC(latitude, longitude);

        MarkerOptions curPositionMark = new MarkerOptions();
        curPositionMark.position(new LatLng(latitude,longitude));
        curPositionMark.title(getString(R.string.you_r_here));

        currentPosition = mMap.addMarker(curPositionMark);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
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
}
