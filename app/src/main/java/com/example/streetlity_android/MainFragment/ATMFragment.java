package com.example.streetlity_android.MainFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.streetlity_android.Chat.Chat;
import com.example.streetlity_android.MainNavigationHolder;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MapsActivity;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.User.UserInfoOther;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ATMFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ATMFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ATMFragment extends Fragment implements LocationListener {
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
                t.putExtra("item", displayItems.get(position));

                locationManager.removeUpdates(ATMFragment.this);

                startActivity(t);
            }
        });

        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.location_services_off)
                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {

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
                    callATM(currLat, currLon, (float) 0);
                }
                Log.e("", "onMapReady: " + currLat + " , " + currLon);
            }
        }


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
                adapter.getFilter().filter(Integer.toString(arrBank.get(position).getId()));
                dialogSearch.hide();
                edtFind.setText(arrBank.get(position).getName());
                edtFilter.setText("");
                adapter1.getFilter().filter("");
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
                    findLocation(edtDialogFind.getText().toString(), edtDialogFind);
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

    public void findLocation(String address, EditText editText){
        searchItems.clear();
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

                            Call<ResponseBody> call2 = tour2.getATMInRange("1.0.0", (float)mLat, (float)mLon,(float)0.1);
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

                                                String bankName="";
                                                for (int j=0;j<arrBank.size();j++){
                                                    if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                                        bankName = arrBank.get(j).getName();
                                                    }
                                                }

                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                                    Log.e("", "onResponse: " + jsonObject1.toString());
                                                    Log.e("", "onResponse: " + jsonObject1.getInt("Id"));
                                                    MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 3,
                                                            jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                            (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                                    float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                                    item.setImages(jsonObject1.getString("Images"));

                                                    item.setDistance(distance);

                                                    item.setContributor(jsonObject1.getString("Contributor"));

                                                    searchItems.add(item);
                                                }

                                                if(searchItems.size()>0){
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
                                                }

                                            }else{
                                                Toast toast = Toast.makeText(getActivity(), R.string.no_result, Toast.LENGTH_LONG);
                                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                                tv.setTextColor(Color.RED);

                                                toast.show();
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
        if(atcpBank!= null)
            atcpBank.setSelection(0);
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
else{
 tvNoItem.setVisibility(View.GONE);
}
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
            Call<ResponseBody> call = tour.getATMInRange("1.0.0", (float) lat, (float) lon, (float)0.1);
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
                            Log.e("", "onResponse: "+response );
                            if (!jsonObject.getJSONArray("Services").toString().equals("")) {
                                jsonArray = jsonObject.getJSONArray("Services");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    Log.e("", "onResponse: " + jsonObject1.toString());
                                    String bankName="";
                                    for (int j=0;j<arrBank.size();j++){
                                        if (jsonObject1.getInt("BankId") == arrBank.get(j).getId()) {
                                            bankName = arrBank.get(j).getName();
                                        }
                                    }
                                    MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 3,
                                            jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                            (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                    item.setBankId(jsonObject1.getInt("BankId"));

                                    item.setImages(jsonObject1.getString("Images"));

                                    float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                    item.setDistance(distance);

                                    item.setContributor(jsonObject1.getString("Contributor"));

                                    items.add(item);
                                }

                                Collections.sort(items, new Comparator<MapObject>() {
                                    @Override
                                    public int compare(MapObject o1, MapObject o2) {
                                        return Float.compare(o1.getDistance(), o2.getDistance());
                                    }
                                });

                                for(MapObject item: items){
                                    if (item.getDistance() <= 1000){
                                        displayItems.add(item);
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

        Call<ResponseBody> call = tour.getBank("1.0.0",token);
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
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                    if (location == null) {
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
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
