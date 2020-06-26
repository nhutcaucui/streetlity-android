package com.example.streetlity_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Rating;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.streetlity_android.Firebase.StreetlityFirebaseMessagingService;
import com.example.streetlity_android.MainFragment.MapObject;
import com.example.streetlity_android.User.Login;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    ArrayList<Review> reviewItems = new ArrayList<Review>();
    ArrayList<Review> displayReviewItems = new ArrayList<Review>();
    ReviewAdapter adapter;
    MapObject item;
    RatingBar rb;
    TextView tvRating;
    DecimalFormat df = new DecimalFormat("#.#");

    Marker currMarker;

    LocationManager locationManager;

    Polyline polyline;

    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 100;

    boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        BottomSheetBehavior sheetBehavior;
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        item = (MapObject) getIntent().getSerializableExtra("item");

        LinearLayout layoutNote = findViewById(R.id.layout_note);
        TextView tvNote =findViewById(R.id.tv_note);
        if(!item.getNote().equals("")) {
            layoutNote.setVisibility(View.VISIBLE);
            tvNote.setText(item.getNote());
        }

        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText(item.getName());
        TextView tvAddress = findViewById(R.id.tv_address);
        tvAddress.setText(item.getAddress());

        TextView tvDistance = findViewById(R.id.tv_distance);
        float distance = item.getDistance();
        String dis = "m";
        if(distance > 1000){
            dis = "km";
            distance = distance / 1000;
        }
        tvDistance.setText("~" + df.format(distance) + dis);

        rb=findViewById(R.id.ratingbar_map_object);

        loadReviews();

        ListView reviewList = findViewById(R.id.lv_review);

        //reviewItems.add(new Review("nhut", "i donek know kaahfeeefffffffeijkla jkl ja klj akljfklajj kajkljw klj lkaj eklwaj elkjwa kljela ej l", (float)2.5));

        adapter = new ReviewAdapter(this, R.layout.review_item, displayReviewItems);

        reviewList.setAdapter(adapter);

        reviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(reviewItems.get(position).getUsername().equals(MyApplication.getInstance().getUsername()));
                    Dialog dialogUpdate = new Dialog(MapsActivity.this);

                    final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                    final android.view.View dialogView2 = inflater2.inflate(R.layout.dialog_edit_review, null);

                    com.google.android.material.textfield.TextInputEditText edtComment = dialogView2.findViewById(R.id.edt_comment);
                    edtComment.setText(reviewItems.get(position).getReviewBody());
                    RatingBar rtReview = dialogView2.findViewById(R.id.rating_review);
                    rtReview.setRating(reviewItems.get(position).getRating());

                    LayerDrawable stars = (LayerDrawable) rtReview.getProgressDrawable();
                    stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    //stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

                    Button updateReview = dialogView2.findViewById(R.id.btn_update_review);
                    Button deleteReview = dialogView2.findViewById(R.id.btn_delete_review);

                    updateReview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateReviews(position, reviewItems.get(position).getId(), rtReview.getRating(),
                                    edtComment.getText().toString(), dialogUpdate);
                        }
                    });

                    deleteReview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteReviews(position, reviewItems.get(position).getId(), dialogUpdate);
                        }
                    });

                    dialogUpdate.setContentView(dialogView2);
                    dialogUpdate.show();
                }

        });

        LinearLayout imgContainer = findViewById(R.id.img_holder);

        addImages(imgContainer);

        Button leaveReview = findViewById(R.id.btn_leave_comment);

        if(!((MyApplication) this.getApplication()).getToken().equals("")){
            leaveReview.setVisibility(View.VISIBLE);
        }

        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialogComment = new Dialog(MapsActivity.this);

                final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                final View dialogView2 = View.inflate(MapsActivity.this,R.layout.dialog_review ,null);

                com.google.android.material.textfield.TextInputEditText edtComment = dialogView2.findViewById(R.id.edt_comment);
                RatingBar rtReview = dialogView2.findViewById(R.id.rating_review);

                LayerDrawable stars = (LayerDrawable) rtReview.getProgressDrawable();
                stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                //stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

                Button confirmReview = dialogView2.findViewById(R.id.btn_confrim_review);

                confirmReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(edtComment.getText().toString().equals("")){
                            Toast toast = Toast.makeText(MapsActivity.this, R.string.empty_comment, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        else{
                            createReviews(rtReview.getRating(),edtComment.getText().toString());



                            dialogComment.cancel();
                        }
                    }
                });

                dialogComment.setContentView(dialogView2);

                dialogComment.show();
            }
        });

        LinearLayout peekLayout = findViewById(R.id.layout_peek);
        peekLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING: {
                        if (sheetBehavior.getPeekHeight() < 150){
                            sheetBehavior.setPeekHeight(150);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //ArrayList<MarkerOptions> markList = new ArrayList<MarkerOptions>();

        float latitude = getIntent().getFloatExtra("currLat", 0);
        float longitude = getIntent().getFloatExtra("currLon", 0);

        MapObject item = (MapObject) getIntent().getSerializableExtra("item");

        String serverKey = "AIzaSyB56CeF7ccQ9ZeMn0O4QkwlAQVX7K97-Ss";
        LatLng origin = new LatLng(latitude, longitude);
        LatLng destination = new LatLng(item.getLat(), item.getLon());
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)

                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction) {

                        String status = direction.getStatus();
                        Log.e("", "onDirectionSuccess: " + status);
                        if(status.equals(RequestResult.OK)) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.RED);
                            polyline=mMap.addPolyline(polylineOptions);

                        } else if(status.equals(RequestResult.NOT_FOUND)) {
                            Toast toast = Toast.makeText(MapsActivity.this, R.string.cant_go, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e("", "onDirectionFailure: ");
                        Toast toast = Toast.makeText(MapsActivity.this, "Something went wrong when trying to find direction", Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }
                });

        MarkerOptions currOption = new MarkerOptions();
        currOption.position(new LatLng(latitude,longitude));
        currOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.cursor));
        currOption.title(getString(R.string.you_r_here));
        currMarker = mMap.addMarker(currOption);

        MarkerOptions desOption = new MarkerOptions();
        desOption.position(new LatLng(item.getLat(),item.getLon()));
        if(item.getType() == 1)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fuel));
        if(item.getType() == 2)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wc));
        if(item.getType() == 3)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        if(item.getType() == 4)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));
        desOption.title(getString(R.string.destination));

        Marker desMarker = mMap.addMarker(desOption);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currMarker.getPosition());
        builder.include(desMarker.getPosition());
        LatLngBounds bounds = builder.build();

        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager)
                    MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        }

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cu);
            }
        });


    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
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

    public void addImages(LinearLayout imgContainer){
        ProgressBar loading = findViewById(R.id.loading_img);
        loading.setVisibility(View.VISIBLE);
        if(!item.getImages().equals("")) {
            String[] split = item.getImages().split(";");
            Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getDriverURL())
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
                                ImageView img = new ImageView(MapsActivity.this);
                                img.setImageBitmap(bmp);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        300,
                                        300
                                );
                                  lp.setMargins(5,0,5,0);
                                img.setLayoutParams(lp);
                                img.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
                                        new PhotoFullPopupWindow(MapsActivity.this, R.layout.popup_photo_full, img, "", bitmap);
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

        }else{
            loading.setVisibility(View.GONE);
            TextView tvNoImg = findViewById(R.id.tv_no_img);
            tvNoImg.setVisibility(View.VISIBLE);
        }
    }

    public void createReviews(float rating,String comment){
        //MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.createFuelReview("1.0.0", item.getId(), MyApplication.getInstance().getUsername(),
                rating, comment);
        switch (item.getType()) {
            case 1: {
                call = tour.createFuelReview("1.0.0", item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 2: {
                 call = tour.createWCReview("1.0.0", item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 3: {
                call = tour.createMaintenanceReview("1.0.0", item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 4: {
                call = tour.createAtmReview("1.0.0", item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
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
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (jsonObject.getBoolean("Status")) {
                            Review review = new Review(((MyApplication) MapsActivity.this.getApplication()).getUsername(),
                                    comment,
                                    rating);
                            review.setId(jsonObject.getJSONObject("Review").getInt("Id"));
                            displayReviewItems.add(0, review);
                            reviewItems.add(review);

                            if(displayReviewItems.size()>3 && !isExpanded){
                                displayReviewItems.remove(3);
                                Button btnShowHide = findViewById(R.id.btn_show_hide);
                                btnShowHide.setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();

                            adapter.notifyDataSetChanged();
                            item.setRating(calculateRating(reviewItems));
                            rb.setRating(calculateRating(reviewItems));

                            tvRating.setText("("+ df.format(item.getRating()) +")");

                            TextView tvNoReview = findViewById(R.id.tv_no_review);
                            tvNoReview.setVisibility(View.GONE);
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

            }
        });
    }
    public void updateReviews(int pos,int id, float rating, String comment, Dialog dialog){
        //MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.updateFuelReview("1.0.0", id, rating, comment);
        switch (item.getType()) {
            case 1: {
                call = tour.updateFuelReview("1.0.0", id, rating, comment);
                break;
            }
            case 2: {
                call = tour.updateWCReview("1.0.0", id, rating, comment);
                break;
            }
            case 3: {
                call = tour.updateMaintenanceReview("1.0.0", id, rating, comment);
                break;
            }
            case 4: {
                call = tour.updateATMReview("1.0.0", id, rating, comment);
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
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (jsonObject.getBoolean("Status")) {
                            Review review = new Review(((MyApplication) MapsActivity.this.getApplication()).getUsername(),
                                    comment,
                                    rating);
                            review.setId(id);
                            displayReviewItems.set(pos, review);
                            reviewItems.set(reviewItems.size()-1-pos, review);
                            adapter.notifyDataSetChanged();
                            item.setRating(calculateRating(reviewItems));
                            rb.setRating(calculateRating(reviewItems));

                            tvRating.setText("("+ df.format(item.getRating()) +")");
                            dialog.cancel();
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

            }
        });
    }

    public void deleteReviews(int pos, int id, Dialog dialog){
       // MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.deleteFuelReview("1.0.0", id);
        switch (item.getType()) {
            case 1: {
                call = tour.deleteFuelReview("1.0.0", id);
                break;
            }
            case 2: {
               call = tour.deleteWCReview("1.0.0", id);

                break;
            }
            case 3: {
                call = tour.deleteMaintenanceReview("1.0.0", id);

                break;
            }
            case 4: {
                call = tour.deleteATMReview("1.0.0", id);

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
                        Log.e("", "onResponse: " + jsonObject.toString());
                        if (jsonObject.getBoolean("Status")) {
                            displayReviewItems.remove(pos);
                            reviewItems.remove(reviewItems.size()-1-pos);
                            adapter.notifyDataSetChanged();
                            item.setRating(calculateRating(reviewItems));
                            rb.setRating(calculateRating(reviewItems));

                            if(reviewItems.size()<=3){
                                Button btnShowHide= findViewById(R.id.btn_show_hide);
                                btnShowHide.setVisibility(View.GONE);
                            }

                            if(reviewItems.size() == 0){
                                TextView tvNoReview = findViewById(R.id.tv_no_review);
                                tvNoReview.setVisibility(View.VISIBLE);
                            }

                            adapter.notifyDataSetChanged();

                            tvRating.setText("("+ df.format(item.getRating()) +")");
                            dialog.cancel();
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

            }
        });
    }

    public void loadReviews(){
        reviewItems.clear();
        displayReviewItems.clear();
        ProgressBar loading = findViewById(R.id.loading_review);
        loading.setVisibility(View.VISIBLE);
       // MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(((MyApplication) this.getApplication()).getServiceURL())
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
                                if(review.getUsername().equals(MyApplication.getInstance().getUsername())){
                                    Button leaveReview = findViewById(R.id.btn_leave_comment);
                                    leaveReview.setVisibility(View.GONE);
                                }
                            }

                            if(reviewItems.size() <= 0){
                                TextView tvNoReview = findViewById(R.id.tv_no_review);
                                tvNoReview.setVisibility(View.VISIBLE);
                            }else {

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

                                tvRating = findViewById(R.id.tv_rating);
                                tvRating.setText("(" + df.format(item.getRating()) + ")");

                                LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
                                stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                                stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                                stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                            }
                            Button btnShowHide = findViewById(R.id.btn_show_hide);

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

    public void onLocationChanged(Location location) {
        if(currMarker != null) {
            currMarker.remove();
        }
        MarkerOptions currOption = new MarkerOptions();
        currOption.position(new LatLng(location.getLatitude(),location.getLongitude()));
        currOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.cursor));
        currOption.rotation(location.getBearing()-45);
        currOption.title(getString(R.string.you_r_here));
        currMarker = mMap.addMarker(currOption);
        Log.e("", "onLocationChanged: updation" );
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
