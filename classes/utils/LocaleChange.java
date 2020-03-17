package com.kd8bny.maintenanceman.classes.utils;

import android.content.Context;

import com.kd8bny.maintenanceman.R;
import com.kd8bny.maintenanceman.classes.data.SaveLoadHelper;
import com.kd8bny.maintenanceman.classes.vehicle.Vehicle;

import java.util.ArrayList;

/**
 * Created by kd8bny on 9/23/16.
 */

public class LocaleChange {
    private static final String TAG = "localechange";

    private Context mContext;

    public LocaleChange(Context context){
        mContext = context;
    }

    public void changeUnits(String unit){
        Boolean isUS = true;
        Boolean useDist = true;
        if (!unit.equals("mi")) {
            isUS = false;
        }

        SaveLoadHelper saveLoadHelper = new SaveLoadHelper(mContext, null);
        ArrayList<Vehicle> temp = saveLoadHelper.load();
        for (int i=0; i< temp.size(); i++) {
            Vehicle v = temp.get(i);
            String type = v.getVehicleType();

            if (type.equals("Utility") || type.equals("Marine") || type.equals("Lawn and Garden")){
                useDist = false;
            }

            String unitDist;
            String unitMileage;
            if (useDist) {
                if (isUS) {
                    unitDist = mContext.getResources().getString(R.string.unit_dist_us);
                    unitMileage = mContext.getResources().getString(R.string.unit_mileage_us);
                }else{
                    unitDist = mContext.getResources().getString(R.string.unit_dist_metric);
                    unitMileage = mContext.getResources().getString(R.string.unit_mileage_metric);
                }
            }else{
                if (isUS) {
                    unitMileage = mContext.getResources().getString(R.string.unit_mileage_time_us);
                }else{
                    unitMileage = mContext.getResources().getString(R.string.unit_mileage_time_metric);
                }
                unitDist = mContext.getResources().getString(R.string.unit_time);
            }

            v.setUnitDist(unitDist);
            v.setUnitMileage(unitMileage);

            temp.set(i, v);
        }
        saveLoadHelper.save(temp);
    }
}
