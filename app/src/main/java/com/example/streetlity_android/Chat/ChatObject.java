package com.example.streetlity_android.Chat;

import java.util.Date;

public class ChatObject {
    private String name;
    private String body;
    private Date time;

    public ChatObject(String name, String body, Date time){
        this.name = name;
        this.body = body;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }
}
