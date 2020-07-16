package com.example.streetlity_android.MainFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.streetlity_android.Achievement.ActionObject;
import com.example.streetlity_android.MainNavigationHolder;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MapsActivity;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ATMFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ATMFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ATMFragment extends Fragment implements LocationListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    ArrayList<MapObject> items= new ArrayList<>();
    ArrayList<MapObject> displayItems = new ArrayList<>();
    ArrayList<MapObject> searchItems = new ArrayList<>();
    ArrayList<BankObject> arrBank = new ArrayList<BankObject>();

    Marker currentPosition;

    GoogleMap mMap;

    ArrayList<MarkerOptions> mMarkerOptions = new ArrayList<MarkerOptions>();
    ArrayList<Marker> mMarkers = new ArrayList<>();
    ArrayList<MarkerOptions> searchMakers = new ArrayList<>();

    MapObjectAdapter adapter;
    private static final long MIN_TIME = 1;
    private static final float MIN_DISTANCE = 1000;

    LocationManager locationManager;

    TextView nothingFound;

    ProgressBar loading;
    TextView tvNoItem;
    TextView tvNoInternet;

    float currLat;
    float currLon;

    Spinner atcpBank;

    boolean isSearch = false;

    BankObjectAdapter adapter1;

    Dialog dialogSearch;

    public ATMFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ATMFragment newInstance(String param1, String param2) {
        ATMFragment fragment = new ATMFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_atm, container, false);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

        ListView lv = rootView.findViewById(R.id.list_view);

        loading = rootView.findViewById(R.id.loading);
        tvNoItem = rootView.findViewById(R.id.no_item);
        tvNoInternet = rootView.findViewById(R.id.no_internet);

        adapter = new MapObjectAdapter(getActivity(), R.layout.lv_item_map_object, displayItems);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainNavigationHolder) getActivity()).getLoading().setVisibility(View.VISIBLE);
                Intent t = new Intent(getActivity(), MapsActivity.class);
                t.putExtra("currLat", currLat);
                t.putExtra("currLon", currLon);
                MapObject item = (MapObject) adapter.getItem(position);
                if(isSearch){
                    item.setDistance(distance(currLat,currLon,item.getLat(),
                            item.getLon()));
                }

                t.putExtra("item", item);
                t.putExtra("index", position);

                locationManager.removeUpdates(ATMFragment.this);

                startActivityForResult(t, 2);
            }
        });

