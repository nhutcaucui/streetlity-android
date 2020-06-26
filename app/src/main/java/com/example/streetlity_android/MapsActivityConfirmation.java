package com.example.streetlity_android;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.streetlity_android.MainFragment.MapObject;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivityConfirmation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Marker currentPosition;
    MapObject item;

    ArrayList<MarkerOptions> mMarkers = new ArrayList<MarkerOptions>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        TextView tvTittle = findViewById(R.id.tv_title);
        tvTittle.setText(R.string.confirming_location);

        LinearLayout llReview = findViewById(R.id.ll_review);
        llReview.setVisibility(View.GONE);

        LinearLayout confirmingLayout = findViewById(R.id.layout_confirming);
        confirmingLayout.setVisibility(View.VISIBLE);

        Button btnExist = findViewById(R.id.btn_exist);
        Button btnNonExist = findViewById(R.id.btn_non_exist);

        btnExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upvote();
            }
        });

        btnNonExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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


        MapObject item = (MapObject) getIntent().getSerializableExtra("item");

        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText(item.getName());
        TextView tvAddress = findViewById(R.id.tv_address);
        tvAddress.setText(item.getAddress());

        DecimalFormat df = new DecimalFormat("#.#");
        TextView tvDistance = findViewById(R.id.tv_distance);
        float distance = item.getDistance();
        String dis = "m";
        if(distance > 1000){
            dis = "km";
            distance = distance / 1000;
        }
        tvDistance.setText("~" + df.format(distance) + dis);

        TextView tvRating = findViewById(R.id.tv_rating);
        tvRating.setText("("+ df.format(item.getRating()) +")");

        RatingBar rb = findViewById(R.id.ratingbar_map_object);
        rb.setRating(item.getRating());

        LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        LinearLayout imgContainer = findViewById(R.id.layout_container);

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

        MapObject item = (MapObject) getIntent().getSerializableExtra("item");

        MarkerOptions desOption = new MarkerOptions();
        desOption.position(new LatLng(item.getLat(),item.getLon()));
        if(item.getType() == 1)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fuel));
        else if(item.getType() == 2)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wc));
        else if(item.getType() == 3)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        else if(item.getType() == 4)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));

        mMap.addMarker(desOption);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLat(), item.getLon()), 15f));

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public float calculateRating(ArrayList<Review> list){
        float temp = 0;
        for(int i =0; i < list.size(); i++){
            temp += list.get(i).rating;
        }

        temp = temp / list.size();

        return temp;
    }

    public void addImage(LinearLayout imgContainer){
        ImageView img = new ImageView(this);
        File file = new File("/");
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        img.setImageBitmap(myBitmap);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
                new PhotoFullPopupWindow(MapsActivityConfirmation.this, R.layout.popup_photo_full, img, "", bitmap);
            }
        });
        imgContainer.addView(img);
    }

    public void upvote(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.upvoteATM("1.0.0", item.getId());
        if(item.getType()==1){
            call = tour.upvoteFuel("1.0.0", item.getId());
        }
        if(item.getType()==2){
            call = tour.upvoteWC("1.0.0", item.getId());
        }
        if(item.getType()==3){
            call = tour.upvoteMaintenance("1.0.0", item.getId());
        }
        if(item.getType()==4){
            call = tour.upvoteATM("1.0.0", item.getId());
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        finish();
    }
}
