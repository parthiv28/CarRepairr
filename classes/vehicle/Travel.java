package com.kd8bny.maintenanceman.classes.vehicle;

public class Travel extends Entry {
    private static final String TAG = "Business";

    private Double mStart;
    private Double mStop = -1.0;
    private Double mDelta;
    private String mDest;
    private String mPurpose = "";
    private String mDateEnd = "";

    public Travel(String refID) {
        mRefID = refID;
    }

    public Double getStart() {
        return mStart;
    }

    public void setStart(Double start) {
        mStart = start;
    }

    public Double getStop() {
        return mStop;
    }

    public void setStop(Double stop) {
        mStop = stop;
        mDelta = mStop - mStart;
    }

    public Double getDelta() {
        return mDelta;
    }

    public String getDest() {
        return mDest;
    }

    public void setDest(String price) {
        mDest = price;
    }

    public String getPurpose() {
        return mPurpose;
    }

    public void setPurpose(String comment) {
        mPurpose = comment;
    }

    public String getDateEnd() {
        return mDateEnd;
    }

    public void setDateEnd(String s) {
        mDateEnd = s;
    }
}
