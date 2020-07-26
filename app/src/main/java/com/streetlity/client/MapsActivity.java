package com.streetlity.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.streetlity.client.Achievement.ActionObject;
import com.streetlity.client.Events.EListener;
import com.streetlity.client.Events.Event;
import com.streetlity.client.Events.GlobalEvents;
import com.streetlity.client.MainFragment.MapObject;
import com.streetlity.client.User.UserInfoOther;
import com.streetlity.client.Util.ImageFilePath;
import com.streetlity.client.Util.RandomString;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

import static android.view.View.VISIBLE;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
/*
find direction and show detail information of a service
 */
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

    Intent data = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        data.putExtra("index", getIntent().getIntExtra("index", -1));

        /*
        listener for update action
         */
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
                                Log.e("tag", "onResponse: " + jsonObject1.toString() );
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else{
                            Log.e("tag", "onResponse: "+response.code() );
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
        ImageView editName = findViewById(R.id.btn_edit_name);
        LinearLayout layoutVote = findViewById(R.id.layout_vote);

        TextView tvPoints = findViewById(R.id.tv_points);
        tvPoints.setText(Integer.toString(item.getConfident()));

        if(!MyApplication.getInstance().getToken().equals("")){
            leaveReview.setVisibility(View.VISIBLE);
            addPhoto.setVisibility(View.VISIBLE);
            editNote.setVisibility(View.VISIBLE);
            if(item.getType() != 4) {
                editName.setVisibility(View.VISIBLE);
            }
            layoutVote.setVisibility(VISIBLE);

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

                        //data.putExtra("index", getIntent().getIntExtra("index", -1));
                        data.putExtra("action", 3);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }
                    else if(item.isDownvoted()){
                        imgUpvote.setColorFilter(getColor(R.color.tintUpvote));
                        imgDownvote.setColorFilter(getColor(R.color.tint));
                        item.setDownvoted(false);
                        item.setUpvoted(true);
                        item.setConfident(item.getConfident()+2);

                        data.putExtra("action", 1);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }else{
                        imgUpvote.setColorFilter(getColor(R.color.tintUpvote));
                        item.setUpvoted(true);
                        item.setConfident(item.getConfident()+1);

                        data.putExtra("action", 1);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }
                    tvPoints.setText(Integer.toString(item.getConfident()));

                    if(item.isDownvoted() == oriDownvoted && item.isUpvoted() == oriUpvoted){
                        data.putExtra("action", -1);
                        setResult(RESULT_OK, data);
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

                        data.putExtra("action", 2);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }
                    else if(item.isDownvoted()){
                        imgDownvote.setColorFilter(getColor(R.color.tint));
                        item.setDownvoted(false);
                        item.setConfident(item.getConfident()+1);

                        data.putExtra("action", 3);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }else{
                        imgDownvote.setColorFilter(getColor(R.color.tintDownvote));
                        item.setDownvoted(true);
                        item.setConfident(item.getConfident()-1);

                        data.putExtra("action", 2);
                        data.putExtra("confident",item.getConfident());
                        setResult(RESULT_OK, data);
                    }
                    tvPoints.setText(Integer.toString(item.getConfident()));

                    if(item.isDownvoted() == oriDownvoted && item.isUpvoted() == oriUpvoted){
                        data.putExtra("action", -1);
                        setResult(RESULT_OK, data);
                    }
                }
            });

            addPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
                }
            });

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

            if(item.getType() != 4) {
                editName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialog dialog = new Dialog(MapsActivity.this);

                        //final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                        final View dialogView2 = View.inflate(MapsActivity.this, R.layout.dialog_edit_name, null);

                        com.google.android.material.textfield.TextInputEditText edtName = dialogView2.findViewById(R.id.edt_name);

                        Button confirm = dialogView2.findViewById(R.id.btn_confirm);
                        Log.e("tag", "onClick: "+ tvName.getText().toString() + " " +getString(R.string.fuel));
                        if (item.getType() == 1 && tvName.getText().toString().equals(getString(R.string.fuel))) {
                            edtName.setText("");
                        } else if (item.getType() == 2 && tvName.getText().toString().equals(getString(R.string.wc))) {
                            edtName.setText("");
                        } else {
                            edtName.setText(tvName.getText().toString());
                        }

                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (edtName.getText().toString().equals("")) {
                                    Toast toast = Toast.makeText(MapsActivity.this, R.string.empty_name, Toast.LENGTH_LONG);
                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                    tv.setTextColor(Color.RED);

                                    toast.show();
                                } else {
                                    editName(edtName.getText().toString());
                                    tvName.setText(edtName.getText().toString());
                                    dialog.cancel();
                                }
                            }
                        });

                        dialog.setContentView(dialogView2);

                        dialog.show();
                    }
                });
            }

            editNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dialog = new Dialog(MapsActivity.this);

                    //final LayoutInflater inflater2 = LayoutInflater.from(MapsActivity.this.getApplicationContext());

                    final View dialogView2 = View.inflate(MapsActivity.this,R.layout.dialog_edit_note ,null);

                    com.google.android.material.textfield.TextInputEditText edtNote = dialogView2.findViewById(R.id.edt_note);

                    Button confirm = dialogView2.findViewById(R.id.btn_confirm);

                    if(!tvNote.getText().toString().equals(getString(R.string.no_note))) {
                        edtNote.setText(tvNote.getText().toString());
                    }

                    confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(edtNote.getText().toString().equals("")){
                                Toast toast = Toast.makeText(MapsActivity.this, R.string.empty_note, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }
                            else{
                                editNote(edtNote.getText().toString());
                                tvNote.setText(edtNote.getText().toString());
                                dialog.cancel();
                            }
                        }
                    });

                    dialog.setContentView(dialogView2);

                    dialog.show();
                }
            });
        }

        else {
            ImageView imgUpvote = findViewById(R.id.img_upvote);

            ImageView imgDownvote = findViewById(R.id.img_downvote);

            imgUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(MapsActivity.this, R.string.need_login_info, Toast.LENGTH_LONG);
                    toast.show();
                }
            });

            imgDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(MapsActivity.this, R.string.need_login_info, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
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

    /**
     * edit name of service
     * @param newName
     */
    public void editName(String newName){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        String type = "fuel";
        if(item.getType() == 2){
            type = "toilet";
        }else if(item.getType() == 3){
            type = "maintenance";
        }
        Call<ResponseBody> call = tour.updateServiceName(MyApplication.getInstance().getVersion(), item.getId(), newName, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse create review: " + jsonObject.toString());
                        item.setName(newName);

                        data.putExtra("name", newName);
                        setResult(RESULT_OK, data);


                    }catch (Exception e){
                        e.printStackTrace();}
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * edit the note of service
     * @param newNote
     */
    public void editNote(String newNote){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        String type = "fuel";
        if(item.getType() == 2){
            type = "toilet";
        }else if(item.getType() == 3){
            type = "maintenance";
        }else if(item.getType() == 4){
            type = "atm";
        }
        Call<ResponseBody> call = tour.updateServiceNote(MyApplication.getInstance().getVersion(), item.getId(), newNote, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse create review: " + jsonObject.toString());
                        item.setNote(newNote);

                        data.putExtra("note", newNote);
                        setResult(RESULT_OK, data);

                    }catch (Exception e){
                    e.printStackTrace();}
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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

    /**
     * calculate rating of the services
     * @param list list of reviews
     * @return rating
     */
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

    /**
     * add image to the container
     * @param imgContainer the linear layout contain photos
     */
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

    /**
     * create a new review
     * @param rating rating of the review
     * @param comment comment of the review
     * @param btnReview the add review button, hide once you have 1 review
     */
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

                            //e.trigger(MyApplication.getInstance().getUsername(), builder);

                            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                    .addConverterFactory(GsonConverterFactory.create()).build();
                            final MapAPI tour = retro.create(MapAPI.class);
                            Call<ResponseBody> call2 = tour.addActionReview(MyApplication.getInstance().getUsername(), time,
                                    Integer.toString(item.getId()), type);
                            call2.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if(response.code() == 200){
                                        try{
                                            JSONObject jsonObject1 = new JSONObject(response.body().string());
                                            Log.e("tag", "onResponse: " + jsonObject1.toString() );
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                        Log.e("tag", "onResponse: "+response.code() );
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    t.printStackTrace();
                                }
                            });

                            ActionObject ao = new ActionObject("review " + item.getId(), time,"Review", Integer.toString(item.getId()));

                            if(MyApplication.getInstance().getReviewedMap() == null){
                                MyApplication.getInstance().setReviewedMap(new HashMap<>());
                            }

                            if(MyApplication.getInstance().getReviewedMap().containsKey(type)){
                                MyApplication.getInstance().getReviewedMap().get(type).put("review " + item.getId(), ao);
                            }
                            else{
                                Map<String, ActionObject> map = new HashMap<>();
                                map.put("review " + item.getId(), ao);
                                MyApplication.getInstance().getReviewedMap().put(type, map);
                            }

                            tvNoReview.setVisibility(View.GONE);

                            Log.e("tag", "onResponse create review: " + MyApplication.getInstance().getReviewedMap());
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

    /**
     * update a review of service
     * @param pos position of the review in the list
     * @param id id of the review
     * @param rating new rating
     * @param comment new comment
     * @param dialog dialog confirm
     */
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

    /**
     * used to delete a review of the service
     * @param pos position of the review on the Array list
     * @param id id of the review
     * @param dialog dialog confirm so app can dismiss it after deleting the review
     */
    public void deleteReviews(int pos, int id, Dialog dialog){
       // MapObject item = (MapObject) getIntent().getSerializableExtra("item");
        Log.e("tag", "deleteReviews: "+ id );
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
                                            Log.e("tag", "onResponse del action review: " + response.body().string());
                                        }
                                        else{
                                            Log.e("tag", "onResponse del action review fail: " + response.code());
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

    /**
     * used to load review of the service
     */
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

    /*
    used to check if user add photos
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data2) {
        super.onActivityResult(requestCode, resultCode, data2);
        try {
            if (requestCode == 1) {

                if(imageName.equals("")){
                    imageName = RandomString.getAlphaNumericString(10);
                }

                LinearLayout imgContainer= findViewById(R.id.img_holder);
                if(null == data2) {
//                    arrImg.clear();
//                    paramMap.clear();
//                    bodyMap.clear();
//                    fileName.clear();
                    //EditText edtSelectImg = findViewById(R.id.edt_select_img);
                    //edtSelectImg.setHint(R.string.select_img);
                }else {
                    /*
                    if user only select 1 photo
                     */
                    if (data2.getData() != null) {
//                        arrImg.clear();
//                        paramMap.clear();
//                        bodyMap.clear();
//                        body.clear();
//                        //Uri mImageUri = data.getData();
//                        fileName.clear();

                        /*
                        get path to the file
                         */
                        String path = ImageFilePath.getPath(MapsActivity.this, data2.getData());

                        File file = new File(path);

                        String extension = path.substring(path.lastIndexOf("."));

                        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

//                        EditText edtSelectImg = findViewById(R.id.edt_select_img);

                        /*
                        add photo to the container
                         */
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
                                            JSONObject jsonObject1 = jsonObject.getJSONObject("Paths");
                                            String images = item.getImages();
                                            for (int i = 0; i < jsonObject1.length(); i++) {
                                                JSONObject jsonObject2 = jsonObject1.getJSONObject(f[i]);
                                                if(!images.equals("")){
                                                    images = images +";";
                                                }
                                                images = images + jsonObject2.getString("Message");
                                            }
                                            findViewById(R.id.tv_no_img).setVisibility(View.GONE);

                                            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                                                    .addConverterFactory(GsonConverterFactory.create()).build();
                                            final MapAPI tour = retro.create(MapAPI.class);

                                            String type = "fuel";
                                            if(item.getType() == 2){
                                                type = "toilet";
                                            }else if(item.getType() == 3){
                                                type = "maintenance";
                                            }else if(item.getType() == 4){
                                                type = "atm";
                                            }

                                            final String finalImage = images;

                                            String[] split = images.split(";");

                                            for(int i =0 ; i < split.length; i++) {
                                                Log.e("tag", "onResponse: " + split[i]);
                                            }

                                            Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                                                    .addConverterFactory(GsonConverterFactory.create()).build();
                                            final MapAPI tour2 = retro2.create(MapAPI.class);
                                            Call<ResponseBody> call3 = tour2.addServicePhotos(MyApplication.getInstance().getVersion(),item.getId(), split, type);
                                            call3.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if(response.code() == 200){
                                                        final JSONObject jsonObject;
                                                        try {
                                                            jsonObject = new JSONObject(response.body().string());
                                                            Log.e("", "onResponse create review: " + jsonObject.toString());

                                                            data.putExtra("image", finalImage);
                                                            setResult(RESULT_OK, data);

                                                            item.setImages(finalImage);
                                                        }catch (Exception e){
                                                            e.printStackTrace();}
                                                    }else{
                                                        Log.e("", "onResponse create review: " + response.code());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                }
                                            });

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
                        if (data2.getClipData() != null) {
//                            arrImg.clear();
//                            paramMap.clear();
//                            bodyMap.clear();
//                            fileName.clear();

                            ClipData mClipData = data2.getClipData();

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
                                            JSONObject jsonObject1 = jsonObject.getJSONObject("Paths");
                                            String images = item.getImages();
                                            for (int i = 0; i < jsonObject1.length(); i++) {
                                                JSONObject jsonObject2 = jsonObject1.getJSONObject(f[i]);
                                                if(!images.equals("")){
                                                    images = images +";";
                                                }
                                                images = images + jsonObject2.getString("Message");
                                            }
                                            findViewById(R.id.tv_no_img).setVisibility(View.GONE);

                                            String type = "fuel";
                                            if(item.getType() == 2){
                                                type = "toilet";
                                            }else if(item.getType() == 3){
                                                type = "maintenance";
                                            }else if(item.getType() == 4){
                                                type = "atm";
                                            }

                                            final String finalImage = images;

                                            String[] split = images.split(";");
                                            Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                                                    .addConverterFactory(GsonConverterFactory.create()).build();
                                            final MapAPI tour2 = retro2.create(MapAPI.class);

                                            for(int i =0 ; i < split.length; i++) {
                                                Log.e("tag", "onResponse: " + split[i]);
                                            }

                                            Call<ResponseBody> call3 = tour2.addServicePhotos(MyApplication.getInstance().getVersion(),item.getId(), split, type);call3.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if(response.code() == 200){
                                                        final JSONObject jsonObject;
                                                        try {
                                                            jsonObject = new JSONObject(response.body().string());
                                                            Log.e("", "onResponse create review: " + jsonObject.toString());

                                                            data.putExtra("image", finalImage);
                                                            setResult(RESULT_OK, data);

                                                            item.setImages(finalImage);
                                                        }catch (Exception e){
                                                            e.printStackTrace();}
                                                    }else{
                                                        Log.e("", "onResponse create review: " + response.code());
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                }
                                            });
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

    /*
    upvote a service, increase its points
     */
    public void upvote(boolean isClear) {
        Log.e("tag", "onActivityResult: " + getIntent().getIntExtra("index", -1));
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
                                                Log.e("tag", "onResponse add updoot: " + jsonObject1.toString());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e("tag", "onResponse add updoot: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });

                                ActionObject ao = new ActionObject("upvote " + item.getId(), time, "Upvote", Integer.toString(item.getId()));

                                if(MyApplication.getInstance().getUpvoteMap() == null){
                                    MyApplication.getInstance().setUpvoteMap(new HashMap<>());
                                }
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
                                                Log.e("tag", "onResponse del downvote: " + new JSONObject(response.body().string()).toString());
                                            }
                                            else{
                                                Log.e("tag", "onResponse del downvote: " + response.code() );
                                            }

                                        }catch (Exception e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }

                            Log.e("tag", "onResponse: " + MyApplication.getInstance().getUpvoteMap());
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

    /*
    downvote a service, reduce its points
    isClear is used to check if user is simply remove their previous vote
     */
    public void downvote(boolean isClear) {
        Log.e("tag", "onActivityResult: " + getIntent().getIntExtra("index", -1));
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
                        String type = "";

                        if (item.getType() == 1) {
                            type = "Fuel";
                        } else if (item.getType() == 2) {
                            type = "Toilet";
                        } else if (item.getType() == 3) {
                            type = "Maintenance";
                        } else if (item.getType() == 4) {
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
                                                Log.e("tag", "onResponse add downvote: " + jsonObject1.toString());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e("tag", "onResponse add downvote: " + response.code());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });

                                ActionObject ao = new ActionObject("downvote " + item.getId(), time, "Downvote", Integer.toString(item.getId()));

                                if(MyApplication.getInstance().getUpvoteMap() == null){
                                    MyApplication.getInstance().setDownvoteMap(new HashMap<>());
                                }
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
                                                Log.e("tag", "onResponse del updoot: " + new JSONObject(response.body().string()).toString());
                                            }
                                            else{
                                                Log.e("tag", "onResponse del updoot: " + response.code() );
                                            }

                                        }catch (Exception e) {e.printStackTrace();}
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });


                            }

                            Log.e("tag", "onResponse: " + MyApplication.getInstance().getDownvoteMap());
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

    /*
    check and update user's actions
    isClear is used to check if user is simply remove their previous vote
     */
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
