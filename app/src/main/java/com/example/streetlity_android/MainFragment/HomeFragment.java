package com.example.streetlity_android.MainFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.streetlity_android.AllServiceMap;
import com.example.streetlity_android.BroadcastActivity;
import com.example.streetlity_android.BroadcastEmergencyActivity;
import com.example.streetlity_android.MainNavigationHolder;
import com.example.streetlity_android.MaintainerDirection;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.User.Login;
import com.example.streetlity_android.User.SignUp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Color.RED;
import static android.view.View.GONE;

public class HomeFragment extends Fragment implements LocationListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final long MIN_TIME = 1;
    private static final float MIN_DISTANCE= 0.1f;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    LocationManager locationManager;

    public HomeFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

//        if(MyApplication.getInstance().getToken().equals("")){
//            LinearLayout btnBroadcast = rootView.findViewById(R.id.btn_broadcast);
//            btnBroadcast.setVisibility(GONE);
//            LinearLayout btnBroadcastE = rootView.findViewById(R.id.btn_emergency);
//            btnBroadcastE.setVisibility(GONE);
//        }

        if(!getActivity().getSharedPreferences("first",MODE_PRIVATE).getBoolean("firstHome", false)){
            getActivity().getSharedPreferences("first",MODE_PRIVATE).edit().putBoolean("firstHome", true).apply();

            final Dialog dialog = new Dialog(getActivity());

            final View dialogView = View.inflate(getActivity() ,R.layout.dialog_instruction_home, null);

            Button btnUnderstand = dialogView.findViewById(R.id.btn_understand);

            btnUnderstand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

            dialog.setContentView(dialogView);

            dialog.show();
        }

        LinearLayout btnFuel = rootView.findViewById(R.id.btn_fuel);
        LinearLayout btnWC = rootView.findViewById(R.id.btn_wc);
        LinearLayout btnATM = rootView.findViewById(R.id.btn_atm);
        LinearLayout btnMaintenance = rootView.findViewById(R.id.btn_maintenance);
        LinearLayout btnBroadcast = rootView.findViewById(R.id.btn_broadcast);
        LinearLayout btnAll = rootView.findViewById(R.id.btn_all);
        LinearLayout btnEmergency = rootView.findViewById(R.id.btn_emergency);

        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.getInstance().getToken().equals("")) {
                    final Dialog dialogDecline = new Dialog(getActivity());

                    final LayoutInflater inflater = LayoutInflater.from(getActivity());

                    final View dialogView = View.inflate(getActivity() ,R.layout.dialog_need_login, null);

                    Button btnLogin = dialogView.findViewById(R.id.btn_dialog_to_login);

                    Button btnSignUp = dialogView.findViewById(R.id.btn_dialog_to_signup);

                    btnLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().startActivityForResult(new Intent(getActivity(), Login.class),1);
                            dialogDecline.dismiss();
                        }
                    });

                    btnSignUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent t = new Intent(getActivity(), SignUp.class);
                            t.putExtra("from", 1);
                            getActivity().startActivityForResult(t, 1);
                            dialogDecline.dismiss();
                        }
                    });

                    dialogDecline.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

                    dialogDecline.setContentView(dialogView);

                    dialogDecline.show();
                } else {
                    boolean activeOrder = getActivity().
                            getSharedPreferences("activeOrder", MODE_PRIVATE).contains("activeOrder");
                    if (((MainNavigationHolder) getActivity()).isCanBroadcast() && !activeOrder) {
                        getActivity().startActivityForResult(new Intent(getActivity(), BroadcastEmergencyActivity.class), 5);
                    } else {
                        if (activeOrder) {
                            Toast toast = Toast.makeText(getActivity(), R.string.cant_broadcast_while_active, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(RED);

                            toast.show();
                        } else {
                            String builder = getString(R.string.retry_later) + " " + ((MainNavigationHolder) getActivity()).getTimeLeft()
                                    + " " + getString(R.string.seconds);
                            Toast toast = Toast.makeText(getActivity(), builder, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(RED);

                            toast.show();
                        }

                    }
                }
            }
        });

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AllServiceMap.class));
            }
        });

        btnFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().findViewById(R.id.btn_fuel_bottom).callOnClick();
            }
        });

        btnATM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().findViewById(R.id.btn_atm_bottom).callOnClick();
            }
        });

        btnMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().findViewById(R.id.btn_maintenance_bottom).callOnClick();
            }
        });

        btnWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().findViewById(R.id.btn_wc_bottom).callOnClick();
            }
        });

        btnBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.getInstance().getToken().equals("")) {
                    final Dialog dialog = new Dialog(getActivity());

                    final LayoutInflater inflater = LayoutInflater.from(getActivity());

                    final View dialogView = View.inflate(getActivity() ,R.layout.dialog_need_login, null);

                    Button btnLogin = dialogView.findViewById(R.id.btn_dialog_to_login);

                    Button btnSignUp = dialogView.findViewById(R.id.btn_dialog_to_signup);

                    btnLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().startActivityForResult(new Intent(getActivity(), Login.class),1);
                            dialog.dismiss();
                        }
                    });

                    btnSignUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent t = new Intent(getActivity(), SignUp.class);
                            t.putExtra("from", 1);
                            getActivity().startActivityForResult(t, 1);
                            dialog.dismiss();
                        }
                    });

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

                    dialog.setContentView(dialogView);

                    dialog.show();
                } else {
                    boolean activeOrder = getActivity().
                            getSharedPreferences("activeOrder", MODE_PRIVATE).contains("activeOrder");
                    if (((MainNavigationHolder) getActivity()).isCanBroadcast() && !activeOrder) {
                        getActivity().startActivityForResult(new Intent(getActivity(), BroadcastActivity.class), 5);
                    } else {
                        if (activeOrder) {
                            Toast toast = Toast.makeText(getActivity(), R.string.cant_broadcast_while_active, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(RED);

                            toast.show();
                        } else {
                            String builder = getString(R.string.retry_later) + " " + ((MainNavigationHolder) getActivity()).getTimeLeft()
                                    + " " + getString(R.string.seconds);
                            Toast toast = Toast.makeText(getActivity(), builder, Toast.LENGTH_LONG);
                            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                            tv.setTextColor(RED);

                            toast.show();
                        }
                    }
                }
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
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1);
                            paramDialogInterface.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(locationManager
                        .GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if (location == null) {
                    ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                    Log.e("", "onMapReady: MULL");
                }
            }

        }

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

            if(((MainNavigationHolder)getActivity()).getLoading()!= null) {
                ((MainNavigationHolder) getActivity()).getLoading().setVisibility(View.GONE);
            }
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

    public void onLocationChanged(Location location) {
        if(getActivity()!= null) {
            ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.GONE);
        }
        Log.e("", "onLocationChanged: home" );
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

                        ((MainNavigationHolder) getActivity()).getCantFind().setVisibility(View.VISIBLE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                        Log.e("", "onMapReady: MULL");
                    }
                }

            }
        }
    }
}