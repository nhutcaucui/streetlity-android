package com.example.streetlity_android.Chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.streetlity_android.MyApplication;
import com.example.streetlity_android.R;
import com.example.streetlity_android.RealtimeService.Information;
import com.example.streetlity_android.RealtimeService.InformationListener;
import com.example.streetlity_android.RealtimeService.Listener;
import com.example.streetlity_android.RealtimeService.LocationListener;
import com.example.streetlity_android.RealtimeService.MaintenanceOrder;
import com.example.streetlity_android.RealtimeService.MessageListener;
import com.example.streetlity_android.User.UserInfoOther;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.view.View.GONE;

public class Chat extends AppCompatActivity {

    ArrayList<ChatObject> items = new ArrayList<>();

    Information infomation;

    double lat;
    double lon;

    String room = "";
    ListView lv;
    ChatObjectAdapter adapter;

    MaintenanceOrder socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        lv= findViewById(R.id.lv);
        adapter = new ChatObjectAdapter(this, R.layout.lv_item_chat, items);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!items.get(position).getName().equals(MyApplication.getInstance().getUsername())) {
                    Intent t = new Intent(Chat.this, UserInfoOther.class);
                    t.putExtra("user", items.get(position).getName());
                    startActivity(t);
                }
            }
        });

        SharedPreferences s = getSharedPreferences("Room", MODE_PRIVATE);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("id")) {
                s.edit().putString("room", getIntent().getStringExtra("id")).apply();
            }
        }
        if (getIntent().getStringExtra("id") != null) {
            Log.e("", "onCreate: " + getIntent().getStringExtra("id"));
            room = getIntent().getStringExtra("id");
            s.edit().putString("room", getIntent().getStringExtra("id")).apply();
        } else {
            room = s.getString("room", "noroom");
        }
        s.edit().clear().apply();
        String phone = "";
        if (getSharedPreferences("broadcastPhone", MODE_PRIVATE).contains("phone")) {
            phone = getSharedPreferences("broadcastPhone", MODE_PRIVATE).getString("phone", "no");
        }
        if (phone.equals("")) {
            phone = MyApplication.getInstance().getPhone();
        }

        Log.e("", "onCreate: " + room);

        MaintenanceOrder.Create(room);

        socket = MaintenanceOrder.getInstance();

        socket.InformationListener = new InformationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, Information info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infomation = info;
                        Log.e("", "onReceived: " + info.toString());
                    }
                });

            }
        };
        Information myInfo = new Information(MyApplication.getInstance().getUsername(), phone);

        socket.sendInformation(myInfo);
        socket.pullInformation();

        socket.CompleteListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.layout_complete).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.DeclineListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                getSharedPreferences("activeOrder",MODE_PRIVATE).edit().clear().apply();
                findViewById(R.id.btn_finish_denu).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.layout_denied).setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        socket.MessageListener = new MessageListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, ChatObject message) {
                Log.e("", "onReceived:  this is america");
                new Task().execute(message);
            }
        };
        socket.LocationListener = new LocationListener<MaintenanceOrder>() {
            @Override
            public void onReceived(MaintenanceOrder sender, float lat, float lon) {
                lat = lat;
                lon = lon;
            }
        };

        socket.DeclineListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {

            }
        };
        socket.CompleteListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                Log.e("", "call: completed");
            }
        };

        socket.JoinedListener = new Listener<MaintenanceOrder>() {
            @Override
            public void trigger(MaintenanceOrder sender) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConstraintLayout layoutLoading = findViewById(R.id.layout_loading_top);
                        layoutLoading.setVisibility(GONE);
                        sender.pullChat();

                        new CountDownTimer(5000,1000){
                            public void onTick(long millisUntilFinished) {
                                //here you can have your logic to set text to edittext
                            }

                            public void onFinish() {
                                sortChat();
                            }

                        }.start();
                    }
                });
            }
        };

        socket.join();

        TextInputEditText edtMessage = findViewById(R.id.edt_body);

        ImageButton imgBtnSend = findViewById(R.id.btn_send);
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtMessage.getText().toString().equals("")) {

                    Log.e("", "onClick: " + edtMessage.getText().toString());
                    ChatObject object = new ChatObject(MyApplication.getInstance().getUsername(), edtMessage.getText().toString(), new Date());
                    socket.sendMessage(object);
                    items.add(object);
                    adapter.notifyDataSetChanged();
                    lv.setSelection(adapter.getCount() - 1);
                    edtMessage.setText("");

                }
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(edtMessage.getText().toString().equals("")){
//                    maintenanceOrder.sendTyped(MyApplication.getInstance().getUsername());
//                }else {
//                    maintenanceOrder.sendTyping(MyApplication.getInstance().getUsername());
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        maintenanceOrder.TypingListener = new TypeMessageListener<MaintenanceOrder>(){
//            @Override
//            public void trigger(MaintenanceOrder sender, String typeUser){
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(findViewById(R.id.tv_chat_username) != null){
//                            ((TextView) findViewById(R.id.tv_chat_username)).setText(typeUser);
//                            findViewById(R.id.layout_is_typing).setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//            }
//
//        };

//        maintenanceOrder.TypedListener = new TypeMessageListener<MaintenanceOrder>() {
//            @Override
//            public void trigger(MaintenanceOrder sender, String triggeringUser) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        findViewById(R.id.layout_is_typing).setVisibility(View.GONE);
//                    }
//                });
//            }
//        };
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.finish();

        return true;
    }

    public void saveToSharedPref() {
        Gson gson = new Gson();
        String jsonText = gson.toJson(items);
        getSharedPreferences("chatLog", MODE_PRIVATE).edit().putString(room, jsonText).apply();
    }

    public void getSharedPref() {
        Gson gson = new Gson();
        String jsonText = getSharedPreferences("chatLog", MODE_PRIVATE).getString(room, null);
        if (jsonText != null) {
            Type type = new TypeToken<ArrayList<ChatObject>>() {
            }.getType();
            items = gson.fromJson(jsonText, type);
        }
    }

    public void clearSharedPrefs() {
        getSharedPreferences("chatLog", MODE_PRIVATE).edit().clear().apply();
    }


    class Task extends AsyncTask<ChatObject, Void, ArrayList<ChatObject>> {
        private Exception exception;

        @Override
        protected ArrayList<ChatObject> doInBackground(ChatObject... message) {
            try {
                items.add(message[0]);
                findViewById(R.id.layout_is_typing).setVisibility(View.GONE);
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ChatObject> a) {
            //add the tours from internet to the array
            adapter.notifyDataSetChanged();
            lv.setSelection(adapter.getCount()-1);
        }
    }

    public void sortChat(){
        Collections.sort(items, new Comparator<ChatObject>() {
            @Override
            public int compare(ChatObject o1, ChatObject o2) {
                Log.e("", "compare: "+o1.getTime() +"-"+ o2.getTime());
                return Long.compare(o1.getTime().getTime(), o2.getTime().getTime());
            }
        });
        adapter.notifyDataSetChanged();
        lv.setSelection(adapter.getCount()-1);
    }
}

