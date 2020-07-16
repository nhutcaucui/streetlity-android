package com.example.streetlity_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
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
import android.net.Uri;
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
import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.Contribution.AddAMaintenance;
import com.example.streetlity_android.Events.EListener;
import com.example.streetlity_android.Events.Event;
import com.example.streetlity_android.Events.GlobalEvents;
import com.example.streetlity_android.Firebase.StreetlityFirebaseMessagingService;
import com.example.streetlity_android.MainFragment.MapObject;
import com.example.streetlity_android.User.Login;
import com.example.streetlity_android.User.UserInfoOther;
import com.example.streetlity_android.Util.ImageFilePath;
import com.example.streetlity_android.Util.RandomString;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;

import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    ArrayList<Review> reviewItems = new ArrayList<Review>();
    ArrayList<Review> displayReviewItems = new ArrayList<Review>();
    ReviewAdapter adapter;
    MapObject item;
    RatingBar rb;
    TextView tvRating;
    DecimalFormat df = new DecimalFormat("#.#");
    Button leaveReview;

    Marker currMarker;

    LocationManager locationManager;

    Polyline polyline;

    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 100;

    boolean isExpanded = false;

    Event<String, String> e = GlobalEvents.Example;

    EListener<String, String> listener;

    String imageName = "";

    boolean oriUpvoted;
    boolean oriDownvoted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        listener = new EListener<String, String>() {
            @Override
            public void trigger(String s, String s2) {
                String[] split = s2.split(";", -1);
                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                        .addConverterFactory(GsonConverterFactory.create()).build();
                final MapAPI tour = retro.create(MapAPI.class);
                Call<ResponseBody> call = tour.addActionReview(s, Long.parseLong(split[0]), split[1], split[2]);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.code() == 200){
                            try{
                                JSONObject jsonObject1 = new JSONObject(response.body().string());
                                Log.e("TAG", "onResponse: " + jsonObject1.toString() );
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Log.e(TAG, "onResponse: "+response.code() );
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        };

        e.subcribe(listener);

        BottomSheetBehavior sheetBehavior;
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        item = (MapObject) getIntent().getSerializableExtra("item");

        oriDownvoted = item.isDownvoted();
        oriUpvoted = item.isUpvoted();

        LinearLayout layoutNote = findViewById(R.id.layout_note);

        TextView tvNote =findViewById(R.id.tv_note);
        if(!item.getNote().equals("")) {
            layoutNote.setVisibility(View.VISIBLE);
            tvNote.setText(item.getNote());
        }else{
            tvNote.setText(getString(R.string.no_note));
        }

        ImageView imgIcon = findViewById(R.id.img_service_icon);

        if(item.getType() == 1){
            imgIcon.setImageResource(R.drawable.fuel_big_icon);
        }else if(item.getType() == 2){
            imgIcon.setImageResource(R.drawable.wc_big_icon);
        }else if(item.getType() == 3){
            imgIcon.setImageResource(R.drawable.fix_big_icon);
        }else if(item.getType() == 4){
            imgIcon.setImageResource(R.drawable.atm_big_icon);
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

        leaveReview = findViewById(R.id.btn_leave_comment);

        loadReviews();

        ListView reviewList = findViewById(R.id.lv_review);

        //reviewItems.add(new Review("nhut", "i donek know kaahfeeefffffffeijkla jkl ja klj akljfklajj kajkljw klj lkaj eklwaj elkjwa kljela ej l", (float)2.5));

        adapter = new ReviewAdapter(this, R.layout.review_item, displayReviewItems);

        reviewList.setAdapter(adapter);

        reviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (reviewItems.get(position).getUsername().equals(MyApplication.getInstance().getUsername())) {
                    Dialog dialogUpdate = new Dialog(MapsActivity.this);

                    //final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                    final View dialogView2 = View.inflate(MapsActivity.this,R.layout.dialog_edit_review, null);

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
                }else{
                    Intent t = new Intent(MapsActivity.this, UserInfoOther.class);
                    t.putExtra("user", reviewItems.get(position).getUsername());
                    startActivity(t);
                }
            }
        });

        LinearLayout imgContainer = findViewById(R.id.img_holder);

        addImages(imgContainer);

        ImageView addPhoto = findViewById(R.id.btn_add_photo);
        ImageView editNote = findViewById(R.id.btn_edit_note);
        ImageView editAddress = findViewById(R.id.btn_edit_address);
        LinearLayout layoutVote = findViewById(R.id.layout_vote);

        if(!MyApplication.getInstance().getToken().equals("")){
            leaveReview.setVisibility(View.VISIBLE);
//            addPhoto.setVisibility(View.VISIBLE);
//            editNote.setVisibility(View.VISIBLE);
//            editAddress.setVisibility(View.VISIBLE);
            layoutVote.setVisibility(VISIBLE);

            TextView tvPoints = findViewById(R.id.tv_points);
            tvPoints.setText(Integer.toString(item.getConfident()));

            ImageView imgUpvote = findViewById(R.id.img_upvote);

            ImageView imgDownvote = findViewById(R.id.img_downvote);

            if(item.isDownvoted()){
                imgDownvote.setColorFilter(getColor(R.color.tintDownvote));
            }else if (item.isUpvoted()){
                imgUpvote.setColorFilter(getColor(R.color.tintUpvote));
            }

            imgUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.isUpvoted()){
                        imgUpvote.setColorFilter(getColor(R.color.tint));
                        item.setUpvoted(false);
                        item.setConfident(item.getConfident()-1);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 3);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }
                    else if(item.isDownvoted()){
                        imgUpvote.setColorFilter(getColor(R.color.tintUpvote));
                        imgDownvote.setColorFilter(getColor(R.color.tint));
                        item.setDownvoted(false);
                        item.setUpvoted(true);
                        item.setConfident(item.getConfident()+2);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 1);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }else{
                        imgUpvote.setColorFilter(getColor(R.color.tintUpvote));
                        item.setUpvoted(true);
                        item.setConfident(item.getConfident()+1);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 1);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }
                    tvPoints.setText(Integer.toString(item.getConfident()));

                    if(item.isDownvoted() == oriDownvoted && item.isUpvoted() == oriUpvoted){
                        setResult(RESULT_CANCELED);
                    }
                }
            });

            imgDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.isUpvoted()){
                        imgUpvote.setColorFilter(getColor(R.color.tint));
                        imgDownvote.setColorFilter(getColor(R.color.tintDownvote));
                        item.setDownvoted(true);
                        item.setUpvoted(false);
                        item.setConfident(item.getConfident()-2);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 2);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }
                    else if(item.isDownvoted()){
                        imgDownvote.setColorFilter(getColor(R.color.tint));
                        item.setDownvoted(false);
                        item.setConfident(item.getConfident()+1);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 3);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }else{
                        imgDownvote.setColorFilter(getColor(R.color.tintDownvote));
                        item.setDownvoted(true);
                        item.setConfident(item.getConfident()-1);

                        Intent data2 = new Intent();
                        data2.putExtra("index", getIntent().getIntExtra("index", -1));
                        data2.putExtra("action", 2);
                        data2.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data2);
                    }
                    tvPoints.setText(Integer.toString(item.getConfident()));

                    if(item.isDownvoted() == oriDownvoted && item.isUpvoted() == oriUpvoted){
                        setResult(RESULT_CANCELED);
                    }
                }
            });

