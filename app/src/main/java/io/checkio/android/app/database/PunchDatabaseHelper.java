package io.checkio.android.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class PunchDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "punch.db";
    private static volatile PunchDatabaseHelper sSingleton = null;

    static final int DATABASE_VERSION = 1;

    PunchDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.PROJECT + " ("
                + ProjectColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProjectColumns.UUID + " TEXT,"
                + ProjectColumns.DISPLAY_NAME + " TEXT,"
                + ProjectColumns.TIME_ZONE + " INTEGER,"
                + ProjectColumns.WIFI_TRIGGER + " TEXT"
                + ");");

        db.execSQL("CREATE TABLE " + Tables.CARD + " ("
                + CardColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CardColumns.PROJECT + " TEXT,"
                + CardColumns.TIME + " INTEGER,"
                + CardColumns.IS_PUNCH_IN + " INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public static PunchDatabaseHelper getInstance(Context context) {
        if (sSingleton == null) {
            synchronized (PunchDatabaseHelper.class) {
                if (sSingleton == null) sSingleton = new PunchDatabaseHelper(context);
            }
        }
        return sSingleton;
    }

    public interface Tables {
        public static final String PROJECT = "project";
        public static final String CARD = "card";
    }

    public interface ProjectColumns extends BaseColumns {
        public static final String UUID = "uuid";
        public static final String DISPLAY_NAME = "display_name";
        public static final String TIME_ZONE = "time_zone";
        public static final String WIFI_TRIGGER = "wifi_trigger";
    }

    public interface CardColumns extends BaseColumns {
        public static final String PROJECT = "project";
        public static final String TIME = "unix_time";
        public static final String IS_PUNCH_IN = "is_punch_in";
    }

    public static final String[] PROJECT_PROJECTION = new String[] {
            ProjectColumns._ID,
            ProjectColumns.UUID,
            ProjectColumns.DISPLAY_NAME,
            ProjectColumns.TIME_ZONE,
            ProjectColumns.WIFI_TRIGGER,
    };

    public static final String[] CARD_PROJECTION = new String[] {
            CardColumns._ID,
            CardColumns.PROJECT,
            CardColumns.TIME,
            CardColumns.IS_PUNCH_IN,
    };
}
