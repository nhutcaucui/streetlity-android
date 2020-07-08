package com.example.streetlity_android.RealtimeService;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.example.streetlity_android.Chat.ChatObject;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class MaintenanceOrder {
    public final String Endpoint = "34.87.144.190:6182";
    public final String Tag = "[MaintenanceOrder]";
    public static HashMap<String, MaintenanceOrder> Orders = new HashMap<String, MaintenanceOrder>();

    public String room;
    public Information information;
    public Location location;

    public static MaintenanceOrder getInstance() {
        return self;
    }

    /**
     * Listener for event Joined. Trigger when joined successfully.
     */
    public Listener<MaintenanceOrder> JoinedListener;
    /**
     * Listener for event Ready. Ready trigger when every prerequisite conditions is satisfied.
     */
    public Listener<MaintenanceOrder> ReadyListener;
    /**
     * Listener for event Message. Trigger everytime a message from others is sent
     */
    public MessageListener<MaintenanceOrder> MessageListener;
    /**
     * Listener for event location.
     */
    public LocationListener<MaintenanceOrder> LocationListener;
    /**
     * Listener for event Information
     */
    public InformationListener<MaintenanceOrder> InformationListener;
    /**
     * Listener for event Decline
     */
    public Listener<MaintenanceOrder> DeclineListener;
    /**
     * Listener for event Complete
     */
    public Listener<MaintenanceOrder> CompleteListener;
    /**
     * Listener for event Typing Message. Trigger when an user start to type
     */
    public TypeMessageListener<MaintenanceOrder> TypingListener;
    /**
     * Listener for event Typed Message. Trigger when an user is complete to type
     */
    public TypeMessageListener<MaintenanceOrder> TypedListener;
    /**
     * Listener for event Pulled Message. Trigger when the pull message event is completed.
     */
    public Listener<MaintenanceOrder> PulledMessageListener;
    private Emitter.Listener onChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject json = new JSONObject(args[0].toString());
                String name = json.getString("name");
                String body = json.getString("body");
                String date = json.getString("date");

                ChatObject message = new ChatObject(name, body, new  SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).parse(date));
                Log.println(Log.INFO, Tag, "OnChat: " + message);
                if (MessageListener != null) {
                    MessageListener.onReceived(self, message);
                }
            } catch (Exception e) {
                Log.e(Tag, "onChat: " + e.getMessage());
            }

        }
    };

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

    private Emitter.Listener onDecline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String message = (String)args[0];
            Log.println(Log.INFO, Tag, "OnDecline: " + message);

            mSocket.close();

            if (DeclineListener != null) {
                DeclineListener.trigger(self);
            }
        }
    };


    private Emitter.Listener onComplete = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (CompleteListener != null) {
                CompleteListener.trigger(self);
            }
        }
    };

    private Emitter.Listener onPullInformation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendInformation(information);
        }
    };

    private Emitter.Listener onJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(Tag, "onJoined: joined");
            if (JoinedListener != null) {
                JoinedListener.trigger(self);
            }
        }
    };

    private Emitter.Listener onTypingChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String typingUser = (String)args[0];
            Log.d(Tag, "onTypingChat: " + typingUser);
            if (TypingListener != null) {
                TypingListener.trigger(self, typingUser);
            }
        }
    };

    private Emitter.Listener onTypedChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String typedUser = (String)args[0];
            Log.d(Tag, "onTypedChat: " + typedUser);
            if (TypedListener != null) {
                TypedListener.trigger(self, typedUser);
            }
        }
    };

    private Emitter.Listener onPulledChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (PulledMessageListener != null) {
                PulledMessageListener.trigger(self);
            }
        }
    };

    private Socket mSocket;
    public static MaintenanceOrder self;

    /**
     * Initialize a new instannce of MaintenanceOrder by a specified room
     * @param room
     */
    protected MaintenanceOrder(String room) {
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
        mSocket.on("chat", onChat);
        mSocket.on("pulled-chat", onPulledChat);
        mSocket.on("update-location", onUpdateLocation);
        mSocket.on("update-information", onUpdateInformation);
        mSocket.on("pull-information", onPullInformation);
        mSocket.on("pull-location", onPullLocation);
        mSocket.on("decline", onDecline);
        mSocket.on("complete", onComplete);
        mSocket.on("joined", onJoined);
        mSocket.on("typing-chat", onTypingChat);
        mSocket.on("typed-chat", onTypedChat);
    }

    /**
     * Join to initial room and ready for working on it.
     */
    public void join() {
        if (mSocket.connected()) {
            JoinedListener.trigger(self);
            Log.i(Tag, "join in " + room + ": already connected");
            return;
        }

        mSocket.connect();
    }

    /**
     * Send a specified message to others
     * @param message
     */
    public void sendMessage(ChatObject message) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", message.getName());
            json.put("body", message.getBody());
            json.put("date", new  SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(message.getTime()));
        } catch (JSONException e) {
            Log.e(Tag,"Cannot put new message " + e.getMessage());
        }
        mSocket.emit("chat", json.toString());
    }

    /**
     * Send message to others to inform typing
     * @param typing_user
     */
    public void sendTyping(String typing_user) {
        mSocket.emit("typing-chat", typing_user);
    }

    /**
     * Send message to others to inform typed
     * @param typed_user
     */
    public void sendTyped(String typed_user) {
        mSocket.emit("typed-chat", typed_user);
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
//        mSocket.emit("pull-information");
    }

    /**
     * Pull previous chat
     */
    public void pullChat() {
        mSocket.emit("pull-chat");
    }

    /**
     * Send the current location to others
     * @param location
     */
    public void updateLocation(Location location) {
        JSONObject json = new JSONObject();
        try {
            json.put("Location", location);
        } catch (JSONException e) {
            Log.e(Tag, "updateLocation: " + e.getMessage());
        }
        mSocket.emit("update-location", json.toString());
    }

    /**
     * Send the current location to others by specified latitude and longitude
     * @param lat
     * @param lon
     */
    public void updateLocation(double lat, double lon, float bearing) {
        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setBearing(bearing);
        updateLocation(location);
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
     *
     * Complete the order
     */
    public void complete() {
        mSocket.emit("complete");
    }

    /**
     * Close the current connection.
     */
    public void close() {
        if (!mSocket.connected()) {
            Log.i(Tag, "close " + room + ": is already closed");

            if (Orders.containsKey(room)) {
                Orders.remove(room);
            }

            return;
        }

        mSocket.disconnect();
        mSocket.off("chat", onChat);
        mSocket.off("update-location", onUpdateLocation);
        mSocket.off("update-information", onUpdateInformation);
        mSocket.off("pull-information", onPullInformation);
        mSocket.off("pull-location", onPullLocation);
        mSocket.off("decline", onDecline);
        mSocket.off("complete", onComplete);
        mSocket.off("joined", onJoined);
        mSocket.off("typing-chat", onTypingChat);
        mSocket.off("typed-chat", onTypedChat);
        Orders.remove(room);
    }

    /**
     * Initialize a new instannce of MaintenanceOrder by a specified room
     * @param room
     */
    public static MaintenanceOrder Create(String room) {
        if (!Orders.containsKey(room)) {
            MaintenanceOrder order = new MaintenanceOrder(room);
            Orders.put(room, order);
        }

        return Orders.get(room);
    }
}
