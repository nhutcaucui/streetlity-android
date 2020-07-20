package com.streetlity.client;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.streetlity.client.Achievement.ActionObject;
import com.streetlity.client.Contribution.ContributeToService;
import com.streetlity.client.Firebase.StreetlityFirebaseMessagingService;
import com.streetlity.client.MainFragment.ATMFragment;
import com.streetlity.client.MainFragment.FuelFragment;
import com.streetlity.client.MainFragment.HomeFragment;
import com.streetlity.client.MainFragment.MaintenanceFragment;
import com.streetlity.client.MainFragment.WCFragment;
import com.streetlity.client.Option.MaintainerOption;
import com.streetlity.client.Option.Options;
import com.streetlity.client.User.ChangePassword;
import com.streetlity.client.User.Login;
import com.streetlity.client.User.SignUp;
import com.streetlity.client.User.UserInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import static android.view.View.VISIBLE;

public class MainNavigationHolder extends AppCompatActivity implements FuelFragment.OnFragmentInteractionListener,
        ATMFragment.OnFragmentInteractionListener, MaintenanceFragment.OnFragmentInteractionListener,
        WCFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
        View.OnClickListener {

    /*
    First screen when app is opened
     */
    Fragment fragment;
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    //boolean firstLoop = true;

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

        /*
        show a search dialog and take user to the all services nearby if used, hidden when at service fragments
         */
        ImageView imgSearch = findViewById(R.id.img_search_all);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainNavigationHolder.this);

                //final LayoutInflater inflater = LayoutInflater.from(MainNavigationHolder.this);

                final View dialogView = View.inflate(MainNavigationHolder.this ,R.layout.dialog_search_main_menu, null);

                EditText editText = dialogView.findViewById(R.id.edt_search);
                ImageButton imgBtnSearch = dialogView.findViewById(R.id.img_btn_search);
                imgBtnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!editText.getText().toString().equals("")){
                            dialog.dismiss();

                            Intent t = new Intent(MainNavigationHolder.this, AllServiceMap.class);
                            t.putExtra("search", true);
                            t.putExtra("query", editText.getText().toString());
                            startActivity(t);
                        }
                    }
                });

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

                dialog.setContentView(dialogView);

                dialog.show();
            }
        });

        /*
        used to switch from list mode to map mode and vice versa, hide when at home screen
         */
        ImageView imgSwitch = findViewById(R.id.img_switch);
        imgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = findViewById(R.id.list_view);
                LinearLayout layoutMap = findViewById(R.id.layout_map);
                if(lv.getVisibility() == VISIBLE){
                    lv.setVisibility(GONE);
                    layoutMap.setVisibility(VISIBLE);
                    imgSwitch.setImageResource(R.drawable.list_map);
                }else{
                    lv.setVisibility(VISIBLE);
                    layoutMap.setVisibility(GONE);
                    imgSwitch.setImageResource(R.drawable.location);
                }
            }
        });

        /*
        Check if user is marked as spam and prevent user to use the find repairman function
         */
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

        /*
        open all services nearby screen
         */
        FloatingActionButton fab = findViewById(R.id.chat_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainNavigationHolder.this, AllServiceMap.class));
            }
        });

        cantFind = findViewById(R.id.layout_cant_find_loca);

        MyApplication.getInstance().setDeviceToken(StreetlityFirebaseMessagingService.getToken(this));

        /*
        ask for permission
         */
        String[] Permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.READ_EXTERNAL_STORAGE};
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

        loading = findViewById(R.id.layout_loading);

        drawer = findViewById(R.id.drawer_layout);

        /*
        Set the menu button and the navigation drawer
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        NavigationView navView = findViewById(R.id.nav_view);

        SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);

        /*
        check if user is logged in
         */
        if (s.contains("token")){
            /*
            set the drawer menu to fit the user
             */
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.drawer_menu_user_login);

