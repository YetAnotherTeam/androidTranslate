package ru.bondar.russify;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TranslationFragment.OnFragmentInteractionListener {

    private EditText mTextInput;
    private ImageButton mSpeechButton;
    private int delayTime;
    private final int VOICE_RECOGNITION_REQUEST_CODE = 0;
    private FragmentManager fManager;
    private SharedPreferences sharedPreferences;
    private String mainLanguage;
    private String secondaryLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                                     //может стоит зарефакторить как-то, а то очень много всего в onCreate
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        delayTime = Integer.valueOf(sharedPreferences.getString(getString(R.string.delay_value), "400")); // any way better to get int?

        TranslationFragment fragment = initTranslationFragment();
        initViews();

        TranslateAdapter retrofit = TranslateAdapter.getInstance();

        RxTextView.textChanges(mTextInput)
                .debounce(delayTime, TimeUnit.MILLISECONDS)
                .filter(t -> t.length() != 0)
                .observeOn(Schedulers.io())
                .subscribe(s -> retrofit.getDirection(s.toString())
                                .flatMap(j -> {
                                    mainLanguage = sharedPreferences.getString(getString(R.string.main_lang), "ru");
                                    secondaryLanguage = sharedPreferences.getString(getString(R.string.secondary_lang), "en");
                                    String result = j.get("lang").getAsString();
                                    if (result.equals(mainLanguage)) {
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
                                        Log.d("STRING", "Error here");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> resultText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (resultText != null && !resultText.isEmpty()) {
                mTextInput.setText(resultText.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initViews() {
        mTextInput = (EditText) findViewById(R.id.text_input);
        mSpeechButton = (ImageButton) findViewById(R.id.button_speech_to_text);

        mSpeechButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sharedPreferences.getString(getString(R.string.main_lang), "ru"));
            try {
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
    }

    private TranslationFragment initTranslationFragment() {
        fManager = getFragmentManager();
        TranslationFragment fragment = TranslationFragment
                .newInstance("");
        FragmentTransaction fTrans = fManager.beginTransaction();
        fTrans.add(R.id.translate_fragment_container, fragment);
        fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fTrans.commit();
        return fragment;
    }

}
