package com.example.streetlity_android.RealtimeService;

import java.util.EventListener;

public interface Listener<T> extends EventListener {
    void call(T sender);
}