//        locationManager = (LocationManager)
//                getActivity().getSystemService(Context.LOCATION_SERVICE);
//
//        boolean gps_enabled = false;
//        boolean network_enabled = false;
//
//        try {
//            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        } catch (Exception ex) {
//        }
//
//        try {
//            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch (Exception ex) {
//        }
//
//        if (!gps_enabled && !network_enabled) {
//            // notify user
//            new AlertDialog.Builder(getActivity())
//                    .setMessage(R.string.location_services_off)
//                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
//                        }
//                    })
//                    .setCancelable(false)
//                    .show();
//        } else {
//
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                Location location = locationManager.getLastKnownLocation(locationManager
//                        .NETWORK_PROVIDER);
//                if (location == null) {
//                    loading.setVisibility(View.GONE);
//                    ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
//                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
//                    Log.e("", "onMapReady: MULL");
//                } else {
//                    currLat = (float) location.getLatitude();
//                    currLon = (float) location.getLongitude();
//                    callATM(currLat, currLon, (float) 0);
//                }
//                Log.e("", "onMapReady: " + currLat + " , " + currLon);
//            }
//        }


        final SeekBar sb = rootView.findViewById(R.id.sb_range);
        ImageButton imgSearch = rootView.findViewById(R.id.img_btn_confirm_range);

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvNoItem.setVisibility(View.GONE);
                nothingFound.setVisibility(View.GONE);
                changeRange(sb.getProgress() + 1);
            }
        });

        LinearLayout layoutSearch = rootView.findViewById(R.id.layout_search);
        EditText edtFind = rootView.findViewById(R.id.edt_find);

        View view = View.inflate(getActivity(), R.layout.dialog_atm_search, null);

        EditText edtDialogFind = view.findViewById(R.id.edt_find);
        ImageButton imgFind = view.findViewById(R.id.img_btn_find);
        ListView lv2 = view.findViewById(R.id.lv);
        EditText edtFilter = view.findViewById(R.id.edt_filter_bank);
        ImageView imgClose = view.findViewById(R.id.img_close);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSearch.hide();
            }
        });

        edtFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter1.getFilter().filter(edtFilter.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lv2.setTextFilterEnabled(true);

        getBank(rootView, lv2);
        //lv2.setAdapter(adapter1);

        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.getFilter().filter(Integer.toString(((BankObject)adapter1.getItem(position)).getId()));
                filterMarkerByBank(((BankObject)adapter1.getItem(position)).getName(), sb.getProgress());
                dialogSearch.hide();
                edtFind.setText(((BankObject)adapter1.getItem(position)).getName());
                edtFilter.setText("");
                adapter1.getFilter().filter("");
                rootView.findViewById(R.id.layout_revert).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.layout_range).setVisibility(View.GONE);

                if(adapter.getCount() == 0){
                    tvNoItem.setVisibility(View.VISIBLE);
                }
            }
        });

        imgFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
                if(!edtDialogFind.getText().toString().equals("")) {
                    findLocation(edtDialogFind.getText().toString(), edtDialogFind, edtFind);
                    edtFind.setText(edtDialogFind.getText().toString());
                }else{
                    Toast toast = Toast.makeText(getActivity(), R.string.address_not_found, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                    tv.setTextColor(Color.RED);

                    toast.show();
                }
            }
        });

        layoutSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogSearch == null) {
                    dialogSearch = new Dialog(getActivity());
                    dialogSearch.setContentView(view);
                    dialogSearch.setCanceledOnTouchOutside(false);
                    dialogSearch.show();
                }else{
                    dialogSearch.show();
                }

            }
        });



        LinearLayout layoutRevert = rootView.findViewById(R.id.layout_revert);
        layoutRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSearch = false;
                rootView.findViewById(R.id.layout_range).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.layout_revert).setVisibility(View.GONE);

                changeRange(sb.getProgress()+1);

                edtFind.setText("");
            }
        });

        nothingFound = rootView.findViewById(R.id.nothing_found);
        nothingFound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nothingFound.setVisibility(View.GONE);
            }
        });

        return rootView;

    }

    public void filterMarkerByBank(String name, float range){
        mMap.clear();
        mMarkers.clear();
        addCurrMarker();
        Log.e(TAG, "filterMarkerByBank: "+ displayItems.size());
        for (int i = 0 ;i < displayItems.size();i++){
            if(mMarkerOptions.get(i).getTitle().equals(name) && displayItems.get(i).getDistance()<= (range*1000)){
                mMarkers.add(mMap.addMarker((mMarkerOptions.get(i))));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);

        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_services_off)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1);
                            paramDialogInterface.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(locationManager
                        .GPS_PROVIDER);

                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                    loading.setVisibility(View.GONE);
                    ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                    Log.e("", "onMapReady: MULL");
                } else {
                    currLat = (float) location.getLatitude();
                    currLon = (float) location.getLongitude();
                    callATM(currLat, currLon, (float) 1000);

                    MarkerOptions curPositionMark = new MarkerOptions();
                    curPositionMark.position(new LatLng(currLat,currLon));
                    curPositionMark.title(getString(R.string.you_r_here));
                    currentPosition = mMap.addMarker(curPositionMark);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLon), 13f));
                }
                Log.e("", "onMapReady: " + currLat + " , " + currLon);
            }

        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        if(!marker.equals(currentPosition)) {

            for(int i = 0; i < mMarkers.size();i++){
                if(mMarkers.get(i).equals(marker)){
                   // final LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());

                    final View dialogView = View.inflate(getActivity(),R.layout.dialog_simple_map_info, null);

                    Button btnInfo = dialogView.findViewById(R.id.btn_more_info);

                    TextView tvName = dialogView.findViewById(R.id.tv_name);

                    tvName.setText(displayItems.get(i).getName());

                    TextView tvAddress = dialogView.findViewById(R.id.tv_address);

                    tvAddress.setText(displayItems.get(i).getAddress());

                    TextView tvDistance = dialogView.findViewById(R.id.tv_distance);

                    DecimalFormat df = new DecimalFormat("#.#");

                    ImageView imgIcon = dialogView.findViewById(R.id.img_service_icon);

                    imgIcon.setImageResource(R.drawable.atm_big_icon);


                    float distance = this.displayItems.get(i).getDistance();
                    String dis = "m";
                    if(distance > 1000){
                        dis = "km";
                        distance = distance / 1000;
                    }
                    tvDistance.setText("~" + df.format(distance) + dis);

                    final int pos = i;

                    btnInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((MainNavigationHolder) getActivity()).getLoading().setVisibility(View.VISIBLE);
                            Intent t = new Intent(getActivity(), MapsActivity.class);
                            t.putExtra("currLat", currLat);
                            t.putExtra("currLon", currLon);
                            t.putExtra("item", displayItems.get(pos));
                            t.putExtra("index", pos);
                            Log.e("", "onItemClick: " + displayItems.get(pos).getId());
                            locationManager.removeUpdates(ATMFragment.this);
                            startActivityForResult(t, 2);
                        }
                    });

                    final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), android.R.style.Theme_Black_NoTitleBar);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
                    dialog.setContentView(dialogView);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.setCancelable(true);

                    dialog.show();
                }
            }


        }

        marker.showInfoWindow();

        return true;
    }

    public void addCurrMarker(){
        MarkerOptions curPositionMark = new MarkerOptions();
        curPositionMark.position(new LatLng(currLat,currLon));
        curPositionMark.title(getString(R.string.you_r_here));
        currentPosition = mMap.addMarker(curPositionMark);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat,currLon), 15f));
    }

    public void findLocation(String address, EditText editText, EditText editTextFind){
        searchItems.clear();
        searchMakers.clear();
        Retrofit retro = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        Retrofit retro2 = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        final MapAPI tour2 = retro2.create(MapAPI.class);
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
                            Toast toast = Toast.makeText(getActivity(), R.string.address_not_found, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(Color.RED);

                            toast.show();
                        }
                        else{

                            jsonArray = jsonObject.getJSONArray("results");

                            JSONObject jsonObject1;
                            jsonObject1 = jsonArray.getJSONObject(0);

                            JSONObject jsonObjectGeomertry = jsonObject1.getJSONObject("geometry");
                            JSONObject jsonLatLng = jsonObjectGeomertry.getJSONObject("location");

                            double mLat = jsonLatLng.getDouble("lat");
                            double mLon = jsonLatLng.getDouble("lng");

                            editText.setText(jsonObject1.getString("formatted_address"));
                            editTextFind.setText(jsonObject1.getString("formatted_address"));

                            Call<ResponseBody> call2 = tour2.getATMInRange(MyApplication.getInstance().getVersion(), (float)mLat, (float)mLon,(float)0.1);
                            call2.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if(response.code()==200){
                                        final JSONObject jsonObject;
                                        JSONArray jsonArray;
                                        try {
                                            jsonObject = new JSONObject(response.body().string());
                                            Log.e("", "onResponse: " + jsonObject.toString());
                                            if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                                                jsonArray = jsonObject.getJSONArray("Services");
                                                for (int i = 0; i < jsonArray.length(); i++) {


                                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                                                    String bankName="";
                                                    for (int j=0;j<arrBank.size();j++){
                                                        if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                                            bankName = arrBank.get(j).getName();
                                                        }
                                                    }

                                                    Log.e("", "onResponse: " + jsonObject1.toString());
                                                    Log.e("", "onResponse: " + jsonObject1.getInt("Id"));
                                                    MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 0,
jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                            (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                                    float distance = distance((float)mLat, (float)mLon,(float) jsonObject1.getDouble("Lat"), (float)jsonObject1.getDouble("Lon"));

                                                    item.setImages(jsonObject1.getString("Images"));

                                                    item.setDistance(distance);

                                                    item.setContributor(jsonObject1.getString("Contributor"));

                                                    item.setConfident(jsonObject1.getInt("Confident"));

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
                                                    searchItems.add(item);
                                                }

                                                if(searchItems.size()>0){
                                                    nothingFound.setVisibility(View.GONE);
                                                    Collections.sort(searchItems, new Comparator<MapObject>() {
                                                        @Override
                                                        public int compare(MapObject o1, MapObject o2) {
                                                            return Float.compare(o1.getDistance(), o2.getDistance());
                                                        }
                                                    });

                                                    isSearch = true;

                                                    displayItems.clear();
                                                    displayItems.addAll(searchItems);
                                                    locationManager.removeUpdates(ATMFragment.this);

                                                    mMap.clear();
                                                    mMarkers.clear();
                                                    for(int i =0;i< displayItems.size();i++){
                                                        addATMMarkerToSearchList(displayItems.get(i).getLat(), displayItems.get(i).getLon(), displayItems.get(i).getName());
                                                    }

                                                    MarkerOptions options = new MarkerOptions();
                                                    options.position(new LatLng(mLat, mLon));
                                                    options.title(getString(R.string.search_location));
                                                    mMap.addMarker(options);

                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat,mLon),15f));

                                                    getActivity().findViewById(R.id.layout_range).setVisibility(View.GONE);

                                                    getActivity().findViewById(R.id.layout_revert).setVisibility(View.VISIBLE);

                                                    adapter.notifyDataSetChanged();
//
//                                                    ((MainNavigationHolder) getActivity()).getLoading().setVisibility(View.VISIBLE);
//                                                    Intent t = new Intent(getActivity(), MapsActivity.class);
//                                                    t.putExtra("currLat", currLat);
//                                                    t.putExtra("currLon", currLon);
//                                                    t.putExtra("item", searchItems.get(0));
//                                                    Log.e("", "onItemClick: " + searchItems.get(0).getId());
//                                                    startActivity(t);

                                                    dialogSearch.hide();

                                                    editText.setText("");
                                                }
                                                else{
                                                    Toast toast = Toast.makeText(getActivity(), R.string.no_result, Toast.LENGTH_LONG);
                                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                    tv.setTextColor(Color.RED);

                                                    toast.show();

                                                    dialogSearch.hide();
                                                }

                                            }else{
                                                Toast toast = Toast.makeText(getActivity(), R.string.no_result, Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();

                                                dialogSearch.hide();
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                        Log.e(TAG, "onResponse: " + response.code());
                                        Toast toast = Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

            if(((MainNavigationHolder)getActivity()).getLoading() != null)
                ((MainNavigationHolder)getActivity()).getLoading().setVisibility(View.GONE);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void changeRange(float range){
        loading.setIndeterminate(true);
        loading.setVisibility(View.VISIBLE);
        displayItems.clear();

        mMarkers.clear();
        mMap.clear();

        addCurrMarker();

        for (int i =0 ;i<items.size();i++) {
            if (items.get(i).getDistance() <= (range * 1000)) {
                displayItems.add(items.get(i));
                mMarkers.add(mMap.addMarker(mMarkerOptions.get(i)));
            }
        }

        adapter.getFilter().filter("0");

        if(atcpBank!= null)
            atcpBank.setSelection(0);
//        for(MapObject item: items){
//            if (item.getDistance() <= (range*1000)){
//                displayItems.add(item);
//            }
//        }
        adapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);
        if(displayItems.size()==0){
            tvNoItem.setVisibility(View.VISIBLE);
        }
        else{
         tvNoItem.setVisibility(View.GONE);
        }

        Toast toast = Toast.makeText(getActivity(), getString(R.string.change_range_to) + " " + (range) + "km", Toast.LENGTH_LONG);
        toast.show();

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(314);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(314);
        fadeOut.setDuration(314);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);

        getActivity().findViewById(R.id.aura).setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                getActivity().findViewById(R.id.aura).setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            public void onAnimationStart(Animation animation) {
                getActivity().findViewById(R.id.aura).setVisibility(View.VISIBLE);
            }
        });
    }

    public void callATM(double lat, double lon, float range){
        items.removeAll(items);
        displayItems.clear();
        if(atcpBank!= null)
            atcpBank.setSelection(0);
        if(isNetworkAvailable()) {
            loading.setIndeterminate(true);
            loading.setVisibility(View.VISIBLE);
            tvNoInternet.setVisibility(View.GONE);
            Log.e("", "callATM: " + range);
            Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                    .addConverterFactory(GsonConverterFactory.create()).build();
            final MapAPI tour = retro.create(MapAPI.class);
            Call<ResponseBody> call = tour.getATMInRange(MyApplication.getInstance().getVersion(), (float) lat, (float) lon, (float)0.1);
            //Call<ResponseBody> call = tour.getAllFuel();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        final JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "1onResponse: " + jsonObject.toString());

                            if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                                jsonArray = jsonObject.getJSONArray("Services");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Log.e("", "2onResponse: " + jsonObject1.toString());
                                    String bankName="";
                                    for (int j=0;j<arrBank.size();j++){
                                        if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                            bankName = arrBank.get(j).getName();
                                        }
                                    }
                                    MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 0,
jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                            (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                    item.setBankId(jsonObject1.getInt("BankId"));

                                    item.setImages(jsonObject1.getString("Images"));

                                    float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                    item.setDistance(distance);

                                    item.setContributor(jsonObject1.getString("Contributor"));

                                    item.setConfident(jsonObject1.getInt("Confident"));

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
                                    items.add(item);
                                }

                                Collections.sort(items, new Comparator<MapObject>() {
                                    @Override
                                    public int compare(MapObject o1, MapObject o2) {
                                        return Float.compare(o1.getDistance(), o2.getDistance());
                                    }
                                });

                                for(int i = 0; i< items.size();i++){
                                    addATMMarkerToList(items.get(i).getLat(),
                                            items.get(i).getLon(),items.get(i).getName());
                                    if (items.get(i).getDistance() <= range) {
                                        displayItems.add(items.get(i));
                                        mMarkers.add(mMap.addMarker(mMarkerOptions.get(i)));
                                    }
                                }

                                adapter.notifyDataSetChanged();

                                if (items.size() == 0 || displayItems.size() == 0) {
                                    tvNoItem.setVisibility(View.VISIBLE);
                                }

                                if(displayItems.size() == 0){
                                    if(nothingFound!= null) {
                                        nothingFound.setVisibility(View.VISIBLE);
                                    }
                                }

                                loading.setIndeterminate(false);
                                loading.setVisibility(View.GONE);
                            }
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
        }else{
            tvNoInternet.setVisibility(View.VISIBLE);
loading.setVisibility(View.GONE);
        }
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

    public void getBank(View view, ListView lv){
        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);

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
                            arrBank.add(new BankObject(0,getString(R.string.all)));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                arrBank.add(new BankObject(jsonObject1.getInt("Id"),jsonObject1.getString("Name")));
                                Log.e("", "onResponse: "+ jsonObject1.getString("Name") + getString(R.string.all) );
                            }

