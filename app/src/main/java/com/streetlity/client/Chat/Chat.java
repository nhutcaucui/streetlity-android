package com.streetlity.client.Chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.streetlity.client.MyApplication;
import com.streetlity.client.R;
import com.streetlity.client.RealtimeService.DeclineListener;
import com.streetlity.client.RealtimeService.Information;
import com.streetlity.client.RealtimeService.InformationListener;
import com.streetlity.client.RealtimeService.Listener;
import com.streetlity.client.RealtimeService.LocationListener;
import com.streetlity.client.RealtimeService.MaintenanceOrder;
import com.streetlity.client.RealtimeService.MessageListener;
import com.streetlity.client.User.UserInfoOther;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.view.View.GONE;

public class Chat extends AppCompatActivity implements android.location.LocationListener {

    ArrayList<ChatObject> items = new ArrayList<>();

    Information infomation;

    double lat;
    double lon;

    String room = "";
    ListView lv;
    ChatObjectAdapter adapter;

    MaintenanceOrder socket;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        lv= findViewById(R.id.lv);
        adapter = new ChatObjectAdapter(this, R.layout.lv_item_chat, items);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!items.get(position).getName().equals(MyApplication.getInstance().getUsername())) {
                    Intent t = new Intent(Chat.this, UserInfoOther.class);
                    t.putExtra("user", items.get(position).getName());
                    startActivity(t);
                }
            }
        });

        SharedPreferences s = getSharedPreferences("Room", MODE_PRIVATE);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("id")) {
                s.edit().putString("room", getIntent().getStringExtra("id")).apply();
            }
        }
        if (getIntent().getStringExtra("id") != null) {
            Log.e("", "onCreate: " + getIntent().getStringExtra("id"));
            room = getIntent().getStringExtra("id");
            s.edit().putString("room", getIntent().getStringExtra("id")).apply();
        } else {
            room = s.getString("room", "noroom");
        }

        String phone = "";
        if (getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        if (phone.equals("")) {
            phone = MyApplication.getInstance().getPhone();
        }

        final String fPhone = phone;

        Log.e("", "onCreate: " + room);

        MaintenanceOrder.Create(room);

        socket = MaintenanceOrder.getInstance();

        TextInputEditText edtMessage = findViewById(R.id.edt_body);

        ImageButton imgBtnSend = findViewById(R.id.btn_send);
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtMessage.getText().toString().equals("")) {

                    Log.e("", "onClick: " + edtMessage.getText().toString());
                    ChatObject object = new ChatObject(MyApplication.getInstance().getUsername(), edtMessage.getText().toString(), new Date());
                    socket.sendMessage(object);
                    items.add(object);
                    adapter.notifyDataSetChanged();
                    lv.setSelection(adapter.getCount() - 1);
                    edtMessage.setText("");

                }
            }
        });

        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infomation = info;
                        Log.e("", "onReceived: " + info.toString());
                    }
                });

            }
        };

        socket.CompleteListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                Log.e("TAG", "trigger: order is com le te");
                findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences("broadcastPhone", MODE_PRIVATE).edit()
                        .clear().apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.layout_complete).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.DeclineListener = new DeclineListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, String reason) {
                Log.e("TAG", "trigger: order is cancelededed");
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences("broadcastPhone", MODE_PRIVATE).edit()
                        .clear().apply();
                findViewById(R.id.btn_finish_denu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.tv_deny_reason)).setText(reason);
                        findViewById(R.id.layout_denied).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.MessageListener = new MessageListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, ChatObject message) {
                Log.e("", "onReceived:  this is america");
                new Task().execute(message);
            }
        };

        socket.JoinedListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout layoutLoading = findViewById(R.id.layout_loading_top);
                        layoutLoading.setVisibility(GONE);
                        sender.pullChat();

                        Log.e("", "run: joined");

                        Information myInfo = new Information(MyApplication.getInstance().getUsername(), fPhone);

                        sender.sendInformation(myInfo);
                        sender.pullInformation();

                        new CountDownTimer(5000,1000){
                            public void onTick(long millisUntilFinished) {
                                //here you can have your logic to set text to edittext
                            }

                            public void onFinish() {
                                sortChat();
                            }

                        }.start();
                    }
                });
            }
        };

        socket.LocationListener = new LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                Log.e("TAG", "onReceived: received location" + lat + " " +lon);
            }
        };



        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(edtMessage.getText().toString().equals("")){
//                    maintenanceOrder.sendTyped(MyApplication.getInstance().getUsername());
//                }else {
//                    maintenanceOrder.sendTyping(MyApplication.getInstance().getUsername());
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        maintenanceOrder.TypingListener = new TypeMessageListener<MaintenanceOrder>(){
//            @Override
//            public void trigger(MaintenanceOrder sender, String typeUser){
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(findViewById(R.id.tv_chat_username) != null){
//                            ((TextView) findViewById(R.id.tv_chat_username)).setText(typeUser);
//                            findViewById(R.id.layout_is_typing).setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//            }
//
//        };

//        maintenanceOrder.TypedListener = new TypeMessageListener<MaintenanceOrder>() {
//            @Override
//            public void trigger(MaintenanceOrder sender, String triggeringUser) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        findViewById(R.id.layout_is_typing).setVisibility(View.GONE);
//                    }
//                });
//            }
//        };

        socket.close();
        socket.join();

        if (ContextCompat.checkSelfPermission(Chat.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(Chat.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager)
                    Chat.this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                socket.updateLocation(location.getLatitude(),location.getLongitude(),location.getBearing());
                socket.pullLocation(location);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();

        return true;
    }

//    public void saveToSharedPref() {
//        Gson gson = new Gson();
//        String jsonText = gson.toJson(items);
//        getSharedPreferences("chatLog", MODE_PRIVATE).edit().putString(room, jsonText).apply();
//    }
//
//    public void getSharedPref() {
//        Gson gson = new Gson();
//        String jsonText = getSharedPreferences("chatLog", MODE_PRIVATE).getString(room, null);
//        if (jsonText != null) {
//            Type type = new TypeToken<ArrayList<ChatObject>>() {
//            }.getType();
//            items = gson.fromJson(jsonText, type);
//        }
//    }

    public void clearSharedPrefs() {
        getSharedPreferences("chatLog", MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(socket != null){
            Log.e("TAG", "trigger: update location in chat");
            socket.updateLocation(location.getLatitude(),location.getLongitude(),location.getBearing());
            socket.pullLocation(location);
        }
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


    class Task extends AsyncTask<ChatObject, Void, ArrayList<ChatObject>> {
        private Exception exception;

        @Override
        protected ArrayList<ChatObject> doInBackground(ChatObject... message) {
            try {
                items.add(message[0]);
                findViewById(R.id.layout_is_typing).setVisibility(View.GONE);
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ChatObject> a) {
            //add the tours from internet to the array
            adapter.notifyDataSetChanged();
            lv.setSelection(adapter.getCount()-1);
        }
    }

    /**
     * sort the chat when load
     */
    public void sortChat(){
        Collections.sort(items, new Comparator<ChatObject>() {
            @Override
            public int compare(ChatObject o1, ChatObject o2) {
                Log.e("", "compare: "+o1.getTime() +"-"+ o2.getTime());
                return Long.compare(o1.getTime().getTime(), o2.getTime().getTime());
            }
        });
        adapter.notifyDataSetChanged();
        lv.setSelection(adapter.getCount()-1);
    }

    public void onDestroy(){
        super.onDestroy();

        locationManager.removeUpdates(this);
    }
}

