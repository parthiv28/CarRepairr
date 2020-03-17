package com.kd8bny.maintenanceman.classes.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.kd8bny.maintenanceman.R;
import com.kd8bny.maintenanceman.interfaces.SyncData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * v3
 **/

public class DropboxHelper extends AsyncTask<String, Void, String> {
    private static final String TAG = "dbxHlpr";

    public SyncData listener = null;
    private DbxClientV2 mClient;
    private Context mContext;

    private  VehicleLogDBHelper vehicleLogDBHelper;

    private static final int sTimeDifference = 10000; //ms
    private static final String SHARED_PREF = "com.kd8bny.maintenanceman_preferences";
    private static final String FLEETROSTER = "/fleetRoster.json";
    private static final String VEHICLELOG = "/vehicleLog.db";
    private static final String FLEETROSTER_MD5 = "/fleetRoster.md5";
    private static final String VEHICLELOG_MD5 = "/vehicleLog.md5";
    private static final String FLEETROSTER_EMPTY_MD5 = "d751713988987e9331980363e24189ce";
    private Boolean filesUpdated = false;

    public DropboxHelper(Context context) {
        mContext = context;
        String ACCESS_TOKEN = mContext.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                .getString(mContext.getString(R.string.pref_key_dropbox), "");

        DbxRequestConfig config = new DbxRequestConfig("dropbox/maintenanceman", "en_US");
        mClient = new DbxClientV2(config, ACCESS_TOKEN);
    }

    @Override
    protected void onPostExecute(String results){
        vehicleLogDBHelper.close();
        listener.onDownloadComplete(filesUpdated);
    }

    @Override
    protected void onPreExecute(){
    }

    @Override
    protected String doInBackground(String... params){
        try {
            File fleetRoster = new File(mContext.getFilesDir() + FLEETROSTER);
            HashCode hc = Files.hash(fleetRoster, Hashing.md5());

            if (hc.toString().equals(FLEETROSTER_EMPTY_MD5)){
                download(FLEETROSTER, fleetRoster);
            }else{
                download(FLEETROSTER_MD5, new File(mContext.getFilesDir() + FLEETROSTER_MD5));
                sync(fleetRoster, hc.toString(), FLEETROSTER, FLEETROSTER_MD5);
            }
        }catch (IOException e){}

        vehicleLogDBHelper = VehicleLogDBHelper.getInstance(mContext);
        File vehicleLog = new File(vehicleLogDBHelper.getReadableDatabase().getPath());

        String hash = vehicleLogDBHelper.getTablesHash();

        if (FLEETROSTER_EMPTY_MD5.equals(hash)){
            download(VEHICLELOG, vehicleLog);
        }else{
            download(VEHICLELOG_MD5, new File(mContext.getFilesDir() + VEHICLELOG_MD5));
            sync(vehicleLog , hash, VEHICLELOG, VEHICLELOG_MD5);
        }

        return null;
    }

    private void sync(File local, String localHash, String REMOTE, String remoteHash){
        try {
            FileMetadata remoteMeta = (FileMetadata) mClient.files().getMetadata(REMOTE);
            Date remoteDate = remoteMeta.getServerModified();
            Date localDate = new Date(local.lastModified());
            long timeDiff = Math.abs(remoteDate.getTime() - localDate.getTime());

            if (!localHash.equals(readMD5(remoteHash))) {
                if (timeDiff > sTimeDifference) {
                    if (localDate.before(remoteDate)) {
                        listener.onDownloadStart();
                        download(REMOTE, local);
                        filesUpdated = true;
                        Log.v(TAG, "Replacing local " + local.getName() + " " + remoteDate + " >> " + localDate);
                    } else {
                        upload(local, REMOTE);
                        upload(writeMD5(mContext.getFilesDir() + remoteHash, localHash), remoteHash);
                        Log.v(TAG, "Replacing remote " + local.getName() + " " + localDate + " >> " + remoteDate);
                    }
                }
            }
        }catch (DbxException e){
            e.printStackTrace();
        }
    }

    private Boolean upload(File local, String REMOTE){
        /**
         * Returns true if successful
         **/
        try {
            FileInputStream fis = new FileInputStream(local);
            mClient.files().uploadBuilder(REMOTE).withMode(WriteMode.OVERWRITE).uploadAndFinish(fis);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Boolean download(String REMOTE, File local){
       try {
            FileOutputStream fos = new FileOutputStream(local);
            mClient.files().downloadBuilder(REMOTE).download(fos);
        } catch (IOException | DbxException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private File writeMD5(String name, String md5){
        File file = new File(name);
        try {
            FileWriter fileWriter = new FileWriter(file, false); //no append

            fileWriter.write(md5);
            fileWriter.flush();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        return file;
    }

    private String readMD5(String name){
        File file = new File(mContext.getFilesDir() + name);
        String md5 = "";
        if (file.isFile() && file.canRead()) {
            try {
                BufferedReader buffReader = new BufferedReader(new FileReader(file));
                StringBuffer stringBuffer = new StringBuffer();

                while ((md5 = buffReader.readLine()) != null) {
                    stringBuffer.append(md5);
                }
                md5 = stringBuffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return md5;
    }
}