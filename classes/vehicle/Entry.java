package com.kd8bny.maintenanceman.classes.vehicle;

import java.io.Serializable;

/**
 * Created by kd8bny on 2/13/16.
 */
public class Entry implements Serializable{
    private static final String TAG = "Entry";

    public String mRefID;
    public String mDate;

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getRefID() {
        return mRefID;
    }

    public void setRefID(String refID) {
        mRefID = refID;
    }
}
