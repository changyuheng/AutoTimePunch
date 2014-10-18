package io.checkio.android.app;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;

import io.checkio.android.app.database.PunchDatabaseHelper;

public class EventReceiver extends BroadcastReceiver {
    public EventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();

        if (action == null) return;

        switch (action) {
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                handleWifiNetworkStateChanged(context, intent);
                break;
        }
    }

    private void handleWifiNetworkStateChanged(Context context, Intent intent) {
        if (context == null || intent == null) return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) return;

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if (networkInfo == null) return;

        NetworkInfo.State networkState = networkInfo.getState();

        if (networkState == null) return;

        List<String> projects = null;
        boolean isPunchIn = false;

        switch (networkState) {
            case CONNECTED:
                String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                projects = getProjectsUUID(context, ssid);
                isPunchIn = true;
                break;
            case DISCONNECTED:
                projects = getProjectsUUID(context);
                break;
            default:
                return;
        }

        if (projects.size() == 0) return;

        punch(context, projects, isPunchIn, false);
    }

    private List<String> getProjectsUUID(Context context) {
        List<String> result = new ArrayList<>();

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(context).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.CARD);
        Cursor c = qb.query(db, PunchDatabaseHelper.CARD_PROJECTION, null,
                null, null, null, PunchDatabaseHelper.CardColumns._ID + " DESC");

        if (c == null) return result;

        while (c.moveToNext()) {
            boolean isLastOnePunchIn = c.getInt(c.getColumnIndex(
                    PunchDatabaseHelper.CardColumns.IS_PUNCH_IN)) != 0;

            if (!isLastOnePunchIn) break;

            result.add(c.getString(c.getColumnIndex(PunchDatabaseHelper.CardColumns.PROJECT)));
        }

        return result;
    }

    private List<String> getProjectsUUID(Context context, String ssid) {
        List<String> result = new ArrayList<>();

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(context).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);
        Cursor c = qb.query(db, PunchDatabaseHelper.PROJECT_PROJECTION, null,
                null, null, null, null);

        if (c == null) return result;

        while (c.moveToNext()) {
            String trigger = c.getString(c.getColumnIndex(
                    PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER));

            if (!ssid.equals(trigger)) continue;

            String uuid = c.getString(c.getColumnIndex(
                    PunchDatabaseHelper.ProjectColumns.UUID));

            result.add(uuid);
        }

        return result;
    }

    private void punch(Context context, List<String> projects, boolean isPunchIn,
                       boolean allowDuplicated) {
        SQLiteDatabase roDB = PunchDatabaseHelper.getInstance(context).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.CARD);

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(context).getWritableDatabase();
        Time time = new Time();

        time.setToNow();
        long now = time.toMillis(false);

        for (String project : projects) {
            if (!allowDuplicated) {
                Cursor c = qb.query(roDB, PunchDatabaseHelper.CARD_PROJECTION,
                        PunchDatabaseHelper.CardColumns.PROJECT + "=\"" + project + "\"",
                        null, null, null, PunchDatabaseHelper.CardColumns._ID + " DESC");
                if (c != null && c.moveToFirst()) {
                    boolean lastIsPunchIn = c.getInt(c.getColumnIndex(
                            PunchDatabaseHelper.CardColumns.IS_PUNCH_IN)) != 0;
                    if (lastIsPunchIn == isPunchIn) {
                        int lastTime = c.getInt(c.getColumnIndex(
                                PunchDatabaseHelper.CardColumns.TIME));
                        if (now - lastTime < 60 * 1000 /* 1 minute */) continue;
                    }
                }
            }

            ContentValues values = new ContentValues();

            values.put(PunchDatabaseHelper.CardColumns.PROJECT, project);
            values.put(PunchDatabaseHelper.CardColumns.TIME, now);
            values.put(PunchDatabaseHelper.CardColumns.IS_PUNCH_IN, isPunchIn);

            db.insert(PunchDatabaseHelper.Tables.CARD, null, values);
        }
    }
}
