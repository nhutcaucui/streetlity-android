package com.streetlity.client.Notification;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.streetlity.client.R;

import java.util.ArrayList;
@Deprecated
public class Notification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ListView lv = findViewById(R.id.lv);

        ArrayList<NotifyObject> items = new ArrayList<>();

        NotifyObjectAdapter adapter = new NotifyObjectAdapter(this, R.layout.lv_item_notification, items);

        lv.setAdapter(adapter);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    public void getNotification(){

    }
}
