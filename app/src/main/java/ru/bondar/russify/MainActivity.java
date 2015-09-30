package ru.bondar.russify;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TranslationFragment.OnFragmentInteractionListener {

    private EditText mTextInput;
    private int delayTime;
    private final int DEBOUNCE_TIME = 400;
    private FragmentManager fManager;
    private SharedPreferences sharedPreferences;
    private String mainLanguage;
    private String secondaryLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        delayTime = Integer.valueOf(sharedPreferences.getString(getString(R.string.debounce_value), "400"));

//        mainLanguage = sharedPreferences.getString(getString(R.string.main_lang), "ru");
//        secondaryLanguage = sharedPreferences.getString(getString(R.string.secondary_lang), "en");

        mTextInput = (EditText) findViewById(R.id.text_input);

        fManager = getFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);

        setSupportActionBar(toolbar);

        TranslateAdapter retrofit = TranslateAdapter.getInstance();

        TranslationFragment fragment = TranslationFragment
                .newInstance("");

        FragmentTransaction fTrans = fManager.beginTransaction();

        fTrans.add(R.id.trn_fragment_container, fragment);
        fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fTrans.commit();


        RxTextView.textChanges(mTextInput)
                .filter(t -> {
                    Log.d("STRING", t.toString());
                    return t.length() != 0;
                })
                .debounce(delayTime, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(s -> retrofit.getDirection(s.toString())
                                .flatMap(j -> {
                                    mainLanguage = sharedPreferences.getString(getString(R.string.main_lang), "ru");
                                    secondaryLanguage = sharedPreferences.getString(getString(R.string.secondary_lang), "en");
                                    String result = j.get("lang").getAsString();
                                    if (result.equals(mainLanguage)) {
                                        Log.d("TAG", secondaryLanguage + mainLanguage);
                                        return retrofit.getTranslate(s.toString(), mainLanguage + "-" + secondaryLanguage);
                                    } else {
                                        return retrofit.getTranslate(s.toString(), result + "-" + mainLanguage);
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(JsonObject jsonObject) {
                                        fragment.setText(jsonObject.get("text").getAsJsonArray().get(0).getAsString());
                                    }
                                })
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
            startActivity(intent);
//            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
