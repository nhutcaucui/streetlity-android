package com.example.streetlity_android.Chat;

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
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class Chat extends AppCompatActivity {

    ArrayList<ChatObject> items = new ArrayList<>();

    Information infomation;

    double lat;
    double lon;

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

        ImageButton btnChat = findViewById(R.id.btn_send);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        String room = "";

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
        MaintenanceOrder socket = new MaintenanceOrder(room);
        socket.join();
        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                infomation = info;
            }
        };
        Information myInfo = new Information(MyApplication.getInstance().getUsername(), phone);
        socket.sendInformation(myInfo);
        socket.MessageListener = new MessageListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, String message) {
                ChatObject object = new ChatObject(infomation.Username, message, new Date());
                items.add(object);
                adapter.notifyDataSetChanged();
            }
        };
        socket.LocationListener = new LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                lat = lat;
                lon = lon;
            }
        };
        socket.Decline = new Listener<MaintenanceOrder>() {
            @Override
            public void call(MaintenanceOrder sender) {

            }
        };
        socket.Complete = new Listener<MaintenanceOrder>() {
            @Override
            public void call(MaintenanceOrder sender) {
                Log.e("", "call: completed" );
            }
        };

        TextInputEditText edtMessage = findViewById(R.id.edt_body);

        ImageButton imgBtnSend = findViewById(R.id.btn_send);
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtMessage.getText().toString().equals("")) {
                    socket.sendMessage(edtMessage.getText().toString());
                    Log.e("", "onClick: " + edtMessage.getText().toString() );
                    ChatObject object = new ChatObject(MyApplication.getInstance().getUsername(), edtMessage.getText().toString(), new Date());
                    items.add(object);
                    adapter.notifyDataSetChanged();
                    edtMessage.setText("");

                }
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
