package com.example.streetlity_android;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.ProgressBar;
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
import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.Events.EListener;
import com.example.streetlity_android.Events.Event;
import com.example.streetlity_android.Events.GlobalEvents;
import com.example.streetlity_android.MainFragment.MapObject;
import com.example.streetlity_android.User.UserInfoOther;
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

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MapsActivityConfirmation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Marker currentPosition;
    MapObject item;

    Event<String, String> e = GlobalEvents.Example;

    EListener<String, String> listener;

    ArrayList<MarkerOptions> mMarkers = new ArrayList<MarkerOptions>();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        findViewById(R.id.layout_vote).setVisibility(View.GONE);

        listener = new EListener<String, String>() {
            @Override
            public void trigger(String s, String s2) {
                String[] split = s2.split(";", -1);
                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                        .addConverterFactory(GsonConverterFactory.create()).build();
                final MapAPI tour = retro.create(MapAPI.class);
                Call<ResponseBody> call = tour.addActionUpvote(s, Long.parseLong(split[0]), split[1], split[2]);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {
                                JSONObject jsonObject1 = new JSONObject(response.body().string());
                                Log.e("TAG", "onResponse: " + jsonObject1.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "onResponse: " + response.code());
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

        LinearLayout llReview = findViewById(R.id.ll_review);
        llReview.setVisibility(View.GONE);


        if (item.isDownvoted() || item.isUpvoted()) {
            Button btnClearVote = findViewById(R.id.btn_clear);
            btnClearVote.setVisibility(View.VISIBLE);

            btnClearVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra("index", getIntent().getIntExtra("index", -1));
                    data.putExtra("action", 3);
                    setResult(RESULT_OK, data);

                    String type = "";

                    switch (item.getType()) {
                        case 1: {
                            type = "Fuel";
                            break;
                        }
                        case 2: {
                            type = "Toilet";
                            break;
                        }
                        case 3: {
                            type = "Maintenance";
                            break;
                        }
                        case 4: {
                            type = "Atm";
                            break;
                        }

                    }
                    if (item.isUpvoted()) {
                        MyApplication.getInstance().getUpvoteMap().get(type).remove("upvote " + item.getId());

                        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                .addConverterFactory(GsonConverterFactory.create()).build();
                        final MapAPI tour = retro.create(MapAPI.class);
                        Call<ResponseBody> call = tour.deleteActionUpvote(Integer.toString(item.getId()), type);

                    } else {
                        //reserve for downvote
                    }
                    finish();
                }
            });

        } else {
            Button btnExist = findViewById(R.id.btn_exist);
            Button btnNonExist = findViewById(R.id.btn_non_exist);
            LinearLayout confirmingLayout = findViewById(R.id.layout_confirming);
            confirmingLayout.setVisibility(View.VISIBLE);

            btnExist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upvote();
                }
            });

            btnNonExist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra("index", getIntent().getIntExtra("index", -1));
                    data.putExtra("action", 2);
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
        }

        BottomSheetBehavior sheetBehavior;
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        item = (MapObject) getIntent().getSerializableExtra("item");

        LinearLayout layoutNote = findViewById(R.id.layout_note);
        TextView tvNote = findViewById(R.id.tv_note);
        if (!item.getNote().equals("")) {
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
        if (distance > 1000) {
            dis = "km";
            distance = distance / 1000;
        }
        tvDistance.setText("~" + df.format(distance) + dis);

        TextView tvRating = findViewById(R.id.tv_rating);
        tvRating.setText("(" + df.format(item.getRating()) + ")");

        RatingBar rb = findViewById(R.id.ratingbar_map_object);
        rb.setRating(item.getRating());

        LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        LinearLayout imgContainer = findViewById(R.id.layout_container);
        addImages(imgContainer);

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
                Intent t = new Intent(MapsActivityConfirmation.this, UserInfoOther.class);
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
                        if (sheetBehavior.getPeekHeight() < 150) {
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
        desOption.position(new LatLng(item.getLat(), item.getLon()));
        if (item.getType() == 1)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_fuel));
        else if (item.getType() == 2)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_wc));
        else if (item.getType() == 3)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        else if (item.getType() == 4)
            desOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));

        mMap.addMarker(desOption);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLat(), item.getLon()), 15f));

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();

        return true;
    }

    public float calculateRating(ArrayList<Review> list) {
        float temp = 0;
        for (int i = 0; i < list.size(); i++) {
            temp += list.get(i).rating;
        }

        temp = temp / list.size();

        return temp;
    }

    public void addImages(LinearLayout imgContainer) {
        ProgressBar loading = findViewById(R.id.loading_img);
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
                                ImageView img = new ImageView(MapsActivityConfirmation.this);
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
                                        new PhotoFullPopupWindow(MapsActivityConfirmation.this, R.layout.popup_photo_full, img, "", bitmap);
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
            TextView tvNoImg = findViewById(R.id.tv_no_img);
            tvNoImg.setVisibility(View.VISIBLE);
        }
    }

    public void upvote() {
        Log.e(TAG, "onActivityResult: " + getIntent().getIntExtra("index", -1));
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.upvoteATM("1.0.0", item.getId(), MyApplication.getInstance().getUsername());
        if (item.getType() == 1) {
            call = tour.upvoteFuel("1.0.0", item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 2) {
            call = tour.upvoteWC("1.0.0", item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 3) {
            call = tour.upvoteMaintenance("1.0.0", item.getId(), MyApplication.getInstance().getUsername());
        } else if (item.getType() == 4) {
            call = tour.upvoteATM("1.0.0", item.getId(), MyApplication.getInstance().getUsername());
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        Log.e("", "onResponse: " + response);
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());

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
                            Intent data = new Intent();
                            data.putExtra("index", getIntent().getIntExtra("index", -1));
                            data.putExtra("action", 1);
                            setResult(RESULT_OK, data);

                            e.trigger(MyApplication.getInstance().getUsername(), builder);
                            ActionObject ao = new ActionObject("upvote " + item.getId(), time, "Upvote", Integer.toString(item.getId()));

                            if (MyApplication.getInstance().getUpvoteMap().containsKey(type)) {
                                MyApplication.getInstance().getUpvoteMap().get(type).put("upvote " + item.getId(), ao);
                            } else {
                                Map<String, ActionObject> map = new HashMap<>();
                                map.put("upvote " + item.getId(), ao);
                                MyApplication.getInstance().getUpvoteMap().put(type, map);
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
}