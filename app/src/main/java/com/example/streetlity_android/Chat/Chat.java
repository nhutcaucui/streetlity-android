package com.example.streetlity_android.Chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.RealtimeService.Information;
import com.example.streetlity_android.RealtimeService.InformationListener;
import com.example.streetlity_android.RealtimeService.Listener;
import com.example.streetlity_android.RealtimeService.LocationListener;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.example.streetlity_android.RealtimeService.MessageListener;
import com.example.streetlity_android.User.UserInfo;
import com.example.streetlity_android.User.UserInfoOther;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import static android.view.View.GONE;

public class Chat extends AppCompatActivity {

    ArrayList<ChatObject> items = new ArrayList<>();

    Information infomation;

    double lat;
    double lon;

    String room = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ListView lv = findViewById(R.id.lv);
        ChatObjectAdapter adapter = new ChatObjectAdapter(this, R.layout.lv_item_chat, items);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!items.get(position).getName().equals(MyApplication.getInstance().getUsername())) {
                    Intent t = new Intent(Chat.this, UserInfoOther.class);
                    t.putExtra("user", items.get(position).getName());
                    startActivity(t);
                }
            }
        });

        SharedPreferences s = getSharedPreferences("Room", MODE_PRIVATE);
        if (getIntent().getExtras() != null) {
            if (!getIntent().getExtras().containsKey("id")) {
                s.edit().putString("room", getIntent().getStringExtra("id")).apply();
            }
        }
        if(getIntent().getStringExtra("id") != null) {
            Log.e("", "onCreate: " + getIntent().getStringExtra("id"));
            room = getIntent().getStringExtra("id");
        }else {
            room = s.getString("room", "noroom");
        }
        s.edit().clear().apply();
        String phone="";
        if(getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        if(phone.equals("")){
            phone = MyApplication.getInstance().getPhone();
        }

        Log.e("", "onCreate: " + room );

        MaintenanceOrder socket = MaintenanceOrder.Create("himom");
        socket.close();


        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infomation = info;
                        Log.e("", "onReceived: " + info.toString() );
                    }
                });

            }
        };
        Information myInfo = new Information(MyApplication.getInstance().getUsername(), phone);

        socket.sendInformation(myInfo);
        socket.pullInformation();

        socket.MessageListener = new MessageListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, ChatObject message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("", "onReceived:  this is america" );
                        items.add(message);
                        adapter.notifyDataSetChanged();
                        lv.setSelection(adapter.getCount()-1);
                    }
                });

            }
        };
        socket.LocationListener = new LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                lat = lat;
                lon = lon;
            }
        };

        socket.DeclineListener = new Listener<MaintenanceOrder>() {
            @Override
            public void call(MaintenanceOrder sender) {

            }
        };
        socket.CompleteListener = new Listener<MaintenanceOrder>() {
            @Override
            public void call(MaintenanceOrder sender) {
                Log.e("", "call: completed" );
            }
        };

        socket.JoinedListener = new Listener<MaintenanceOrder>() {
            @Override
            public void call(MaintenanceOrder sender) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout layoutLoading = findViewById(R.id.layout_loading_top);
                        layoutLoading.setVisibility(GONE);
                    }
                });
            }
        };

        socket.join();

        TextInputEditText edtMessage = findViewById(R.id.edt_body);

        ImageButton imgBtnSend = findViewById(R.id.btn_send);
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtMessage.getText().toString().equals("")) {

                    Log.e("", "onClick: " + edtMessage.getText().toString() );
                    ChatObject object = new ChatObject(MyApplication.getInstance().getUsername(), edtMessage.getText().toString(), new Date());
                    socket.sendMessage(object);
                    items.add(object);
                    adapter.notifyDataSetChanged();
                    lv.setSelection(adapter.getCount()-1);
                    edtMessage.setText("");

                }
            }
        });


    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public  void saveToSharedPref(){
        Gson gson = new Gson();
        String jsonText = gson.toJson(items);
        getSharedPreferences("chatLog",MODE_PRIVATE).edit().putString(room, jsonText).apply();
    }

    public void getSharedPref(){
        Gson gson = new Gson();
        String jsonText = getSharedPreferences("chatLog",MODE_PRIVATE).getString(room, null);
        if(jsonText!= null) {
            Type type = new TypeToken<ArrayList<ChatObject>>() {}.getType();
            items = gson.fromJson(jsonText, type);
        }
    }

    public void clearSharedPrefs(){
        getSharedPreferences("chatLog",MODE_PRIVATE).edit().clear().apply();
    }
}
