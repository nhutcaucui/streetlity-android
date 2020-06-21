package com.example.streetlity_android;

import android.app.Application;

//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTQ0NDUzMzIsImlkIjoibXJYWVpaemFidiJ9.0Hd4SpIELulSuTxGAeuCPl_A33X-KoPUpRmgK4dTphk

//401 refresh

//1:common
//7:maintenance

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

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    private String serviceURL = "http://35.240.207.83/";

    private String authURL = "http://35.240.232.218/";

    private String driverURL = "http://34.87.144.190/";

    private String maintenanceURL = "http://35.240.207.83:9002/";

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
}
