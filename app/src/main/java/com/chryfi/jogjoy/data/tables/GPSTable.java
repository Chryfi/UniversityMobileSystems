package com.chryfi.jogjoy.data.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.chryfi.jogjoy.data.GPSPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GPSTable extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "gps";
    private static final int DB_VERSION = 1;
    private static final String RUNID_COL = "run_id";
    private static final String TIMESTAMP_COL = "timestamp";
    private static final String LONGITUDE_COL = "longitude";
    private static final String LATITUDE_COL = "latitude";

    public GPSTable(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + RUNID_COL + " INTEGER PRIMARY KEY, "
                + TIMESTAMP_COL + " INTEGER PRIMARY KEY, "
                + LONGITUDE_COL + " REAL, "
                + LATITUDE_COL + " REAL)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertGPSPoint(GPSPoint gpsPoint) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();

            values.put(RUNID_COL, gpsPoint.getRunid());
            values.put(TIMESTAMP_COL, gpsPoint.getTimestamp());
            values.put(LONGITUDE_COL, gpsPoint.getLongitude());
            values.put(LATITUDE_COL, gpsPoint.getLatitude());

            return db.insert(TABLE_NAME, null, values) != -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     *
     * @param runid
     * @param timestamp
     * @return
     */
    public boolean deleteGPSPoint(long runid, long timestamp) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(TABLE_NAME, RUNID_COL + "=? AND " + TIMESTAMP_COL + "=?",
                    new String[]{String.valueOf(runid), String.valueOf(timestamp)}) != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param runid
     * @param timestamp
     * @return returns the GPS point with the given run id and timestamp.
     */
    public Optional<GPSPoint> getGPSPoint(long runid, long timestamp) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor = db.query(TABLE_NAME, null,
                    RUNID_COL + "=? AND " + TIMESTAMP_COL + "=?", new String[]{String.valueOf(runid), String.valueOf(timestamp)},
                    null, null, null);

            if (cursor == null || !cursor.moveToNext()) return Optional.empty();

            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL));

            cursor.close();

            return Optional.of(new GPSPoint(runid, timestamp, longitude, latitude));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * @param runid
     * @return returns a list of gps points in ascending order sorted by timestamp.
     * Returns an empty list if nothing was found.
     */
    public List<GPSPoint> getGPSPoints(long runid) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor = db.query(TABLE_NAME, null,
                    RUNID_COL + "=?", new String[]{String.valueOf(runid)},
                    null, null, TIMESTAMP_COL + " ASC");

            List<GPSPoint> points = new ArrayList<>();
            if (cursor == null) return points;

            while (cursor.moveToNext()) {
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(TIMESTAMP_COL));

                points.add(new GPSPoint(runid, timestamp, longitude, latitude));
            }

            cursor.close();

            return points;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
