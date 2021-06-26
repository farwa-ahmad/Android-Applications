package com.example.e_cure.Models;

public class CurrentLocationModel {
    private double latitude;
    private double longitude;
    private String locality;
    private String id;

    public CurrentLocationModel(String id, double latitude, double longitude, String locality) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getLocality() {
        return locality;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "CurrentLocation{" +
                "latitude='" + latitude + '\'' +
                ", longitude=" + longitude +
                ", locality=" + locality +
                '}';
    }
}
