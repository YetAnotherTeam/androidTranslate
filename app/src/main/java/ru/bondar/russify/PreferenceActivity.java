package ru.bondar.russify;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;


public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferance);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pref_toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);

        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.pref_fragment_container, new PrefFragment()).commit();
    }

    public static class PrefFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

}

