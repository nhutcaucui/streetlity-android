package com.example.streetlity_android.User.Common;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.streetlity_android.R;
import com.example.streetlity_android.User.Common.OrderHistory;
import com.example.streetlity_android.User.Common.Orders;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyOrders extends AppCompatActivity implements Orders.OnFragmentInteractionListener,
com.example.streetlity_android.User.Maintainer.MyOrders.OnFragmentInteractionListener, OrderHistory.OnFragmentInteractionListener{

    Fragment fragment;
    boolean firstLoad;
    TextView fragment_name;

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_orders);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragment_name = findViewById(R.id.tv_toolbar_tittle);

        firstLoad = true;

        fragment = new Orders();
        loadFragment(fragment);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.my_order:
                    fragment = new Orders();
                    break;
                case R.id.order_history:
                    fragment = new OrderHistory();
                    break;
            }
            return loadFragment(fragment);
        }
    };
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

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (getCurrentFocus() != null) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
