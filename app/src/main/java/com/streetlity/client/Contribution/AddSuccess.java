package com.streetlity.client.Contribution;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.streetlity.client.R;

public class AddSuccess extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_success);

        Button btnFinish = findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tvSuccess = findViewById(R.id.tv_success);
        if(getIntent().getIntExtra("type", -1) == 1){
            tvSuccess.setText(R.string.fuel_success);
        }else if (getIntent().getIntExtra("type", -1) == 2){
            tvSuccess.setText(R.string.wc_success);
        }
    }
}
