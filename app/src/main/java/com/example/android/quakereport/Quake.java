package com.example.android.quakereport;

import android.widget.ArrayAdapter;

/**
 * Created by jayson surface on 11/02/2017.
 */

public class Quake {

    private double mMag;

    private String mLocation;

    private String mDate;

    private String mUrl;

    public Quake(double mag, String location, String date, String url){

        mMag = mag;
        mLocation = location;
        mDate = date;
        mUrl = url;
    }

    public double getmMag(){
        return mMag;
    }

    public String getmLocation(){
        return mLocation;
    }

    public String getmDate(){
        return mDate;
    }

    public String getmUrl(){
        return mUrl;
    }

}
