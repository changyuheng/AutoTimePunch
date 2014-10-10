package changyuheng.autotimepunch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

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
        String ssid = null;

        switch (networkState) {
            case CONNECTED:
                state = "CONNECTED";
                ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                break;
            case DISCONNECTED:
                state = "DISCONNECTED";
                ssid = "";
                break;
            default:
                break;
        }

        if (state != null) {
            Log.d("AutoTimePunch", "network state changed to '" + state + "', SSID is '" + ssid + "'");
        }
    }
}
