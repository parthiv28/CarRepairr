package com.kd8bny.maintenanceman.classes.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.kd8bny.maintenanceman.classes.vehicle.Mileage;
import com.kd8bny.maintenanceman.classes.vehicle.Travel;
import com.kd8bny.maintenanceman.classes.vehicle.Maintenance;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;

public class VehicleLogDBHelper extends SQLiteOpenHelper{
    private static final String TAG = "vehicleLogDB";

    private static VehicleLogDBHelper sInstance;

    private static final int DB_VERSION = 6; //v5 (83) v4 (75) v3(58) v2(50)
    private static final String DB_NAME = "vehicleLog.db";

    private static final String TABLE_VEHICLE = "grandVehicleLog";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_VEHICLE_REFID = "refID";
    private static final String COLUMN_VEHICLE_DATE = "date";
    private static final String COLUMN_VEHICLE_ODO = "odo";
    private static final String COLUMN_VEHICLE_EVENT = "event";
    private static final String COLUMN_VEHICLE_PRICE = "price";
    private static final String COLUMN_VEHICLE_COMMENT = "comment";
    private static final String COLUMN_ICON = "icon";

    private static final String TABLE_TRAVEL = "travelLog";
    private static final String COLUMN_START = "start";
    private static final String COLUMN_STOP = "end";
    private static final String COLUMN_DEST = "dest";
    private static final String COLUMN_PURPOSE = "purpose";
    private static final String COLUMN_VEHICLE_DATE_END = "dateEnd";

    private static final String TABLE_MILEAGE = "mileageLog";
    private static final String COLUMN_MILEAGE = "mileage";
    private static final String COLUMN_TRIP = "tripometer";
    private static final String COLUMN_FILL_VOL = "fillVol";

