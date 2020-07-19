package com.streetlity.client.User;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.streetlity.client.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Button btnSendOTP = findViewById(R.id.btn_send);
        final Button btnResetPassword = findViewById(R.id.btn_reset_pass);
        final LinearLayout resetLayout = findViewById(R.id.layout_reset_pass);

        com.google.android.material.textfield.TextInputEditText edtMail = findViewById(R.id.edt_email);
        com.google.android.material.textfield.TextInputEditText edtOTP = findViewById(R.id.edt_OTP);
        TextInputEditText edtNewPass = findViewById(R.id.edt_new_pass);
        TextInputEditText edtCF = findViewById(R.id.edt_cf_new_pass);

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
