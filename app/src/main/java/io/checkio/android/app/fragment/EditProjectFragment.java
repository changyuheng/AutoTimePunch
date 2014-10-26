package io.checkio.android.app.fragment;

import android.app.Fragment;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import io.checkio.android.app.R;
import io.checkio.android.app.database.PunchDatabaseHelper;

public class EditProjectFragment extends Fragment {

    private EditText mProjectName;
    private Spinner mTimeZoneSpinner;
    private Spinner mWifiSpinner;

    private Map<String, String> mTimeZoneMap;

    public EditProjectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_project, container, false);

        mProjectName = (EditText) view.findViewById(R.id.edittext_name);

        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        List<String> wifiList = new ArrayList<>();
        if (wifi != null && wifi.getConfiguredNetworks() != null) {
            for (WifiConfiguration ap : wifi.getConfiguredNetworks()) {
                wifiList.add(ap.SSID.replace("\"", ""));
            }
        }
        mWifiSpinner = (Spinner) view.findViewById(R.id.wifi_spinner);
        mWifiSpinner.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, wifiList));

        List<String> timeZones = new ArrayList<>();
        mTimeZoneMap = new HashMap<>();
        // TODO: get the time zones from system
        for (String timeZoneId : TimeZone.getAvailableIDs()) {
            String timeZone = TimeZone.getTimeZone(timeZoneId).getDisplayName();
            if (mTimeZoneMap.containsKey(timeZone)) continue;
            mTimeZoneMap.put(timeZone, timeZoneId);
            timeZones.add(timeZone);
        }
        Collections.sort(timeZones);
        mTimeZoneSpinner = (Spinner) view.findViewById(R.id.time_zone_spinner);
        mTimeZoneSpinner.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, timeZones));
        mTimeZoneSpinner.setSelection(timeZones.indexOf(
                Calendar.getInstance().getTimeZone().getDisplayName()));

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
                saveProjectConfigs();
            case R.id.action_cancel:
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Service.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mProjectName.getWindowToken(), 0);
                getFragmentManager().popBackStack();
                return true;
        }
        return false;
    }

    private void saveProjectConfigs() {
        String projectName = mProjectName.getText().toString();
        String timeZone = mTimeZoneMap.get(mTimeZoneSpinner.getSelectedItem().toString());
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mProjectName, 0);
    }
}
