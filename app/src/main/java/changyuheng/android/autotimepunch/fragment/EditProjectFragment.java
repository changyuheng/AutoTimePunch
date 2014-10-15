package changyuheng.android.autotimepunch.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import changyuheng.android.autotimepunch.R;
import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

public class EditProjectFragment extends Fragment {

    private EditText mProjectName;
    private Spinner mWifiSpinner;
    private Spinner mTimeZoneSpinner;

    public EditProjectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_project, container, false);

        mProjectName = (EditText) view.findViewById(R.id.edittext_name);

        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        List<String> wifiList = new ArrayList<String>();
        if (wifi != null) {
            for (WifiConfiguration ap : wifi.getConfiguredNetworks()) {
                String ssid = ap.SSID.replace("\"", "");
                wifiList.add(ssid);
            }
        }
        mWifiSpinner = (Spinner) view.findViewById(R.id.wifi_spinner);
        mWifiSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, wifiList));

        mTimeZoneSpinner = (Spinner) view.findViewById(R.id.time_zone_spinner);
        mTimeZoneSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[] {Calendar.getInstance().getTimeZone().getDisplayName()}
        ));

        return view;
    }

    @Override
    public void onResume() {
        updateUi();

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_accept:
                saveProjectDetail();
            case R.id.action_cancel:
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return false;
    }

    private void saveProjectDetail() {
        String projectName = mProjectName.getText().toString();
        int timeZone = 8 * 60 * 60 * 1000;
        String wifiTrigger = mWifiSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(projectName)) return;

        ContentValues values = new ContentValues();
        values.put(PunchDatabaseHelper.ProjectColumns.UUID, UUID.randomUUID().toString());
        values.put(PunchDatabaseHelper.ProjectColumns.DISPLAY_NAME, projectName);
        values.put(PunchDatabaseHelper.ProjectColumns.TIME_ZONE, timeZone);
        values.put(PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER, wifiTrigger);

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getWritableDatabase();
        db.insert(PunchDatabaseHelper.Tables.PROJECT, null, values);
     }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        inflater.inflate(R.menu.edit_project, menu);
    }


    private void updateUi() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
        mProjectName.requestFocus();
    }
}
