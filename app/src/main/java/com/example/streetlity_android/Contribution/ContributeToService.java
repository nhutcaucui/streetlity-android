package com.example.streetlity_android.Contribution;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.streetlity_android.R;

public class ContributeToService extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute_to_service);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        LinearLayout btnFuel = findViewById(R.id.btn_fuel);
        LinearLayout btnATM = findViewById(R.id.btn_atm);
        LinearLayout btnMaintenance = findViewById(R.id.btn_maintenance);
        LinearLayout btnWC = findViewById(R.id.btn_wc);
        Button btnConfirming = findViewById(R.id.btn_confirm_location);

        btnFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(ContributeToService.this, SelectFromMap.class);
                t.putExtra("type", 1);
                startActivity(t);
            }
        });

        btnATM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(ContributeToService.this, AddAnATM.class);
                startActivity(t);
            }
        });

        btnMaintenance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(ContributeToService.this, AddAMaintenance.class);
                startActivity(t);
            }
        });

        btnWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(ContributeToService.this, SelectFromMap.class);
                t.putExtra("type", 2);
                startActivity(t);
            }
        });

        btnConfirming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(ContributeToService.this, ConfirmLocationsHolder.class);
                startActivity(t);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
