package com.example.streetlity_android.Achievement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.streetlity_android.MainFragment.MapObject;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MapsActivity;
import com.example.streetlity_android.MapsActivityConfirmation;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;

import org.json.JSONObject;

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
 * Use the {@link ReviewedOtherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewedOtherFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;

    AchievementObjectAdapter adapterReview;
    ListView lvReview;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReviewedOtherFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReviewdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReviewedOtherFragment newInstance(String param1, String param2) {
        ReviewedOtherFragment fragment = new ReviewedOtherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

            if(getActivity().findViewById(R.id.layout_loading)!= null){
                getActivity().findViewById(R.id.layout_loading).setVisibility(View.GONE);
            }
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        View rootView = inflater.inflate(R.layout.fragment_reviewed, container, false);

        adapterReview = new AchievementObjectAdapter(getActivity(), R.layout.lv_item_achievement, ((AchievementOther) getActivity()).getReviewedItems());
        lvReview = rootView.findViewById(R.id.list_view);

        Map<String, Map<String, ActionObject>> reviewedMap = ((AchievementOther)getActivity()).getReviewedMap();

        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getServiceURL())
                .addConverterFactory(GsonConverterFactory.create()).build();

        final float currLat = ((AchievementOther) getActivity()).getCurrLat();
        final float currLon = ((AchievementOther) getActivity()).getCurrLon();

        final MapAPI tour = retro.create(MapAPI.class);

        if(!((AchievementOther) getActivity()).isGotReviewItems()) {

            ((AchievementOther) getActivity()).setGotReviewItems(true);
            if (reviewedMap.containsKey("Fuel")) {
                Map<String, ActionObject> map = reviewedMap.get("Fuel");
                for (String key : map.keySet()) {
                    Call<ResponseBody> call = tour.getFuel(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                    Log.e("", "onResponse: " + map.get(key).getAffected());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.code() == 200) {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    Log.e("", "onResponse: " + jsonObject.toString());
                                    Log.e(TAG, "onResponse: 00" + jsonObject.toString());

                                    if (jsonObject.getBoolean("Status")) {
                                        String name = getString(R.string.fuel);
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                        if (!jsonObject1.getString("Name").equals("")) {
                                            name = jsonObject1.getString("Name");
                                        }
                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 1);
                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                        item.setImages(jsonObject1.getString("Images"));

                                        item.setDistance(distance);

                                        item.setContributor(jsonObject1.getString("Contributor"));

                                        item.setConfident(jsonObject1.getInt("Confident"));

                                        item.setDownvoted(false);
                                        item.setUpvoted(false);

                                        if (MyApplication.getInstance().getUpvoteMap().containsKey("Fuel")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Fuel");
                                            if (map.containsKey("upvote " + item.getId())) {
                                                item.setUpvoted(true);
                                            }

                                        }
                                        if (MyApplication.getInstance().getDownvoteMap().containsKey("Fuel")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Fuel");
                                            if (map.containsKey("downvote " + item.getId())) {
                                                item.setDownvoted(true);
                                            }
                                        }
                                        ((AchievementOther) getActivity()).getReviewedItems().add(item);
                                        adapterReview.notifyDataSetChanged();
                                    }


                                } else {
                                    Log.e(TAG, "onResponse: " + response.code());
                                }
                            } catch (Exception e) {
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

            if (reviewedMap.containsKey("Toilet")) {
                Map<String, ActionObject> map = reviewedMap.get("Toilet");
                for (String key : map.keySet()) {
                    Call<ResponseBody> call = tour.getWC(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.code() == 200) {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    Log.e("", "onResponse: " + jsonObject.toString());

                                    if (jsonObject.getBoolean("Status")) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                        String name = getString(R.string.wc);
                                        if (!jsonObject1.getString("Name").equals("")) {
                                            name = jsonObject1.getString("Name");
                                        }
                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), name, 0,
                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 2);
                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                        item.setImages(jsonObject1.getString("Images"));

                                        item.setDistance(distance);

                                        item.setContributor(jsonObject1.getString("Contributor"));

                                        item.setConfident(jsonObject1.getInt("Confident"));

                                        item.setDownvoted(false);
                                        item.setUpvoted(false);

                                        if (MyApplication.getInstance().getUpvoteMap().containsKey("Toilet")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Toilet");
                                            if (map.containsKey("upvote " + item.getId())) {
                                                item.setUpvoted(true);
                                            }

                                        }
                                        if (MyApplication.getInstance().getDownvoteMap().containsKey("Toilet")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Toilet");
                                            if (map.containsKey("downvote " + item.getId())) {
                                                item.setDownvoted(true);
                                            }
                                        }
                                        ((AchievementOther) getActivity()).getReviewedItems().add(item);
                                        adapterReview.notifyDataSetChanged();
                                    }
                                } else {
                                    Log.e(TAG, "onResponse: " + response.code());
                                }
                            } catch (Exception e) {
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

            if (reviewedMap.containsKey("Maintenance")) {
                Map<String, ActionObject> map = reviewedMap.get("Maintenance");
                for (String key : map.keySet()) {
                    Call<ResponseBody> call = tour.getMaintenance(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.code() == 200) {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    Log.e("", "onResponse: " + jsonObject.toString());

                                    if (jsonObject.getBoolean("Status")) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), jsonObject1.getString("Name"), 0,
                                                jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 3);

                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                        item.setImages(jsonObject1.getString("Images"));

                                        item.setDistance(distance);

                                        item.setContributor(jsonObject1.getString("Contributor"));

                                        item.setConfident(jsonObject1.getInt("Confident"));

                                        item.setDownvoted(false);
                                        item.setUpvoted(false);

                                        if (MyApplication.getInstance().getUpvoteMap().containsKey("Maintenance")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Maintenance");
                                            if (map.containsKey("upvote " + item.getId())) {
                                                item.setUpvoted(true);
                                            }

                                        }
                                        if (MyApplication.getInstance().getDownvoteMap().containsKey("Maintenance")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Maintenance");
                                            if (map.containsKey("downvote " + item.getId())) {
                                                item.setDownvoted(true);
                                            }
                                        }
                                        ((AchievementOther) getActivity()).getReviewedItems().add(item);
                                        adapterReview.notifyDataSetChanged();
                                    }
                                } else {
                                    Log.e(TAG, "onResponse: " + response.code());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
                adapterReview.notifyDataSetChanged();
            }

            if (reviewedMap.containsKey("Atm")) {
                Map<String, ActionObject> map = reviewedMap.get("Atm");

                String token = MyApplication.getInstance().getToken();

                Call<ResponseBody> call = tour.getBank(MyApplication.getInstance().getVersion(), token);

                for (String key : map.keySet()) {
                    Call<ResponseBody> call2 = tour.getAtm(MyApplication.getInstance().getVersion(), Integer.parseInt(map.get(key).getAffected()));
                    call2.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.code() == 200) {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    Log.e("", "onResponse: " + jsonObject.toString());

                                    if (jsonObject.getBoolean("Status")) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("Service");
                                        String bankName = "";
                                        for (int j = 0; j < ((AchievementOther)getActivity()).getArrBank().size(); j++) {
                                            if (jsonObject1.getInt("BankId") == ((AchievementOther)getActivity()).getArrBank().get(j).getId()) {
                                                bankName = ((AchievementOther)getActivity()).getArrBank().get(j).getName();
                                            }
                                        }
                                        MapObject item = new MapObject(jsonObject1.getInt("Id"), bankName, 0,
jsonObject1.getString("Address"), (float) jsonObject1.getDouble("Lat"),
                                                (float) jsonObject1.getDouble("Lon"), jsonObject1.getString("Note"), 4);

                                        float distance = distance(item.getLat(), item.getLon(), currLat, currLon);

                                        item.setImages(jsonObject1.getString("Images"));

                                        item.setDistance(distance);

                                        item.setContributor(jsonObject1.getString("Contributor"));

                                        item.setConfident(jsonObject1.getInt("Confident"));

                                        item.setDownvoted(false);
                                        item.setUpvoted(false);

                                        if (MyApplication.getInstance().getUpvoteMap().containsKey("Atm")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getUpvoteMap().get("Atm");
                                            if (map.containsKey("upvote " + item.getId())) {
                                                item.setUpvoted(true);
                                            }

                                        }
                                        if (MyApplication.getInstance().getDownvoteMap().containsKey("Atm")) {
                                            Map<String, ActionObject> map = MyApplication.getInstance().getDownvoteMap().get("Atm");
                                            if (map.containsKey("downvote " + item.getId())) {
                                                item.setDownvoted(true);
                                            }
                                        }
                                        ((AchievementOther) getActivity()).getReviewedItems().add(item);

                                        adapterReview.notifyDataSetChanged();
                                    }
                                } else {
                                    Log.e(TAG, "onResponse: " + response.code());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
                adapterReview.notifyDataSetChanged();
            }
        }

        int count = 0;
        if (reviewedMap.containsKey("Atm")) {
            count += ((Map<String, ActionObject>) reviewedMap.get("Atm")).size();
        }
        if (reviewedMap.containsKey("Fuel")) {
            count += ((Map<String, ActionObject>) reviewedMap.get("Fuel")).size();
        }
        if (reviewedMap.containsKey("Maintenance")) {
            count += ((Map<String, ActionObject>) reviewedMap.get("Maintenance")).size();
        }
        if (reviewedMap.containsKey("Toilet")) {
            count += ((Map<String, ActionObject>) reviewedMap.get("Atm")).size();
        }

        TextView tvNumOfContribute = rootView.findViewById(R.id.tv_contributed);
        tvNumOfContribute.setText(Integer.toString(count));


        lvReview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MapObject item = (MapObject) adapterReview.getItem(position);
                if(item.getConfident()<=5){

                    Intent t = new Intent(getActivity(), MapsActivityConfirmation.class);
                    t.putExtra("currLat", ((AchievementOther) getActivity()).getCurrLat());
                    t.putExtra("currLon", ((AchievementOther) getActivity()).getCurrLon());

                    t.putExtra("item", item);
                    t.putExtra("index", position);

                    startActivityForResult(t,1);

                }else {
                    Intent t = new Intent(getActivity(), MapsActivity.class);
                    t.putExtra("currLat", ((AchievementOther) getActivity()).getCurrLat());
                    t.putExtra("currLon", ((AchievementOther) getActivity()).getCurrLon());


                    t.putExtra("item", item);

                    startActivity(t);
                }
            }
        });

        lvReview.setAdapter(adapterReview);

        return rootView;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
                if(data.getIntExtra("action", -1 )== 1) {
                    if (data.getIntExtra("index", -1) != -1) {
                        ((AchievementOther)getActivity()).getReviewedItems().get((data.getIntExtra("index", -1))).setUpvoted(true);
                        adapterReview.notifyDataSetChanged();

                        Toast toast = Toast.makeText(getActivity(), R.string.vote_success, Toast.LENGTH_LONG);

                        toast.show();
                    }
                }
                if(data.getIntExtra("action", -1 )== 2) {
                    if (data.getIntExtra("index", -1) != -1) {
                        ((AchievementOther)getActivity()).getReviewedItems().get((data.getIntExtra("index", -1))).setDownvoted(true);
                        adapterReview.notifyDataSetChanged();

                        Toast toast = Toast.makeText(getActivity(), R.string.vote_success, Toast.LENGTH_LONG);

                        toast.show();

                    }
                }
                if(data.getIntExtra("action", -1 )== 3) {
                    if (data.getIntExtra("index", -1) != -1) {
                        ((AchievementOther)getActivity()).getReviewedItems().get((data.getIntExtra("index", -1))).setUpvoted(false);
                        ((AchievementOther)getActivity()).getReviewedItems().get((data.getIntExtra("index", -1))).setDownvoted(false);
                        adapterReview.notifyDataSetChanged();

                        Toast toast = Toast.makeText(getActivity(), R.string.clear_vote, Toast.LENGTH_LONG);

                        toast.show();

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();}
    }
}