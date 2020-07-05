package com.example.streetlity_android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

public class ServiceChecking extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
        public void onTaskRemoved(Intent rootIntent) {
            System.out.println("onTaskRemoved called");
            super.onTaskRemoved(rootIntent);

            Gson gs = new Gson();

            SharedPreferences s = getSharedPreferences("map", MODE_PRIVATE);
            SharedPreferences.Editor e = s.edit();

            e.putString("review", gs.toJson(MyApplication.getInstance().getReviewedMap()));
            e.putString("contribute", gs.toJson(MyApplication.getInstance().getContributeMap()));
            e.putString("upvote", gs.toJson(MyApplication.getInstance().getUpvoteMap()));

            e.apply();

            this.stopSelf();
        }
    }
