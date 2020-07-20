package com.streetlity.client.User;

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
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.streetlity.client.MapAPI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.streetlity.client.MapFragment.MaintenanceObject;
import com.streetlity.client.MyApplication;
import com.streetlity.client.PhotoFullPopupWindow;
import com.streetlity.client.R;
import com.streetlity.client.Util.ImageFilePath;
import com.streetlity.client.Util.RandomString;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.streetlity.client.MainNavigationHolder.hasPermissions;

public class SignUp extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    ArrayList<Marker> mMarkers = new ArrayList<>();
    ArrayList<MaintenanceObject> items = new ArrayList<>();
    ArrayList<File> arrImg = new ArrayList<>();
    List<MultipartBody.Part> body = new ArrayList<>();
    ArrayList<String> fileName = new ArrayList<>();
    
    boolean hasImg = false;

    private GoogleMap mMap;
    String username = "";
    String pass = "";
    String cfPass = "";
    String mail = "";
    String phone = "";
    String address = "";
    int type = -1;
    int id = -1;
    String mName = "";
    double mLat = -500;
    double mLon = -500;
    String mNote= "";
    String mAddress = "";
    String mImages[];
    String random = "";
    int size = 0;

    int exist=-1;

    boolean changed = false;

    private ViewPager mPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private ArrayList<Integer> layouts;
    private Button btnPrevious, btnNext;

    ArrayList<String> paramMap = new ArrayList<>();

    int step = 0;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private MyViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        String[] Permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, 4);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            this.finish();
            return;
        }

        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        mPager = findViewById(R.id.view_pager);
        mPager.setOffscreenPageLimit(7);

        layouts = new ArrayList<>();
        layouts.add(R.layout.vp_signup_select_user_type);
        layouts.add(R.layout.vp_signup_main_info);
        layouts.add(R.layout.vp_signup_additional_info);

        myViewPagerAdapter = new MyViewPagerAdapter();
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
                boolean isPass = false;

                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                // checking for last page
                // if last page home screen will be launched
                if(step == 0){
                    RadioButton rdoCommon = mPager.findViewById(R.id.rdo_common);
                    RadioButton rdoOwner = mPager.findViewById(R.id.rdo_owner);
                    if(rdoCommon.isChecked()){
                        type = 0;
                        if(layouts.contains(R.layout.vp_signup_store_exist)) {
                            layouts.remove(3);
                            myViewPagerAdapter.notifyDataSetChanged();

                            changed = true;
                        }
                        if(!layouts.contains(R.layout.vp_signup_success)) {
                            layouts.add(R.layout.vp_signup_success);
                            myViewPagerAdapter.notifyDataSetChanged();
                        }
                        isPass = true;
                    }
                    else if(rdoOwner.isChecked()){
                        type = 1;
                        if(layouts.contains(R.layout.vp_signup_success)) {
                            layouts.remove(3);
                            myViewPagerAdapter.notifyDataSetChanged();
                            changed = true;
                        }
                        if(!layouts.contains(R.layout.vp_signup_store_exist)) {
                            layouts.add(R.layout.vp_signup_store_exist);
                            myViewPagerAdapter.notifyDataSetChanged();
                        }
                        isPass = true;
                    }
                    else if(!rdoCommon.isChecked() && !rdoOwner.isChecked()){
                        Toast toast = Toast.makeText(SignUp.this, R.string.please_select_type, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }

                }

                if(step ==1){
                    com.google.android.material.textfield.TextInputEditText edtUsername = mPager.findViewById(R.id.edt_username);
                    com.google.android.material.textfield.TextInputEditText edtEmail = mPager.findViewById(R.id.edt_email);
                    TextInputEditText edtPassword = mPager.findViewById(R.id.edt_password);
                    TextInputEditText edtCFPassword = mPager.findViewById(R.id.edt_cfpassword);

                    if(edtUsername.getText().toString().equals("")){
                        Toast toast = Toast.makeText(SignUp.this, R.string.empty_user_name, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }else if(edtUsername.getText().length() < 6){
                        Toast toast = Toast.makeText(SignUp.this, R.string.username_short, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }else if(edtEmail.getText().toString().equals("")){
                        Toast toast = Toast.makeText(SignUp.this, R.string.empty_user_mail, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }else if(!isValid(edtEmail.getText().toString())){
                        Toast toast = Toast.makeText(SignUp.this, R.string.invalid_email, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }
                    else if(edtPassword.getText().toString().equals("")){
                        Toast toast = Toast.makeText(SignUp.this, R.string.empty_pass, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else if(edtPassword.getText().toString().length() < 6){
                        Toast toast = Toast.makeText(SignUp.this, R.string.pass_short, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }
                    else if(edtCFPassword.getText().toString().equals("")){
                        Toast toast = Toast.makeText(SignUp.this, R.string.empty_cf_pass, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    } else if(!edtCFPassword.getText().toString().equals(edtPassword.getText().toString())){
                        Toast toast = Toast.makeText(SignUp.this, R.string.password_mismatch, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } else{
                        username = edtUsername.getText().toString();
                        mail = edtEmail.getText().toString();
                        pass = edtPassword.getText().toString();
                        cfPass = edtCFPassword.getText().toString();
                        validateUsername(username, mail);
                    }
                }

                if(step == 2){
                    EditText edtAddress = mPager.findViewById(R.id.edt_address);
                    EditText edtPhone = mPager.findViewById(R.id.edt_phone);

                    if(edtPhone.getText().toString().equals("")){
                        Toast toast = Toast.makeText(SignUp.this, R.string.phone, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }else {

                        address = edtAddress.getText().toString();
                        phone = edtPhone.getText().toString();

                        //Log.e("", "onClick: "+phone);

                        isPass = true;
                    }
                }

                if(step == 2 && type == 0){
                    isPass=false;
                    //Log.e("", "onClick: "+phone);
                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                            .addConverterFactory(GsonConverterFactory.create()).build();
                    final MapAPI tour = retro.create(MapAPI.class);

                    Call<ResponseBody> call = tour.signUpCommon(username,pass,mail,phone,address);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    //Log.e("", "onResponse: " + jsonObject.toString());
                                    if (jsonObject.getBoolean("Status")) {

                                        btnNext.setText(R.string.finish);

                                        int current = getItem(+1);
                                        if (current < layouts.size()) {
                                            // move to next screen
                                            mPager.setCurrentItem(current);
                                            step++;
                                        }
                                    } else {
                                        Toast toast = Toast.makeText(SignUp.this, jsonObject.getString("Message"), Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    //Log.e("", "onResponse: " + response.code());
                                    Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                    tv.setTextColor(Color.RED);

                                    toast.show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //Log.e("", "onFailure: " + t.toString());
                        }
                    });
                }


                if(step == 3 && type ==0){
                    Intent t = getIntent();
                    int from = t.getIntExtra("from", 1);
                    if(from == 1){
                        Intent t2 = new Intent(SignUp.this, Login.class);
                        t2.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                        startActivity(t2);

                        finish();
                    }
                    else if (from == 2){
                        finish();
                    }
                }else if (step == 3 && type == 1) {
                    RadioButton rdoYes = mPager.findViewById(R.id.rdo_yes);
                    RadioButton rdoNo = mPager.findViewById(R.id.rdo_no);
                    if(!rdoYes.isChecked() && !rdoNo.isChecked()){
                        Toast toast = Toast.makeText(SignUp.this, R.string.please_select_option, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();
                    }
                    else if(rdoNo.isChecked()){
                        exist = 0;
                        if(layouts.contains(R.layout.vp_signup_store_select) &&
                                layouts.contains(R.layout.vp_signup_success)) {
                            layouts.remove(5);
                            layouts.remove(4);
                            myViewPagerAdapter.notifyDataSetChanged();

                            changed = true;
                        }
                        if(!layouts.contains(R.layout.vp_signup_store_location) &&
                                !layouts.contains(R.layout.vp_signup_store_info) &&
                                !layouts.contains(R.layout.vp_signup_success)) {
                            layouts.add(R.layout.vp_signup_store_info);
                            layouts.add(R.layout.vp_signup_store_location);
                            layouts.add(R.layout.vp_signup_success);
                            myViewPagerAdapter.notifyDataSetChanged();
                        }
                        isPass = true;
                    }
                    else if(rdoYes.isChecked()){
                        exist = 1;
                        if(layouts.contains(R.layout.vp_signup_store_location) &&
                                layouts.contains(R.layout.vp_signup_store_info) &&
                                layouts.contains(R.layout.vp_signup_success)) {
                            layouts.remove(6);
                            layouts.remove(5);
                            layouts.remove(4);
                            myViewPagerAdapter.notifyDataSetChanged();

                            changed = true;
                        }
                        if(!layouts.contains(R.layout.vp_signup_store_select) &&
                                !layouts.contains(R.layout.vp_signup_success)) {
                            layouts.add(R.layout.vp_signup_store_select);
                            layouts.add(R.layout.vp_signup_success);
                            myViewPagerAdapter.notifyDataSetChanged();
                        }
                        isPass = true;
                    }
                }

                if(exist == 0){
                    if (step == 5){
                        EditText edtStoreAddress = mPager.findViewById(R.id.edt_store_address);
                        if(edtStoreAddress.getText().toString().equals("")){
                            Toast toast = Toast.makeText(SignUp.this, R.string.empty_address, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        else if(mLat == -500 || mLon == -500){
                            Toast toast = Toast.makeText(SignUp.this, R.string.please_select_location, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        else{
                            mAddress = edtStoreAddress.getText().toString();
                            //Log.e("", "onClick: " + mAddress );
                            signUpMaintainNonService();
                        }
                    }
                    if (step == 4){
                        EditText edtName = mPager.findViewById(R.id.edt_store_name);
                        EditText edtNote = mPager.findViewById(R.id.edt_store_note);
                        if(edtName.getText().toString().equals("")){
                            Toast toast = Toast.makeText(SignUp.this, R.string.empty_name, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        } else{
                            mName = edtName.getText().toString();
                            mNote=edtNote.getText().toString();
                            //addMaintenance();
                            isPass = true;
                        }
                    }
                    if (step == 6){
                        Intent t = getIntent();
                        int from = t.getIntExtra("from", 1);
                        if(from == 1){
                            Intent t2 = new Intent(SignUp.this, Login.class);
                            t2.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            startActivity(t2);

                            finish();
                        }
                        else if (from == 2){
                            finish();
                        }
                    }
                }else if (exist == 1){
                    if (step == 4){
                        if(id == -1){
                            Toast toast = Toast.makeText(SignUp.this, R.string.please_select_store, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }else{
                            signUpMaintain();
                           // isPass = true;
                        }
                    }
                    if (step == 5){
                        Intent t = getIntent();
                        int from = t.getIntExtra("from", 1);
                        if(from == 1){
                            Intent t2 = new Intent(SignUp.this, Login.class);
                            t2.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            startActivity(t2);

                            finish();
                        }
                        else if (from == 2){
                            finish();
                        }
                    }
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


//        final EditText edtUsername = findViewById(R.id.edt_username);
//        final EditText edtPassword = findViewById(R.id.edt_password);
//        final EditText edtCFPassword = findViewById(R.id.edt_cfpassword);
//        final EditText edtMail = findViewById(R.id.edt_email);
//        final EditText edtPhone = findViewById(R.id.edt_phone);
//        final EditText edtAddress = findViewById(R.id.edt_address);

//        Button btnSighUp = findViewById(R.id.btn_signup);
//        btnSighUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getCurrentFocus() != null) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//                }
//
//



    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener mPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == 0 || position == layouts.size()-1) {
                btnPrevious.setVisibility(View.GONE);

                if((step == 2 && type == 1)){
                    btnPrevious.setVisibility(View.VISIBLE);
                }

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

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts.get(position), container, false);

            if(layouts.get(position) == R.layout.vp_signup_store_location){
                EditText edtAddress = view.findViewById(R.id.edt_store_address);

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

            if(layouts.get(position) == R.layout.vp_signup_store_select ||
                    layouts.get(position) == R.layout.vp_signup_store_location){
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(SignUp.this);
            }

            if(layouts.get(position) == R.layout.vp_signup_success){
                if(type == 1){
                    TextView tvSuccess = view.findViewById(R.id.tv_success);
                    tvSuccess.setText(R.string.signup_success_owner);
                }
            }

            if(layouts.get(position) == R.layout.vp_signup_store_info){
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

        @Override
        public int getItemPosition(Object object)
        {
            if (changed) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }
    }

    private int getItem(int i) {
        return mPager.getCurrentItem() + i;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        double latitude = 0;
        double longitude = 0;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .GPS_PROVIDER);
            if(location == null){
                //Log.e("", "onMapReady: MULL");
            }else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //Log.e("", "onMapReady: " + latitude + " , " + longitude);
            }
        }

        if(exist == 1) {
            getLocations((float) latitude, (float) longitude);
        } else if (exist == 0){
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mMap.clear();
                    MarkerOptions opt = new MarkerOptions().position(latLng).title("Here");

                    mMap.addMarker(opt);

                    mLat = latLng.latitude;
                    mLon = latLng.longitude;
                }
            });
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
    }

    public void getLocations(float lat, float lon){
        mMap.clear();
        mMarkers.removeAll(mMarkers);
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange(MyApplication.getInstance().getVersion(),(float)lat,(float)lon,(float)0.1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());
                        if(jsonObject.getBoolean("Status")) {
                            jsonArray = jsonObject.getJSONArray("Services");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                //Log.e("", "onResponse: " + jsonObject1.toString());
                                addMaintenanceMarkerToList((float) jsonObject1.getDouble("Lat"),
                                        (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Name"));

                                MaintenanceObject object = new MaintenanceObject(jsonObject1.getInt("Id"),
                                        jsonObject1.getString("Name"), (float) jsonObject1.getDouble("Lat"),
                                        (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"),
                                        jsonObject1.getString("Address"));

                                items.add(object);
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        //Log.e(", ",response.errorBody().toString() + response.code());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e("", "onFailure: " + t.toString());
            }
        });
    }

    public void addMaintenanceMarkerToList(float lat, float lon, String name){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(name);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        option.position(pos);
        Marker marker = mMap.addMarker(option);
        mMarkers.add(marker);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        marker.showInfoWindow();
        if(exist == 1) {
            for (int i = 0; i < mMarkers.size(); i++) {
                if (marker.equals(mMarkers.get(i))) {
                    id = items.get(i).getId();
                    EditText edtStore = mPager.findViewById(R.id.edt_store);
                    edtStore.setText(items.get(i).getName());
                    break;
                }
            }
        }
        return true;
    }

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
                        //Log.e("", "onResponse: " + jsonObject.toString());

                        if(jsonObject.getString("status").equals("ZERO_RESULTS")){
                            Toast toast = Toast.makeText(SignUp.this, "Address not found", Toast.LENGTH_LONG);
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

                            LatLng location = new LatLng(mLat,mLon);

                            MarkerOptions opt = new MarkerOptions().position(location).title("Here");

                            EditText edtAddress = mPager.findViewById(R.id.edt_address);

                            edtAddress.setText(jsonObject1.getString("formatted_address"));

                            mMap.addMarker(opt);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18f));
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        //Log.e(", ",response.errorBody().toString() + response.code());
                        //Log.e("", "onResponse: " + response.errorBody());

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

    public void signUpMaintain(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

        Call<ResponseBody> call = tour.signUpMaintainer(username, pass, mail,phone,address, id);
        //Log.e("", "signUpMaintain: "+"-"+username+ "-"+pass +"-"+mail+"-"+id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject;
                if (response.code() == 200) {
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: "+ jsonObject.toString() + response.code());
                        if(jsonObject.getBoolean("Status")) {
                            btnNext.setText(R.string.finish);

                            int current = getItem(+1);
                            if (current < layouts.size()) {
                                // move to next screen
                                mPager.setCurrentItem(current);
                                step++;
                            }
                        }else{
                            Toast toast = Toast.makeText(SignUp.this, jsonObject.getString("Message"), Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    try {
                        //Log.e("", "onResponse: "+  response.code());
                        Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e("", "onFailure: " + t.toString());
            }
        });
    }

    public void signUpMaintainNonService(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

        Call<ResponseBody> call = tour.signUpMaintainerNonService(username, pass, mail,phone,address, mName
                ,mLat,mLon,mAddress,mNote,mImages, -1);
        //Log.e("", "signUpMaintainNon: "+"-"+username+ "-"+pass +"-"+mail+"-"+id+"-"+mLat+"-"+mLon);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                JSONObject jsonObject;
                if (response.code() == 200) {
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: "+ jsonObject.toString() + response.code());
                        if(jsonObject.getBoolean("Status")) {
                            btnNext.setText(R.string.finish);

                            int current = getItem(+1);
                            if (current < layouts.size()) {
                                // move to next screen
                                mPager.setCurrentItem(current);
                                step++;
                            }
                        }else{
                            Toast toast = Toast.makeText(SignUp.this, jsonObject.getString("Message"), Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    try {
                        //Log.e("", "onResponse: "+  response.code());
                        Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e("", "onFailure: " + t.toString());
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
                            //Log.e("", "onResponse: " + jsonObject.toString());

                            if (jsonObject.getBoolean("Status")) {
                                JSONObject jsonObject1 = jsonObject.getJSONObject("Paths");
                                mImages = new String[fileName.size()];
                                for (int i = 0; i < fileName.size(); i++) {
                                    JSONObject jsonObject2 = jsonObject1.getJSONObject(fileName.get(i));
                                    mImages[i] = jsonObject2.getString("Message");
                                }

                                Call<ResponseBody> call1 = tour.addMaintenance(MyApplication.getInstance().getVersion(),token,(float) mLat,(float) mLon, mAddress, mName, mImages,mNote);

                                call1.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200) {
                                            final JSONObject jsonObject;
                                            JSONArray jsonArray;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                //Log.e("", "onResponse: " + jsonObject.toString());
                                                if(jsonObject.getBoolean("Status")) {
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
                                                Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                        else{
                                            try {
                                                //Log.e(", ",response.errorBody().toString());
                                                csLayout.setVisibility(View.GONE);
                                                Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                csLayout.setVisibility(View.GONE);
                                                Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        //Log.e("", "onFailure: " + t.toString());
                                        csLayout.setVisibility(View.GONE);
                                        Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();

                                    }
                                });
                            } else {
                                csLayout.setVisibility(View.GONE);
                                Toast toast = Toast.makeText(SignUp.this, R.string.error_upload, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //Log.e("", "onFailure: " + t.toString());
                    csLayout.setVisibility(View.GONE);
                    Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                }
            });
        }
        else{
            mImages=new String[0];
            Call<ResponseBody> call = tour.addMaintenance(MyApplication.getInstance().getVersion(),token,(float) mLat,(float) mLon, mAddress, mName,mImages,mNote);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.code() == 200) {
                        final JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            //Log.e("", "onResponse: " + jsonObject.toString());
                            if(jsonObject.getBoolean("Status")) {
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
                            Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                    else{
                        try {
                            //Log.e(", ",response.errorBody().toString());
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();

                        }catch (Exception e){
                            e.printStackTrace();
                            csLayout.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(SignUp.this, "!", Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //Log.e("", "onFailure: " + t.toString());
                }
            });
        }
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

                        String path = ImageFilePath.getPath(SignUp.this, data.getData());

                        File file = new File(path);

                        String extension = path.substring(path.lastIndexOf("."));

                        //Log.e("", "onActivityResult: " + arrImg.size());

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
                        ImageView img = new ImageView(SignUp.this);
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
                                new PhotoFullPopupWindow(SignUp.this, R.layout.popup_photo_full, img, "", bitmap);
                            }
                        });
                        img.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();
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
                                String path = ImageFilePath.getPath(SignUp.this, uri);

                                File file = new File(path);

                                String extension = path.substring(path.lastIndexOf("."));

                                paramMap.add(random+size+extension);

                                RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                MultipartBody.Part mBody =
                                        MultipartBody.Part.createFormData(random+size+extension, file.getName(), fbody);

                                body.add(mBody);

                                fileName.add(random+size+extension);

                                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                                ImageView img = new ImageView(SignUp.this);
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
                                        new PhotoFullPopupWindow(SignUp.this, R.layout.popup_photo_full, img, "", bitmap);
                                    }
                                });
                                img.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();
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

                            //Log.e("", "onActivityResult: " + arrImg.size());

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

    public void validateEmail(String email, MapAPI tour){

        Call<ResponseBody> call = tour.validateEmail(email);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());
                        if (jsonObject.getBoolean("Status")) {
                            int current = getItem(+1);
                            if (current < layouts.size()) {
                                // move to next screen
                                mPager.setCurrentItem(current);
                                step++;
                            }
                        } else {
                            Toast toast = Toast.makeText(SignUp.this, R.string.email_exist, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        //Log.e("", "onResponse: " + response.code());
                        Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e("", "onFailure: " + t.toString());
            }
        });
    }

    public void validateUsername(String username, String email){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

        Call<ResponseBody> call = tour.validateUser(username, email, 3);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        boolean validUsername = true;
                        boolean validEmail = true;
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());
                        if (jsonObject.getBoolean("Status")) {
                            JSONObject jsonObject1 = jsonObject.getJSONObject("Username");
                            if(!jsonObject1.getBoolean("Status")){
                                validUsername = false;
                            }
                             jsonObject1 = jsonObject.getJSONObject("Email");
                            if(!jsonObject1.getBoolean("Status")){
                                validEmail = false;
                            }
                        } else {
                            Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        if(validEmail && validUsername){
                            int current = getItem(+1);
                            if (current < layouts.size()) {
                                // move to next screen
                                mPager.setCurrentItem(current);
                                step++;
                            }
                        } else if(!validUsername && !validEmail){
                            Toast toast = Toast.makeText(SignUp.this, R.string.usermail_existed, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        } else if(!validUsername){
                            Toast toast = Toast.makeText(SignUp.this, R.string.username_existed, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }else if(!validEmail){
                            Toast toast = Toast.makeText(SignUp.this, R.string.email_exist, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        //Log.e("", "onResponse: " + response.code());
                        Toast toast = Toast.makeText(SignUp.this, R.string.something_wrong, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        tv.setTextColor(Color.RED);

                        toast.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //Log.e("", "onFailure: " + t.toString());
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    static boolean isValid(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}