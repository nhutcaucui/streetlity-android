package com.streetlity.client;

import android.app.Application;
import android.graphics.Bitmap;

import com.streetlity.client.Achievement.ActionObject;
import com.streetlity.client.Option.OptionInterface;
import com.streetlity.client.RealtimeService.MaintenanceOrder;

import java.util.Map;
import java.util.Timer;

//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTQ0NDUzMzIsImlkIjoibXJYWVpaemFidiJ9.0Hd4SpIELulSuTxGAeuCPl_A33X-KoPUpRmgK4dTphk

//401 refresh

//1:common
//7:maintenance

/**
 * this is for some global values
 */
public class MyApplication extends Application { //35.240.232.218 auth server

    static MyApplication myAppInstance;
    public MyApplication() {
        myAppInstance = this;
    }
    public static MyApplication getInstance() {
        return myAppInstance;
    }

    private String token = "";

    private String refreshToken = "";

    private String deviceToken = "";

    private int userType;

    private String username="";

    private String userId;

    private String email="";

    private  String address="";

    private String phone="";

    private String name = "";

    private Bitmap image;

    private String version = "1.0.0";

    private MaintenanceOrder socket;

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

   // private String serviceURL = "http://34.87.144.190/";
   private String serviceURL = "http://34.87.144.190/";
    //private String authURL = "http://35.240.232.218/";
    private String authURL = "http://35.240.195.17/";
   // private String driverURL = "http://34.87.144.190/";
   private String driverURL = "http://34.87.165.234/";

    private String maintenanceURL = "http://34.87.144.190:9002/";

    private Map<String, Map<String, ActionObject>> reviewedMap;
    private Map<String, Map<String, ActionObject>> contributeMap;
    private Map<String, Map<String, ActionObject>> upvoteMap;
    private Map<String, Map<String, ActionObject>> downvoteMap;

    private OptionInterface option;

    public void setOption(OptionInterface option) {
        this.option = option;
    }

    public OptionInterface getOption() {
        return option;
    }

    public MaintenanceOrder getSocket() {
        return socket;
    }

    public void setSocket(MaintenanceOrder socket) {
        this.socket = socket;
    }

    public String getDriverURL() {
        return driverURL;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public String getAuthURL() {
        return authURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMaintenanceURL() {
        return maintenanceURL;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setReviewedMap(Map<String, Map<String, ActionObject>> reviewedMap) {
        this.reviewedMap = reviewedMap;
    }

    public void setContributeMap(Map<String, Map<String, ActionObject>> contributeMap) {
        this.contributeMap = contributeMap;
    }

    public void setUpvoteMap(Map<String, Map<String, ActionObject>> upvoteMap) {
        this.upvoteMap = upvoteMap;
    }

    public void setDownvoteMap(Map<String, Map<String, ActionObject>> downvoteMap) {
        this.downvoteMap = downvoteMap;
    }

    public Map<String, Map<String, ActionObject>> getReviewedMap() {
        return reviewedMap;
    }

    public Map<String, Map<String, ActionObject>> getContributeMap() {
        return contributeMap;
    }

    public Map<String, Map<String, ActionObject>> getUpvoteMap() {
        return upvoteMap;
    }

    public Map<String, Map<String, ActionObject>> getDownvoteMap() {
        return downvoteMap;
    }

    private Timer thread = new Timer();

    public Timer getThread() {
        return thread;
    }

    public void setThread() {
        thread = new Timer();
    }

    public String getVersion(){
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    float range = 0.3f;

    public float getRange(){
        return range;
    }
}
