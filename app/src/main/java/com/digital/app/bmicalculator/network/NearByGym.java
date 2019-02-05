package com.digital.app.bmicalculator.network;

public class NearByGym {

    String name;
    String address;
    double lat;
    double lon;

    public NearByGym(String name, String address, double lat, double lon) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
