package io.checkio.android.app.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.checkio.android.app.R;
import io.checkio.android.app.database.PunchDatabaseHelper;

public class ProjectsFragment extends ListFragment implements ListView.MultiChoiceModeListener {

    private Activity mActivity;
    private MenuItem mEditMenuItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initLayout(view);
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
        SQLiteDatabase db = PunchDatabaseHelper.getInstance(mActivity).getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PunchDatabaseHelper.Tables.PROJECT);

        Cursor c = qb.query(db, PunchDatabaseHelper.PROJECT_PROJECTION, null,
                null, null, null, null);

        ListAdapter adapter = new SimpleCursorAdapter(mActivity,
                android.R.layout.simple_list_item_activated_1, c,
                new String[] {PunchDatabaseHelper.ProjectColumns.DISPLAY_NAME},
                new int[] {android.R.id.text1}, 0);

        setListAdapter(adapter);
    }

    private void updateUi() {
        setHasOptionsMenu(true);
        mActivity.getActionBar().setDisplayHomeAsUpEnabled(false);
        mActivity.getActionBar().setHomeButtonEnabled(false);
        mActivity.getActionBar().setTitle(R.string.app_name);
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
        ListView listView = getListView();

        setListViewPadding(listView);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
    }

    private void setListViewPadding(ListView listView) {
        // 16dp is defined in http://developer.android.com/design/style/metrics-grids.html#examples
        int pixels = (int) (16 * mActivity.getResources().getDisplayMetrics().density + 0.5f);

        listView.setPadding(pixels, 0, pixels, 0);
        listView.setClipToPadding(false);
        listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
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

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        final int checkedCount = getListView().getCheckedItemCount();
        switch (checkedCount) {
            case 0:
                mode.setSubtitle(null);
                break;
            case 1:
                mEditMenuItem.setVisible(true);
                break;
            default:
                mEditMenuItem.setVisible(false);
                break;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.select_projects, menu);
        mEditMenuItem = menu.findItem(R.id.action_edit);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                break;
            case R.id.action_delete:
                ListView listView = getListView();
                SparseBooleanArray items = listView.getCheckedItemPositions();
                List<String> projectNames = new ArrayList<>();
                for (int i = 0; i < listView.getCount(); i++) {
                    if (!items.get(i)) continue;

                    Cursor c = (Cursor) listView.getItemAtPosition(i);

                    String projectName = c.getString(c.getColumnIndex(
                            PunchDatabaseHelper.ProjectColumns.DISPLAY_NAME));

                    projectNames.add(projectName);
                }
                PunchDatabaseHelper.deleteProjects(mActivity, projectNames);
                updateAdapter();
                break;
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }
}
