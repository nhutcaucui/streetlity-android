package com.example.streetlity_android.Achievement;

public class ActionObject {
    private String name;
    private long time;
    private String action;
    private String affected;

    public ActionObject(String name, long time, String action, String affected) {
        this.name = name;
        this.time = time;
        this.action = action;
        this.affected = affected;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public String getAction() {
        return action;
    }

    public String getAffected() {
        return affected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setAffected(String affected) {
        this.affected = affected;
    }
}
