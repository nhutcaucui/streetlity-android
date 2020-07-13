package com.example.streetlity_android.Contribution;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.Events.EListener;
import com.example.streetlity_android.Events.Event;
import com.example.streetlity_android.Events.GlobalEvents;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.PhotoFullPopupWindow;
import com.example.streetlity_android.R;
import com.example.streetlity_android.Util.ImageFilePath;
import com.example.streetlity_android.Util.RandomString;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AddAMaintenance extends AppCompatActivity implements OnMapReadyCallback {

    ArrayList<File> arrImg = new ArrayList<>();
    List<MultipartBody.Part> body = new ArrayList<>();
    ArrayList<String> fileName = new ArrayList<>();
    String random = "";
    int size=0;

    boolean hasImg = false;

    private GoogleMap mMap;

    boolean firstClick = false;

    double mLat = -500;
    double mLon = -500;

    String mName = "";
    String mNote= "";
    String mAddress = "";
    String mImages[];

    private ViewPager mPager;
    private AddAMaintenance.MyViewPagerAdapter myViewPagerAdapter;
    private ArrayList<Integer> layouts;
    private Button btnPrevious, btnNext;

    ArrayList<String> paramMap = new ArrayList<>();

    EditText edtAddress;

    int step = 0;

    Event<String, String> e = GlobalEvents.Example;

    EListener<String, String> listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_maintenance);

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
                Call<ResponseBody> call = tour.addActionContribute(s, Long.parseLong(split[0]), split[1], split[2]);
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

        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        mPager = findViewById(R.id.view_pager);
        mPager.setOffscreenPageLimit(3);

        layouts = new ArrayList<>();
        layouts.add(R.layout.vp_maintenance_store_info);
        layouts.add(R.layout.vp_maintenance_store_location);
        layouts.add(R.layout.vp_maintenance_success);

        myViewPagerAdapter = new AddAMaintenance.MyViewPagerAdapter();
        mPager.setAdapter(myViewPagerAdapter);
        mPager.addOnPageChangeListener(mPagerPageChangeListener);

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(-1);
                if (current < layouts.size()) {
                    // move to next screen
                    mPager.setCurrentItem(current);
                    step--;
                } else {

                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                boolean isPass = false;

                if(step == 0){
                    EditText edtName = mPager.findViewById(R.id.edt_store_name);
                    if(edtName.getText().toString().equals("")){
                        Toast toast = Toast.makeText(AddAMaintenance.this,  R.string.empty_name, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }
                    else{
                        mName = edtName.getText().toString();
                        EditText edtNote = findViewById(R.id.edt_store_note);
                        mNote = edtNote.getText().toString();
                        isPass = true;
                    }
                }
                else if(step == 1){
                    edtAddress = findViewById(R.id.edt_store_address);
                    if(edtAddress.getText().toString().equals("")){
                        Toast toast = Toast.makeText(AddAMaintenance.this,  R.string.empty_address, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else if(mLon == -500 || mLat == -500){
                        Toast toast = Toast.makeText(AddAMaintenance.this,  R.string.please_select_location, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else{
                        mAddress = edtAddress.getText().toString();
                        addMaintenance();
                    }

                }
                else if(step == 2){
                    finish();
                }


                if(isPass){

                    int current = getItem(+1);
                    if (current < layouts.size()) {
                        // move to next screen
                        mPager.setCurrentItem(current);
                        step++;
                    } else {

                    }
                }
            }


        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        double latitude = 0;
        double longitude = 0;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .GPS_PROVIDER);
            if(location == null){
                Log.e("", "onMapReady: MULL");
            }else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
            }
            Log.e("", "onMapReady: " + latitude+" , " + longitude );
        }


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions opt = new MarkerOptions().position(latLng).title("Here");

                mMap.clear();

                mMap.addMarker(opt);



                mLat = latLng.latitude;
                mLon = latLng.longitude;
            }
        });
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public void callGeocoding(String address){
        Retrofit retro = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.geocode(address, "AIzaSyB56CeF7ccQ9ZeMn0O4QkwlAQVX7K97-Ss");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                final JSONObject jsonObject;
                if(response.code() == 0 || response.code() == 200) {

                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());

                        if(jsonObject.getString("status").equals("ZERO_RESULTS")){
                            Toast toast = Toast.makeText(AddAMaintenance.this, R.string.address_not_found, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        else{
                            mMap.clear();

                            jsonArray = jsonObject.getJSONArray("results");

                            JSONObject jsonObject1;
                            jsonObject1 = jsonArray.getJSONObject(0);

                            JSONObject jsonObjectGeomertry = jsonObject1.getJSONObject("geometry");
                            JSONObject jsonLatLng = jsonObjectGeomertry.getJSONObject("location");

                            mLat = jsonLatLng.getDouble("lat");
                            mLon = jsonLatLng.getDouble("lng");

                            LatLng location = new LatLng(mLat, mLon);

                            MarkerOptions opt = new MarkerOptions().position(location).title("Here");

                            edtAddress.setText(jsonObject1.getString("formatted_address"));

                            mMap.addMarker(opt);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18.0f ));
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        Log.e(", ",response.errorBody().toString() + response.code());
                        Log.e("", "onResponse: " + response.errorBody());

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void addMaintenance(){
        ConstraintLayout csLayout = findViewById(R.id.layout_cant_find_loca);
        csLayout.setVisibility(View.VISIBLE);
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        final MapAPI tour2 = retro2.create(MapAPI.class);

        String token = MyApplication.getInstance().getToken();

        Log.e("", "addATM: "+ mNote+"-"+mLat+"-"+mLon+"-"+mName+"-"+mAddress);

        if(hasImg){
            String[] f = new String[paramMap.size()];
            for(int i=0;i<paramMap.size();i++){
                f[i] = paramMap.get(i);
            }

            Call<ResponseBody> call2 = tour2.upload(f, body);
            call2.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        final JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "onResponse: " + jsonObject.toString());

                            if (jsonObject.getBoolean("Status")) {
                                JSONObject jsonObject1 = jsonObject.getJSONObject("Paths");
                                mImages = new String[fileName.size()];
                                for (int i = 0; i < fileName.size(); i++) {
                                    JSONObject jsonObject2 = jsonObject1.getJSONObject(fileName.get(i));
                                    mImages[i] = jsonObject2.getString("Message");
                                }

                                Call<ResponseBody> call1 = tour.addMaintenance(MyApplication.getInstance().getVersion(),token,(float) mLat,(float) mLon, mAddress, mName, mNote);

                                call1.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200) {
                                            final JSONObject jsonObject;
                                            JSONArray jsonArray;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                Log.e("", "onResponse: " + jsonObject.toString());
                                                if(jsonObject.getBoolean("Status")) {
                                                    btnNext.setText(R.string.finish);

                                                    int current = getItem(+1);
                                                    if (current < layouts.size()) {
                                                        // move to next screen
                                                        mPager.setCurrentItem(current);
                                                        step++;
                                                    }
                                                    csLayout.setVisibility(View.GONE);
                                                    Calendar calendar = Calendar.getInstance();
                                                    long time = calendar.getTimeInMillis();
                                                    String builder = "";
                                                    builder = time+ ";"+ jsonObject.getJSONObject("Service").getInt("Id")
                                                    +";"+ "Maintenance";
                                                    e.trigger(MyApplication.getInstance().getUsername(), builder);

                                                    ActionObject ao = new ActionObject("", time,"Contribute", Integer.toString(jsonObject.getJSONObject("Service").getInt("Id")));

                                                    if(MyApplication.getInstance().getContributeMap().containsKey("Maintenance")){
                                                        MyApplication.getInstance().getContributeMap().get("Maintenance").put("contributed " + jsonObject.getJSONObject("Service").getInt("Id"), ao);
                                                    }
                                                    else{
                                                        Map<String, ActionObject> map = new HashMap<>();
                                                        map.put("contributed " + jsonObject.getJSONObject("Service").getInt("Id"), ao);
                                                        MyApplication.getInstance().getContributeMap().put("Maintenance", map);
                                                    }

                                                    Log.e(TAG, "onResponse: " + MyApplication.getInstance().getContributeMap());
                                                }
                                                //finish();
                                            } catch (Exception e){
                                                e.printStackTrace();
                                                csLayout.setVisibility(View.GONE);
                                                Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                        else{
                                            try {
                                                Log.e(", ",response.errorBody().toString());
                                                csLayout.setVisibility(View.GONE);
                                                Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                csLayout.setVisibility(View.GONE);
                                                Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Log.e("", "onFailure: " + t.toString());
                                        csLayout.setVisibility(View.GONE);
                                        Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();

                                    }
                                });
                            } else {
                                csLayout.setVisibility(View.GONE);
                                Toast toast = Toast.makeText(AddAMaintenance.this, R.string.error_upload, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("", "onFailure: " + t.toString());
                    csLayout.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                }
            });
        }
        else{
            mImages=new String[0];
            Call<ResponseBody> call = tour.addMaintenance(MyApplication.getInstance().getVersion(),token,(float) mLat,(float) mLon, mAddress, mName, mNote);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.code() == 200) {
                        final JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "onResponse: " + jsonObject.toString());
                            if(jsonObject.getBoolean("Status")) {

                                Calendar calendar = Calendar.getInstance();
                                long time = calendar.getTimeInMillis();
                                String builder = "";
                                builder = time+ ";"+ jsonObject.getJSONObject("Service").getInt("Id")
                                        +";"+ "Maintenance";
                                e.trigger(MyApplication.getInstance().getUsername(), builder);

                                ActionObject ao = new ActionObject("", time,"Contribute", Integer.toString(jsonObject.getJSONObject("Service").getInt("Id")));

                                if(MyApplication.getInstance().getContributeMap().containsKey("Maintenance")){
                                    MyApplication.getInstance().getContributeMap().get("Maintenance").put("contributed " + jsonObject.getJSONObject("Service").getInt("Id"), ao);
                                }
                                else{
                                    Map<String, ActionObject> map = new HashMap<>();
                                    map.put("contributed " + jsonObject.getJSONObject("Service").getInt("Id"), ao);
                                    MyApplication.getInstance().getContributeMap().put("Maintenance", map);
                                }

                                Log.e(TAG, "onResponse: " + MyApplication.getInstance().getContributeMap());

                                btnNext.setText(R.string.finish);

                                int current = getItem(+1);
                                if (current < layouts.size()) {
                                    // move to next screen
                                    mPager.setCurrentItem(current);
                                    step++;
                                }
                                csLayout.setVisibility(View.GONE);
                            }
                            //finish();
                        } catch (Exception e){
                            e.printStackTrace();
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                    else{
                        try {
                            Log.e(", ",response.errorBody().toString());
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();

                        }catch (Exception e){
                            e.printStackTrace();
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(AddAMaintenance.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("", "onFailure: " + t.toString());
                }
            });
        }
    }

    ViewPager.OnPageChangeListener mPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == 0 || position == layouts.size()-1) {
                btnPrevious.setVisibility(View.GONE);
            } else {
                btnPrevious.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts.get(position), container, false);

            if(layouts.get(position) == R.layout.vp_maintenance_store_location){
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(AddAMaintenance.this);

                edtAddress = view.findViewById(R.id.edt_store_address);

                ImageButton imgBtnSearch = view.findViewById(R.id.img_btn_search_address);
                imgBtnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getCurrentFocus() != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        }
                        callGeocoding(edtAddress.getText().toString());
                    }
                });
            }

            if(layouts.get(position) == R.layout.vp_maintenance_store_info){
                EditText edtImg = view.findViewById(R.id.edt_select_img);
                edtImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
                    }
                });
            }

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private int getItem(int i) {
        return mPager.getCurrentItem() + i;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1) {
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

                        String path = ImageFilePath.getPath(AddAMaintenance.this, data.getData());

                        File file = new File(path);

                        String extension = path.substring(path.lastIndexOf("."));

                        Log.e("", "onActivityResult: " + arrImg.size()+extension);

                        EditText edtSelectImg = findViewById(R.id.edt_select_img);

                        hasImg = true;

                        if(random.equals("")) {
                            String generatedString = RandomString.getAlphaNumericString(10);
                            random = generatedString;
                        }

                        RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part mBody =
                                MultipartBody.Part.createFormData(random+size+extension, file.getName(), fbody);

                        body.add(mBody);

                        fileName.add(random+size+extension);

                        paramMap.add(random+size+extension);

                        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                        ImageView img = new ImageView(AddAMaintenance.this);
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
                                new PhotoFullPopupWindow(AddAMaintenance.this, R.layout.popup_photo_full, img, "", bitmap);
                            }
                        });
                        img.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog alertDialog = new AlertDialog.Builder(AddAMaintenance.this).create();
                                alertDialog.setTitle(getString(R.string.remove)+"?");
                                alertDialog.setMessage(getString(R.string.remove_pic));
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                int idToDel = ((LinearLayout) img.getParent()).indexOfChild(img);
                                                arrImg.remove(idToDel);
                                                fileName.remove(idToDel);
                                                paramMap.remove(idToDel);
                                                body.remove(idToDel);
                                                if(arrImg.size()==0){
                                                    hasImg=false;
                                                    EditText edtSelectImg = findViewById(R.id.edt_select_img);
                                                    edtSelectImg.setHint(R.string.select_img);
                                                }else{
                                                    String temp = getString(R.string.selected);
                                                    temp = temp + " "+arrImg.size()+" " + getString(R.string.images);
                                                    edtSelectImg.setHint(temp);
                                                }

                                                imgContainer.removeViewAt(idToDel);

                                                dialog.dismiss();
                                            }
                                        });

                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                return false;
                            }
                        });
                        img.setTag(size);
                        imgContainer.addView(img);

                        arrImg.add(file);

                        size++;
                        String temp = getString(R.string.selected);
                        temp = temp + " "+arrImg.size()+" " + getString(R.string.images);
                        edtSelectImg.setHint(temp);
                        //bodyMap.put(generatedString+0, body);
                    } else {
                        if (data.getClipData() != null) {
//                            arrImg.clear();
//                            paramMap.clear();
//                            bodyMap.clear();
//                            fileName.clear();

                            ClipData mClipData = data.getClipData();

                            //body.clear();

                            if(random.equals("")) {
                                String generatedString = RandomString.getAlphaNumericString(10);
                                random = generatedString;
                            }

                            EditText edtSelectImg = findViewById(R.id.edt_select_img);

                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                String path = ImageFilePath.getPath(AddAMaintenance.this, uri);

                                File file = new File(path);

                                String extension = path.substring(path.lastIndexOf("."));

                                paramMap.add(random+size+extension);

                                RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                MultipartBody.Part mBody =
                                        MultipartBody.Part.createFormData(random+size+extension, file.getName(), fbody);

                                body.add(mBody);

                                fileName.add(random+size+extension);

                                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                                ImageView img = new ImageView(AddAMaintenance.this);
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
                                        new PhotoFullPopupWindow(AddAMaintenance.this, R.layout.popup_photo_full, img, "", bitmap);
                                    }
                                });
                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(AddAMaintenance.this).create();
                                        alertDialog.setTitle(getString(R.string.remove)+"?");
                                        alertDialog.setMessage(getString(R.string.remove_pic));
                                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        int idToDel = ((LinearLayout) img.getParent()).indexOfChild(img);
                                                        arrImg.remove(idToDel);
                                                        fileName.remove(idToDel);
                                                        paramMap.remove(idToDel);
                                                        body.remove(idToDel);
                                                        if(arrImg.size()==0){
                                                            hasImg=false;
                                                            EditText edtSelectImg = findViewById(R.id.edt_select_img);
                                                            edtSelectImg.setHint(R.string.select_img);
                                                        }else{
                                                            String temp = getString(R.string.selected);
                                                            temp = temp + " "+arrImg.size()+" " + getString(R.string.images);
                                                            edtSelectImg.setHint(temp);
                                                        }
                                                        imgContainer.removeViewAt(idToDel);

                                                        dialog.dismiss();
                                                    }
                                                });

                                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                        return false;
                                    }
                                });
                                img.setTag(size);
                                imgContainer.addView(img);

                                arrImg.add(file);
                                size++;
                            }

                            Log.e("", "onActivityResult: " + arrImg.size());

                            String temp = getString(R.string.selected);
                            temp = temp + " " + arrImg.size() + " " + getString(R.string.images);
                            edtSelectImg.setHint(temp);
                            hasImg = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void onStop(){
        super.onStop();
        e.unsubcribe(listener);
    }
}
