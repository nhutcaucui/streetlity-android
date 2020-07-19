package com.streetlity.client.MainFragment;

import java.io.Serializable;

public class BankObject implements Serializable {

    int id;
    String name;

    public BankObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
