package com.example.streetlity_android.Achievement;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AchievementOther extends AppCompatActivity {

    ArrayList<AchievementObject> items = new ArrayList<>();
    ArrayList<MapObject> reviewedItems = new ArrayList<>();
    ArrayList<MapObject> contributedItems = new ArrayList<>();
    ArrayList<BankObject> arrBank = new ArrayList<>();

    float currLat;
    float currLon;

    Map<String, Map<String, ActionObject>> reviewedMap;
    Map<String, Map<String, ActionObject>> contributedMap;

    AchievementObjectAdapter adapterReview;
    AchievementObjectAdapter adapterContribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

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
                if(response.code() == 200){
                    try{
                        JSONObject jsonObject2 = new JSONObject(response.body().string());
                        Log.e("TAG", "onResponse: " +jsonObject2.toString() );
                        if(jsonObject2.getBoolean("Status")){
                            Map<String, Map<String, ActionObject>> reviewMap = new HashMap<>();
                            Map<String, Map<String, ActionObject>> contributeMap = new HashMap<>();

                            if(!jsonObject2.getString("Progress").equals("")) {
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

                            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                                    .addConverterFactory(GsonConverterFactory.create()).build();

                            final MapAPI tour = retro.create(MapAPI.class);

                            if(reviewedMap.containsKey("Fuel")) {
                                Map<String, ActionObject> map = reviewedMap.get("Fuel");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getFuel(MyApplication.getInstance().getVersion(),Integer.parseInt(map.get(key).getAffected()));
                                    Log.e("", "onResponse: " + map.get(key).getAffected());
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());

                                                    if(jsonObject.getBoolean("Status")){
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        String name = getString(R.string.fuel);
                                                        if(!jsonObject1.getString("Name").equals("")){
                                                            name = jsonObject1.getString("Name");
                                                        }

                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 1);
                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Fuel")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Fuel");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Fuel")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Fuel");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        reviewedItems.add(item);
                                                        adapterReview.notifyDataSetChanged();
                                                    }
                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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
                            }

                            if(reviewedMap.containsKey("Toilet")) {
                                Map<String, ActionObject> map = reviewedMap.get("Toilet");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getWC(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());

                                                    if(jsonObject.getBoolean("Status")){
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        String name = getString(R.string.wc);
                                                        if(!jsonObject1.getString("Name").equals("")){
                                                            name = jsonObject1.getString("Name");
                                                        }
                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 2);
                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Toilet")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Toilet");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Toilet")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Toilet");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        reviewedItems.add(item);
                                                        adapterReview.notifyDataSetChanged();
                                                    }
                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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
                            }

                            if(reviewedMap.containsKey("Maintenance")) {
                                Map<String, ActionObject> map = reviewedMap.get("Maintenance");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getMaintenance(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());

                                                    if(jsonObject.getBoolean("Status")){
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name"), 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 3);
                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Maintenance")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Maintenance");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Maintenance")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Maintenance");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        reviewedItems.add(item);
                                                        adapterReview.notifyDataSetChanged();
                                                    }
                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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
                            }

                            if(reviewedMap.containsKey("Atm")) {
                                Map<String, ActionObject> map = reviewedMap.get("Atm");

                                String token = MyApplication.getInstance().getToken();

                                Call<ResponseBody> call = tour.getBank(MyApplication.getInstance().getVersion(),token);

                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200) {
                                            final JSONObject jsonObject;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                Log.e("", "onResponse: " + jsonObject.toString());

                                                if(jsonObject.getBoolean("Status")) {
                                                    JSONArray jsonArray = jsonObject.getJSONArray("Banks");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                        arrBank.add(new BankObject(jsonObject1.getInt("Id"),jsonObject1.getString("Name")));
                                                        Log.e("", "onResponse: "+ jsonObject1.getString("Name") + getString(R.string.all) );
                                                    }

                                                    for (String key : map.keySet()) {
                                                        Call<ResponseBody> call2 = tour.getAtm(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                                        call2.enqueue(new Callback<ResponseBody>() {
                                                            @Override
                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                try{
                                                                    if(response.code() == 200){
                                                                        JSONObject jsonObject = new JSONObject(response.body().string());
                                                                        Log.e("", "onResponse: " + jsonObject.toString());

                                                                        if(jsonObject.getBoolean("Status")){
                                                                            JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                                            String bankName="";
                                                                            for (int j=0;j<arrBank.size();j++){
                                                                                if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                                                                    bankName = arrBank.get(j).getName();
                                                                                }
                                                                            }
                                                                            MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 3,
                                                                                    jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                                    (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);
                                                                            float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                                            item.setImages(jsonObject1.getString("Images"));

                                                                            item.setDistance(distance);

                                                                            item.setContributor(jsonObject1.getString("Contributor"));

                                                                            item.setDownvoted(false);
                                                                            item.setUpvoted(false);

                                                                            if(MyApplication.getInstance().getUpvoteMap().containsKey("Atm")) {
                                                                                Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Fuel");
                                                                                if(map.containsKey("upvote "+ item.getId())) {
                                                                                    item.setUpvoted(true);
                                                                                }

                                                                            }if (MyApplication.getInstance().getDownvoteMap().containsKey("Atm")){
                                                                                Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Fuel");
                                                                                if(map.containsKey("downvote "+ item.getId())){
                                                                                    item.setDownvoted(true);
                                                                                }
                                                                            }
                                                                            reviewedItems.add(item);

                                                                            adapterReview.notifyDataSetChanged();
                                                                        }
                                                                    }else{
                                                                        Log.e(TAG, "onResponse: "+ response.code());
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
                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                        else{
                                            try {
                                                Log.e(", ",response.errorBody().toString() + response.code());
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

                            if(contributedMap.containsKey("Fuel")) {
                                Map<String, ActionObject> map = contributedMap.get("Fuel");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getFuel(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                    Log.e("", "onResponse: " + map.get(key).getAffected());
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());
                                                    Log.e(TAG, "onResponse: 00" + jsonObject.toString() );

                                                    if(jsonObject.getBoolean("Status")){
                                                        String name = getString(R.string.fuel);
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        if(!jsonObject1.getString("Name").equals("")){
                                                            name = jsonObject1.getString("Name");
                                                        }
                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 1);
                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Fuel")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Fuel");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Fuel")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Fuel");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        contributedItems.add(item);
                                                        adapterContribute.notifyDataSetChanged();
                                                    }


                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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

                            }

                            if(contributedMap.containsKey("Toilet")) {
                                Map<String, ActionObject> map = contributedMap.get("Toilet");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getWC(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());

                                                    if(jsonObject.getBoolean("Status")){
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        String name = getString(R.string.wc);
                                                        if(!jsonObject1.getString("Name").equals("")){
                                                            name = jsonObject1.getString("Name");
                                                        }
                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 2);
                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Toilet")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Toilet");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Toilet")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Toilet");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        contributedItems.add(item);
                                                        adapterContribute.notifyDataSetChanged();
                                                    }
                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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

                            }

                            if(contributedMap.containsKey("Maintenance")) {
                                Map<String, ActionObject> map = contributedMap.get("Maintenance");
                                for (String key : map.keySet()) {
                                    Call<ResponseBody> call = tour.getMaintenance(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try{
                                                if(response.code() == 200){
                                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                                    Log.e("", "onResponse: " + jsonObject.toString());

                                                    if(jsonObject.getBoolean("Status")){
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name"), 0,
                                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 3);

                                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                        item.setImages(jsonObject1.getString("Images"));

                                                        item.setDistance(distance);

                                                        item.setContributor(jsonObject1.getString("Contributor"));

                                                        item.setDownvoted(false);
                                                        item.setUpvoted(false);

                                                        if(MyApplication.getInstance().getUpvoteMap().containsKey("Maintenance")) {
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Maintenance");
                                                            if(map.containsKey("upvote "+ item.getId())) {
                                                                item.setUpvoted(true);
                                                            }

                                                        }if (MyApplication.getInstance().getDownvoteMap().containsKey("Maintenance")){
                                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Maintenance");
                                                            if(map.containsKey("downvote "+ item.getId())){
                                                                item.setDownvoted(true);
                                                            }
                                                        }
                                                        contributedItems.add(item);
                                                        adapterContribute.notifyDataSetChanged();
                                                    }
                                                }else{
                                                    Log.e(TAG, "onResponse: "+ response.code());
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
                                adapterContribute.notifyDataSetChanged();
                            }

                            if(contributedMap.containsKey("Atm")) {
                                Map<String, ActionObject> map = contributedMap.get("Atm");

                                String token = MyApplication.getInstance().getToken();

                                Call<ResponseBody> call = tour.getBank(MyApplication.getInstance().getVersion(),token);

                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        if(response.code() == 200) {
                                            final JSONObject jsonObject;
                                            try {
                                                jsonObject = new JSONObject(response.body().string());
                                                Log.e("", "onResponse: " + jsonObject.toString());

                                                if(jsonObject.getBoolean("Status")) {
                                                    JSONArray jsonArray = jsonObject.getJSONArray("Banks");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                        arrBank.add(new BankObject(jsonObject1.getInt("Id"),jsonObject1.getString("Name")));
                                                        Log.e("", "onResponse: "+ jsonObject1.getString("Name") + getString(R.string.all) );
                                                    }

                                                    for (String key : map.keySet()) {
                                                        Call<ResponseBody> call2 = tour.getAtm(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                                                        call2.enqueue(new Callback<ResponseBody>() {
                                                            @Override
                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                try{
                                                                    if(response.code() == 200){
                                                                        JSONObject jsonObject = new JSONObject(response.body().string());
                                                                        Log.e("", "onResponse: " + jsonObject.toString());

                                                                        if(jsonObject.getBoolean("Status")){
                                                                            JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                                                            String bankName="";
                                                                            for (int j=0;j<arrBank.size();j++){
                                                                                if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                                                                    bankName = arrBank.get(j).getName();
                                                                                }
                                                                            }
                                                                            MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 3,
                                                                                    jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                                                    (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                                                            float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                                            item.setImages(jsonObject1.getString("Images"));

                                                                            item.setDistance(distance);

                                                                            item.setContributor(jsonObject1.getString("Contributor"));

                                                                            item.setDownvoted(false);
                                                                            item.setUpvoted(false);

                                                                            if(MyApplication.getInstance().getUpvoteMap().containsKey("Atm")) {
                                                                                Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Atm");
                                                                                if(map.containsKey("upvote "+ item.getId())) {
                                                                                    item.setUpvoted(true);
                                                                                }

                                                                            }if (MyApplication.getInstance().getDownvoteMap().containsKey("Atm")){
                                                                                Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Atm");
                                                                                if(map.containsKey("downvote "+ item.getId())){
                                                                                    item.setDownvoted(true);
                                                                                }
                                                                            }
                                                                            contributedItems.add(item);

                                                                            adapterContribute.notifyDataSetChanged();
                                                                        }
                                                                    }else{
                                                                        Log.e(TAG, "onResponse: "+ response.code());
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
                                                    adapterContribute.notifyDataSetChanged();
                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                        else{
                                            try {
                                                Log.e(", ",response.errorBody().toString() + response.code());
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


                            ListView lvReview = findViewById(R.id.lv_review);
                            ListView lvContribute = findViewById(R.id.lv_contribute);

                            lvReview.setAdapter(adapterReview);
                            lvContribute.setAdapter(adapterContribute);

                            lvReview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent t = new Intent(AchievementOther.this, MapsActivity.class);
                                    t.putExtra("currLat", currLat);
                                    t.putExtra("currLon", currLon);
                                    MapObject item = (MapObject) adapterReview.getItem(position);

                                    t.putExtra("item", item);

                                    startActivity(t);
                                }
                            });

                            lvContribute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent t = new Intent(AchievementOther.this, MapsActivity.class);
                                    t.putExtra("currLat", currLat);
                                    t.putExtra("currLon", currLon);
                                    MapObject item = (MapObject) adapterContribute.getItem(position);

                                    t.putExtra("item", item);

                                    startActivity(t);
                                }
                            });

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

        Call<ResponseBody> call = tour.getReputation(getIntent().getStringExtra("user"));
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
}