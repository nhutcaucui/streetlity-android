package com.example.streetlity_android.RealtimeService;

import java.util.EventListener;

public interface InformationListener<T> extends EventListener {
    void onReceived(T sender, Information info);
}
