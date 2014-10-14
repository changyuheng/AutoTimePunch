package changyuheng.android.autotimepunch.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import changyuheng.android.autotimepunch.R;
import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

public class EditProjectFragment extends Fragment {

    private EditText mProjectName;
    private Spinner mWifiSpinner;
    private Spinner mTimeZoneSpinner;

    public EditProjectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_project, container, false);

        mProjectName = (EditText) view.findViewById(R.id.edittext_name);

        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        List<String> wifiList = new ArrayList<String>();
        if (wifi != null) {
            for (ScanResult result : wifi.getScanResults()) {
                String ssid = result.SSID.replace("\"", "");
                wifiList.add(ssid);
            }
        }
        mWifiSpinner = (Spinner) view.findViewById(R.id.wifi_spinner);
        mWifiSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, wifiList));

        mTimeZoneSpinner = (Spinner) view.findViewById(R.id.time_zone_spinner);
        mTimeZoneSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[] {"-11", "-10.5", "-10", "-9.5", "-9", "-8.5", "-8", "-7.5", "-7",
                        "-6.5", "-6", "-5.5", "-5", "-4.5", "-4", "-3.5", "-3", "-2.5", "-2",
                        "-1.5", "-1", "-0.5", "0", "0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4",
                        "4.5", "5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10",
                        "10.5", "11", "11.5", "12", "12.5", "13"}
        ));

        return view;
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
        int timeZone = (int) (Float.parseFloat(mTimeZoneSpinner.getSelectedItem().toString())
                * 60 * 60 * 1000);
        String wifiTrigger = mWifiSpinner.getSelectedItem().toString();

        ContentValues values = new ContentValues();
        values.put(PunchDatabaseHelper.ProjectColumns.NAME, mProjectName.getText().toString());
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

    private void initOptionsMenu() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
    }
}
