package com.example.streetlity_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.Chat.Chat;
import com.example.streetlity_android.Contribution.ContributeToService;
import com.example.streetlity_android.Firebase.StreetlityFirebaseMessagingService;
import com.example.streetlity_android.MainFragment.ATMFragment;
import com.example.streetlity_android.MainFragment.FuelFragment;
import com.example.streetlity_android.MainFragment.HomeFragment;
import com.example.streetlity_android.MainFragment.MaintenanceFragment;
import com.example.streetlity_android.MainFragment.WCFragment;
import com.example.streetlity_android.Notification.Notification;
import com.example.streetlity_android.Option.MaintainerOption;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.example.streetlity_android.User.ChangePassword;
import com.example.streetlity_android.User.Common.MyOrders;
import com.example.streetlity_android.User.Login;
import com.example.streetlity_android.User.Maintainer.OrderInfo;
import com.example.streetlity_android.User.Maintainer.Works;
import com.example.streetlity_android.User.SignUp;
import com.example.streetlity_android.User.UserInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;

public class MainNavigationHolder extends AppCompatActivity implements FuelFragment.OnFragmentInteractionListener,
        ATMFragment.OnFragmentInteractionListener, MaintenanceFragment.OnFragmentInteractionListener,
        WCFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
        View.OnClickListener {
    Fragment fragment;
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    DrawerLayout drawer;

    ConstraintLayout cantFind;

    ConstraintLayout loading;

    BottomNavigationView navigation;

    boolean canBroadcast = true;

    long timeLeft=0;

    int current= R.id.btn_home_bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation_holder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startService(new Intent(getBaseContext(), ServiceChecking.class));

        //getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();

        if(getSharedPreferences("spam", MODE_PRIVATE).contains("spam")){
            canBroadcast = false;

//            Thread thread = new Thread(new Runnable () {
//                @Override
//                public void run(){
//                    for (int i = 15 ; i >= 0; i--) {
//                        try{
//                            timeLeft = i;
//                            Thread.sleep(1000);
//                        }catch (InterruptedException e) {}
//
//                    }
//                    canBroadcast=true;
//                    getSharedPreferences("spam", MODE_PRIVATE).edit().clear().apply();
//                }
//            });
//            thread.start();

            if(System.currentTimeMillis() -
                    getSharedPreferences("spam", MODE_PRIVATE).getLong("spam", 0) >= 300000 ){
                canBroadcast = true;
                getSharedPreferences("spam", MODE_PRIVATE).edit().clear().apply();
            }else {

                new CountDownTimer(300000- (System.currentTimeMillis() -
                        getSharedPreferences("spam", MODE_PRIVATE).getLong("spam", 0)), 1000) {

                    public void onTick(long millisUntilFinished) {
                        timeLeft = millisUntilFinished / 1000;
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
                        canBroadcast = true;
                        getSharedPreferences("spam", MODE_PRIVATE).edit().clear().apply();
                    }

                }.start();
            }
        }



        FloatingActionButton fab = findViewById(R.id.chat_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainNavigationHolder.this, AllServiceMap.class));
            }
        });

        cantFind = findViewById(R.id.layout_cant_find_loca);

        MyApplication.getInstance().setDeviceToken(StreetlityFirebaseMessagingService.getToken(this));

        String[] Permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!hasPermissions(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, 4);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            this.finish();
            return;
        }

        loading = findViewById(R.id.layout_loading);

        drawer = findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        NavigationView navView = findViewById(R.id.nav_view);

        ImageButton imgNotify = findViewById(R.id.img_notify);
        imgNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainNavigationHolder.this, Notification.class));
            }
        });

        SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
        if (s.contains("token")){

            navView.getMenu().clear();
            navView.inflateMenu(R.menu.drawer_menu_user_login);

            if(s.getInt("userType",0) == 1) {
                Menu nav_Menu = navView.getMenu();
                nav_Menu.findItem(R.id.works).setVisible(false);
            }

            MyApplication.getInstance().setToken(s.getString("token",""));
            MyApplication.getInstance().setRefreshToken(s.getString("refreshToken",""));
            MyApplication.getInstance().setUsername(s.getString("username",""));
            MyApplication.getInstance().setUserType(s.getInt("userType", -1));
            MyApplication.getInstance().setEmail(s.getString("email",""));
            MyApplication.getInstance().setPhone(s.getString("phone",""));
            MyApplication.getInstance().setAddress(s.getString("address",""));

            if(!s.getString("avatar","").equals("")){
                getAvatar(s.getString("avatar",""));
            }

            if(MyApplication.getInstance().getUserType() == 7){
                if(getSharedPreferences("acceptEmergency", MODE_PRIVATE).contains("acceptEmergency")){
                    MyApplication.getInstance().setOption(new MaintainerOption(getSharedPreferences("acceptEmergency", MODE_PRIVATE).getBoolean("acceptEmergency", false)));
                }
                else if(MyApplication.getInstance().getOption() == null){
                    MyApplication.getInstance().setOption(new MaintainerOption());
                    MyApplication.getInstance().getOption().setAcceptEmergency(false);
                }
            }

            refreshToken();
            Log.e("TAG", "onCreate: " );
            setDrawerForUser(navView);

            View header=navView.getHeaderView(0);
            TextView tvUsername = header.findViewById(R.id.username);
            tvUsername.setText(MyApplication.getInstance().getUsername());

            imgNotify.setVisibility(View.VISIBLE);
        }else{
            setDrawerForNonUser(navView);
        }

        s = getSharedPreferences("map", MODE_PRIVATE);
        Gson gson = new Gson();
        if (s.contains("review")) {
            MyApplication.getInstance().setReviewedMap(
                    gson.fromJson(s.getString("review", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            Log.e("TAG", "onCreate: " + MyApplication.getInstance().getReviewedMap());
        }if(s.contains("upvote")) {
            MyApplication.getInstance().setUpvoteMap(
                    gson.fromJson(s.getString("upvote", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            Log.e("TAG", "onCreate: " + MyApplication.getInstance().getUpvoteMap());
        }
        if(s.contains("contribute")) {
            MyApplication.getInstance().setContributeMap(
                    gson.fromJson(s.getString("contribute", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            Log.e("TAG", "onCreate: " + MyApplication.getInstance().getContributeMap());
        }

        fragment = new HomeFragment();
        loadFragment(fragment);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        LinearLayout btnHome = findViewById(R.id.btn_home_bottom);
        LinearLayout btnFuel = findViewById(R.id.btn_fuel_bottom);
        LinearLayout btnATM = findViewById(R.id.btn_atm_bottom);
        LinearLayout btnMaintenance = findViewById(R.id.btn_maintenance_bottom);
        LinearLayout btnWC = findViewById(R.id.btn_wc_bottom);

        btnHome.setOnClickListener(this);
        btnFuel.setOnClickListener(this);
        btnATM.setOnClickListener(this);
        btnMaintenance.setOnClickListener(this);
        btnWC.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean notTheSame = false;
        if(v.getId() == R.id.btn_home_bottom && current != R.id.btn_home_bottom){
            fragment = new HomeFragment();
            setToUnselect(current);
            current= R.id.btn_home_bottom;
            setToSelect(current);

            notTheSame = true;
        }else if(v.getId() == R.id.btn_fuel_bottom && current != R.id.btn_fuel_bottom){
            fragment = new FuelFragment();
            setToUnselect(current);
            current= R.id.btn_fuel_bottom;
            setToSelect(current);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_wc_bottom && current != R.id.btn_wc_bottom) {
            fragment = new WCFragment();
            setToUnselect(current);
            current = R.id.btn_wc_bottom;
            setToSelect(current);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_atm_bottom && current != R.id.btn_atm_bottom) {
            fragment = new ATMFragment();
            setToUnselect(current);
            current = R.id.btn_atm_bottom;
            setToSelect(current);
            notTheSame = true;
        }
        else if(v.getId() == R.id.btn_maintenance_bottom && current != R.id.btn_maintenance_bottom) {
            fragment = new MaintenanceFragment();
            setToUnselect(current);
            current = R.id.btn_maintenance_bottom;
            setToSelect(current);
            notTheSame = true;
        }
        if(notTheSame) {
            loadFragment(fragment);
        }
    }

    public void setToUnselect(int id){
        LinearLayout btnCurrent = findViewById(id);
        if(btnCurrent != null) {
            btnCurrent.getChildAt(1).setVisibility(View.GONE);
            Log.e( "setToUnselect: ", btnCurrent.getClass().getName() + btnCurrent.getChildCount());
            ImageView img = (ImageView)  btnCurrent.getChildAt(0);
            img.setColorFilter(Color.argb(255, 255, 255, 255));
        }
    }

    public void setToSelect(int id){
        LinearLayout btnCurrent = findViewById(id);
        btnCurrent.getChildAt(1).setVisibility(View.VISIBLE);
        Log.e( "setToUnselect: ", btnCurrent.getClass().getName() + btnCurrent.getChildCount());
        ImageView img = (ImageView) ((LinearLayout) btnCurrent).getChildAt(0);
        img.setColorFilter(Color.argb(255, 0, 0, 0));

        int[] location = new int[2];
        btnCurrent.getLocationInWindow(location);
        HorizontalScrollView scrollView = findViewById(R.id.bottom_horizontal);
        scrollView.smoothScrollTo(location[0], location[1]);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (!navigation.getMenu().getItem(0).isChecked())
                    fragment = new HomeFragment();
                    break;
                case R.id.navigation_fuel:
                    if (!navigation.getMenu().getItem(1).isChecked())
                    fragment = new FuelFragment();
                    break;
                case R.id.nav_atm:
                    if (!navigation.getMenu().getItem(2).isChecked())
                    fragment = new ATMFragment();
                    break;
                case R.id.navigation_wc:
                    if (!navigation.getMenu().getItem(3).isChecked())
                    fragment = new WCFragment();
                    break;
                case R.id.navigation_maintenance:
                    if (!navigation.getMenu().getItem(4).isChecked())
                    fragment = new MaintenanceFragment();
                    break;
            }
            return loadFragment(fragment);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.logged_in, Toast.LENGTH_LONG);
                toast.show();

                NavigationView navView = findViewById(R.id.nav_view);

                navView.getMenu().clear();
                navView.inflateMenu(R.menu.drawer_menu_user_login);

                setDrawerForUser(navView);

                View header=navView.getHeaderView(0);
                TextView tvUsername = header.findViewById(R.id.username);
                tvUsername.setText(MyApplication.getInstance().getUsername());

//                LinearLayout btnBroadcast = findViewById(R.id.btn_broadcast);
//                if(btnBroadcast != null){
//                    btnBroadcast.setVisibility(View.VISIBLE);
//                }
//
//                LinearLayout btnBroadcastE = findViewById(R.id.btn_emergency);
//                if(btnBroadcastE != null){
//                    btnBroadcastE.setVisibility(View.VISIBLE);
//                }

                ImageButton imgNotify = findViewById(R.id.img_notify);
                imgNotify.setVisibility(View.VISIBLE);
            }else if (requestCode == 2 && resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.location_added, Toast.LENGTH_LONG);
                toast.show();
            } else if (requestCode == 5 && resultCode == RESULT_OK) {

                Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.mark_spam, Toast.LENGTH_LONG);
                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                tv.setTextColor(Color.RED);

                toast.show();

//                Toast toast = Toast.makeText(MainNavigationHolder.this, temp, Toast.LENGTH_LONG);
//                toast.show();

                getSharedPreferences("spam", MODE_PRIVATE).edit().putLong("spam",System.currentTimeMillis()).apply();
                canBroadcast = false;

//                Thread thread = new Thread(new Runnable () {
//                    @Override
//                    public void run(){
//                        for (int i = 300 ; i >= 0; i--) {
//                            try{
//                                timeLeft = i;
//                                Thread.sleep(1000);
//                            }catch (InterruptedException e) {}
//
//                        }
//                        canBroadcast=true;
//                        getSharedPreferences("spam", MODE_PRIVATE).edit().clear().apply();
//                    }
//                });
//                thread.start();

                new CountDownTimer(300000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timeLeft = millisUntilFinished / 1000;
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
                        canBroadcast=true;
                        getSharedPreferences("spam", MODE_PRIVATE).edit().clear().apply();
                    }

                }.start();
            }

        } catch (Exception e) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        }

    }

    private boolean loadFragment(Fragment fragment) {
        // load fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
            //Thread.sleep(1000);
            return true;
        }
        return false;
    }

    private void detachFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.detach(fragment);
        transaction.commit();
    }

    public void setDrawerForUser(NavigationView navView){
        if(MyApplication.getInstance().getUserType() == 1){
            Menu navMenu = navView.getMenu();
            navMenu.findItem(R.id.works).setVisible(false);
            navMenu.findItem(R.id.settings).setVisible(false);
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                int id=menuItem.getItemId();
                switch (id){
                    case R.id.user_info:

                        startActivity(new Intent(MainNavigationHolder.this, UserInfo.class));
                        break;
                    case  R.id.logout:
                        logout(navView);
                        break;
                    case R.id.works:
                        startActivity(new Intent(MainNavigationHolder.this, Works.class));
                        break;
                    case R.id.my_order: {
                        startActivity(new Intent(MainNavigationHolder.this, MaintainerLocation.class));
                        startActivity(new Intent( MainNavigationHolder.this, Chat.class));
                        break;
                    }
                    case R.id.contribute:
                        startActivity(new Intent(MainNavigationHolder.this, ContributeToService.class));
                        break;
                    case R.id.change_pass:
                        startActivity(new Intent(MainNavigationHolder.this, ChangePassword.class));
                        break;
                    case R.id.about:
                        startActivity(new Intent(MainNavigationHolder.this, AboutUs.class));
                        break;
                }

                return true;
            }
        });
    }

    public void setDrawerForNonUser(NavigationView navView){
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                int id=menuItem.getItemId();
                switch (id){
                    case R.id.login:
                        startActivityForResult(new Intent(MainNavigationHolder.this, Login.class),1);
                        break;
                    case  R.id.signup:
                        Intent t = new Intent(MainNavigationHolder.this, SignUp.class);
                        t.putExtra("from", 1);
                        startActivityForResult(t, 1);
                        break;
                    case R.id.about:
                        startActivity(new Intent(MainNavigationHolder.this, AboutUs.class));
                        break;
                }

                return true;
            }
        });
    }

    public void logout(NavigationView navView){
        drawer.closeDrawers();
        ConstraintLayout layoutTop = findViewById(R.id.layout_loading_top);
        layoutTop.setVisibility(View.VISIBLE);
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        String token = MyApplication.getInstance().getToken();
        Call<ResponseBody> call = tour.logout(token, MyApplication.getInstance().getUsername(),
                MyApplication.getInstance().getRefreshToken(),((MyApplication) getApplication()).getDeviceToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject );
                        if (jsonObject.getBoolean("Status")) {
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setToken("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setRefreshToken("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setUsername("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setEmail("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setPhone("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setAddress("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setUserType(-1);
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setImage(null);


                            SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor e = s.edit();
                            e.clear();
                            e.apply();

                            ImageButton imgNotify = findViewById(R.id.img_notify);
                            imgNotify.setVisibility(View.GONE);

                            navView.getMenu().clear();
                            navView.inflateMenu(R.menu.drawer_menu_user_not_login);

                            View header=navView.getHeaderView(0);
                            TextView tvUsername = header.findViewById(R.id.username);
                            tvUsername.setText(R.string.not_login);

                            setDrawerForNonUser(navView);

//                            LinearLayout btnBroadcast = findViewById(R.id.btn_broadcast);
//                            if(btnBroadcast != null){
//                                btnBroadcast.setVisibility(View.GONE);
//                            }
//
//                            LinearLayout btnBroadcastE = findViewById(R.id.btn_emergency);
//                            if(btnBroadcastE != null){
//                                btnBroadcastE.setVisibility(View.GONE);
//                            }

                            FloatingActionButton fab = findViewById(R.id.fab_broadcast);
                            if(fab != null){
                                fab.hide();
                            }

                            layoutTop.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.logged_out, Toast.LENGTH_LONG);
                            toast.show();

                            SharedPreferences s1 = getSharedPreferences("map", MODE_PRIVATE);
                            SharedPreferences.Editor e1 = s1.edit();

                            e1.clear();

                            e1.apply();

                        }else{

                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        //JSONObject jsonObject = new JSONObject(response.body().string());
                        //JSONObject jsonObject1 = new JSONObject(response.errorBody().toString());

                        Log.e("", "onResponse: "  + response.code());
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

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public ConstraintLayout getCantFind() {
        return cantFind;
    }

    public BottomNavigationView getNavigation(){
        return navigation;
    }

    public ConstraintLayout getLoading() {
        return loading;
    }

    public boolean isCanBroadcast() {
        return canBroadcast;
    }

    @Override
    public void onResume(){
        super.onResume();

        this.getLoading().setVisibility(View.GONE);
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public  void refreshToken(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.refresh(MyApplication.getInstance().getRefreshToken());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    final JSONObject jsonObject;
                    try{
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: "+jsonObject.toString() );

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    Log.e("TAG", "onResponse: "+response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public  void getAvatar(String avatar){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.download(avatar);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        MyApplication.getInstance().setImage(bmp);
                    }catch (Exception e){
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

        Gson gs = new Gson();

        SharedPreferences s = getSharedPreferences("map", MODE_PRIVATE);
        SharedPreferences.Editor e = s.edit();

        e.putString("review", gs.toJson(MyApplication.getInstance().getReviewedMap()));
        e.putString("contribute", gs.toJson(MyApplication.getInstance().getContributeMap()));
        e.putString("upvote", gs.toJson(MyApplication.getInstance().getUpvoteMap()));

        e.apply();
    }
}
