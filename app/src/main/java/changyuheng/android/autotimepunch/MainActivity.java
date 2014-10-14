package changyuheng.android.autotimepunch;

import android.app.Activity;
import android.os.Bundle;

import changyuheng.android.autotimepunch.fragment.ProjectsFragment;

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
