package com.amenity.amenity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by vvvro on 2/17/2017.
 */

public class AmenityMarker {
    public String pid;
    public double latitude;
    public double longitude;
    public ArrayList<String> resources;
    public ArrayList<Integer> need;
    public ArrayList<Integer> recv;
    public String message;
    public String userName;
    public String uid;
    public Date date;
    public String phone;
    public Boolean forMe;
    public AmenityMarker(){

    }
    public AmenityMarker(double lat, double lon, ArrayList<String> res, ArrayList<Integer> need, ArrayList<Integer> recv, String mes, Date dat, String name, String uid, String number, Boolean forMe){
        latitude = lat;
        longitude = lon;
        resources = res;
        this.need = need;
        this.recv = recv;
        message = mes;
        userName = name;
        date = dat;
        phone = number;
        this.uid = uid;
        this.forMe = forMe;
    }
}
