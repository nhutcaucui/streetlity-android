package com.streetlity.client.RealtimeService;

import java.util.EventListener;

public interface DeclineListener<T> extends EventListener {
    void onReceived(T sender, String reason);
}
