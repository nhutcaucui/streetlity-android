package com.example.streetlity_android.RealtimeService;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.streetlity_android.MainActivity;
import com.example.streetlity_android.User.Common.Orders;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaintenanceOrder {
    public final String Endpoint = "http://35.240.207.83:9002";
    public final String Tag = "[MaintenanceOrder]";
    public static HashMap<String, MaintenanceOrder> Orders = new HashMap<String, MaintenanceOrder>();

    public String Room;
    /**
     * Listener for event Message
     */
    public MessageListener<MaintenanceOrder> Message;
    private Emitter.Listener onChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnChat: " + message);
            if (Message != null) {
                Message.onReceived(self, message);
            }
        }
    };

    /**
     * Listener for event location
     */
    public LocationListener<MaintenanceOrder> Location;
    private Emitter.Listener onUpdateLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String slat = (String)args[0];
            String slon = (String)args[1];
            Log.println(Log.INFO, Tag, "OnUpdateLocation: " + slat + ":" + slon);

            float lat = Float.parseFloat(slat);
            float lon = Float.parseFloat(slon);
            if (Location != null) {
                Location.onReceived(self, lat, lon);
            }
        }
    };

    /**
     * Listener for event Information
     */
    public InformationListener<MaintenanceOrder> Information;
    private Emitter.Listener onInformation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String username = data.getString("username");
                String phone = data.getString("phone");

                if (Information != null) {
                    Information.onReceived(self, new Information(username, phone));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Listener for event Decline
     */
    public Listener<MaintenanceOrder> Decline;
    private Emitter.Listener onDecline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnDecline: " + message);

            mSocket.close();

            if (Decline != null) {
                Decline.call(self);
            }
        }
    };

    /**
     * Listener for event Complete
     */
    public Listener<MaintenanceOrder> Complete;
    private Emitter.Listener onComplete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (Complete != null) {
                Complete.call(self);
            }
        }
    };

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Endpoint);
        }catch (URISyntaxException e) {}
    }

    private MaintenanceOrder self;

    public MaintenanceOrder(String room) {
        Room = room;
        Orders.put(Room, this);
        self = this;
    }

    public void Create() {

        mSocket.connect();
        mSocket.emit("join");
        mSocket.on("chat", onChat);
        mSocket.on("update-location", onUpdateLocation);
        mSocket.on("information", onInformation);
        mSocket.on("decline", onDecline);
        mSocket.on("complete", onComplete);
    }

    public void SendMessage(String message) {
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
    public void SendInformation(String name, String email, String phone, String address, String avatar) {
        mSocket.emit("information", name, email, phone, address, avatar);
    }

    /**
     * Send the current location to others
     * @param location
     */
    public void UpdateLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        UpdateLocation(lat, lon);
    }

    /**
     * Send the current location to others by specified latitude and longitude
     * @param lat
     * @param lon
     */
    public void UpdateLocation(double lat, double lon) {
        mSocket.emit("update-location", lat, lon);
    }

    /**
     * Deny send a request to decline the order.
     */
    public void Decline() {
        mSocket.emit("decline", "Cancel the order");
    }

    /**
     * Deny send a request to decline the order
     * @param reason the reason of the
     */
    public void Decline(String reason) {
        mSocket.emit("decline", reason);
    }

    /**
     * Close the current connection.
     */
    public void Close() {
        mSocket.disconnect();
        mSocket.off("chat", onChat);
    }
}
