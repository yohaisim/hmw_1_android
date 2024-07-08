package com.example.yohai_simhony_hw1;

import com.google.android.gms.maps.model.LatLng;

public class HighScore {
    private final int score;
    private final String location;
    private final String date;
    private final LatLng latLng;

    public HighScore(int score, String location, String date, LatLng latLng) {
        this.score = score;
        this.location = location;
        this.date = date;
        this.latLng = latLng;
    }

    public int getScore() {
        return score;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
