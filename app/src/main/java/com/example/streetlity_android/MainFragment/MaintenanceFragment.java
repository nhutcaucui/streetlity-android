package com.example.streetlity_android.MainFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.streetlity_android.BroadcastActivity;
import com.example.streetlity_android.MainNavigationHolder;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MapsActivity;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MaintenanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MaintenanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MaintenanceFragment extends Fragment implements LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    ArrayList<MapObject> items= new ArrayList<>();
    ArrayList<MapObject> displayItems= new ArrayList<>();
    MapObjectAdapter adapter;

    ProgressBar loading;
    TextView tvNoItem;
    TextView tvNoInternet;

    float currLat;
    float currLon;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    LocationManager locationManager;

    public MaintenanceFragment() {
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
    public static MaintenanceFragment newInstance(String param1, String param2) {
        MaintenanceFragment fragment = new MaintenanceFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_maintenance, container, false);

        loading = rootView.findViewById(R.id.loading);
        tvNoItem = rootView.findViewById(R.id.no_item);
        tvNoInternet = rootView.findViewById(R.id.no_internet);

        ListView lv = rootView.findViewById(R.id.list_view);

        adapter = new MapObjectAdapter(getActivity(), R.layout.lv_item_map_object, displayItems);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent t = new Intent(getActivity(), MapsActivity.class);
                t.putExtra("currLat", currLat);
                t.putExtra("currLon", currLon);
                t.putExtra("item", displayItems.get(position));

                startActivity(t);
            }
        });

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
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
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
                        .NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if (location == null) {
                    loading.setVisibility(View.GONE);
                    ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                    Log.e("", "onMapReady: MULL");
                } else {
                    currLat = (float) location.getLatitude();
                    currLon = (float) location.getLongitude();
                    callMaintenance(currLat, currLon, (float) 0);


                }
                Log.e("", "onMapReady: " + currLat + " , " + currLon);
            }
        }

        FloatingActionButton fab = rootView.findViewById(R.id.fab_broadcast);

        if(MyApplication.getInstance().getUsername().equals("")){
            fab.hide();
        }else {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent t = new Intent(getActivity(), BroadcastActivity.class);
                    t.putExtra("currLat", currLat);
                    t.putExtra("currLon", currLon);

                    getActivity().startActivityForResult(t, 5);
                }
            });
        }


        final SeekBar sb = rootView.findViewById(R.id.sb_range);
        ImageButton imgSearch = rootView.findViewById(R.id.img_btn_confirm_range);

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvNoItem.setVisibility(View.GONE);
                changeRange(sb.getProgress()+1);
            }
        });

        return rootView;
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
        for(MapObject item: items){
            if (item.getDistance() <= (range*1000)){
                displayItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        loading.setVisibility(View.GONE);
        if(displayItems.size()==0){
            tvNoItem.setVisibility(View.VISIBLE);
        }
    }

    public void callMaintenance(double lat, double lon, float range){
        items.removeAll(items);
        displayItems.clear();
        if(isNetworkAvailable()) {
            loading.setIndeterminate(true);
            loading.setVisibility(View.VISIBLE);
            tvNoInternet.setVisibility(View.GONE);
            Log.e("", "callMaintenance: " + range);
            Retrofit retro = new Retrofit.Builder().baseUrl("http://35.240.207.83/")
                    .addConverterFactory(GsonConverterFactory.create()).build();
            final MapAPI tour = retro.create(MapAPI.class);
            Call<ResponseBody> call = tour.getMaintenanceInRange("1.0.0", (float) lat, (float) lon, (float)0.1);
            //Call<ResponseBody> call = tour.getAllFuel();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        final JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            Log.e("", "onResponse: " + jsonObject.toString());
                            if (jsonObject.getJSONArray("Services").toString() != "null") {
                                jsonArray = jsonObject.getJSONArray("Services");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Log.e("", "onResponse: " + jsonObject1.toString());
                                    MapObject item = new MapObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name"), 3,
                                            jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                            (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 3);

                                    float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                    item.setImages(jsonObject1.getString("Images"));

                                    item.setDistance(distance);
                                    items.add(item);
                                }

                                for(MapObject item: items){
                                    if (item.getDistance() <= 1000){
                                        displayItems.add(item);
                                    }
                                }

                                adapter.notifyDataSetChanged();

                                Collections.sort(items, new Comparator<MapObject>() {
                                    @Override
                                    public int compare(MapObject o1, MapObject o2) {
                                        return Float.compare(o1.getDistance(), o2.getDistance());
                                    }
                                });
                                if (items.size() == 0 || displayItems.size() == 0) {
                                    tvNoItem.setVisibility(View.VISIBLE);
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
        }else {
            tvNoInternet.setVisibility(View.VISIBLE);
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

    public void onLocationChanged(Location location) {
        currLat = (float) location.getLatitude();
        currLon = (float) location.getLongitude();
        loading.setVisibility(View.VISIBLE);
        ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.GONE);
        callMaintenance(location.getLatitude(),location.getLongitude(),0);
        locationManager.removeUpdates(this);
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
        if (resultCode == 1) {
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
                            .NETWORK_PROVIDER);
                    if (location == null) {
                        loading.setVisibility(View.GONE);
                        ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                        Log.e("", "onMapReady: MULL");
                    } else {
                        currLat = (float) location.getLatitude();
                        currLon = (float) location.getLongitude();
                        callMaintenance(currLat, currLon, (float) 0);
                    }
                    Log.e("", "onMapReady: " + currLat + " , " + currLon);
                }

            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
