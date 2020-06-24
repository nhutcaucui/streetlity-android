package com.example.streetlity_android.RealtimeService;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class MaintenanceOrder {
    public final String Endpoint = "35.240.207.83:6182";
    public final String Tag = "[MaintenanceOrder]";
    public static HashMap<String, MaintenanceOrder> Orders = new HashMap<String, MaintenanceOrder>();

    public String room;
    public Information information;
    public Location location;
    /**
     * Listener for event Message
     */
    public MessageListener<MaintenanceOrder> MessageListener;
    private Emitter.Listener onChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnChat: " + message);
            if (MessageListener != null) {
                MessageListener.onReceived(self, message);
            }
        }
    };

    /**
     * Listener for event location
     */
    public LocationListener<MaintenanceOrder> LocationListener;
    private Emitter.Listener onUpdateLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String slat = (String)args[0];
            String slon = (String)args[1];
            Log.println(Log.INFO, Tag, "OnUpdateLocation: " + slat + ":" + slon);

            float lat = Float.parseFloat(slat);
            float lon = Float.parseFloat(slon);
            if (LocationListener != null) {
                LocationListener.onReceived(self, lat, lon);
            }
        }
    };

    private Emitter.Listener onPullLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            updateLocation(location);
        }
    };

    /**
     * Listener for event Information
     */
    public InformationListener<MaintenanceOrder> InformationListener;
    private Emitter.Listener onUpdateInformation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {
                JSONObject data = new JSONObject(args[0].toString());
                String username = data.getString("username");
                String phone = data.getString("phone");

                if (InformationListener != null) {
                    InformationListener.onReceived(self, new Information(username, phone));
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

    private Emitter.Listener onPullInformation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendInformation(information);
        }
    };

    private Socket mSocket;


    private MaintenanceOrder self;

    /**
     * Initialize a new instannce of MaintenanceOrder by a specified room
     * @param room
     */
    public MaintenanceOrder(String room) {

        this.room = room;
        Orders.put(this.room, this);
        self = this;

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(Endpoint)
                    .appendPath("socket.io");
            Manager manager = new Manager(new URI(builder.build().toString()));
            mSocket = manager.socket("/" + room);
        }catch (URISyntaxException e) {}
    }

    /**
     * Join to initial room and ready for working on it.
     */
    public void join() {
        mSocket.connect();
        mSocket.emit("join");
        mSocket.on("chat", onChat);
        mSocket.on("update-location", onUpdateLocation);
        mSocket.on("update-information", onUpdateInformation);
        mSocket.on("pull-information", onPullInformation);
        mSocket.on("pull-location", onPullLocation);
        mSocket.on("decline", onDecline);
        mSocket.on("complete", onComplete);
    }

    /**
     * Send a specified message to others
     * @param message
     */
    public void sendMessage(String message) {
        mSocket.emit("chat", message);
    }


    /**
     * Send information to others
     * @param information
     */
    public void sendInformation(Information information) {
        this.information = information;
        JSONObject json = new JSONObject();
        try {
            json.put("username", information.Username);
            json.put("phone", this.information.Phone);
        } catch (JSONException e) {
            Log.e("[MaintenanceOrder]","Cannot put new information " + e.getMessage());
        }

        mSocket.emit("update-information", json.toString());
    }

    /**
     * Pull information from others
     */
    public void pullInformation() {
        mSocket.emit("pull-information");
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
        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lon);
        mSocket.emit("update-location", lat, lon);
    }

    /**
     * Pull new location from other
     * @param location
     */
    public void pullLocation(Location location) {
        mSocket.emit("pull-location");
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
