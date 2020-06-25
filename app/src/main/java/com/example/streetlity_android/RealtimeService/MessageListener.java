package com.example.streetlity_android.RealtimeService;

import com.example.streetlity_android.Chat.ChatObject;

import java.util.EventListener;

public interface MessageListener<T> extends EventListener {
    void onReceived(T sender, ChatObject message);
}
