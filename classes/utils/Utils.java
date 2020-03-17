package com.kd8bny.maintenanceman.classes.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import com.kd8bny.maintenanceman.R;

import org.joda.time.DateTime;

/**
 * Created by kd8bny on 10/14/16.
 */

public class Utils {
    private final String TAG = "utils";

    private Context mContext;

    public Utils(Context context){
        mContext = context;
    }

    public String toFriendlyDate(DateTime dateTime){
        TypedArray months = mContext.getResources().obtainTypedArray(R.array.spec_month);
        String date = String.format("%s %s %s", dateTime.getDayOfMonth(),
                months.getString(dateTime.getMonthOfYear()-1), dateTime.getYear());
        months.recycle();

        return date;
    }

    public String toFriendlyTime(DateTime dateTime) {
        int hour = dateTime.getHourOfDay();
        int min = dateTime.getMinuteOfHour();
        String MIN = (min < 10) ? "0" + min : min + "";
        String xM = (hour < 12) ? "am" : "pm";
        hour = (hour > 12) ? hour % 12 : hour;

        return String.format("%s:%s %s", hour, MIN, xM);
    }
}
