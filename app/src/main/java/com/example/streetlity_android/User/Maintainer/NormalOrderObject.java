package com.example.streetlity_android.User.Maintainer;

import java.io.Serializable;

public class NormalOrderObject implements Serializable {
    private int id;
    private String reason;
    private String name;
    private String address;
    private String phone;
    private String note;
    private String time;

    public NormalOrderObject(int id, String name, String reason) {
        this.id = id;
        this.name = name;
        this.reason = reason;
    }

    public NormalOrderObject() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getNote() {
        return note;
    }

    public String getTime() {
        return time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
