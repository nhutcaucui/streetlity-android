package com.example.streetlity_android.Events;

import java.util.EventListener;

public interface EListener<TSender, TArgs> extends EventListener {
    void trigger(TSender sender, TArgs args);
}
