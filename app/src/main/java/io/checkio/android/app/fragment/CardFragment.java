package io.checkio.android.app.fragment;

import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.checkio.android.app.database.PunchDatabaseHelper;

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
    public static CardFragment newInstance(String projectName) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString(PROJECT_NAME, projectName);
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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initLayout(view);
    }

    @Override
    public void onResume() {
        updateUi();
        updateAdapter();

        super.onResume();
    }

    private void updateAdapter() {
        String uuid = getProjectUUID();

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(PunchDatabaseHelper.Tables.CARD);

        Cursor c = qb.query(db, PunchDatabaseHelper.CARD_PROJECTION,
                PunchDatabaseHelper.CardColumns.PROJECT + "=\"" + uuid + "\"",
                null, null, null, PunchDatabaseHelper.CardColumns.TIME + " DESC");

        if (c == null) return;

        List<Map<String, String>> l = new ArrayList<>();

        while (c.moveToNext()) {
            long unixTime = c.getLong(c.getColumnIndex(PunchDatabaseHelper.CardColumns.TIME));
            boolean isPunchIn = c.getInt(c.getColumnIndex(
                    PunchDatabaseHelper.CardColumns.IS_PUNCH_IN)) != 0;

            DateFormat df = DateFormat.getInstance();
            String date = df.format(new Date(unixTime));

            Map map = new HashMap();
            map.put("date", date);
            map.put("punch_in", "");
            map.put("punch_out", "");
            map.put("duration", "");
            l.add(map);
        }

//        ListAdapter adapter = new SimpleAdapter(getActivity(), l, R.layout.list_item_card,
//                new String[] {"date", "punch_in", "punch_out", "duration"},
//                new int[] {R.id.date, R.id.punch_in, R.id.punch_out, R.id.duration});
        ListAdapter adapter = new SimpleAdapter(getActivity(), l, android.R.layout.simple_list_item_1,
                new String[] {"date"},
                new int[] {android.R.id.text1});

        setListAdapter(adapter);
    }

    private void updateUi() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeButtonEnabled(true);
        getActivity().getActionBar().setTitle(mProjectName.toString());
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

    public String getProjectUUID() {
        String uuid = null;

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);
        Cursor c = qb.query(db, PunchDatabaseHelper.PROJECT_PROJECTION,
                PunchDatabaseHelper.ProjectColumns.DISPLAY_NAME + "=\"" + mProjectName + "\"",
                null, null, null, null);

        if (c == null) return uuid;

        if (!c.moveToFirst()) return uuid;

        uuid = c.getString(c.getColumnIndex(PunchDatabaseHelper.ProjectColumns.UUID));

        return uuid;
    }

    private void initLayout(View rootView) {
        setListViewPadding(getListView());
    }

    private void setListViewPadding(ListView listView) {
        // 16dp is defined in http://developer.android.com/design/style/metrics-grids.html#examples
        int pixels = (int) (16 * getActivity().getResources().getDisplayMetrics().density + 0.5f);

        listView.setPadding(pixels, 0, pixels, 0);
        listView.setClipToPadding(false);
        listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
    }
}
