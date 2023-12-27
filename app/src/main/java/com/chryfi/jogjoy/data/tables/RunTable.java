package com.chryfi.jogjoy.data.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.chryfi.jogjoy.data.GPSPoint;
import com.chryfi.jogjoy.data.Run;

import java.util.Optional;

public class RunTable extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "run";
    private static final int DB_VERSION = 1;
    private static final String ID_COL = "id";
    private static final String GOAL_COL = "goal";
    private static final String USERNAME_COL = "username";

    private final Context context;

    public RunTable(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GOAL_COL + " REAL, "
                + USERNAME_COL + " TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean deleteRun(long runid, String username) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(TABLE_NAME, USERNAME_COL + "=? AND " + ID_COL + "=?",
                    new String[]{username, String.valueOf(runid)}) != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts the run. See {@link GPSTable} to insert the points.
     * @param run
     * @return true if the insertion was successful.
     */
    public boolean insertRun(Run run) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();

            values.put(GOAL_COL, run.getGoal());
            values.put(USERNAME_COL, run.getUsername());

            long id = db.insert(TABLE_NAME, null, values);

            if (id != -1) {
                run.setId(id);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public Optional<Run> getRunById(long id) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             GPSTable gpsTable = new GPSTable(this.context)) {
            Cursor cursor = db.query(TABLE_NAME, null,
                    ID_COL + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor == null || !cursor.moveToNext()) return Optional.empty();

            float goal = cursor.getFloat(cursor.getColumnIndexOrThrow(GOAL_COL));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL));

            return Optional.of(new Run(id, goal, username, gpsTable.getGPSPoints(id)));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
