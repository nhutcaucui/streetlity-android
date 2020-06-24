package com.example.streetlity_android.RealtimeService;

import java.util.EventListener;

public interface TypeMessageListener<T> extends EventListener {
    void trigger(T sender, String triggeringUser);
}
