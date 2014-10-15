package changyuheng.android.autotimepunch;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.text.format.Time;

import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

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

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(context).getWritableDatabase();
        String state = null;
        boolean isPunchIn = false;
        String ssid = null;
        Time now = new Time();
        now.setToNow();

        switch (networkState) {
            case CONNECTED:
                state = "CONNECTED";
                ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                isPunchIn = true;
                break;
            case DISCONNECTED:
                state = "DISCONNECTED";
                break;
            default:
                break;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.CARD);
        Cursor c = qb.query(
                db,
                CARD_SUMMARY_PROJECTION,
                null, null, null, null, null);

        if (TextUtils.isEmpty(ssid)) {
            c.moveToLast();
            boolean in = c.getInt(c.getColumnIndex(PunchDatabaseHelper.CardColumns.PUNCH_IN)) != 0;

            if (TextUtils.isEmpty(ssid)) return;
        }

        String project = null;
        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);
        c = qb.query(
                db,
                PROJECT_SUMMARY_PROJECTION,
                null, null, null, null, null);

        while (c.moveToNext()) {
            String trigger = c.getString(c.getColumnIndex(
                    PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER));
            if (trigger.equals(ssid)) project = c.getString(c.getColumnIndex(
                    PunchDatabaseHelper.ProjectColumns.NAME));
        }

        if (TextUtils.isEmpty(project)) return;

        ContentValues values = new ContentValues();
        values.put(PunchDatabaseHelper.CardColumns.TIME, now.toMillis(false));
        values.put(PunchDatabaseHelper.CardColumns.PUNCH_IN, isPunchIn);
        values.put(PunchDatabaseHelper.CardColumns.PROJECT, project);

        if (state != null) {
            db.insert(PunchDatabaseHelper.Tables.CARD, null, values);
        }
    }

    private static final String[] CARD_SUMMARY_PROJECTION = new String[] {
            PunchDatabaseHelper.CardColumns._ID,
            PunchDatabaseHelper.CardColumns.PUNCH_IN,
    };

    private static final String[] PROJECT_SUMMARY_PROJECTION = new String[] {
            PunchDatabaseHelper.ProjectColumns._ID,
            PunchDatabaseHelper.ProjectColumns.NAME,
            PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER,
    };
}
