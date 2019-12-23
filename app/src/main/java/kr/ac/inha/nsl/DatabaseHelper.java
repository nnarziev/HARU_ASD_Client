package kr.ac.inha.nsl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    //region Constants for DB
    private static final String DB_NAME = "EDD-DB";
    private static final String SMARTPHONE_SENSOR_TABLE = "sensor_smartphone";
    private static final String EMA_ANSWERS_TABLE = "ema_answers";
    private static final String COL_1 = "DATA_SRC";
    private static final String COL_2 = "VALUE";
    private static final String COL_3 = "CHECK_DELETE";

    private static final String EMA_COL_1 = "EMA_ORDER";
    private static final String EMA_COL_2 = "TIMESTAMP";
    private static final String EMA_COL_3 = "ANSWERS";
    //endregion

    public DatabaseHelper(Context con) {
        super(con, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + SMARTPHONE_SENSOR_TABLE +
                "(" +
                COL_1 + " TINYINT DEFAULT(0), " +
                COL_2 + " TEXT DEFAULT(0), " +
                COL_3 + " BOOLEAN DEFAULT('false')" +
                ")"
        );
        db.execSQL("create table " + EMA_ANSWERS_TABLE +
                "(" +
                EMA_COL_1 + " TINYINT DEFAULT(0), " +
                EMA_COL_2 + " BIGINT DEFAULT(0), " +
                EMA_COL_3 + " TEXT DEFAULT(-1) " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + SMARTPHONE_SENSOR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EMA_ANSWERS_TABLE);
        onCreate(db);
    }

    //region DB operations with sensor data

    public synchronized void insertSensorData(int data_src, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, data_src);
        contentValues.put(COL_2, value);
        db.insert(SMARTPHONE_SENSOR_TABLE, null, contentValues);
    }

    public synchronized void updateSensorDataForDelete() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, "true");
        db.update(SMARTPHONE_SENSOR_TABLE, contentValues, null, null);
    }

    public List<String[]> getSensorData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + SMARTPHONE_SENSOR_TABLE + " where " + COL_3 + "=?;", new String[]{"true"});


        List<String[]> dataResultList = new ArrayList<>();
        if (res != null && res.moveToFirst()) {
            do {
                String[] data = new String[3];
                data[0] = res.getString(0);
                data[1] = res.getString(1);
                data[2] = res.getString(2);
                dataResultList.add(data);
            } while (res.moveToNext());

            res.close();
        }

        return dataResultList;

    }

    public void deleteSensorData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SMARTPHONE_SENSOR_TABLE, COL_3 + " = ?", new String[]{"true"});
    }

    /*public Cursor getAllSensorData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + SMARTPHONE_SENSOR_TABLE + ";", null);
    }

    public void clearAllSensorData() {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from " + SMARTPHONE_SENSOR_TABLE);
    }*/

    //endregion

    //region DB operations with EMA data
    synchronized boolean insertEMAData(short emaOrder, long timestamp, String answers) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EMA_COL_1, emaOrder);
        contentValues.put(EMA_COL_2, timestamp);
        contentValues.put(EMA_COL_3, answers);

        long res = db.insert(EMA_ANSWERS_TABLE, null, contentValues);
        return res != -1;
    }

    public List<String[]> getEMAData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + EMA_ANSWERS_TABLE, null);

        List<String[]> dataResultList = new ArrayList<>();
        if (res.moveToFirst()) {
            do {
                String[] data = new String[3];
                data[0] = res.getString(0);
                data[1] = res.getString(1);
                data[2] = res.getString(2);
                dataResultList.add(data);
            } while (res.moveToNext());
        }
        res.close();
        return dataResultList;
    }

    public Integer deleteEMAData(long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(EMA_ANSWERS_TABLE, EMA_COL_2 + " = ?", new String[]{String.valueOf(timestamp)});
    }
    //endregion

}
