package com.example.streetlity_android.Option;

import android.os.Bundle;

import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        if(MyApplication.getInstance().getUserType() == 7) {
            Switch switchEmergency = findViewById(R.id.emergency_switch);
            switchEmergency.setChecked(MyApplication.getInstance().getOption().isAcceptEmergency());
            switchEmergency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MyApplication.getInstance().getOption().setAcceptEmergency(isChecked);
                    getSharedPreferences("acceptEmergency", MODE_PRIVATE).edit().putBoolean("acceptEmergency", isChecked).apply();
                }
            });
        }

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}