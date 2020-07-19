package com.streetlity.client.RealtimeService;

import java.util.EventListener;

public interface Listener<T> extends EventListener {
    void trigger(T sender);
}
