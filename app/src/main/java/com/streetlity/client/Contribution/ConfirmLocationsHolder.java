package com.streetlity.client.Contribution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.streetlity.client.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ConfirmLocationsHolder extends AppCompatActivity implements FuelFragment.OnFragmentInteractionListener,
        ATMFragment.OnFragmentInteractionListener, MaintenanceFragment.OnFragmentInteractionListener,
        WCFragment.OnFragmentInteractionListener, View.OnClickListener {
    Fragment fragment;

    int current =R.id.btn_fuel_bottom;
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    ConstraintLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_locations_holder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loading = findViewById(R.id.layout_loading);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        fragment = new FuelFragment();
        loadFragment(fragment);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        
        LinearLayout btnFuel = findViewById(R.id.btn_fuel_bottom);
        LinearLayout btnATM = findViewById(R.id.btn_atm_bottom);
        LinearLayout btnMaintenance = findViewById(R.id.btn_maintenance_bottom);
        LinearLayout btnWC = findViewById(R.id.btn_wc_bottom);
        
        btnFuel.setOnClickListener(this);
        btnATM.setOnClickListener(this);
        btnMaintenance.setOnClickListener(this);
        btnWC.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        boolean notTheSame = false;
        if(v.getId() == R.id.btn_fuel_bottom && current != R.id.btn_fuel_bottom){
            fragment = new  FuelFragment();
            setToUnselect(current);
            current= R.id.btn_fuel_bottom;
            setToSelect(current);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_wc_bottom && current != R.id.btn_wc_bottom) {
            fragment = new  WCFragment();
            setToUnselect(current);
            current = R.id.btn_wc_bottom;
            setToSelect(current);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_atm_bottom && current != R.id.btn_atm_bottom) {
            fragment = new  ATMFragment();
            setToUnselect(current);
            current = R.id.btn_atm_bottom;
            setToSelect(current);
            notTheSame = true;
        }
        else if(v.getId() == R.id.btn_maintenance_bottom && current != R.id.btn_maintenance_bottom) {
            fragment = new  MaintenanceFragment();
            setToUnselect(current);
            current = R.id.btn_maintenance_bottom;
            setToSelect(current);
            notTheSame = true;
        }
        if(notTheSame) {
            getLoading().setVisibility(View.VISIBLE);
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
                case R.id.navigation_fuel:
                    fragment = new FuelFragment();
                    break;
                case R.id.nav_atm:
                    fragment = new ATMFragment();
                    break;
                case R.id.navigation_wc:
                    fragment = new WCFragment();
                    break;
                case R.id.navigation_maintenance:
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

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public ConstraintLayout getLoading() {
        return loading;
    }

    public void onResume(){
        super.onResume();

        loading.setVisibility(GONE);
    }
}