//                            atcpBank = view.findViewById(R.id.actv_bank);

                            adapter1 = new BankObjectAdapter(getActivity(), R.layout.bank_item, arrBank);

                            lv.setAdapter(adapter1);

                            adapter1.notifyDataSetChanged();

//                            atcpBank.setAdapter(adapter1);
//
//                            atcpBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                                @Override
//                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                                        adapter.getFilter().filter(Integer.toString(arrBank.get(position).getId()));
//                                        Log.e("", "onItemSelected: " + arrBank.get(position).getId());
//
//                                }
//
//                                @Override
//                                public void onNothingSelected(AdapterView<?> parent) {
//
//                                }
//                            });

//                            try {
//                                Field popup = Spinner.class.getDeclaredField("mPopup");
//                                popup.setAccessible(true);
//
//                                // Get private mPopup member variable and try cast to ListPopupWindow
//                                android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(atcpBank);
//
//                                // Set popupWindow height to 500px
//                                popupWindow.setHeight(500);
//                            }
//                            catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
//                                // silently fail...
//                            }
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
                Log.e("", "onFailure: " + t.toString());
            }
        });
    }

    public void onLocationChanged(Location location) {
        currLat = (float) location.getLatitude();
        currLon = (float) location.getLongitude();
        loading.setVisibility(View.VISIBLE);
        if(getActivity()!= null) {
            ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.GONE);
            callATM(location.getLatitude(), location.getLongitude(), 0);
        }

        MarkerOptions curPositionMark = new MarkerOptions();
        curPositionMark.position(new LatLng(currLat,currLon));
        curPositionMark.title(getString(R.string.you_r_here));
        mMap.addMarker(curPositionMark);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLat, currLon), 13f));

        locationManager.removeUpdates(this);

    }

    public void addATMMarkerToList(float lat, float lon, String name){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(name);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));
        option.position(pos);
        mMarkerOptions.add(option);
        //mMarkers.add(mMap.addMarker(option));
    }

    public void addATMMarkerToSearchList(float lat, float lon, String name){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(name);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_atm));
        option.position(pos);
        searchMakers.add(option);
        mMarkers.add(mMap.addMarker(option));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                // notify user
                AlertDialog al =new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.location_services_off)
                        .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                getActivity().startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1);
                                paramDialogInterface.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            else {

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(locationManager
                            .GPS_PROVIDER);
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                        loading.setVisibility(View.GONE);
                        ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                        Log.e("", "onMapReady: MULL");
                    } else {
                        currLat = (float) location.getLatitude();
                        currLon = (float) location.getLongitude();
                        callATM(currLat, currLon, (float) 0);
                    }
                    Log.e("", "onMapReady: " + currLat + " , " + currLon);
                }

            }
        }

        if(requestCode == 2 && resultCode == RESULT_OK&& null!= data) {
            int action = data.getIntExtra("action", -1);
            int index = data.getIntExtra("index", -1);
            int confident = data.getIntExtra("confident", -1);
            if(action != -1) {
                displayItems.get(index).setConfident(confident);
                if (action == 1) {
                    displayItems.get(index).setUpvoted(true);
                    displayItems.get(index).setDownvoted(false);
                    for (MapObject m : items) {
                        if (m.getId() == displayItems.get(index).getId()) {
                            m.setUpvoted(true);
                            m.setDownvoted(false);
                        }
                    }
                } else if (action == 2) {
                    displayItems.get(index).setUpvoted(false);
                    displayItems.get(index).setDownvoted(true);
                    for (MapObject m : items) {
                        if (m.getId() == displayItems.get(index).getId()) {
                            m.setUpvoted(false);
                            m.setDownvoted(true);
                        }
                    }
                } else if (action == 3) {
                    displayItems.get(index).setUpvoted(false);
                    displayItems.get(index).setDownvoted(false);
                    for (MapObject m : items) {
                        if (m.getId() == displayItems.get(index).getId()) {
                            m.setUpvoted(false);
                            m.setDownvoted(false);
                        }
                    }
                }
            }
            if(data.hasExtra("image")){
                displayItems.get(index).setImages(data.getStringExtra("image"));
                for (MapObject m : items) {
                    if (m.getId() == displayItems.get(index).getId()) {
                        m.setImages(data.getStringExtra("image"));
                    }
                }
            }
            if(data.hasExtra("note")){
                displayItems.get(index).setNote(data.getStringExtra("note"));
                for (MapObject m : items) {
                    if (m.getId() == displayItems.get(index).getId()) {
                        m.setNote(data.getStringExtra("note"));
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
