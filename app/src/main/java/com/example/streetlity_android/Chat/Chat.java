package com.example.streetlity_android.Chat;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    ArrayList<ChatObject> items = new ArrayList<>();

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
        room = s.getString("room", "noroom");


        Log.e("", "onCreate: " + room );
        MaintenanceOrder socket = new MaintenanceOrder();
        socket.create(room);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
