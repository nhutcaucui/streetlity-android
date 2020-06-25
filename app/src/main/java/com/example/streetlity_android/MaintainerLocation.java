package com.example.streetlity_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.example.streetlity_android.Chat.Chat;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.example.streetlity_android.User.SignUp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MaintainerLocation extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintainer_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton btnCall = findViewById(R.id.btn_call);
        ImageButton btnChat = findViewById(R.id.btn_chat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MaintainerLocation.this, Chat.class));
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0123456789"));
                startActivity(intent);
            }
        });

        ImageButton btnCancel = findViewById(R.id.btn_cancel_order);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialogDecline = new Dialog(MaintainerLocation.this);

                final LayoutInflater inflater = LayoutInflater.from(MaintainerLocation.this);

                final View dialogView = View.inflate(MaintainerLocation.this ,R.layout.dialog_decline, null);

                EditText edtReason = dialogView.findViewById(R.id.edt_reason);

                Button btnConfirm = dialogView.findViewById(R.id.btn_decline);

                Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogDecline.cancel();
                    }
                });

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Retrofit retro = new Retrofit.Builder().baseUrl(MyApplication.getInstance().getMaintenanceURL())
                                .addConverterFactory(GsonConverterFactory.create()).build();
                        final MapAPI tour = retro.create(MapAPI.class);
                        Call<ResponseBody> call = tour.denyOrder(0, 2, edtReason.getText().toString());
                        //need Id
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    final JSONObject jsonObject;
                                    try {
                                        jsonObject = new JSONObject(response.body().string());
                                        Log.e("", "onResponse: " + jsonObject.toString());
                                        if(jsonObject.getBoolean("Status")){

                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    Toast toast = Toast.makeText(MaintainerLocation.this, R.string.something_wrong, Toast.LENGTH_LONG);
                                    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                                    tv.setTextColor(Color.RED);

                                    toast.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });
                    }
                });

                dialogDecline.setContentView(dialogView);

                dialogDecline.show();
            }
        });

        ImageButton imgComplete = findViewById(R.id.btn_done);
        imgComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MaintainerLocation.this).create();
                alertDialog.setTitle(getString(R.string.remove)+"?");
                alertDialog.setMessage(getString(R.string.remove_pic));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no),
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

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