//            if(s.getInt("userType",0) == 1) {
//                Menu nav_Menu = navView.getMenu();
//                //nav_Menu.findItem(R.id.works).setVisible(false);
//            }

            /*
            load user info to MyApplication
             */
            MyApplication.getInstance().setToken(s.getString("token",""));
            MyApplication.getInstance().setRefreshToken(s.getString("refreshToken",""));
            MyApplication.getInstance().setUsername(s.getString("username",""));
            MyApplication.getInstance().setUserType(s.getInt("userType", -1));
            MyApplication.getInstance().setEmail(s.getString("email",""));
            MyApplication.getInstance().setPhone(s.getString("phone",""));
            MyApplication.getInstance().setAddress(s.getString("address",""));
            MyApplication.getInstance().setName(s.getString("name",""));

            /*
            check if user have avatar
             */
            if(!s.getString("avatar","").equals("")){
                getAvatar(s.getString("avatar",""));
                //Log.e("TAG", "onCreate: get avatar" );
            }else{
                //Log.e("TAG", "run: no avaatar 1" );
            }

            /*
            if user is a repairman, check if accept emergency option is on
            if the option is one, create a service that update his location
             */
            if(MyApplication.getInstance().getUserType() == 7){
                if(getSharedPreferences("acceptEmergency", MODE_PRIVATE).contains("acceptEmergency")){
                    MyApplication.getInstance().setOption(new MaintainerOption(getSharedPreferences("acceptEmergency", MODE_PRIVATE).getBoolean("acceptEmergency", false)));
                }
                else if(MyApplication.getInstance().getOption() == null){
                    MyApplication.getInstance().setOption(new MaintainerOption());
                    MyApplication.getInstance().getOption().setAcceptEmergency(false);
                }

                ConstraintLayout btnBroadcast = findViewById(R.id.layoutbroadcast);
                if (btnBroadcast != null) {
                    btnBroadcast.setVisibility(View.GONE);
                }

                ConstraintLayout btnBroadcastE = findViewById(R.id.layoutemergency);
                if (btnBroadcastE != null) {
                    btnBroadcastE.setVisibility(View.GONE);
                }

                FloatingActionButton floatingActionButton = findViewById(R.id.fab_broadcast);
                if(floatingActionButton!= null){
                    floatingActionButton.hide();
                }

                if(MyApplication.getInstance().getOption().isAcceptEmergency()) {
//                    MyApplication.getInstance().setThread();
//                    MyApplication.getInstance().getThread().scheduleAtFixedRate(new TimerTask() {
//                        @Override
//                        public void run() {
//                            LocationManager locationManager = (LocationManager)
//                                    getSystemService(Context.LOCATION_SERVICE);
//
//                            if (ContextCompat.checkSelfPermission(MainNavigationHolder.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                                    ContextCompat.checkSelfPermission(MainNavigationHolder.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                                Location location = locationManager.getLastKnownLocation(locationManager
//                                        .GPS_PROVIDER);
//                                if(location != null){
//                                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
//                                            .addConverterFactory(GsonConverterFactory.create()).build();
//                                    final MapAPI tour = retro.create(MapAPI.class);
//                                    Call<ResponseBody> call2 = tour.updateLocation(MyApplication.getInstance().getUsername(),
//                                            (float)location.getLatitude(), (float)location.getLongitude());
//                                    call2.enqueue(new Callback<ResponseBody>() {
//                                        @Override
//                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                            if (response.code() == 200) {
//                                                try {
//                                                    //Log.e("TAG", "onResponse: " + new JSONObject(response.body().string()));
//                                                }catch (Exception e){
//                                                    e.printStackTrace();}
//                                            }else{
//                                                //Log.e("TAG", "onResponse: " + response.code());
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                                        }
//                                    });
//                                }
//                            }
//
//                        }
//                    }, 5000, 600000);
                    Intent t= new Intent(MainNavigationHolder.this, UpdateLocationService.class);
                    t.setAction("start");
                    startService(t);
                }
            }

            refreshToken();
            //Log.e("TAG", "onCreate: " );
            setDrawerForUser(navView); //set drawer menu item onclick listener

            /*
            set header
             */
            View header=navView.getHeaderView(0);
            TextView tvUsername = header.findViewById(R.id.username);
            tvUsername.setText(MyApplication.getInstance().getUsername());
        }else{
            /*
            set drawer for guest user
             */
            setDrawerForNonUser(navView);
        }

        /*
        get the action map of the user, every action that related to the achievements is save in these maps
         */
        s = getSharedPreferences("map", MODE_PRIVATE);
        Gson gson = new Gson();
        if (s.contains("review")) {
            MyApplication.getInstance().setReviewedMap(
                    gson.fromJson(s.getString("review", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            //Log.e("TAG", "onCreate: " + MyApplication.getInstance().getReviewedMap());
        }if(s.contains("upvote")) {
            MyApplication.getInstance().setUpvoteMap(
                    gson.fromJson(s.getString("upvote", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            //Log.e("TAG", "onCreate: " + MyApplication.getInstance().getUpvoteMap());
        }
        if(s.contains("contribute")) {
            MyApplication.getInstance().setContributeMap(
                    gson.fromJson(s.getString("contribute", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            //Log.e("TAG", "onCreate: " + MyApplication.getInstance().getContributeMap());
        }if(s.contains("downvote")) {
            MyApplication.getInstance().setDownvoteMap(
                    gson.fromJson(s.getString("downvote", ""),
                            new TypeToken<Map<String, Map<String, ActionObject>>>() {
                            }.getType()));
            //Log.e("TAG", "onCreate: " + MyApplication.getInstance().getDownvoteMap());
        }

        /*
        load the home fragment, the home screen of the app
         */
        fragment = new HomeFragment();
        loadFragment(fragment);

//        navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /*
        find view for the bottom navigation bar
         */
        LinearLayout btnHome = findViewById(R.id.btn_home_bottom);
        LinearLayout btnFuel = findViewById(R.id.btn_fuel_bottom);
        LinearLayout btnATM = findViewById(R.id.btn_atm_bottom);
        LinearLayout btnMaintenance = findViewById(R.id.btn_maintenance_bottom);
        LinearLayout btnWC = findViewById(R.id.btn_wc_bottom);

        /*
        set the on click listener to the bottom navigation bar buttons
         */
        btnHome.setOnClickListener(this);
        btnFuel.setOnClickListener(this);
        btnATM.setOnClickListener(this);
        btnMaintenance.setOnClickListener(this);
        btnWC.setOnClickListener(this);
    }

    /*
    on click listener, only used to set the bottom navigation bar button
    check if user tap the same button
    if not load a new fragment and handle the view accordingly
     */
    @Override
    public void onClick(View v) {
        boolean notTheSame = false;
        if(v.getId() == R.id.btn_home_bottom && current != R.id.btn_home_bottom){
            fragment = new HomeFragment();
            setToUnselect(current);
            current= R.id.btn_home_bottom;
            setToSelect(current);
            findViewById(R.id.img_switch).setVisibility(GONE);
            findViewById(R.id.img_search_all).setVisibility(VISIBLE);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_fuel_bottom && current != R.id.btn_fuel_bottom){
            fragment = new FuelFragment();
            setToUnselect(current);
            current= R.id.btn_fuel_bottom;
            setToSelect(current);

            findViewById(R.id.img_switch).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.img_switch)).setImageResource(R.drawable.location);
            findViewById(R.id.img_search_all).setVisibility(GONE);

            notTheSame = true;
        }else if(v.getId() == R.id.btn_wc_bottom && current != R.id.btn_wc_bottom) {
            fragment = new WCFragment();
            setToUnselect(current);
            current = R.id.btn_wc_bottom;
            setToSelect(current);

            findViewById(R.id.img_switch).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.img_switch)).setImageResource(R.drawable.location);
            findViewById(R.id.img_search_all).setVisibility(GONE);

            notTheSame = true;
        }else if(v.getId() == R.id.btn_atm_bottom && current != R.id.btn_atm_bottom) {
            fragment = new ATMFragment();
            setToUnselect(current);
            current = R.id.btn_atm_bottom;
            setToSelect(current);

            findViewById(R.id.img_switch).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.img_switch)).setImageResource(R.drawable.location);
            findViewById(R.id.img_search_all).setVisibility(GONE);

            notTheSame = true;
        }
        else if(v.getId() == R.id.btn_maintenance_bottom && current != R.id.btn_maintenance_bottom) {
            fragment = new MaintenanceFragment();
            setToUnselect(current);
            current = R.id.btn_maintenance_bottom;
            setToSelect(current);

            findViewById(R.id.img_switch).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.img_switch)).setImageResource(R.drawable.location);
            findViewById(R.id.img_search_all).setVisibility(GONE);

            notTheSame = true;
        }
        if(notTheSame) {
            getLoading().setVisibility(VISIBLE);
            loadFragment(fragment);
        }
    }

    /*
    set the navigation bar button to inactive and hide the label
     */
    public void setToUnselect(int id){
        LinearLayout btnCurrent = findViewById(id);
        if(btnCurrent != null) {
            btnCurrent.getChildAt(1).setVisibility(View.GONE);
            //Log.e( "setToUnselect: ", btnCurrent.getClass().getName() + btnCurrent.getChildCount());
            ImageView img = (ImageView)  btnCurrent.getChildAt(0);
            img.setColorFilter(Color.argb(255, 255, 255, 255));
        }
    }

    /*
    set the navigation bar button to active and show the label
     */
    public void setToSelect(int id){
        LinearLayout btnCurrent = findViewById(id);
        btnCurrent.getChildAt(1).setVisibility(View.VISIBLE);
        //Log.e( "setToUnselect: ", btnCurrent.getClass().getName() + btnCurrent.getChildCount());
        ImageView img = (ImageView) ((LinearLayout) btnCurrent).getChildAt(0);
        img.setColorFilter(Color.argb(255, 0, 0, 0));

        int[] location = new int[2];
        btnCurrent.getLocationInWindow(location);
        HorizontalScrollView scrollView = findViewById(R.id.bottom_horizontal);
        scrollView.smoothScrollTo(location[0], location[1]);
    }

    @Deprecated
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

    /*
    trigger when using startActivityForResult handle the result the activity send back
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            /*
            request code 1, handle login, show toast notification, set drawer
             */
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

                /*
                set avatar
                 */
                if(MyApplication.getInstance().getImage()!= null) {
                    ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
                }else{
                    new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(MyApplication.getInstance().getImage()!= null) {
                                        ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
                                    }
                                }
                            });
                        }
                    };
                }

                /*
                if user is repairman, hide the find repairman options
                 */
                if(MyApplication.getInstance().getUserType() == 7) {
                    ConstraintLayout btnBroadcast = findViewById(R.id.layoutbroadcast);
                    if (btnBroadcast != null) {
                        btnBroadcast.setVisibility(View.GONE);
                    }

                    ConstraintLayout btnBroadcastE = findViewById(R.id.layoutemergency);
                    if (btnBroadcastE != null) {
                        btnBroadcastE.setVisibility(View.GONE);
                    }

                    FloatingActionButton floatingActionButton = findViewById(R.id.fab_broadcast);
                    if(floatingActionButton!= null){
                        floatingActionButton.hide();
                    }
                }

            }
            /*
            code 2 deprecated
             */