//            addPhoto.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
//                }
//            });

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
                                createReviews(rtReview.getRating(),edtComment.getText().toString(), leaveReview);
                                dialogComment.cancel();
                            }
                        }
                    });

                    dialogComment.setContentView(dialogView2);

                    dialogComment.show();
                }
            });

//            editAddress.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Dialog dialog = new Dialog(MapsActivity.this);
//
//                    //final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());
//
//                    final View dialogView2 = View.inflate(MapsActivity.this,R.layout.dialog_edit_address ,null);
//
//                    com.google.android.material.textfield.TextInputEditText edtAddress = dialogView2.findViewById(R.id.edt_address);
//
//                    Button confirm = dialogView2.findViewById(R.id.btn_confirm);
//
//                    edtAddress.setText(tvAddress.getText().toString());
//
//                    confirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if(edtAddress.getText().toString().equals("")){
//                                Toast toast = Toast.makeText(MapsActivity.this, R.string.empty_address, Toast.LENGTH_LONG);
//                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
//                                tv.setTextColor(Color.RED);
//
//                                toast.show();
//                            }
//                            else{
//                                editAddress(edtAddress.getText().toString());
//                                tvAddress.setText(edtAddress.getText().toString());
//                                dialog.cancel();
//                            }
//                        }
//                    });
//
//                    dialog.setContentView(dialogView2);
//
//                    dialog.show();
//                }
//            });

