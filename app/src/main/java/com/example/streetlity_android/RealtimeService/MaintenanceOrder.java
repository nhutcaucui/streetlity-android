package com.example.streetlity_android.RealtimeService;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.streetlity_android.MainActivity;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MaintenanceOrder {
    public final String Endpoint = "http://35.240.207.83:9002";
    public final String Tag = "[MaintenanceOrder]";
    public String PhoneNumber;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Endpoint);
        }catch (URISyntaxException e) {}
    }

    public Emitter.Listener onChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnChat: " + message);
        }
    };

    public Emitter.Listener onUpdateLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String lat = (String)args[0];
            String lon = (String)args[1];
            Log.println(Log.INFO, Tag, "OnUpdateLocation: " + lat + ":" + lon);

        }
    };

    public Emitter.Listener onInformation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                PhoneNumber = data.getString("phone");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public Emitter.Listener onDecline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnDecline: " + message);

            mSocket.close();
        }
    };

    public void create(String room) {
        mSocket.connect();
        mSocket.emit("join");
        mSocket.on("chat", onChat);
        mSocket.on("update-location", onUpdateLocation);
        mSocket.on("information", onInformation);
        mSocket.on("decline", onDecline);
    }

    public void sendMessage(String message) {
        mSocket.emit("chat", message);
    }

    /**
     * Send information to others
     * @param name
     * @param email
     * @param phone
     * @param address
     * @param avatar
     */
    public void sendInformation(String name, String email, String phone, String address, String avatar) {
        mSocket.emit("information", name, email, phone, address, avatar);
    }

    /**
     * Send the current location to others
     * @param location
     */
    public void updateLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        updateLocation(lat, lon);
    }

    /**
     * Send the current location to others by specified latitude and longitude
     * @param lat
     * @param lon
     */
    public void updateLocation(double lat, double lon) {
        mSocket.emit("update-location", lat, lon);
    }

    /**
     * Deny send a request to decline the order.
     */
    public void decline() {
        mSocket.emit("decline", "Cancel the order");
    }

    /**
     * Deny send a request to decline the order
     * @param reason the reason of the
     */
    public void decline(String reason) {
        mSocket.emit("decline", reason);
    }

    /**
     * Close the current connection.
     */
    public void close() {
        mSocket.disconnect();
        mSocket.off("chat", onChat);
    }
}
