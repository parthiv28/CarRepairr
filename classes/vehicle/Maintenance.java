package com.kd8bny.maintenanceman.classes.vehicle;

/**
 * Created by kd8bny on 2/13/16.
 */
public class Maintenance extends Entry{
    private static final String TAG = "Maintenance";

    public int mIcon = 0;
    public String mEvent;
    public String mOdometer;
    public String mPrice = "";
    public String mComment = "";

    public Maintenance(String refID){
        mRefID = refID;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getEvent() {
        return mEvent;
    }

    public void setEvent(String event) {
        mEvent = event;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public String getOdometer() {
        return mOdometer;
    }

    public void setOdometer(String odometer) {
        mOdometer = odometer;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public String getRefID() {
        return mRefID;
    }

    public void setRefID(String refID) {
        mRefID = refID;
    }
}
