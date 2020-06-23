package com.example.streetlity_android.User.Common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.streetlity_android.R;

public class OrderInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info_common);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NormalOrderObject item = (NormalOrderObject) getIntent().getSerializableExtra("item");
        int from = getIntent().getIntExtra("from", 0);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tvName = findViewById(R.id.tv_name);
        TextView tvPhone = findViewById(R.id.tv_phone);
        TextView tvReason = findViewById(R.id.tv_reason);
        TextView tvNote = findViewById(R.id.tv_note);

        tvName.setText(item.getName());
        tvPhone.setText(item.getPhone());
        tvNote.setText(item.getNote());
        tvReason.setText(item.getReason());

        Button btnMark = findViewById(R.id.btn_complete);


        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(OrderInfo.this).create();
                alertDialog.setTitle("Completed?");
                alertDialog.setMessage("Mark this order as completed?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }



    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }
}
