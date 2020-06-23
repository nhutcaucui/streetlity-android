package com.example.streetlity_android.MainFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.streetlity_android.BroadcastActivity;
import com.example.streetlity_android.MainNavigationHolder;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.User.Login;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.view.View.GONE;

public class HomeFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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

        if(MyApplication.getInstance().getToken().equals("")){
            LinearLayout btnBroadcast = rootView.findViewById(R.id.btn_broadcast);
            btnBroadcast.setVisibility(GONE);
        }

        LinearLayout btnFuel = rootView.findViewById(R.id.btn_fuel);
        LinearLayout btnWC = rootView.findViewById(R.id.btn_wc);
        LinearLayout btnATM = rootView.findViewById(R.id.btn_atm);
        LinearLayout btnMaintenance = rootView.findViewById(R.id.btn_maintenance);
        LinearLayout btnBroadcast = rootView.findViewById(R.id.btn_broadcast);

        btnFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainNavigationHolder) getActivity()).getNavigation().setSelectedItemId(R.id.navigation_fuel);
            }
        });

        btnATM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainNavigationHolder) getActivity()).getNavigation().setSelectedItemId(R.id.nav_atm);
            }
        });

        btnMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainNavigationHolder) getActivity()).getNavigation().setSelectedItemId(R.id.navigation_maintenance);
            }
        });

        btnWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainNavigationHolder) getActivity()).getNavigation().setSelectedItemId(R.id.navigation_wc);
            }
        });

        btnBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainNavigationHolder)getActivity()).isCanBroadcast()) {
                    getActivity().startActivityForResult(new Intent(getActivity(), BroadcastActivity.class), 5);
                }
                else{
                    Toast toast = Toast.makeText(getActivity(), R.string.retry_later, Toast.LENGTH_LONG);
                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);

                    toast.show();
                }
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
}