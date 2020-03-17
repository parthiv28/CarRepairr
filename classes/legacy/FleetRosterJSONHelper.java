package com.kd8bny.maintenanceman.classes.legacy;

import android.content.Context;
import android.util.Log;

import com.kd8bny.maintenanceman.classes.vehicle.Vehicle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/*
This file is being discontinued when users are brought up to new spec
 */

public class FleetRosterJSONHelper {
    private static final String TAG = "fltRstrJSONHlpr";

    public static final String JSON_NAME = "fleetRoster.json";

    private Context mContext;

    public FleetRosterJSONHelper(){

    }

    public String openJSON(Context context) {
        File file = new File(context.getFilesDir() + "/" + JSON_NAME);

        if (file.isFile() && file.canRead()) {
            try {
                BufferedReader buffReader = new BufferedReader(new FileReader(file));

                String json;
                StringBuffer stringBuffer = new StringBuffer();

                while ((json = buffReader.readLine()) != null) {
                    stringBuffer.append(json);
                }

                json = stringBuffer.toString();

                return json;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void writeJSON(Context context, JSONObject object){
        try {
            File file = new File(context.getFilesDir() + "/" + JSON_NAME);
            FileWriter fileWriter = new FileWriter(file);

            fileWriter.write(object.toString());
            fileWriter.flush();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveEntry(Context context, String refID, ArrayList<ArrayList> vehicleDataAll) {

        String json = openJSON(context);

        if (refID == null) {
            refID = UUID.randomUUID().toString();
        }

        JSONObject vehicle = new JSONObject();
        JSONObject gen = new JSONObject();
        JSONObject eng = new JSONObject();
        JSONObject pwr = new JSONObject();
        JSONObject other = new JSONObject();

        try{
            for (int i = 0; i < vehicleDataAll.size(); i++){
                ArrayList<String> tempData = new ArrayList<String>(vehicleDataAll.get(i));
                switch (tempData.get(0)){
                    case ("General"):
                        gen.put(tempData.get(1), tempData.get(2));
                        break;

                    case ("Engine"):
                        eng.put(tempData.get(1), tempData.get(2));
                        break;

                    case ("Power Train"):
                        pwr.put(tempData.get(1), tempData.get(2));
                        break;

                    case ("Other"):
                        other.put(tempData.get(1), tempData.get(2));
                        break;

                    default:
                        Log.wtf(TAG, "NOT GOOD: category is missing");
                }
            }

            vehicle.put("General", gen);
            vehicle.put("Engine", eng);
            vehicle.put("Power Train", pwr);
            vehicle.put("Other", other);

            if(json != null) {
                JSONObject roster = new JSONObject(json);
                roster.put(refID, vehicle);

                writeJSON(context, roster);

            }else{
                JSONObject roster = new JSONObject();
                roster.put(refID, vehicle);

                writeJSON(context, roster);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public HashMap<String, HashMap> getEntries(Context context){
        mContext = context;
        String json = openJSON(context);

        HashMap<String, HashMap> vehicleDataAll = new HashMap<>();

        try {
            if (json != null) {
                JSONObject roster = new JSONObject(json);
                JSONArray vehicles = roster.names();

                for (int i = 0; i < vehicles.length(); i++) {
                    JSONObject vehicle = roster.getJSONObject(vehicles.getString(i));
                    JSONArray categories = vehicle.names();
                    LinkedHashMap<String, HashMap> vehicleData = new LinkedHashMap<>();
                    for (int j = 0; j < categories.length(); j++) {
                        JSONObject category = vehicle.getJSONObject(categories.getString(j));
                        JSONArray specs = category.names();

                        HashMap<String, String> tempSpecMap = new HashMap<>();

                        if(specs != null) {
                            for (int k = 0; k < specs.length(); k++) {
                                tempSpecMap.put(specs.getString(k), category.getString(specs.getString(k)));
                            }
                            switch (categories.getString(j)) {
                                case "General":
                                    vehicleData.put("General", tempSpecMap);
                                    break;

                                case "Engine":
                                    vehicleData.put("Engine", tempSpecMap);
                                    break;

                                case "Power Train":
                                    vehicleData.put("Power Train", tempSpecMap);
                                    break;

                                case "Other":
                                    vehicleData.put("Other", tempSpecMap);
                                    break;
                            }
                            vehicleDataAll.put(vehicles.getString(i), vehicleData);
                        }
                    }
                }
            }else{
                vehicleDataAll.put(null, null);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        return vehicleDataAll;
    }

    public void deleteEntry(Context context, String refID){
        String json = openJSON(context);

        try {
            JSONObject roster = new JSONObject(json);
            roster.remove(refID);

            writeJSON(context, roster);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public ArrayList<Vehicle> saveToNew(HashMap<String, HashMap> oldRoster){
        Log.i(TAG, "Attempting to capture old data and store new format");
        ArrayList<Vehicle> newRoster = new ArrayList<>();
        for (String key : oldRoster.keySet()) {
            HashMap<String, HashMap> oldVehicle = oldRoster.get(key);
            HashMap<String, String> genTemp = oldVehicle.get("General");
            String type = genTemp.get("type");
            String year = genTemp.get("Year");
            String make = genTemp.get("Make");
            String model = genTemp.get("Model");
            Vehicle tempVehicle = new Vehicle(key, type, year, make, model);

            genTemp.remove("type");
            genTemp.remove("Year");
            genTemp.remove("Make");
            genTemp.remove("Model");

            tempVehicle.setGeneralSpecs(genTemp);
            if (oldVehicle.containsKey("Engine")){
                tempVehicle.setEngineSpecs(oldVehicle.get("Engine"));
            }else{
                tempVehicle.setEngineSpecs(new HashMap<String, String>());
            }
            if (oldVehicle.containsKey("Power Train")){
                tempVehicle.setPowerTrainSpecs(oldVehicle.get("Power Train"));
            }else{
                tempVehicle.setPowerTrainSpecs(new HashMap<String, String>());
            }
            if (oldVehicle.containsKey("Other")){
                tempVehicle.setOtherSpecs(oldVehicle.get("Other"));
            }else{
                tempVehicle.setOtherSpecs(new HashMap<String, String>());
            }

            newRoster.add(tempVehicle);
        }
        return newRoster;
    }
}

