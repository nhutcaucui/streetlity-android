package com.example.streetlity_android.Achievement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.streetlity_android.MainFragment.BankObject;
import com.example.streetlity_android.MainFragment.MapObject;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MapsActivity;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class AchievementOther extends AppCompatActivity implements View.OnClickListener, ContributedFragment.OnFragmentInteractionListener,
        ReviewedFragment.OnFragmentInteractionListener {

    ArrayList<AchievementObject> items = new ArrayList<>();
    ArrayList<MapObject> reviewedItems = new ArrayList<>();
    ArrayList<MapObject> contributedItems = new ArrayList<>();
    ArrayList<BankObject> arrBank = new ArrayList<>();

    float currLat;
    float currLon;

    boolean gotReviewItems= false;
    boolean gotContributeItems= false;

    Map<String, Map<String, ActionObject>> reviewedMap;
    Map<String, Map<String, ActionObject>> contributedMap;

    AchievementObjectAdapter adapterReview;
    AchievementObjectAdapter adapterContribute;

    Fragment fragment;

    int current =R.id.btn_contributed;

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

        getBank();

        getRepu();

        adapterReview = new AchievementObjectAdapter(AchievementOther.this, R.layout.lv_item_achievement, reviewedItems);
        adapterContribute = new AchievementObjectAdapter(AchievementOther.this, R.layout.lv_item_achievement, contributedItems);

        if (ContextCompat.checkSelfPermission(AchievementOther.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(AchievementOther.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager)
                    AchievementOther.this.getSystemService(Context.LOCATION_SERVICE);
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

//        ListView lv = findViewById(R.id.lv);
//
//        items.add(new AchievementObject("achievement notActive", 69, false));
//        items.add(new AchievementObject("achievement notActive", 1, true));
//        items.add(new AchievementObject("achievement active", 500, true));
//        items.add(new AchievementObject("achievement notActive", 123456, false));
//
//        Collections.sort(items, new Comparator<AchievementObject>() {
//            @Override
//            public int compare(AchievementObject abc1, AchievementObject abc2) {
//                return Boolean.compare(abc2.isEarned(),abc1.isEarned());
//            }
//        });

//        AchievementObjectAdapter adapter = new AchievementObjectAdapter(this, R.layout.lv_item_achievement, items);
//
//        lv.setAdapter(adapter);

        Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getAuthURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour2 = retro2.create(MapAPI.class);
        Call<ResponseBody> call2 = tour2.getProgress(getIntent().getStringExtra("user"));

        call2.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call2, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject2 = new JSONObject(response.body().string());
                        Log.e("TAG", "onResponse: " + jsonObject2.toString());
                        if (jsonObject2.getBoolean("Status")) {
                            Map<String, Map<String, ActionObject>> reviewMap = new HashMap<>();
                            Map<String, Map<String, ActionObject>> contributeMap = new HashMap<>();

                            if (!jsonObject2.getString("Progress").equals("")) {
                                jsonObject2 = new JSONObject(jsonObject2.getString("Progress"));

                                JSONObject jsonObject3;
                                JSONObject jsonObject4;

                                if (!jsonObject2.toString().equals("")) {

                                    if (jsonObject2.has("Reviewed")) {
                                        jsonObject3 = jsonObject2.getJSONObject("Reviewed");
                                        jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            reviewMap.put("Maintenance", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Atm");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            reviewMap.put("Atm", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            reviewMap.put("Fuel", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            reviewMap.put("Toilet", insideMap);
                                        }
                                    }

                                    if (jsonObject2.has("Contributed")) {
                                        jsonObject3 = jsonObject2.getJSONObject("Contributed");
                                        jsonObject4 = jsonObject3.getJSONObject("Maintenance");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            contributeMap.put("Maintenance", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Atm");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            contributeMap.put("Atm", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Fuel");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            contributeMap.put("Fuel", insideMap);
                                        }

                                        jsonObject4 = jsonObject3.getJSONObject("Toilet");
                                        if (jsonObject4.length() > 0) {
                                            Iterator<String> keys = jsonObject4.keys();
                                            Map<String, ActionObject> insideMap = new HashMap<>();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                JSONObject jsonObject5 = jsonObject4.getJSONObject(key);
                                                ActionObject ao = new ActionObject(key, jsonObject5.getLong("Time"),
                                                        jsonObject5.getString("Action"), jsonObject5.getString("Affect"));
                                                insideMap.put(key, ao);
                                            }
                                            contributeMap.put("Toilet", insideMap);
                                        }
                                    }
                                }
                            }

                            reviewedMap = reviewMap;
                            contributedMap = contributeMap;

                            fragment = new ContributedFragment();
                            loadFragment(fragment);
                            current= R.id.btn_contributed;

                        }
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
                        Log.e(TAG, "onResponse: " + jsonObject.toString() );
                        if(jsonObject.getBoolean("Status")){
                            TextView tvPoint = findViewById(R.id.tv_point);
                            tvPoint.setText(Integer.toString(jsonObject.getInt("Reputation")));
                        }
                    }else{
                        Log.e(TAG, "onResponse: "+response.code() );
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

    public Map<String, Map<String, ActionObject>> getReviewedMap() {
        return reviewedMap;
    }

    public Map<String, Map<String, ActionObject>> getContributedMap() {
        return contributedMap;
    }
}