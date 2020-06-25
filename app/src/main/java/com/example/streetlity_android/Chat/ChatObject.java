package com.example.streetlity_android.Chat;

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatObject {
    private String name;
    private String body;
    private Date time;
    private Bitmap image;

    public ChatObject(String name, String body, Date time){
        this.name = name;
        this.body = body;
        this.time = time;
    }

//    public ChatObject(String name, String body, String time){
//            this(name,body, new Date());
//            try {
//                DateFormat formatter;
//                formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.ENGLISH);
//                this.time = formatter.parse(time);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//    }

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

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }
}
