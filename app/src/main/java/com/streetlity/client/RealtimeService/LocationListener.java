package com.streetlity.client.RealtimeService;

import java.util.EventListener;

public interface LocationListener<T> extends EventListener {
    void onReceived(T sender, float lat, float lon);
}
