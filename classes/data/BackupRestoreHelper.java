package com.kd8bny.maintenanceman.classes.data;

import android.content.Context;
import android.util.Log;

import com.kd8bny.maintenanceman.R;
import com.kd8bny.maintenanceman.interfaces.SyncData;

public class BackupRestoreHelper {
    private static final String TAG = "bckp_rstr_hlpr";

    private final String SHARED_PREF = "com.kd8bny.maintenanceman_preferences";
    private SyncData mSyncData;
    private DropboxHelper mDropboxHelper;

    public BackupRestoreHelper(){}

    public void startAction(Context context, SyncData syncData){
        String cloudDefault = context.getString(R.string.pref_cloud_default);
        mSyncData = syncData;
        String cloudExists = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF, 0).getString(cloudDefault, "");

        if (!cloudExists.isEmpty()) {
            switch (cloudDefault) {
                case "dropbox":
                    mDropboxHelper = new DropboxHelper(context);
                    mDropboxHelper.listener = mSyncData;
                    mDropboxHelper.execute();

                    break;

                case "gdrive":

                    break;

                default:

                    break;
            }
        }else{
            Log.i(TAG, "Cloud source not set up");
        }
    }
}
