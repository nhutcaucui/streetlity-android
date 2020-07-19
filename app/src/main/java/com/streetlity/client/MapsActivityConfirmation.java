package com.streetlity.client;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.streetlity.client.Achievement.ActionObject;
import com.streetlity.client.Events.EListener;
import com.streetlity.client.Events.Event;
import com.streetlity.client.Events.GlobalEvents;
import com.streetlity.client.MainFragment.MapObject;
import com.streetlity.client.User.UserInfoOther;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONObject;

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

        item = (MapObject) getIntent().getSerializableExtra("item");

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
                                //Log.e("TAG", "onResponse: " + jsonObject1.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            //Log.e(TAG, "onResponse: " + response.code());
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
                        Call<ResponseBody> call = tour.deleteActionUpvote(Integer.toString(item.getId()), type, MyApplication.getInstance().getUsername());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try{
                                    if(response.code()==200){
                                        //Log.e(TAG, "onResponse: " + new JSONObject(response.body().string()).toString());
                                        downvote(true);
                                    }
                                    else{
                                        //Log.e(TAG, "onResponse: " + response.code() );
                                    }

                                }catch (Exception e) {e.printStackTrace();}
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    } else {
                        MyApplication.getInstance().getDownvoteMap().get(type).remove("downvote " + item.getId());

                        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                                .addConverterFactory(GsonConverterFactory.create()).build();
                        final MapAPI tour = retro.create(MapAPI.class);
                        Call<ResponseBody> call = tour.deleteActionDownvote(Integer.toString(item.getId()), type, MyApplication.getInstance().getUsername());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try{
                                    if(response.code()==200){
                                        //Log.e(TAG, "onResponse: " + response.body().string());
                                        upvote(true);
                                        finish();
                                    }
                                    else{
                                        //Log.e(TAG, "onResponse: " + response.code() );
                                    }

                                }catch (Exception e) {e.printStackTrace();}
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }

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
                    upvote(false);
                }
            });

            btnNonExist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downvote(false);

                }
            });
        }

        BottomSheetBehavior sheetBehavior;
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);



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

        LinearLayout layoutNote = findViewById(R.id.layout_note);
        TextView tvNote = findViewById(R.id.tv_note);
        if(!item.getNote().equals("")) {
            //layoutNote.setVisibility(View.VISIBLE);
            tvNote.setText(item.getNote());
        }else{
            tvNote.setText(getString(R.string.no_note));
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

//    public float calculateRating(ArrayList<Review> list) {
//        float temp = 0;
//        for (int i = 0; i < list.size(); i++) {
//            temp += list.get(i).rating;
//        }
//
//        temp = temp / list.size();
//
//        return temp;
//    }

    /**
     * load img to the linear layout
     * @param imgContainer
     */
    public void addImages(LinearLayout imgContainer) {
        ProgressBar loading = findViewById(R.id.loading_img);
        loading.setVisibility(View.VISIBLE);
        if (!item.getImages().equals("")) {
            String[] split = item.getImages().split(";");
            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                    .addConverterFactory(GsonConverterFactory.create()).build();
            final MapAPI tour = retro.create(MapAPI.class);
            for (int i = 0; i < split.length; i++) {
                //Log.e("", "addImages: " + split[i]);
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

    public void upvote(boolean isClear) {
        //Log.e(TAG, "onActivityResult: " + getIntent().getIntExtra("index", -1));
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
                        //Log.e("", "onResponse: " + response);
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());

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

                            if(!isClear) {
                                e.trigger(MyApplication.getInstance().getUsername(), builder);
                                ActionObject ao = new ActionObject("upvote " + item.getId(), time, "Upvote", Integer.toString(item.getId()));

                                if (MyApplication.getInstance().getUpvoteMap().containsKey(type)) {
                                    MyApplication.getInstance().getUpvoteMap().get(type).put("upvote " + item.getId(), ao);
                                } else {
                                    Map<String, ActionObject> map = new HashMap<>();
                                    map.put("upvote " + item.getId(), ao);
                                    MyApplication.getInstance().getUpvoteMap().put(type, map);
                                }
                            }else{
                                Intent data2 = new Intent();
                                data2.putExtra("index", getIntent().getIntExtra("index", -1));
                                data2.putExtra("action", 3);
                                setResult(RESULT_OK, data2);
                                finish();
                            }

                            //Log.e(TAG, "onResponse: " + MyApplication.getInstance().getUpvoteMap());

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
        //Log.e(TAG, "onActivityResult: " + getIntent().getIntExtra("index", -1));
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
                        //Log.e("", "onResponse: " + response);
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());

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
                            data.putExtra("action", 2);
                            setResult(RESULT_OK, data);
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
                                                //Log.e("TAG", "onResponse: " + jsonObject1.toString());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            //Log.e(TAG, "onResponse: " + response.code());
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
                                Intent data2 = new Intent();
                                data2.putExtra("index", getIntent().getIntExtra("index", -1));
                                data2.putExtra("action", 3);
                                setResult(RESULT_OK, data2);
                                finish();
                            }

                            //Log.e(TAG, "onResponse: " + MyApplication.getInstance().getDownvoteMap());

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