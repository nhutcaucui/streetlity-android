package com.streetlity.client.Achievement;

import android.Manifest;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.streetlity.client.MainFragment.BankObject;
import com.streetlity.client.MainFragment.MapObject;
import com.streetlity.client.MapAPI;
import com.streetlity.client.MyApplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.streetlity.client.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.VISIBLE;

public class Achievement extends AppCompatActivity implements View.OnClickListener, ContributedFragment.OnFragmentInteractionListener,
    ReviewedFragment.OnFragmentInteractionListener{

    //ArrayList<AchievementObject> items = new ArrayList<>();
    ArrayList<MapObject> reviewedItems = new ArrayList<>();
    ArrayList<MapObject> contributedItems = new ArrayList<>();
    ArrayList<BankObject> arrBank = new ArrayList<>();

    boolean gotReviewItems = false;
    boolean gotContributeItems = false;

    float currLat;
    float currLon;

    AchievementObjectAdapter adapterReview;

    ListView lvReview;


    Fragment fragment;

    int current = R.id.btn_contributed;

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        lvReview = findViewById(R.id.lv_review);


        getRepu();

        getBank();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager)
                    this.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(locationManager
                    .GPS_PROVIDER);

            if (location == null) {
                Log.e("", "onMapReady: MULL");
            } else {
                currLat = (float) location.getLatitude();
                currLon = (float) location.getLongitude();
            }
            Log.e("", "onMapReady: " + currLat + " , " + currLon);
        }


        LinearLayout btnContributed = findViewById(R.id.btn_contributed);
        LinearLayout btnReviewed = findViewById(R.id.btn_reviewed);

        btnContributed.setOnClickListener(this);
        btnReviewed.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        boolean notTheSame = false;
        if(v.getId() == R.id.btn_contributed && current != R.id.btn_contributed){
            fragment = new ContributedFragment();
            setToUnselect(current);
            current= R.id.btn_contributed;
            setToSelect(current);
            notTheSame = true;
        }else if(v.getId() == R.id.btn_reviewed && current != R.id.btn_reviewed){
            fragment = new ReviewedFragment();
            setToUnselect(current);
            current= R.id.btn_reviewed;
            setToSelect(current);

            notTheSame = true;
        }
        if(notTheSame) {
            findViewById(R.id.layout_loading).setVisibility(VISIBLE);
            loadFragment(fragment);
        }
    }

    public void setToUnselect(int id){
        LinearLayout btnCurrent = findViewById(id);
        if(btnCurrent != null) {
            btnCurrent.setBackgroundResource(android.R.color.transparent);
            ((TextView) btnCurrent.getChildAt(0)).setTypeface(Typeface.DEFAULT);
        }
    }

    public void setToSelect(int id){
        LinearLayout btnCurrent = findViewById(id);
        btnCurrent.setBackgroundResource(R.drawable.bottom_border);
        ((TextView) btnCurrent.getChildAt(0)).setTypeface(Typeface.DEFAULT_BOLD);
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

    public static float distance(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    public void getRepu(){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();

        final MapAPI tour = retro.create(MapAPI.class);

        Call<ResponseBody> call = tour.getReputation(MyApplication.getInstance().getUsername());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    if(response.code() == 200){
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        Log.e("tag", "onResponse: " + jsonObject.toString() );
                        if(jsonObject.getBoolean("Status")){
                            TextView tvPoint = findViewById(R.id.tv_point);
                            tvPoint.setText(Integer.toString(jsonObject.getInt("Reputation")));
                        }
                    }else{
                        Log.e("tag", "onResponse: "+response.code() );
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public ArrayList<MapObject> getReviewedItems() {
        return reviewedItems;
    }

    public ArrayList<MapObject> getContributedItems() {
        return contributedItems;
    }

    public ArrayList<BankObject> getArrBank() {
        return arrBank;
    }

    public boolean isGotReviewItems() {
        return gotReviewItems;
    }

    public boolean isGotContributeItems() {
        return gotContributeItems;
    }

    public float getCurrLat() {
        return currLat;
    }

    public float getCurrLon() {
        return currLon;
    }

    public void setReviewedItems(ArrayList<MapObject> reviewedItems) {
        this.reviewedItems = reviewedItems;
    }

    public void setContributedItems(ArrayList<MapObject> contributedItems) {
        this.contributedItems = contributedItems;
    }

    public void setArrBank(ArrayList<BankObject> arrBank) {
        this.arrBank = arrBank;
    }

    public void setGotReviewItems(boolean gotReviewItems) {
        this.gotReviewItems = gotReviewItems;
    }

    public void setGotContributeItems(boolean gotContributeItems) {
        this.gotContributeItems = gotContributeItems;
    }

    public void getBank() {
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

        String token = MyApplication.getInstance().getToken();

        Call<ResponseBody> call = tour.getBank(MyApplication.getInstance().getVersion(), token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        Log.e("", "onResponse: " + jsonObject.toString());

                        if (jsonObject.getBoolean("Status")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("Banks");
                            arrBank.add(new BankObject(0, getString(R.string.all)));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                arrBank.add(new BankObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name")));
                                Log.e("", "onResponse: " + jsonObject1.getString("Name") + getString(R.string.all));
                            }

                            fragment = new ContributedFragment();
                            loadFragment(fragment);
                            current= R.id.btn_contributed;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.e(", ", response.errorBody().toString() + response.code());
                    } catch (Exception e) {
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
}