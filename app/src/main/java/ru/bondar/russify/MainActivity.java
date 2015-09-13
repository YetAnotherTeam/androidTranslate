package ru.bondar.russify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity {

    private EditText mTextInput;
    private TextView mTextOutput;
    private final int DELAY_TIME = 400;
    private final String ACCESS_KEY = "trnsl.1.1.20150911T085342Z.79f8b7b676e2face.b1fca036ecb27cb8260a55bf9704ff61a0e006b9";
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
                .subscribe(s -> service.detectLanguage(ACCESS_KEY, s.text().toString())
                                .subscribe(new Subscriber<JsonObject>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(JsonObject jsonObject) {
                                        String direction;
                                        if (jsonObject.get("lang").getAsString().equals("ru")) {
                                            direction = "ru-en";
                                        } else {
                                            direction = jsonObject.get("lang").getAsString() + "-ru";
                                        }
                                        service.getTranslate(ACCESS_KEY, s.text().toString(), direction)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
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
                                                        mTextOutput.setText(jsonObject.get("text")
                                                                .getAsJsonArray().get(0)
                                                                .getAsString());
                                                    }
                                                });
                                    }
                                })
                );
//                .subscribe(s -> service.getTranslate(ACCESS_KEY, s.text().toString(), "en-ru")
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(new Subscriber<JsonObject>() {
//                        @Override
//                        public void onCompleted() {
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onNext(JsonObject jsonObject) {
//                            Log.d("onNext", jsonObject.toString());
//                            mTextOutput.setText(jsonObject.get("text")
//                                    .getAsJsonArray().get(0).getAsString(); //???
//                        }
//                    })
//                );
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
