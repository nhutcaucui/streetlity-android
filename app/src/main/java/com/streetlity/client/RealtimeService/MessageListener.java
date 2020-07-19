package com.streetlity.client.RealtimeService;

import com.streetlity.client.Chat.ChatObject;

import java.util.EventListener;

public interface MessageListener<T> extends EventListener {
    void onReceived(T sender, ChatObject message);
}
