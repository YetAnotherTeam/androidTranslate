package ru.bondar.russify;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TranslationFragment.OnFragmentInteractionListener {

    private EditText mTextInput;
    private final int DELAY_TIME = 400;
    private final String ACCESS_KEY = "trnsl.1.1.20150911T085342Z.79f8b7b676e2face.b1fca036ecb27cb8260a55bf9704ff61a0e006b9";
    private final String SERVICE_ENDPOINT = "https://translate.yandex.net";
    private FragmentManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextInput = (EditText) findViewById(R.id.text_input);

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        fManager = getFragmentManager();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

//        mTextOutput.setOnClickListener(v -> {
//            ClipData clipData = ClipData.newPlainText("Translation", mTextOutput.getText().toString());
//            clipboardManager.setPrimaryClip(clipData);
//            Toast.makeText(this, R.string.text_copied, Toast.LENGTH_SHORT).show();
//        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        TranslateAPI service = retrofit.create(TranslateAPI.class);

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
                .debounce(DELAY_TIME, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(s -> service.detectLanguage(ACCESS_KEY, s.toString())
                                .flatMap(j -> {
                                    String result = j.get("lang").getAsString();
                                    if (result.equals("ru")) {
                                        return service.getTranslate(ACCESS_KEY, s.toString(), "ru-en");
                                    } else {
                                        return service.getTranslate(ACCESS_KEY, s.toString(), result + "-ru");
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