//            editNote.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Dialog dialog = new Dialog(MapsActivity.this);
//
//                    //final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());
//
//                    final View dialogView2 = View.inflate(MapsActivity.this,R.layout.dialog_edit_note ,null);
//
//                    com.google.android.material.textfield.TextInputEditText edtNote = dialogView2.findViewById(R.id.edt_note);
//
//                    Button confirm = dialogView2.findViewById(R.id.btn_confirm);
//
//                    if(!tvNote.getText().toString().equals(getString(R.string.no_note))) {
//                        edtNote.setText(tvNote.getText().toString());
//                    }
//
//                    confirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if(edtNote.getText().toString().equals("")){
//
//                            }
//                            else{
//                                editNote(edtNote.getText().toString());
//                                tvNote.setText(edtNote.getText().toString());
//                                dialog.cancel();
//                            }
//                        }
//                    });
//
//                    dialog.setContentView(dialogView2);
//
//                    dialog.show();
//                }
//            });
        }



        LinearLayout peekLayout = findViewById(R.id.layout_peek);
        peekLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        TextView tvContributor = findViewById(R.id.tv_submit_user);
        tvContributor.setText(item.getContributor());

        LinearLayout layoutSubmited = findViewById(R.id.layout_submitted);
        layoutSubmited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(MapsActivity.this, UserInfoOther.class);
                t.putExtra("user", item.getContributor());
                startActivity(t);
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

    public void editAddress(String newAddress){

    }

    public void editNote(String newNote){

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
                int padding = 150; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.moveCamera(cu);
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
            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                    .addConverterFactory(GsonConverterFactory.create()).build();
            final MapAPI tour = retro.create(MapAPI.class);
            for (int i = 0; i < split.length; i++) {
                imageName = split[i].substring(0,9) + i;
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

    public void createReviews(float rating,String comment, Button btnReview){
        //MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.createFuelReview(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername(),
                rating, comment);
        switch (item.getType()) {
            case 1: {
                call = tour.createFuelReview(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 2: {
                 call = tour.createWCReview(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 3: {
                call = tour.createMaintenanceReview(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername(),
                        rating, comment);
                break;
            }
            case 4: {
                call = tour.createAtmReview(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername(),
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
                        Log.e("", "onResponse create review: " + jsonObject.toString());
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

                            tvRating = findViewById(R.id.tv_rating);
                            tvRating.setText("("+ df.format(item.getRating()) +")");

                            btnReview.setVisibility(View.GONE);

                            TextView tvNoReview = findViewById(R.id.tv_no_review);
                            tvNoReview.setVisibility(View.GONE);

                            String builder = "";

                            Calendar calendar = Calendar.getInstance();

                            long time = calendar.getTimeInMillis();

                            builder = time + ";"+ item.getId() +";";
                            String type = "";
                            if(item.getType() == 1){
                                builder +="Fuel";
                                type = "Fuel";
                            }else if(item.getType() == 2){
                                builder +="Toilet";
                                type = "Toilet";
                            }else if(item.getType() == 3){
                                builder +="Maintenance";
                                type = "Maintenance";
                            }else if(item.getType() == 4){
                                builder +="Atm";
                                type = "Atm";
                            }

                            e.trigger(MyApplication.getInstance().getUsername(), builder);

                            ActionObject ao = new ActionObject("review " + item.getId(), time,"Review", Integer.toString(item.getId()));

                            if(MyApplication.getInstance().getReviewedMap().containsKey(type)){
                                MyApplication.getInstance().getReviewedMap().get(type).put("review " + item.getId(), ao);
                            }
                            else{
                                Map<String, ActionObject> map = new HashMap<>();
                                map.put("review " + item.getId(), ao);
                                MyApplication.getInstance().getReviewedMap().put(type, map);
                            }

                            tvNoReview.setVisibility(View.GONE);

                            Log.e(TAG, "onResponse create review: " + MyApplication.getInstance().getReviewedMap());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Log.e("", "onResponse create rview failed: " +response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
    public void updateReviews(int pos,int id, float rating, String comment, Dialog dialog){
        //MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.updateFuelReview(MyApplication.getInstance().getVersion(), id, rating, comment);
        switch (item.getType()) {
            case 1: {
                call = tour.updateFuelReview(MyApplication.getInstance().getVersion(), id, rating, comment);
                break;
            }
            case 2: {
                call = tour.updateWCReview(MyApplication.getInstance().getVersion(), id, rating, comment);
                break;
            }
            case 3: {
                call = tour.updateMaintenanceReview(MyApplication.getInstance().getVersion(), id, rating, comment);
                break;
            }
            case 4: {
                call = tour.updateATMReview(MyApplication.getInstance().getVersion(), id, rating, comment);
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
                        Log.e("", "onResponse update review: " + jsonObject.toString());
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
                    Log.e("", "onResponse update review fail: " +response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void deleteReviews(int pos, int id, Dialog dialog){
       // MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Log.e(TAG, "deleteReviews: "+ id );
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.deleteFuelReview(MyApplication.getInstance().getVersion(), id);
        String type1 = "Fuel";
        switch (item.getType()) {
            case 1: {
                call = tour.deleteFuelReview(MyApplication.getInstance().getVersion(), id);
                type1 = "Fuel";
                break;
            }
            case 2: {
               call = tour.deleteWCReview(MyApplication.getInstance().getVersion(), id);
                type1 = "Toilet";
                break;
            }
            case 3: {
                call = tour.deleteMaintenanceReview(MyApplication.getInstance().getVersion(), id);
                type1 = "Maintenance";
                break;
            }
            case 4: {
                call = tour.deleteATMReview(MyApplication.getInstance().getVersion(), id);
                type1 = "Atm";
                break;
            }
        }

        final String type = type1;

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse del review: " + jsonObject.toString());
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

                            MyApplication.getInstance().getReviewedMap().get(type).remove("review " + item.getId());

                            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                    .addConverterFactory(GsonConverterFactory.create()).build();
                            final MapAPI tour = retro.create(MapAPI.class);
                            Call<ResponseBody> call2 = tour.deleteActionReview(Integer.toString(item.getId()), type,
                                    MyApplication.getInstance().getUsername());
                            call2.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try{
                                        if(response.code()==200){
                                            Log.e(TAG, "onResponse del action review: " + response.body().string());
                                        }
                                        else{
                                            Log.e(TAG, "onResponse del action review fail: " + response.code());
                                        }

                                    }catch (Exception e) {e.printStackTrace();}
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    t.printStackTrace();
                                }
                            });

                            dialog.cancel();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    Log.e("", "onResponse del review failed: " +response.code() );
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
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getFuelReview(MyApplication.getInstance().getVersion(), item.getId(), 0,-1);
        switch (item.getType()){
            case 1:{
                call = tour.getFuelReview(MyApplication.getInstance().getVersion(), item.getId(), 0,-1);
                break;
            }
            case 2:{
                call = tour.getWCReview(MyApplication.getInstance().getVersion(), item.getId(), 0,-1);
                break;
            }
            case 3:{
                call = tour.getMaintenanceReview(MyApplication.getInstance().getVersion(), item.getId(), 0,-1);
                break;
            }
            case 4:{
                call = tour.getAtmReview(MyApplication.getInstance().getVersion(), item.getId(), 0, -1);
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
                        Log.e("", "onResponse load review: " + jsonObject.toString() + " reviewload" + item.getId());
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

                            loading.setVisibility(View.GONE);

                            if(reviewItems.size() <= 0){
                                TextView tvNoReview = findViewById(R.id.tv_no_review);
                                loading.setVisibility(View.GONE);
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
                    Log.e("", "onResponse load review failed: " +response.code() );
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



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1) {

                if(imageName.equals("")){
                    imageName = RandomString.getAlphaNumericString(10);
                }

                LinearLayout imgContainer= findViewById(R.id.img_holder);
                if(null == data) {
//                    arrImg.clear();
//                    paramMap.clear();
//                    bodyMap.clear();
//                    fileName.clear();
                    //EditText edtSelectImg = findViewById(R.id.edt_select_img);
                    //edtSelectImg.setHint(R.string.select_img);
                }else {
                    if (data.getData() != null) {
//                        arrImg.clear();
//                        paramMap.clear();
//                        bodyMap.clear();
//                        body.clear();
//                        //Uri mImageUri = data.getData();
//                        fileName.clear();

                        String path = ImageFilePath.getPath(MapsActivity.this, data.getData());

                        File file = new File(path);

                        String extension = path.substring(path.lastIndexOf("."));

                        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                        EditText edtSelectImg = findViewById(R.id.edt_select_img);

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

                        RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part mBody =
                                MultipartBody.Part.createFormData(imageName+imgContainer.getChildCount()+extension, file.getName(), fbody);

                        List<MultipartBody.Part> body = new ArrayList<>();
                        body.add(mBody);

                        String[] f =new String[1];
                        f[0] = imageName+imgContainer.getChildCount()+extension;

                        Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                                .addConverterFactory(GsonConverterFactory.create()).build();
                        final MapAPI tour2 = retro2.create(MapAPI.class);
                        Call<ResponseBody> call2 = tour2.upload(f, body);
                        call2.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    final JSONObject jsonObject;
                                    try {
                                        jsonObject = new JSONObject(response.body().string());
                                        Log.e("", "onResponse upload: " + jsonObject.toString());

                                        if (jsonObject.getBoolean("Status")) {
                                            findViewById(R.id.tv_no_img).setVisibility(View.GONE);
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }else{
                                    Log.e("", "onResponse uploadfail: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });

                        //bodyMap.put(generatedString+0, body);
                    } else {
                        if (data.getClipData() != null) {
//                            arrImg.clear();
//                            paramMap.clear();
//                            bodyMap.clear();
//                            fileName.clear();

                            ClipData mClipData = data.getClipData();

                            //body.clear();

                            EditText edtSelectImg = findViewById(R.id.edt_select_img);

                            List<MultipartBody.Part> body = new ArrayList<>();
                            ArrayList<String> fName = new ArrayList<>();

                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                String path = ImageFilePath.getPath(MapsActivity.this, uri);

                                File file = new File(path);

                                String extension = path.substring(path.lastIndexOf("."));


                                RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                MultipartBody.Part mBody =
                                        MultipartBody.Part.createFormData(imageName+imgContainer.getChildCount()+extension,
                                                file.getName(), fbody);

                                fName.add(imageName+imgContainer.getChildCount()+extension);
                                body.add(mBody);

                                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
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
                            }

                            String[] f = new String[body.size()];
                            for(int i =0 ;i<body.size();i++){
                                f[i] = fName.get(i);
                            }

                            Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                                    .addConverterFactory(GsonConverterFactory.create()).build();
                            final MapAPI tour2 = retro2.create(MapAPI.class);
                            Call<ResponseBody> call2 = tour2.upload(f, body);
                            call2.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        final JSONObject jsonObject;
                                        try {
                                            jsonObject = new JSONObject(response.body().string());
                                            Log.e("", "onResponse upload: " + jsonObject.toString());

                                            if (jsonObject.getBoolean("Status")) {
                                                findViewById(R.id.tv_no_img).setVisibility(View.GONE);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                        Log.e("", "onResponse upload failed: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public void upvote(boolean isClear) {
        Log.e(TAG, "onActivityResult: " + getIntent().getIntExtra("index", -1));
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.upvoteATM(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        if (item.getType() == 1) {
            call = tour.upvoteFuel(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 2) {
            call = tour.upvoteWC(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 3) {
            call = tour.upvoteMaintenance(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 4) {
            call = tour.upvoteATM(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        Log.e("", "onResponse updoot: " + response);
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse updoot: " + jsonObject.toString());

                        Calendar calendar = Calendar.getInstance();
                        long time = calendar.getTimeInMillis();
                        String builder = "";
                        builder = builder + time + ";" + item.getId() + ";";
                        String type = "";

                        if (item.getType() == 1) {
                            builder += "Fuel";
                            type = "Fuel";
                        } else if (item.getType() == 2) {
                            builder += "Toilet";
                            type = "Toilet";
                        } else if (item.getType() == 3) {
                            builder += "Maintenance";
                            type = "Maintenance";
                        } else if (item.getType() == 4) {
                            builder += "Atm";
                            type = "Atm";
                        }

                        if (jsonObject.getBoolean("Status")) {

                            if(!isClear) {
                                //e.trigger(MyApplication.getInstance().getUsername(), builder);

                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.addActionUpvote(MyApplication.getInstance().getUsername(), time,Integer.toString(item.getId()), type);
                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.code() == 200) {
                                            try {
                                                JSONObject jsonObject1 = new JSONObject(response.body().string());
                                                Log.e("TAG", "onResponse updoot: " + jsonObject1.toString());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e(TAG, "onResponse add updoot: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });

                                ActionObject ao = new ActionObject("upvote " + item.getId(), time, "Upvote", Integer.toString(item.getId()));

                                if (MyApplication.getInstance().getUpvoteMap().containsKey(type)) {
                                    MyApplication.getInstance().getUpvoteMap().get(type).put("upvote " + item.getId(), ao);
                                } else {
                                    Map<String, ActionObject> map = new HashMap<>();
                                    map.put("upvote " + item.getId(), ao);
                                    MyApplication.getInstance().getUpvoteMap().put(type, map);
                                }

                            }else{
                                MyApplication.getInstance().getDownvoteMap().get(type).remove("downvote " + item.getId());

                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.deleteActionDownvote(Integer.toString(item.getId()), type, MyApplication.getInstance().getUsername());
                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try{
                                            if(response.code()==200){
                                                Log.e(TAG, "onResponse del downvote: " + new JSONObject(response.body().string()).toString());
                                            }
                                            else{
                                                Log.e(TAG, "onResponse del downvote: " + response.code() );
                                            }

                                        }catch (Exception e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }

                            Log.e(TAG, "onResponse: " + MyApplication.getInstance().getUpvoteMap());

                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void downvote(boolean isClear) {
        Log.e(TAG, "onActivityResult: " + getIntent().getIntExtra("index", -1));
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.downvoteFuel(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        if (item.getType() == 1) {
            call = tour.downvoteFuel(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 2) {
            call = tour.downvoteWC(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 3) {
            call = tour.downvoteMaintenance(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 4) {
            call = tour.downvoteATM(MyApplication.getInstance().getVersion(), item.getId(), MyApplication.getInstance().getUsername());
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        Log.e("", "onResponse downvote: " + response);
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse downvote: " + jsonObject.toString());

                        Calendar calendar = Calendar.getInstance();
                        long time = calendar.getTimeInMillis();
                        String builder = "";
                        builder = builder + time + ";" + item.getId() + ";";
                        String type = "";

                        if (item.getType() == 1) {
                            builder += "Fuel";
                            type = "Fuel";
                        } else if (item.getType() == 2) {
                            builder += "Toilet";
                            type = "Toilet";
                        } else if (item.getType() == 3) {
                            builder += "Maintenance";
                            type = "Maintenance";
                        } else if (item.getType() == 4) {
                            builder += "Atm";
                            type = "Atm";
                        }

                        if (jsonObject.getBoolean("Status")) {
                            if(!isClear) {
//                            e.trigger(MyApplication.getInstance().getUsername(), builder);

                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.addActionDownvote(MyApplication.getInstance().getUsername(), time,Integer.toString(item.getId()), type);
                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if (response.code() == 200) {
                                            try {
                                                JSONObject jsonObject1 = new JSONObject(response.body().string());
                                                Log.e("TAG", "onResponse add downvote: " + jsonObject1.toString());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e(TAG, "onResponse add downvote: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });

                                ActionObject ao = new ActionObject("downvote " + item.getId(), time, "Downvote", Integer.toString(item.getId()));


                                if (MyApplication.getInstance().getDownvoteMap().containsKey(type)) {
                                    MyApplication.getInstance().getDownvoteMap().get(type).put("downvote " + item.getId(), ao);
                                } else {
                                    Map<String, ActionObject> map = new HashMap<>();
                                    map.put("downvote " + item.getId(), ao);
                                    MyApplication.getInstance().getDownvoteMap().put(type, map);
                                }



                            }else{

                                MyApplication.getInstance().getUpvoteMap().get(type).remove("upvote " + item.getId());

                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.deleteActionUpvote(Integer.toString(item.getId()), type, MyApplication.getInstance().getUsername());
                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try{
                                            if(response.code()==200){
                                                Log.e(TAG, "onResponse del updoot: " + new JSONObject(response.body().string()).toString());
                                            }
                                            else{
                                                Log.e(TAG, "onResponse del updoot: " + response.code() );
                                            }

                                        }catch (Exception e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });


                            }

                            Log.e(TAG, "onResponse: " + MyApplication.getInstance().getDownvoteMap());

                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void onStop(){
        super.onStop();

        locationManager.removeUpdates(this);
        e.unsubcribe(listener);

        if(oriUpvoted != item.isUpvoted() || oriDownvoted != item.isDownvoted()){
            if(item.isUpvoted() && oriDownvoted){
                upvote(true);
                upvote(false);
            }else if(item.isUpvoted() && !oriDownvoted){
                upvote(false);
            }else if(item.isDownvoted() && oriUpvoted){
                downvote(true);
                downvote(false);
            }else if (item.isDownvoted() && !oriUpvoted){
                downvote(false);
            }else if(!item.isUpvoted() && oriUpvoted && !item.isDownvoted()){
                downvote(true);
            }else if(!item.isUpvoted() && oriDownvoted && !item.isDownvoted()){
                upvote(true);
            }
        }
    }
}