    public static synchronized VehicleLogDBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VehicleLogDBHelper(context);
        }

        return sInstance;
    }

    public VehicleLogDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL,"
                        + " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL DEFAULT '0');",
                TABLE_VEHICLE, COLUMN_ID, COLUMN_VEHICLE_REFID,
                COLUMN_VEHICLE_DATE, COLUMN_VEHICLE_ODO, COLUMN_VEHICLE_EVENT, COLUMN_VEHICLE_PRICE, COLUMN_VEHICLE_COMMENT, COLUMN_ICON);

        String CREATE_BUSINESS = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL,"
                        + " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL);",
                TABLE_TRAVEL, COLUMN_ID, COLUMN_VEHICLE_REFID,
                COLUMN_VEHICLE_DATE, COLUMN_START, COLUMN_STOP, COLUMN_DEST, COLUMN_PURPOSE, COLUMN_VEHICLE_DATE_END);

        String CREATE_MILEAGE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL,"
                        + " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL);",
                TABLE_MILEAGE, COLUMN_ID, COLUMN_VEHICLE_REFID,
                COLUMN_VEHICLE_DATE, COLUMN_MILEAGE, COLUMN_TRIP, COLUMN_FILL_VOL, COLUMN_VEHICLE_PRICE);

        try {
            db.beginTransaction();
            db.execSQL(CREATE);
            db.execSQL(CREATE_BUSINESS);
            db.execSQL(CREATE_MILEAGE);
            db.setTransactionSuccessful();
        } catch (Exception e){
            Log.e(TAG, "Error onCreate");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * vehicle log
     */
    public void insertEntry(Maintenance maintenance) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_VEHICLE_REFID, maintenance.getRefID());
        contentValues.put(COLUMN_VEHICLE_DATE, maintenance.getDate());
        contentValues.put(COLUMN_VEHICLE_ODO, maintenance.getOdometer());
        contentValues.put(COLUMN_VEHICLE_EVENT, maintenance.getEvent());
        contentValues.put(COLUMN_VEHICLE_PRICE, maintenance.getPrice());
        contentValues.put(COLUMN_VEHICLE_COMMENT, maintenance.getComment());
        contentValues.put(COLUMN_ICON, maintenance.getIcon());

        try {
            db.beginTransaction();
            db.insertOrThrow(TABLE_VEHICLE, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Error on insert entry");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Maintenance> getMaintenanceEntries(String refID, Boolean sortDesc) {
        SQLiteDatabase db = getReadableDatabase();
        String sort = (sortDesc) ? "DESC" : "ASC";
        String QUERY = String.format("SELECT * FROM %s WHERE %s = '%s' ORDER BY %s %s;", TABLE_VEHICLE, COLUMN_VEHICLE_REFID, refID, COLUMN_VEHICLE_DATE, sort);
        Cursor cursor = db.rawQuery(QUERY, null);

        ArrayList<Maintenance> maintenanceList = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Maintenance maintenance = new Maintenance(refID);
                    if (cursor.getString(cursor.getColumnIndex(COLUMN_ICON)).isEmpty()){
                        maintenance.setIcon(0);
                    }else{
                        maintenance.setIcon(Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ICON))));
                    }
                    maintenance.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    maintenance.setOdometer(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_ODO)));
                    maintenance.setEvent(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_EVENT)));
                    maintenance.setPrice(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE)));
                    maintenance.setComment(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_COMMENT)));

                    maintenanceList.add(maintenance);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return maintenanceList;
    }

    public HashSet<String> getEntries() {
        SQLiteDatabase db = getReadableDatabase();
        String QUERY = String.format("SELECT %s FROM %s;", COLUMN_VEHICLE_EVENT, TABLE_VEHICLE);
        Cursor cursor = db.rawQuery(QUERY, null);

        HashSet<String> entryList = new HashSet<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    entryList.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_EVENT)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return entryList;
    }

    public ArrayList<Maintenance> getCostByYear(String refID, int year) {
        SQLiteDatabase db = getReadableDatabase();
        String wildcard = year + "%"; //SQL wildcard %
        String QUERY = String.format("SELECT %s, %s FROM %s WHERE %s = '%s' AND %s LIKE '%s';",
                COLUMN_VEHICLE_DATE, COLUMN_VEHICLE_PRICE, TABLE_VEHICLE,
                COLUMN_VEHICLE_REFID, refID, COLUMN_VEHICLE_DATE, wildcard);
        Cursor cursor = db.rawQuery(QUERY, null);

        ArrayList<Maintenance> maintenanceList = new ArrayList<>();

        try {
            if (cursor.moveToFirst()) {
                do {
                    Maintenance maintenance = new Maintenance(refID);
                    maintenance.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    maintenance.setPrice(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE)));

                    maintenanceList.add(maintenance);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return maintenanceList;
    }

    public void deleteEntry(Maintenance maintenance){
        SQLiteDatabase db = getWritableDatabase();
        String QUERY = String.format("DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s';",
                TABLE_VEHICLE,
                COLUMN_VEHICLE_REFID, maintenance.getRefID(),
                COLUMN_VEHICLE_DATE, maintenance.getDate(),
                COLUMN_VEHICLE_EVENT, maintenance.getEvent());
        try {
            db.beginTransaction();
            db.execSQL(QUERY);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
    * Trip Log
     */
    public void insertEntry(Travel travel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_VEHICLE_REFID, travel.getRefID());
        contentValues.put(COLUMN_VEHICLE_DATE, travel.getDate());
        contentValues.put(COLUMN_START, travel.getStart());
        contentValues.put(COLUMN_STOP, travel.getStop());
        contentValues.put(COLUMN_DEST, travel.getDest());
        contentValues.put(COLUMN_PURPOSE, travel.getPurpose());
        contentValues.put(COLUMN_VEHICLE_DATE_END, travel.getDateEnd());

        try {
            db.beginTransaction();
            db.insertOrThrow(TABLE_TRAVEL, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Error on insert entry");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Travel> getFullTravelEntries(String refID, Boolean sortDesc) {
        SQLiteDatabase db = getReadableDatabase();
        String sort = (sortDesc) ? "DESC" : "ASC";
        String QUERY = String.format("SELECT * FROM %s WHERE %s = '%s' ORDER BY %s %s;", TABLE_TRAVEL, COLUMN_VEHICLE_REFID, refID, COLUMN_VEHICLE_DATE, sort);
        Cursor cursor = db.rawQuery(QUERY, null);

        ArrayList<Travel> travelList = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Travel travel = new Travel(refID);
                    travel.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    travel.setStart(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_START))));
                    travel.setStop(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_STOP))));
                    travel.setDest(cursor.getString(cursor.getColumnIndex(COLUMN_DEST)));
                    travel.setPurpose(cursor.getString(cursor.getColumnIndex(COLUMN_PURPOSE)));
                    travel.setDateEnd(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE_END)));

                    travelList.add(travel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return travelList;
    }

    public void deleteEntry(Travel travel){
        SQLiteDatabase db = getWritableDatabase();
        String QUERY = String.format("DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s = '%s';",
                TABLE_TRAVEL,
                COLUMN_VEHICLE_REFID, travel.getRefID(),
                COLUMN_VEHICLE_DATE, travel.getDate(),
                COLUMN_START, travel.getStart());
        try {
            db.beginTransaction();
            db.execSQL(QUERY);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Mileage logs
     * @param mileage
     */

    public void insertEntry(Mileage mileage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_VEHICLE_REFID, mileage.getRefID());
        contentValues.put(COLUMN_VEHICLE_DATE, mileage.getDate());
        contentValues.put(COLUMN_MILEAGE, mileage.getMileage());
        contentValues.put(COLUMN_TRIP, mileage.getTripometer());
        contentValues.put(COLUMN_FILL_VOL, mileage.getFillVol());
        contentValues.put(COLUMN_VEHICLE_PRICE, mileage.getPrice());

        try {
            db.beginTransaction();
            db.insertOrThrow(TABLE_MILEAGE, null, contentValues);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Error on insert entry");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Mileage> getMileageEntries(String refID, Boolean sortDesc) {
        SQLiteDatabase db = getReadableDatabase();
        String sort = (sortDesc) ? "DESC" : "ASC";
        String QUERY = String.format("SELECT * FROM %s WHERE %s = '%s' ORDER BY %s %s;", TABLE_MILEAGE, COLUMN_VEHICLE_REFID, refID, COLUMN_VEHICLE_DATE, sort);
        Cursor cursor = db.rawQuery(QUERY, null);

        ArrayList<Mileage> mileageList = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Mileage mileage = new Mileage(refID);
                    mileage.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    mileage.setMileage(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_TRIP))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_FILL_VOL))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE))));

                    mileageList.add(mileage);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return mileageList;
    }

    public ArrayList<Mileage> getMileageEntriesByYear(String refID, int year) {
        SQLiteDatabase db = getReadableDatabase();
        String YEAR = year + "%"; //SQL wildcard %
        String QUERY = String.format("SELECT * FROM %s WHERE %s = '%s' AND %s LIKE '%s';",
                TABLE_MILEAGE, COLUMN_VEHICLE_REFID, refID, COLUMN_VEHICLE_DATE, YEAR);
        Cursor cursor = db.rawQuery(QUERY, null);

        ArrayList<Mileage> mileageList = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    Mileage mileage = new Mileage(refID);
                    mileage.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    mileage.setMileage(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_TRIP))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_FILL_VOL))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE))));

                    mileageList.add(mileage);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        return mileageList;
    }

    public void deleteEntry(Mileage mileage){
        SQLiteDatabase db = getWritableDatabase();
        String QUERY = String.format("DELETE FROM %s WHERE %s = '%s' AND %s = '%s' AND %s LIKE '%s';",
                TABLE_MILEAGE,
                COLUMN_VEHICLE_REFID, mileage.getRefID(),
                COLUMN_VEHICLE_DATE, mileage.getDate(),
                COLUMN_FILL_VOL, mileage.getFillVol().toString());
        try {
            db.beginTransaction();
            db.execSQL(QUERY);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * For all tables
     * @param refID
     */
    public void purgeVehicle(String refID){
        SQLiteDatabase db = getWritableDatabase();
        String QUERY = String.format("DELETE FROM %s WHERE %s = '%s';",
                TABLE_VEHICLE,
                COLUMN_VEHICLE_REFID, refID);
        String QUERY_TRAVEL = String.format("DELETE FROM %s WHERE %s = '%s';",
                TABLE_TRAVEL,
                COLUMN_VEHICLE_REFID, refID);
        String QUERY_MILEAGE = String.format("DELETE FROM %s WHERE %s = '%s';",
                TABLE_MILEAGE,
                COLUMN_VEHICLE_REFID, refID);
        try {
            db.beginTransaction();
            db.execSQL(QUERY);
            db.execSQL(QUERY_TRAVEL);
            db.execSQL(QUERY_MILEAGE);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
            db.close();
        }
    }

    public String getTablesHash(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> allData = new ArrayList<>();

        String QUERY = String.format("SELECT * FROM %s;", TABLE_VEHICLE);
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    cursor.getString(cursor.getColumnIndex(COLUMN_ICON));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_ODO)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_EVENT)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_COMMENT)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        QUERY = String.format("SELECT * FROM %s;", TABLE_TRAVEL);
        cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_START)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_STOP)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_DEST)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_PURPOSE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE_END)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        QUERY = String.format("SELECT * FROM %s;", TABLE_MILEAGE);
        cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_MILEAGE)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_TRIP)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_FILL_VOL)));
                    allData.add(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_PRICE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
                db.close();
            }
        }

        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(allData.toString(), Charsets.UTF_8).hash();

        return hc.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.w(VehicleLogDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.beginTransaction();
        try {
            if (oldVersion < 3) {
                Log.v(TAG, "icon");
                String TABLE_VEHICLE_NEW = TABLE_VEHICLE + "_new";
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VEHICLE_NEW + " AS SELECT * FROM " + TABLE_VEHICLE);
                db.execSQL("ALTER TABLE " + TABLE_VEHICLE_NEW + " ADD COLUMN " + COLUMN_ICON + " text not null default '0'");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
                db.execSQL("ALTER TABLE " + TABLE_VEHICLE_NEW + " RENAME TO " + TABLE_VEHICLE);
                Log.v(TAG, "icondone");
            }
            if (oldVersion < 4){
                Log.v(TAG, "bus");
                String CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL,"
                        + " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL);",
                        TABLE_TRAVEL, COLUMN_ID, COLUMN_VEHICLE_REFID,
                        COLUMN_VEHICLE_DATE, COLUMN_START, COLUMN_STOP, COLUMN_DEST, COLUMN_PURPOSE);
                db.execSQL(CREATE);
                Log.v(TAG, "bus done");
            }
            if (oldVersion < 5){
                Log.v(TAG, "mileage");
                String CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL,"
                                + " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL);",
                        TABLE_MILEAGE, COLUMN_ID, COLUMN_VEHICLE_REFID,
                        COLUMN_VEHICLE_DATE, COLUMN_MILEAGE, COLUMN_TRIP, COLUMN_FILL_VOL, COLUMN_VEHICLE_PRICE);
                db.execSQL(CREATE);
                Log.v(TAG, "mileage done");
            }
            if (oldVersion < 6){
                Log.v(TAG, "switch to iso date");
                ArrayList<String> tables = new ArrayList<>();
                tables.add(TABLE_VEHICLE);
                tables.add(TABLE_MILEAGE);
                tables.add(TABLE_TRAVEL);

                for (String table:tables) {
                    Log.v("updating", "dates in " + table);
                    String QUERY = String.format("SELECT * FROM %s;", table);
                    Cursor cursor = db.rawQuery(QUERY, null);

                    try {
                        if (cursor.moveToFirst()) {
                            do {
                                String oldDate = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_DATE));
                                String[] dateArr = oldDate.split("/");

                                DateTime dateTime = new DateTime();
                                dateTime = dateTime.withTimeAtStartOfDay().withDate(
                                        Integer.parseInt(dateArr[2]), Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]));

                                String QUERY_UPDATE = String.format("UPDATE %s SET %s='%s' WHERE %s = '%s';", table,
                                        COLUMN_VEHICLE_DATE, dateTime.toString(),
                                        COLUMN_VEHICLE_DATE, oldDate);
                                db.execSQL(QUERY_UPDATE);
                            } while (cursor.moveToNext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                }
                Log.v(TAG, "iso date done");

                Log.v(TAG, "add end time to travel");
                String TABLE_TRAVEL_NEW = TABLE_TRAVEL + "_new";
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRAVEL_NEW + " AS SELECT * FROM " + TABLE_TRAVEL);
                db.execSQL("ALTER TABLE " + TABLE_TRAVEL_NEW + " ADD COLUMN " + COLUMN_VEHICLE_DATE_END + " text not null default ''");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAVEL);
                db.execSQL("ALTER TABLE " + TABLE_TRAVEL_NEW + " RENAME TO " + TABLE_TRAVEL);
                Log.v(TAG, "end time added");
                
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Error updating db");
        } finally {
            Log.w(TAG, "Upgrade completed");
            db.endTransaction();
        }
    }
}
