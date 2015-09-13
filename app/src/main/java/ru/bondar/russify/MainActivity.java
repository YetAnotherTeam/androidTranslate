package ru.bondar.russify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    private EditText mTextInput;
    private TextView mTextOutput;
    private Handler handler;
    private final int DELAY_TIME = 600;
    private final int NEW_TEXT_VIEW_TEXT = 1;
    private final String ACCESS_KEY = "trnsl.1.1.20150911T085342Z.79f8b7b676e2face.b1fca036ecb27cb8260a55bf9704ff61a0e006b9";
    private final String YANDEX_TRANSLATE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private final String YANDEX_DETECT_URL = "https://translate.yandex.net/api/v1.5/tr.json/detect?";
    private final String SERVICE_ENDPOINT = "https://translate.yandex.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextInput = (EditText) findViewById(R.id.text_input);
        mTextOutput = (TextView) findViewById(R.id.text_output);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Log.d("here", "here");
        TranslateAPI service = retrofit.create(TranslateAPI.class);
        Log.d("here", "here1");
        Observable<JsonObject> observable = service.getTranslate(ACCESS_KEY, "hello", "en-ru");


        Log.d("here", "here3");
        RxTextView.textChangeEvents(mTextInput)
                .debounce(DELAY_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> service.getTranslate(ACCESS_KEY, s.text().toString(), "en-ru")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(JsonObject jsonObject) {
                                        Log.d("onNext", jsonObject.toString());
                                        mTextOutput.setText(jsonObject.get("text")
                                                .getAsJsonArray().get(0).toString().replace("\"", "")); //???
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
}
