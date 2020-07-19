package com.streetlity.client.Events;

import java.util.ArrayList;

public class Event<TSender, TArgs> {
    private ArrayList<EListener<TSender, TArgs>> events = new ArrayList<>();

    /**
     * Subcribe the listener, this listener will
     * @param listener
     */
    public void subcribe(EListener<TSender, TArgs> listener) {
        events.add(listener);
    }

    /**
     * Unsubcribe the listener, this listener will no longer be triggered
     * @param listener
     */
    public void unsubcribe(EListener<TSender, TArgs> listener) {
        events.remove(listener);
    }

    /**
     * Trigger all subcribed listener
     * @param sender
     * @param args
     */
    public void trigger(TSender sender, TArgs args) {
        int size = events.size();
        for (int loop = 0; loop < size; loop++) {
            events.get(loop).trigger(sender, args);
        }
    }
}
