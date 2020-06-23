package com.example.streetlity_android.RealtimeService;

import java.util.EventListener;

public interface LocationListener<T> extends EventListener {
    void onReceived(T sender, float lat, float lon);
}
