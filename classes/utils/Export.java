package com.kd8bny.maintenanceman.classes.utils;

import android.net.Uri;
import android.os.Environment;

import com.kd8bny.maintenanceman.classes.vehicle.Maintenance;
import com.kd8bny.maintenanceman.classes.vehicle.Mileage;
import com.kd8bny.maintenanceman.classes.vehicle.Travel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by kd8bny on 3/20/16.
 */
public class Export {
    private static final String TAG = "utils_export";

    private static  final String DIR = "maintenanceman";
    private File dir;

    public Export(){
        File sdcard = Environment.getExternalStorageDirectory();
        dir = new File(String.format("%s/%s/",
                sdcard.getAbsoluteFile(), DIR));
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Uri maintenanceToCSV(String title, ArrayList<Maintenance> l){
        Calendar cal = Calendar.getInstance();
        String date = String.format("%s_%s_%s",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
        String fileName = String.format("%s_%s_%s.csv", "maintenanceLog", title.replace(" ", "_"), date);

        File file = new File(dir, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (Maintenance m : l) {
                fileWriter.write(String.format("%s,%s,%s,%s,%s,%s\n",
                       m.getRefID(), m.getDate(), m.getOdometer(),
                        m.getEvent(), m.getPrice(), m.getComment()));
            }
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    public Uri mileageToCSV(String title, ArrayList<Mileage> l){
        Calendar cal = Calendar.getInstance();
        String date = String.format("%s_%s_%s",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
        String fileName = String.format("%s_%s_%s.csv", "mileageLog", title.replace(" ", "_"), date);

        File file = new File(dir, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (Mileage m : l) {
                fileWriter.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        m.getRefID(), m.getDate(), m.getMileage().toString(), m.getPrice().toString(),
                        m.getFillVol().toString(), m.getTripometer().toString()));
            }
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    public Uri travelToCSV(String title, ArrayList<Travel> l){
        Calendar cal = Calendar.getInstance();
        String date = String.format("%s_%s_%s",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR));
        String fileName = String.format("%s_%s_%s.csv", "travelLog", title.replace(" ", "_"), date);

        File file = new File(dir, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (Travel m : l) {
                fileWriter.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        m.getRefID(), m.getDate(), m.getStart(), m.getStop(),
                        m.getDest(), m.getPurpose()));
            }
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }
}
