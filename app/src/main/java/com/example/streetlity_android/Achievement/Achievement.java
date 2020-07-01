package com.example.streetlity_android.Achievement;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.streetlity_android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Achievement extends AppCompatActivity {

    ArrayList<AchievementObject> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        ListView lv = findViewById(R.id.lv);

        items.add(new AchievementObject("achievement notActive", 69, false));
        items.add(new AchievementObject("achievement notActive", 1, true));
        items.add(new AchievementObject("achievement active", 500, true));
        items.add(new AchievementObject("achievement notActive", 123456, false));

        Collections.sort(items, new Comparator<AchievementObject>() {
            @Override
            public int compare(AchievementObject abc1, AchievementObject abc2) {
                return Boolean.compare(abc2.isEarned(),abc1.isEarned());
            }
        });

        AchievementObjectAdapter adapter = new AchievementObjectAdapter(this, R.layout.lv_item_achievement, items);

        lv.setAdapter(adapter);

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}