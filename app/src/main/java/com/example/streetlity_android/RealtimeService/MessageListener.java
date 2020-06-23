package com.example.streetlity_android.RealtimeService;

import java.util.EventListener;

public interface MessageListener<T> extends EventListener {
    void onReceived(T sender, String message);
}