//            else if (requestCode == 2 && resultCode == RESULT_OK) {
//                Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.location_added, Toast.LENGTH_LONG);
//                toast.show();
//            }
            /*
            code 3 is for when user change their avatar
             */
            else if (requestCode == 3 && resultCode == RESULT_OK) {
                NavigationView navView = findViewById(R.id.nav_view);
                ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
            }
            /*
            code 5 is for when user cancel order multiple time and get marked as spammer
             */
            else if (requestCode == 5 && resultCode == RESULT_OK) {

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

    /*
    load the fragment
     */
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

    /*
    unload the fragment, unused for now
     */
    private void detachFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.detach(fragment);
        transaction.commit();
    }

    /*
    set the drawer buttons click handler
     */
    public void setDrawerForUser(NavigationView navView){
        if(MyApplication.getInstance().getUserType() == 1){
            Menu navMenu = navView.getMenu();
           // navMenu.findItem(R.id.works).setVisible(false);
            navMenu.findItem(R.id.settings).setVisible(false);
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                /*
                hide keyboard
                 */
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                /*
                check the id of the button and handle accordingly
                 */
                int id=menuItem.getItemId();
                switch (id){
                    case R.id.user_info:
                        /*
                        open user info screen
                         */
                        startActivityForResult(new Intent(MainNavigationHolder.this, UserInfo.class), 3);
                        break;
                    case R.id.logout:
                        logout(navView);
                        break;
//                    case R.id.works:
//                        startActivity(new Intent(MainNavigationHolder.this, Works.class));
//                        break;
                    case R.id.my_orders: {
                        /*
                        open map and chat if have order active
                         */
                        if(getSharedPreferences("activeOrder",MODE_PRIVATE).contains("activeOrder")) {
                            //Intent t = new Intent(MainNavigationHolder.this, Chat.class);
                            //t.putExtra("id", getSharedPreferences("activeOrder",MODE_PRIVATE).getString("activeOrder",""));
                            if(MyApplication.getInstance().getUserType() == 1) {
                                Intent t2 = new Intent(MainNavigationHolder.this, MaintainerLocation.class);
                                t2.putExtra("id", getSharedPreferences("activeOrder",MODE_PRIVATE).getString("activeOrder",""));
                                startActivity(t2);
                            }
                            else if (MyApplication.getInstance().getUserType() == 7){
                                Intent t2 = new Intent(MainNavigationHolder.this, MaintainerDirection.class);
                                t2.putExtra("id", getSharedPreferences("activeOrder",MODE_PRIVATE).getString("activeOrder",""));
                                startActivity(t2);
                            }
                            //startActivity(t);
                        }else{
                            Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.no_order, Toast.LENGTH_LONG);
                            toast.show();
                        }
                        break;
                    }
                    case R.id.contribute:
                        /*
                        open contribute screen
                         */
                        startActivity(new Intent(MainNavigationHolder.this, ContributeToService.class));
                        break;
                    case R.id.change_pass:
                        /*
                        open change password screen
                         */
                        startActivity(new Intent(MainNavigationHolder.this, ChangePassword.class));
                        break;
                    case R.id.about:
                        /*
                        open about us screen
                         */
                        startActivity(new Intent(MainNavigationHolder.this, AboutUs.class));
                        break;
                    case R.id.settings:
                        /*
                        open options screen, repairman only
                         */
                        startActivity(new Intent(MainNavigationHolder.this, Options.class));
                        break;
                }

                return true;
            }
        });
    }

    /*
    set the drawer button for guest user
     */
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
                        /*
                        open login screen
                         */
                        startActivityForResult(new Intent(MainNavigationHolder.this, Login.class),1);
                        break;
                    case  R.id.signup:
                        /*
                        open sign up screen
                         */
                        Intent t = new Intent(MainNavigationHolder.this, SignUp.class);
                        t.putExtra("from", 1);
                        startActivityForResult(t, 1);
                        break;
                    case R.id.about:
                        /*
                        open about us screen
                         */
                        startActivity(new Intent(MainNavigationHolder.this, AboutUs.class));
                        break;
                }

                return true;
            }
        });
    }

    /*
    logout, set drawer for non user, clear the sharedpreference contain user info and clear the info in MyApplication
     */
    public void logout(NavigationView navView){
        drawer.closeDrawers();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
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
                        //Log.e("", "onResponse: " + jsonObject );
                        if (jsonObject.getBoolean("Status")) {

                            if(MyApplication.getInstance().getUserType() == 7 && MyApplication.getInstance().getOption().isAcceptEmergency()){
                                MyApplication.getInstance().getOption().setAcceptEmergency(false);
                                MyApplication.getInstance().getThread().cancel();

                                Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                final MapAPI tour = retro.create(MapAPI.class);
                                Call<ResponseBody> call2 = tour.removeEmergency(MyApplication.getInstance().getUsername());

                                call2.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        //Log.e("TAG", "onResponse: response when remove maintenance" );
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }
                            if(MyApplication.getInstance().getUserType() == 7) {
                                if (MyApplication.getInstance().getOption().isAcceptEmergency()) {

                                    Intent t= new Intent(MainNavigationHolder.this, UpdateLocationService.class);
                                    t.setAction("stop");
                                    startService(t);

                                    Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                                            .addConverterFactory(GsonConverterFactory.create()).build();
                                    final MapAPI tour = retro.create(MapAPI.class);
                                    call = tour.removeEmergency(MyApplication.getInstance().getUsername());
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.code() == 200) {
                                                try {
                                                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                                                    //Log.e("TAG", "onResponse: " + jsonObject1);
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
                            }
                            
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setToken("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setRefreshToken("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setUsername("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setEmail("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setPhone("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setAddress("");
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setUserType(-1);
                            ((MyApplication) MainNavigationHolder.this.getApplication()).setImage(null);
                            MyApplication.getInstance().setName("");


                            SharedPreferences s = getSharedPreferences("userPref", Context.MODE_PRIVATE);
                            SharedPreferences.Editor e = s.edit();
                            e.clear();
                            e.apply();

                            navView.getMenu().clear();
                            navView.inflateMenu(R.menu.drawer_menu_user_not_login);

                            ((ImageView)navView.findViewById(R.id.avatar)).setImageResource(R.drawable.avatar);

                            View header=navView.getHeaderView(0);
                            TextView tvUsername = header.findViewById(R.id.username);
                            tvUsername.setText(R.string.not_login);

                            setDrawerForNonUser(navView);

                            ConstraintLayout btnBroadcast = findViewById(R.id.layoutbroadcast);
                            if (btnBroadcast != null) {
                                btnBroadcast.setVisibility(VISIBLE);
                            }

                            ConstraintLayout btnBroadcastE = findViewById(R.id.layoutemergency);
                            if (btnBroadcastE != null) {
                                btnBroadcastE.setVisibility(VISIBLE);
                            }

                            FloatingActionButton floatingActionButton = findViewById(R.id.fab_broadcast);
                            if(floatingActionButton!= null){
                                floatingActionButton.show();
                            }

                            layoutTop.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.logged_out, Toast.LENGTH_LONG);
                            toast.show();

                            /*
                            clear the map saved in shared preferences and reset the value of first time open find repairman
                             */
                            SharedPreferences s1 = getSharedPreferences("map", MODE_PRIVATE);
                            SharedPreferences.Editor e1 = s1.edit();

                            e1.clear();

                            e1.apply();

                            SharedPreferences s2 = getSharedPreferences("first",MODE_PRIVATE);

                            SharedPreferences.Editor e2 = s2.edit();

                            e2.putBoolean("firstBroadcast", false);
                            e2.putBoolean("firstEmergency", false);
                            e2.apply();

                            /*
                            clear active order and accept emergency
                             */
                            getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();

                            getSharedPreferences("acceptEmergency",MODE_PRIVATE).edit().clear().apply();

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

                        //Log.e("", "onResponse: "  + response.code());
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

    /*
    check if user grant permission
     */
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

    /*
    function to get some view so fragment can use it
     */
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

    /*
    when activity return to foreground, hide the loading
     */
    @Override
    public void onResume(){
        super.onResume();

        this.getLoading().setVisibility(View.GONE);

        /*
        check if user have active order and set menu with a red dot
         */
        if(getSharedPreferences("activeOrder",MODE_PRIVATE).contains("activeOrder")){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_active);
            NavigationView navView = findViewById(R.id.nav_view);
            if(navView.getMenu().findItem(R.id.my_orders) != null) {
                navView.getMenu().findItem(R.id.my_orders).setIcon(R.drawable.my_order_active);
            }
        }else{
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
            NavigationView navView = findViewById(R.id.nav_view);
            if(navView.getMenu().findItem(R.id.my_orders) != null) {
                navView.getMenu().findItem(R.id.my_orders).setIcon(R.drawable.my_order_icon);
            }
        }
    }

    /*
    get time user need to wait in order to use the find repairman
     */
    public long getTimeLeft() {
        return timeLeft;
    }

    /*
    request server to refresh the token
     */
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
                        //Log.e("", "onResponse: "+jsonObject.toString() );

                        ((MyApplication) MainNavigationHolder.this.getApplication()).setToken(jsonObject.getString("AccessToken"));
                        getSharedPreferences("userPref", Context.MODE_PRIVATE).edit()
                                .putString("token", ((MyApplication) MainNavigationHolder.this.getApplication()).getToken()).apply();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    //Log.e("TAG", "onResponse: "+response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /*
    get and load avatar
     */
    public  void getAvatar(String avatar){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getDriverURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.download(avatar);
        //Log.e("TAG", "getAvatar: "+ avatar );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        MyApplication.getInstance().setImage(bmp);

                        //Log.e("TAG", "onResponse: hey" + avatar);

                        /*
                        in some case, this run before the view is fully loaded, so the avatar on header can't be set
                        in this case, add a delay to load the avatar
                         */
                        NavigationView navView = findViewById(R.id.nav_view);
                        if(navView!= null) {
                            if (navView.findViewById(R.id.avatar) != null) {
                                //Log.e("TAG", "run: no timer 1" );
                                ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
                            }else{
                                new CountDownTimer(3000,1000){

                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Log.e("TAG", "run: timer 1" );
                                                ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
                                            }
                                        });
                                    }
                                }.start();
                            }
                        }else {
                            new CountDownTimer(3000,1000){

                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Log.e("TAG", "run: timer 2" );
                                            NavigationView navView = findViewById(R.id.nav_view);
                                            ((ImageView) navView.findViewById(R.id.avatar)).setImageBitmap(MyApplication.getInstance().getImage());
                                        }
                                    });
                                }
                            }.start();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    //Log.e("TAG", "onResponse: "+ response.code() );
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /*
    when this activity is closed, write the map to shared preferences
     */
    public void onStop(){
        super.onStop();

        if(!MyApplication.getInstance().getToken().equals("")) {
            Gson gs = new Gson();

            SharedPreferences s = getSharedPreferences("map", MODE_PRIVATE);
            SharedPreferences.Editor e = s.edit();

            e.putString("review", gs.toJson(MyApplication.getInstance().getReviewedMap()));
            e.putString("contribute", gs.toJson(MyApplication.getInstance().getContributeMap()));
            e.putString("upvote", gs.toJson(MyApplication.getInstance().getUpvoteMap()));
            e.putString("downvote", gs.toJson(MyApplication.getInstance().getDownvoteMap()));

            e.apply();
        }
    }
}
