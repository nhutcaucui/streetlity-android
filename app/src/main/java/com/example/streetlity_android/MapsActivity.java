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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Rating;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Marker currentPosition;

    ArrayList<MarkerOptions> mMarkers = new ArrayList<MarkerOptions>();;

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

        ListView reviewList = findViewById(R.id.lv_review);
        ArrayList<Review> reviewItems = new ArrayList<Review>();

        reviewItems.add(new Review("nhut", "i donek know kaahfeeefffffffeijkla jkl ja klj akljfklajj kajkljw klj lkaj eklwaj elkjwa kljela ej l", (float)2.5));

        final ReviewAdapter adapter = new ReviewAdapter(this, R.layout.review_item, reviewItems);

        reviewList.setAdapter(adapter);

        LinearLayout imgContainer = findViewById(R.id.layout_container);

        Button leaveReview = findViewById(R.id.btn_leave_comment);

        if(!((MyApplication) this.getApplication()).getToken().equals("")){
            leaveReview.setVisibility(View.VISIBLE);
        }

        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialogComment = new Dialog(MapsActivity.this);

                final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                final android.view.View dialogView2 = inflater2.inflate(R.layout.dialog_review, null);

                EditText edtComment = dialogView2.findViewById(R.id.edt_comment);
                RatingBar rtReview = dialogView2.findViewById(R.id.rating_review);

                LayerDrawable stars = (LayerDrawable) rtReview.getProgressDrawable();
                stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
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
                            reviewItems.add(new Review(((MyApplication) MapsActivity.this.getApplication()).getUsername(),
                                            edtComment.getText().toString(),
                                            rtReview.getRating()));
                            adapter.notifyDataSetChanged();
                            item.setRating(calculateRating(reviewItems));
                            rb.setRating(calculateRating(reviewItems));

                            tvRating.setText("("+ df.format(item.getRating()) +")");

                            dialogComment.cancel();
                        }
                    }
                });

                dialogComment.setContentView(dialogView2);

                dialogComment.show();
            }
        });

        ImageView imgView = findViewById(R.id.imageView5);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)imgView.getDrawable()).getBitmap();
//                imgView.setDrawingCacheEnabled(true);
//                imgView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//                imgView.layout(0, 0,
//                        imgView.getMeasuredWidth(), imgView.getMeasuredHeight());
//                imgView.buildDrawingCache(true);
//                Bitmap bitmap = Bitmap.createBitmap(imgView.getDrawingCache());
//                imgView.setDrawingCacheEnabled(false);
                new PhotoFullPopupWindow(MapsActivity.this, R.layout.popup_photo_full, imgView, "", bitmap);
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

        MarkerOptions curPositionMark = new MarkerOptions();
        curPositionMark.position(new LatLng(latitude,longitude));
        curPositionMark.title("You are here");

        currentPosition = mMap.addMarker(curPositionMark);

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
                            mMap.addPolyline(polylineOptions);

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
        currOption.title(getString(R.string.you_r_here));
        Marker currMarker = mMap.addMarker(currOption);

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

        int padding = 5; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
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
}
