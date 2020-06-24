package com.example.streetlity_android.User;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import com.example.streetlity_android.Contribution.SelectFromMap;
import com.example.streetlity_android.MapAPI;
import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.Util.ImageFilePath;
import com.example.streetlity_android.Util.RandomString;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streetlity_android.R;

import org.json.JSONObject;

import java.io.File;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfo extends AppCompatActivity {

    boolean edtState = false;
    boolean hasImg = false;

    MultipartBody.Part body;
    String fileName = "";
    ImageView imgAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        EditText edtAddress = findViewById(R.id.edt_address);
        EditText edtPhone = findViewById(R.id.edt_phone);

        edtAddress.setText(MyApplication.getInstance().getAddress());
        edtPhone.setText(MyApplication.getInstance().getPhone());

        EditText tvUsername = findViewById(R.id.edt_username);
        tvUsername.setText((MyApplication.getInstance().getUsername()));

        EditText tvEmail = findViewById(R.id.edt_email);
        tvEmail.setText((MyApplication.getInstance().getEmail()));

        imgAvatar = findViewById(R.id.img_avatar);
        ImageView imgEditable = findViewById(R.id.img_editable);
        LinearLayout preventClick = findViewById(R.id.prevent_click);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
            }
        });

        FloatingActionButton fabEdit = findViewById(R.id.fab_edit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtState){
                    edtState = true;
                    imgEditable.setVisibility(View.VISIBLE);
                    preventClick.setVisibility(View.GONE);
                    fabEdit.setImageResource(R.drawable.checkmark_black);

                    edtAddress.setFocusable(true);
                    edtAddress.setFocusableInTouchMode(true);

                    edtPhone.setFocusable(true);
                    edtPhone.setFocusableInTouchMode(true);

                    edtAddress.setInputType(InputType.TYPE_CLASS_TEXT);
                    edtPhone.setInputType(InputType.TYPE_CLASS_NUMBER);

                    edtAddress.setLongClickable(true);
                    edtPhone.setLongClickable(true);

                    edtAddress.requestFocus();

                    InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }else {
                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }

                    edtState = false;
                    imgEditable.setVisibility(View.GONE);
                    preventClick.setVisibility(View.VISIBLE);

                    fabEdit.setImageResource(R.drawable.edit);

                    edtAddress.setFocusable(false);
                    edtAddress.setFocusableInTouchMode(false);

                    edtPhone.setFocusable(false);
                    edtAddress.setFocusableInTouchMode(false);

                    edtAddress.setInputType(InputType.TYPE_NULL);
                    edtPhone.setInputType(InputType.TYPE_NULL);

                    edtAddress.setLongClickable(false);
                    edtPhone.setLongClickable(false);

                    if (getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 1 && data != null) {
                int leftLimit = 48; // letter 'a'
                int rightLimit = 122; // letter 'z'
                int targetStringLength = 10;
                if (data.getData() != null) {

                    String path = ImageFilePath.getPath(UserInfo.this, data.getData());

                    File file = new File(path);

                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                    imgAvatar.setImageBitmap(myBitmap);

                    hasImg = true;

                    String generatedString = RandomString.getAlphaNumericString(10);

                    RequestBody fbody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part mBody =
                            MultipartBody.Part.createFormData(generatedString+0, file.getName(), fbody);

                    body=mBody;

                    fileName = generatedString;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
