package com.example.streetlity_android.Chat;

public class ChatObject {
    private String name;
    private String body;

    public ChatObject(String name, String body){
        this.name = name;
        this.body = body;
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
}
