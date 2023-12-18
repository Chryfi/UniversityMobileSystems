package com.chryfi.jogjoy.data.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.chryfi.jogjoy.data.User;

import java.util.Optional;

public class UserTable extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "user";
    private static final int DB_VERSION = 1;
    private static final String USERNAME_COL = "username";
    private static final String PASSWORD_COL = "password";
    private static final String HEIGHT_COL = "height";
    private static final String WEIGHT_COL = "weight";
    private static final String GENDER_COL = "gender";

    public UserTable(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + USERNAME_COL + " TEXT PRIMARY KEY, "
                + PASSWORD_COL + " TEXT, "
                + HEIGHT_COL + " INTEGER, "
                + WEIGHT_COL + " REAL, "
                + GENDER_COL + " TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertUser(User user) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();

            values.put(USERNAME_COL, user.getUsername());
            values.put(PASSWORD_COL, user.getPassword());
            values.put(HEIGHT_COL, user.getHeight());
            values.put(WEIGHT_COL, user.getWeight());
            values.put(GENDER_COL, user.getGender().toString());

            return db.insert(TABLE_NAME, null, values) != -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param username
     * @return returns the user, if it was found.
     */
    public Optional<User> getUserByUsername(String username) {
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            Cursor cursor = db.query(TABLE_NAME, null,
                    USERNAME_COL + "=?", new String[]{username},
                    null, null, null);

            if (cursor == null || !cursor.moveToNext()) return Optional.empty();

            String password = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD_COL));
            float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT_COL));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(HEIGHT_COL));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(GENDER_COL));

            cursor.close();

            return Optional.of(new User(username, password, weight, height, User.Gender.fromString(gender)));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * @param username
     * @return true if a user was deleted. false if nothing was deleted.
     */
    public boolean deleteUser(String username) {
        try (SQLiteDatabase db = this.getWritableDatabase()){
            return db.delete(TABLE_NAME, USERNAME_COL + "=?", new String[]{username}) != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
