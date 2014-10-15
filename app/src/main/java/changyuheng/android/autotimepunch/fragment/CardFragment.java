package changyuheng.android.autotimepunch.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changyuheng.android.autotimepunch.R;
import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

/**
 * A fragment representing a list of Items.
 */
public class CardFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PROJECT_NAME = "project_name";

    // TODO: Rename and change types of parameters
    private String mProjectName;

    // TODO: Rename and change types of parameters
    public static CardFragment newInstance(String param1) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString(PROJECT_NAME, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CardFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mProjectName = getArguments().getString(PROJECT_NAME);
        }

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);
        Cursor c = qb.query(
                db,
                PROJECT_SUMMARY_PROJECTION,
                null, null, null, null, null);

        int timeZone = 0;
        String trigger = null;

        while (c.moveToNext()) {
            String project = c.getString(c.getColumnIndex(PunchDatabaseHelper.ProjectColumns.NAME));

            if (!mProjectName.equals(project)) continue;

            timeZone = c.getInt(c.getColumnIndex(PunchDatabaseHelper.ProjectColumns.TIME_ZONE));
            trigger = c.getString(c.getColumnIndex(PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER));
        }

        qb.setTables(PunchDatabaseHelper.Tables.CARD);

        c = qb.query(
                db,
                CARD_PROJECTION,
                null, null, null, null, null);

        List<Map<String, String>> l = new ArrayList<Map<String, String>>();

        int punchInTime = 0;
        int punchOutTime = 0;
        while (c.moveToNext()) {
            String t = c.getString(c.getColumnIndex(PunchDatabaseHelper.CardColumns.PROJECT));
            android.util.Log.d("henry", "project = " + trigger);

            if (!t.equals(trigger)) continue;

            int time = c.getInt(c.getColumnIndex(PunchDatabaseHelper.CardColumns.TIME));
            boolean punchIn = c.getInt(c.getColumnIndex(PunchDatabaseHelper.CardColumns.PUNCH_IN)) != 0;

            time += timeZone;

            if (punchInTime == 0 && punchIn) {
                punchInTime = time;
            } else {
                continue;
            }

            Map map = new HashMap();
            map.put("date", Integer.toString(time));
            map.put("punch_in", Integer.toString(punchInTime));
            map.put("punch_out", Integer.toString(punchOutTime));
            map.put("duration", "");
            l.add(map);
        }

        ListAdapter adapter = new SimpleAdapter(getActivity(),
                l,
                R.layout.list_item_card,
                new String[] {"date", "punch_in", "punch_out", "duration"},
                new int[] {R.id.date, R.id.punch_in, R.id.punch_out, R.id.duration});

        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        initOptionsMenu();

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, null);
    }

    private void initOptionsMenu() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return false;
    }

    private static final String[] CARD_PROJECTION = new String[] {
            PunchDatabaseHelper.CardColumns._ID,
            PunchDatabaseHelper.CardColumns.TIME,
            PunchDatabaseHelper.CardColumns.PUNCH_IN,
            PunchDatabaseHelper.CardColumns.PROJECT,
    };
    private static final String[] PROJECT_SUMMARY_PROJECTION = new String[] {
            PunchDatabaseHelper.ProjectColumns._ID,
            PunchDatabaseHelper.ProjectColumns.NAME,
            PunchDatabaseHelper.ProjectColumns.TIME_ZONE,
            PunchDatabaseHelper.ProjectColumns.WIFI_TRIGGER,
    };
}