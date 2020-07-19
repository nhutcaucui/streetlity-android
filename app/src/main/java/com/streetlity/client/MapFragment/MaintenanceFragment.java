package com.streetlity.client.MapFragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.streetlity.client.MapAPI;
import com.streetlity.client.MyApplication;
import com.streetlity.client.R;
import com.streetlity.client.Review;
import com.streetlity.client.ReviewAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
@Deprecated
public class MaintenanceFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    double latitude = 0;
    double longitude = 0;

    Marker currentPosition;

    ArrayList<MaintenanceObject> items = new ArrayList<>();

    ArrayList<Marker> mMarkers = new ArrayList<Marker>();
    ArrayList<String> arrReason = new ArrayList<String>();

    Polyline polyline;
    boolean isOther = false;
    boolean first = true;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FuelFragment.OnFragmentInteractionListener mListener;

    public MaintenanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new_trip instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new_trip instance of fragment SuggestTour.
     */
    public static FuelFragment newInstance(String param1, String param2) {
        FuelFragment fragment = new FuelFragment();
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

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

        ImageButton imgConfirmRange = rootView.findViewById(R.id.img_btn_confirm_range);
        final SeekBar sbRange = rootView.findViewById((R.id.sb_range));

        imgConfirmRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callMaintenance(latitude,longitude,(float)sbRange.getProgress());
                //Log.e("", "onClick: " +  sbRange.getProgress());
            }
        });

        FloatingActionButton fabBroadcast = rootView.findViewById(R.id.fab_broadcast);

        fabBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogOrder = new Dialog(getActivity());

                final LayoutInflater inflater2 = LayoutInflater.from(getActivity().getApplicationContext());

                final android.view.View dialogView2 = inflater2.inflate(R.layout.dialog_broadcast, null);

                final EditText edtName = dialogView2.findViewById(R.id.edt_name);
                final EditText edtPhone = dialogView2.findViewById(R.id.edt_phone);
                final Spinner spnReason = dialogView2.findViewById(R.id.spn_reason);
                final EditText edtReason = dialogView2.findViewById(R.id.edt_reason);
                final EditText edtNote = dialogView2.findViewById(R.id.edt_note);

                Button btnConfirm = dialogView2.findViewById(R.id.btn_broadcast);

                if(first) {
                    first = false;
                    arrReason.add(getString(R.string.select_reason_spinner));
                    arrReason.add("Vehicle broke down");
                    arrReason.add("Tires are flat");
                    arrReason.add(getString(R.string.other));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        R.layout.spinner_item_broadcast, arrReason);

                spnReason.setAdapter(adapter);

                ImageButton imgAdd = dialogView2.findViewById(R.id.img_add);
                ImageButton imgRemove = dialogView2.findViewById(R.id.img_add);
                LinearLayout layoutOther = dialogView2.findViewById(R.id.layout_other_reason);

                spnReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(spnReason.getSelectedItem().toString().equals(getString(R.string.other))) {
                            isOther = true;
                            layoutOther.setVisibility(View.VISIBLE);
                        }
                        else{
                            isOther = false;
                            layoutOther.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOther){
                            if(edtReason.getText().toString().equals("")){
                                Toast toast = Toast.makeText(getActivity(), R.string.please_select_reason, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }else{
                                sendBroadcast(dialogOrder, edtReason.getText().toString(), edtName.getText().toString(),
                                        edtPhone.getText().toString(),edtNote.getText().toString(), latitude, longitude);
                            }
                        }else {
                            if(spnReason.getSelectedItemPosition() == 0){
                                Toast toast = Toast.makeText(getActivity(), R.string.please_select_reason, Toast.LENGTH_LONG);
                                TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                tv.setTextColor(Color.RED);

                                toast.show();
                            }else {
                                sendBroadcast(dialogOrder, spnReason.getSelectedItem().toString(), edtName.getText().toString(),
                                        edtPhone.getText().toString(), edtNote.getText().toString(), latitude, longitude);
                            }
                        }
                    }
                });

                Button btnCancel = dialogView2.findViewById(R.id.btn_cancel);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogOrder.cancel();
                    }
                });

                dialogOrder.setContentView(dialogView2);

                dialogOrder.show();
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
        if (context instanceof FuelFragment.OnFragmentInteractionListener) {
            mListener = (FuelFragment.OnFragmentInteractionListener) context;


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);
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

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        //ArrayList<MarkerOptions> markList = new ArrayList<MarkerOptions>();

        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager
                    .NETWORK_PROVIDER);
            if (location == null) {
                //Log.e("", "onMapReady: MULL");
            }
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            //Log.e("", "onMapReady: " + latitude + " , " + longitude);
        }

        callMaintenance(latitude, longitude, 1);

        MarkerOptions curPositionMark = new MarkerOptions();
        curPositionMark.position(new LatLng(latitude, longitude));
        curPositionMark.title(getString(R.string.you_r_here));

        currentPosition = mMap.addMarker(curPositionMark);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        if(!marker.equals(currentPosition)) {

            MaintenanceObject temp = new MaintenanceObject(-1,"",0,0,
                    "","");

            for (int i = 0; i<mMarkers.size(); i++){
                if(mMarkers.get(i).equals(marker)){
                    temp = items.get(i);
                    break;
                }
            }

            final MaintenanceObject maintenanceObject = temp;

            final LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());

            final android.view.View dialogView = inflater.inflate(R.layout.dialog_point_info, null);

            ListView lv = dialogView.findViewById(R.id.lv_review);

            ArrayList<Review> items = new ArrayList<Review>();

            items.add(new Review("nhut", "i donek know kaahfeeefffffffeijkla jkl ja klj akljfklajj kajkljw klj lkaj eklwaj elkjwa kljela ej l", (float)2.5));

            final ReviewAdapter adapter = new ReviewAdapter(getActivity(), R.layout.review_item, items);

            lv.setAdapter(adapter);

            //TextView tvName = dialogView.findViewById(R.id.tv_name_of_place);
            TextView tvAddress = dialogView.findViewById(R.id.tv_address);
            TextView tvNote = dialogView.findViewById(R.id.tv_note);

            //tvName.setText(maintenanceObject.getName());
            tvAddress.setText(maintenanceObject.getAddress());
            tvNote.setText(maintenanceObject.getNote());

            if(!maintenanceObject.getNote().equals("")){
                tvNote.setVisibility(View.VISIBLE);
            }

            //leave review
            final Button btnLeaveComment = dialogView.findViewById(R.id.btn_leave_comment);

            if(!((MyApplication)getActivity().getApplication()).getToken().equals("")){
                btnLeaveComment.setVisibility(View.VISIBLE);
            }

            btnLeaveComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //dialog.cancel();

                    Dialog dialogComment = new Dialog(getActivity());

                    final LayoutInflater inflater2 = LayoutInflater.from(getActivity().getApplicationContext());

                    final android.view.View dialogView2 = inflater2.inflate(R.layout.dialog_review, null);

                    EditText edtComment = dialogView2.findViewById(R.id.edt_comment);
                    RatingBar rtReview = dialogView2.findViewById(R.id.rating_review);

                    Button confirmReview = dialogView2.findViewById(R.id.btn_confrim_review);

                    confirmReview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    dialogComment.setContentView(dialogView2);

                    dialogComment.show();
                }
            });

            //end leave review
            //order
            final Button btnOrder = dialogView.findViewById(R.id.btn_order);
            btnOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialogOrder = new Dialog(getActivity());

                    final LayoutInflater inflater2 = LayoutInflater.from(getActivity().getApplicationContext());

                    final android.view.View dialogView2 = inflater2.inflate(R.layout.dialog_order, null);

                    final EditText edtName = dialogView2.findViewById(R.id.edt_name);
                    final EditText edtPhone = dialogView2.findViewById(R.id.edt_phone);
                    final EditText edtAddress = dialogView2.findViewById(R.id.edt_address);
                    final EditText edtNote = dialogView2.findViewById(R.id.edt_note);
                    final EditText edtTime = dialogView2.findViewById(R.id.edt_time);

                    Button btnConfirm = dialogView2.findViewById(R.id.btn_order);

                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int[] id = new int[1];
                            id[0] = maintenanceObject.getId();
                            order(dialogOrder, edtName.getText().toString(), edtAddress.getText().toString(),
                                    edtPhone.getText().toString(), edtNote.getText().toString(), edtTime.getText().toString(),
                                    id);
                        }
                    });

                    Button btnCancel = dialogView2.findViewById(R.id.btn_cancel);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogOrder.cancel();
                        }
                    });

                    dialogOrder.setContentView(dialogView2);

                    dialogOrder.show();
                }
            });

            //end order


            //Log.e("", "onMarkerClick: mapclick");
            marker.showInfoWindow();

            final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), android.R.style.Theme_Black_NoTitleBar);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
            dialog.setContentView(dialogView);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            //go
            Button btnGo = dialogView.findViewById(R.id.btn_go);

            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String serverKey = "AIzaSyB56CeF7ccQ9ZeMn0O4QkwlAQVX7K97-Ss";
                    LatLng origin = new LatLng(latitude, longitude);
                    LatLng destination = new LatLng(maintenanceObject.getLat(), maintenanceObject.getLon());
                    GoogleDirection.withServerKey(serverKey)
                            .from(origin)
                            .to(destination)

                            .execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction) {

                                    String status = direction.getStatus();
                                    //Log.e("", "onDirectionSuccess: " + status);
                                    if(status.equals(RequestResult.OK)) {
                                        if(polyline != null){
                                            polyline.remove();
                                        }
                                        Route route = direction.getRouteList().get(0);
                                        Leg leg = route.getLegList().get(0);
                                        ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                        PolylineOptions polylineOptions = DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED);
                                        polyline = mMap.addPolyline(polylineOptions);
                                        dialog.cancel();

                                    } else if(status.equals(RequestResult.NOT_FOUND)) {
                                        Toast toast = Toast.makeText(getActivity(), "Can't go to destination from here", Toast.LENGTH_LONG);
                                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                        tv.setTextColor(Color.RED);

                                        toast.show();
                                    }
                                }

                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    //Log.e("", "onDirectionFailure: ");
                                    Toast toast = Toast.makeText(getActivity(), "Something went wrong when trying to find direction", Toast.LENGTH_LONG);
                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                    tv.setTextColor(Color.RED);

                                    toast.show();
                                }
                            });
                }
            });
            //end go

            dialog.show();


        }

        marker.showInfoWindow();

        return true;
    }

    public void addMaintenanceMarkerToList(float lat, float lon, String name){
        LatLng pos = new LatLng(lat,lon);
        MarkerOptions option = new MarkerOptions();
        option.title(name);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_maintenance));
        option.position(pos);

        Marker marker = mMap.addMarker(option);

        mMarkers.add(marker);

    }

    public void callMaintenance(double lat, double lon, float range){
        mMap.clear();
        mMarkers.removeAll(mMarkers);
        items.removeAll(items);
        Retrofit retro = new Retrofit.Builder().baseUrl("http://34.87.144.190/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange("1.0.0",(float)lat,(float)lon,(float)range + 1);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());
                        jsonArray = jsonObject.getJSONArray("Maintenances");

                        for (int i = 0; i< jsonArray.length();i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            //Log.e("", "onResponse: " + jsonObject1.toString());
                            addMaintenanceMarkerToList((float)jsonObject1.getDouble("Lat"),
                                    (float)jsonObject1.getDouble("Lon"),jsonObject1.getString("Name"));

                            MaintenanceObject maintenanceObject = new MaintenanceObject(jsonObject1.getInt("Id"),
                                    jsonObject1.getString("Name"), (float)jsonObject1.getDouble("Lat"),
                                    (float)jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"),
                                    jsonObject1.getString("Address"));

                            items.add(maintenanceObject);
                        }

                        MarkerOptions curPositionMark = new MarkerOptions();
                        curPositionMark.position(new LatLng(latitude,longitude));
                        curPositionMark.title(getString(R.string.you_r_here));

                        currentPosition = mMap.addMarker(curPositionMark);

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        //Log.e(", ",response.errorBody().toString() + response.code());
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

    public void sendBroadcast(final Dialog dialog, final String reason, final String name, final String phone, final String note, double lat, double lon){
        Retrofit retro = new Retrofit.Builder().baseUrl("http://34.87.144.190/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
        Call<ResponseBody> call = tour.getMaintenanceInRange("1.0.0",(float)lat,(float)lon,(float)15);
        //Call<ResponseBody> call = tour.getAllATM();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200) {
                    final JSONObject jsonObject;
                    final JSONArray jsonArray;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.e("", "onResponse: " + jsonObject.toString());
                        jsonArray = jsonObject.getJSONArray("Maintenances");

                        final ArrayList<Integer> idList = new ArrayList<>();

                        for (int i = 0; i< jsonArray.length();i++){
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            idList.add(jsonObject1.getInt("Id"));
                        }

                        int[] id = new int[idList.size()];

                        for(int i =0; i< idList.size();i++){
                            id[i] = idList.get(i);
                        }

//                        Call<ResponseBody> call2 = tour.broadcast("1.0.0", reason, name, phone, note, id, "", "");
//                        call2.enqueue(new Callback<ResponseBody>() {
//                            @Override
//                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                JSONObject jsonObject;
//                                if(response.code() == 200) {
//                                    //Log.e("", "onResponse: " + response.raw().request());
//                                    try{
//                                        jsonObject = new JSONObject(response.body().string());
//                                        //Log.e("", "onResponse: " + jsonObject.toString());
//                                        if (jsonObject.getBoolean("Status")){
//                                            dialog.cancel();
//
//                                            Toast toast = Toast.makeText(getActivity(), "Contacted " + idList.size() + " nearest stores", Toast.LENGTH_LONG);
//                                            toast.show();
//                                        }
//                                    }catch (Exception e){
//                                        e.printStackTrace();
//                                    }
//                                }
//                                else{
//                                    try{ ;
//                                        //Log.e("", "onResponse: " + response.code());
//                                    }catch (Exception e){
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                                //Log.e("", "onFailure: " + t.toString());
//                            }
//                        });


                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        //Log.e(", ",response.errorBody().toString() + response.code());
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

    public void order(final Dialog dialog, String name, String address, String phone, String note, String time, int[] id){
        Retrofit retro = new Retrofit.Builder().baseUrl("http://34.87.144.190/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        final MapAPI tour = retro.create(MapAPI.class);
//        Call<ResponseBody> call = tour.broadcast("1.0.0", "", name, phone, note, id, address, time);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                JSONObject jsonObject;
//                if(response.code() == 200) {
//                    //Log.e("", "onResponse: " + response.raw().request());
//                    try{
//                        jsonObject = new JSONObject(response.body().string());
//                        //Log.e("", "onResponse: " + jsonObject.toString());
//                        if (jsonObject.getBoolean("Status")){
//                            dialog.cancel();
//
//                            Toast toast = Toast.makeText(getActivity(), "Ordered", Toast.LENGTH_LONG);
//                            toast.show();
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//                else{
//                    try{
//                        //Log.e("", "onResponse: " + response.code());
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                //Log.e("", "onFailure: " + t.toString());
//            }
//        });
    }
}