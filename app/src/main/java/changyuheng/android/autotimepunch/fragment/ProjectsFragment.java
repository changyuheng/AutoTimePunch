package changyuheng.android.autotimepunch.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import changyuheng.android.autotimepunch.R;
import changyuheng.android.autotimepunch.database.PunchDatabaseHelper;

public class ProjectsFragment extends ListFragment implements
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText(getText(R.string.project_list_empty));

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        SQLiteDatabase db = PunchDatabaseHelper.getInstance(getActivity()).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);
        Cursor c = qb.query(
                db,
                PROJECTS_SUMMARY_PROJECTION,
                null, null, null, null, null);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                c,
                new String[]{PunchDatabaseHelper.ProjectColumns.NAME},
                new int[]{android.R.id.text1},
                0);

        setListAdapter(mAdapter);

        // Start out with a progress indicator.
//        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    public void onResume() {
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        inflater.inflate(R.menu.projects, menu);

        SearchView sv = new SearchView(getActivity());
        sv.setOnQueryTextListener(this);

        MenuItem item = menu.findItem(R.id.action_search);
        item.setActionView(sv);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(android.R.id.content, new NewProjectFragment())
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] PROJECTS_SUMMARY_PROJECTION = new String[] {
            PunchDatabaseHelper.ProjectColumns._ID,
            PunchDatabaseHelper.ProjectColumns.NAME,
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: Build ContentProvider to response the request.
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}
