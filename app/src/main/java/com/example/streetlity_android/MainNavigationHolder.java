package com.example.streetlity_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.streetlity_android.Contribution.ContributeToService;
import com.example.streetlity_android.Firebase.StreetlityFirebaseMessagingService;
import com.example.streetlity_android.MainFragment.ATMFragment;
import com.example.streetlity_android.MainFragment.FuelFragment;
import com.example.streetlity_android.MainFragment.HomeFragment;
import com.example.streetlity_android.MainFragment.MaintenanceFragment;
import com.example.streetlity_android.MainFragment.WCFragment;
import com.example.streetlity_android.Notification.Notification;
import com.example.streetlity_android.User.ChangePassword;
import com.example.streetlity_android.User.Login;
import com.example.streetlity_android.User.Maintainer.Works;
import com.example.streetlity_android.User.SignUp;
import com.example.streetlity_android.User.UserInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

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

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainNavigationHolder extends AppCompatActivity implements FuelFragment.OnFragmentInteractionListener,
        ATMFragment.OnFragmentInteractionListener, MaintenanceFragment.OnFragmentInteractionListener,
        WCFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation_holder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

            ((MyApplication) this.getApplication()).setToken(s.getString("token",""));
            ((MyApplication) this.getApplication()).setRefreshToken(s.getString("refreshToken",""));
            ((MyApplication) this.getApplication()).setUsername(s.getString("username",""));
            ((MyApplication) this.getApplication()).setUserType(s.getInt("userType", -1));
            ((MyApplication) this.getApplication()).setEmail(s.getString("email",""));
            ((MyApplication) this.getApplication()).setPhone(s.getString("phone",""));
            ((MyApplication) this.getApplication()).setAddress(s.getString("address",""));

            setDrawerForUser(navView);

            View header=navView.getHeaderView(0);
            TextView tvUsername = header.findViewById(R.id.username);
            tvUsername.setText(((MyApplication) this.getApplication()).getUsername());

            imgNotify.setVisibility(View.VISIBLE);
        }else{
            setDrawerForNonUser(navView);
        }

        fragment = new HomeFragment();
        loadFragment(fragment);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
                tvUsername.setText(((MyApplication) this.getApplication()).getUsername());

                Button btnBroadcast = findViewById(R.id.btn_broadcast);
                if(btnBroadcast != null){
                    btnBroadcast.setVisibility(View.VISIBLE);
                }

                ImageButton imgNotify = findViewById(R.id.img_notify);
                imgNotify.setVisibility(View.VISIBLE);
            }else if (requestCode == 2 && resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.location_added, Toast.LENGTH_LONG);
                toast.show();
            } else if (requestCode == 5 && resultCode == RESULT_OK && null != data) {

                String temp = getString(R.string.contacted);
                temp += " " + data.getIntExtra("numStore", 0);
                temp += " " + getString(R.string.nearby_store);

                int range = data.getIntExtra("range", 0);
                temp += " " + getString(R.string.in_range) + " " + (range / 1000) + "km";

                Toast toast = Toast.makeText(MainNavigationHolder.this, temp, Toast.LENGTH_LONG);
                toast.show();

                canBroadcast = false;
                Thread thread = new Thread(new Runnable () {
                    @Override
                    public void run(){
                        for (int i = 300 ; i >= 0; i--) {
                            try{
                                Thread.sleep(1000);
                            }catch (InterruptedException e) {}

                        }
                        canBroadcast=true;
                    }
                });
                thread.start();
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
        if(((MyApplication) this.getApplication()).getUserType() == 1){
            Menu nav_Menu = navView.getMenu();
            nav_Menu.findItem(R.id.works).setVisible(false);
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
                    case R.id.contribute:
                        startActivity(new Intent(MainNavigationHolder.this, ContributeToService.class));
                        break;
                    case R.id.change_pass:
                        startActivity(new Intent(MainNavigationHolder.this, ChangePassword.class));
                }

                return true;
            }
        });
    }

    public void setDrawerForNonUser(NavigationView navView){
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
                }

                return true;
            }
        });
    }

    public void logout(NavigationView navView){

        Retrofit retro = new Retrofit.Builder().baseUrl("http://35.240.232.218/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        String token = ((MyApplication) this.getApplication()).getToken();
        Call<ResponseBody> call = tour.logout(token, ((MyApplication) this.getApplication()).getUsername(),
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

                            drawer.closeDrawers();

                            setDrawerForNonUser(navView);

                            Toast toast = Toast.makeText(MainNavigationHolder.this, R.string.logged_out, Toast.LENGTH_LONG);
                            toast.show();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

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
}
