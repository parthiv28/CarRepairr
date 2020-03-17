package com.kd8bny.maintenanceman.classes.vehicle;

public class Mileage extends Entry {
    private static final String TAG = "Mileage";

    private Double mMileage;
    private Double mPrice;
    private Double mTripometer;
    private Double mFillVol;

    public Mileage(String refID) {
        mRefID = refID;
    }

    public void setMileage(Double tripometer, Double fillVol, Double price) {
        mTripometer = tripometer;
        mFillVol = fillVol;
        mPrice = price;
        mMileage = tripometer / fillVol;
    }

    public Double getMileage() {
        return mMileage;
    }

    public Double getPrice() {
        return mPrice;
    }

    public Double getFillVol() {
        return mFillVol;
    }

    public Double getTripometer() {
        return mTripometer;
    }
}
