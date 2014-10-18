package io.checkio.android.app;

import android.app.Activity;
import android.os.Bundle;

import io.checkio.android.app.fragment.ProjectsFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new ProjectsFragment())
                    .commit();
        }
    }
}
