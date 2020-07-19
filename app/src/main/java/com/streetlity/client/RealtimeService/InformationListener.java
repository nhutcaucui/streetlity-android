package com.streetlity.client.RealtimeService;

import java.util.EventListener;

public interface InformationListener<T> extends EventListener {
    void onReceived(T sender, Information info);
}
