package io.checkio.android.app.fragment;

import android.app.ListFragment;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import io.checkio.android.app.R;
import io.checkio.android.app.database.PunchDatabaseHelper;

public class ProjectsFragment extends ListFragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectsFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initLayout(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getText(R.string.project_list_empty));
    }

    public void onResume() {
        updateAdapter();
        updateUi();

        super.onResume();
    }

    private void updateAdapter() {
        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);

        Cursor c = qb.query(db, PunchDatabaseHelper.PROJECT_PROJECTION, null,
                null, null, null, null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, c,
                new String[] {PunchDatabaseHelper.ProjectColumns.DISPLAY_NAME},
                new int[] {android.R.id.text1}, 0);

        setListAdapter(adapter);
    }

    private void updateUi() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setHomeButtonEnabled(false);
        getActivity().getActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String name = ((TextView) v.findViewById(android.R.id.text1)).getText().toString();

        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(android.R.id.content, CardFragment.newInstance(name))
                .commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.projects, menu);
    }

    private void initLayout(View rootView) {
        setMargins(rootView);
    }

    private void setMargins(View view) {
        View layout = view.findViewById(
                Resources.getSystem().getIdentifier("listContainer","id", "android"));

        final float scale = getActivity().getResources().getDisplayMetrics().density;
        int pixels = (int) (16 * scale + 0.5f);

        layout.setPadding(pixels, 0, pixels, 0);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(android.R.id.content, new EditProjectFragment())
                    .commit();
            return true;
        }
        return false;
    }
}
