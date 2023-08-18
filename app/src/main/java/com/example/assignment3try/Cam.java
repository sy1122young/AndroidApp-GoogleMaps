package com.example.assignment3try;

public class Cam {
    private String id;
    private double lat;
    private double lon;

    public Cam(String id, double lat, double lon){
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
    public String getid(){
        return id;
    }
    public double getlat(){
        return lat;
    }
    public double getlon(){
        return lon;
    }
}
