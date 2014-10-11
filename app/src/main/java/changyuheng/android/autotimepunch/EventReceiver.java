package changyuheng.android.autotimepunch;

import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Time;

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
            default:
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

        String state = null;
        String ssid = "";
        ContentValues values = new ContentValues();

        Time now = new Time();
        now.setToNow();

        switch (networkState) {
            case CONNECTED:
                state = "CONNECTED";
                ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                values.put(PunchDatabaseHelper.CardColumns.TIME, now.toMillis(false) / 1000L);
                values.put(PunchDatabaseHelper.CardColumns.PUNCH_IN, 1);
                values.put(PunchDatabaseHelper.CardColumns.PROJECT, ssid);
                break;
            case DISCONNECTED:
                state = "DISCONNECTED";
                values.put(PunchDatabaseHelper.CardColumns.TIME, now.toMillis(false) / 1000L);
                values.put(PunchDatabaseHelper.CardColumns.PUNCH_IN, 0);
                values.put(PunchDatabaseHelper.CardColumns.PROJECT, ssid);
                break;
            default:
                break;
        }

        if (state != null) {
            SQLiteDatabase db = PunchDatabaseHelper.getInstance(context).getWritableDatabase();
            db.insert(PunchDatabaseHelper.Tables.CARD, null, values);
        }
    }
}
