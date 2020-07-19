package com.streetlity.client;

public class Review {
    public int id;
    public String username;
    public String reviewBody;
    public float rating;

    public Review(String username, String reviewBody, float rating) {
        this.username = username;
        this.reviewBody = reviewBody;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getReviewBody() {
        return reviewBody;
    }

    public float getRating() {
        return rating;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
