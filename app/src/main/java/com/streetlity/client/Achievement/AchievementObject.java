package com.streetlity.client.Achievement;

import java.io.Serializable;

public class AchievementObject implements Serializable {
    private String name;
    private int point;
    private boolean earned;

    public AchievementObject(String name, int point, boolean earned) {
        this.name = name;
        this.point = point;
        this.earned = earned;
    }

    public AchievementObject(String name, int point) {
        this.name = name;
        this.point = point;
        this.earned = false;
    }

    public String getName() {
        return name;
    }

    public int getPoint() {
        return point;
    }

    public boolean isEarned() {
        return earned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setEarned(boolean earned) {
        this.earned = earned;
    }
}
