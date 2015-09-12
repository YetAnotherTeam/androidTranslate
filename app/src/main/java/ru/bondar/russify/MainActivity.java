package ru.bondar.russify;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import ru.bondar.russify.util.RequestTask;

public class MainActivity extends Activity {

    private EditText mTextInput;
    private TextView mTextOutput;
    private Handler handler;
    private final int DELAY_TIME = 600;
    private final int NEW_TEXT_VIEW_TEXT = 1;
    private final String ACCESS_KEY = "trnsl.1.1.20150911T085342Z.79f8b7b676e2face.b1fca036ecb27cb8260a55bf9704ff61a0e006b9";
    private final String YANDEX_TRANSLATE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private final String YANDEX_DETECT_URL = "https://translate.yandex.net/api/v1.5/tr.json/detect?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextInput = (EditText) findViewById(R.id.text_input);
        mTextOutput = (TextView) findViewById(R.id.text_output);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                final int what = msg.what;
                switch (what) {
                    case NEW_TEXT_VIEW_TEXT: {
                        String messageText = (String) msg.obj;
                        TranslatorTask translatorTask = new TranslatorTask();
                        translatorTask.execute(messageText);
                        Log.d("NEW TASK", "task gone to execute");
                        try {
                            String translatedText = translatorTask.get();
                            Log.d("NEW TASK", translatedText);
                            updateTextView(translatedText);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        JsonObject result = api.getTranslate(ACCESS_KEY, "Hello", "en-ru");
//                        Log.d("RESULT!", result.toString());
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };

        TextWatcher inputTW = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            Runnable translateTask = new Runnable() {
                @Override
                public void run() {
                    final String result = mTextInput.getText().toString();
                    Message msg = Message.obtain();
                    msg.obj = result;
                    msg.what = NEW_TEXT_VIEW_TEXT;
                    handler.sendMessage(msg);
                }
            };

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(translateTask);
                handler.postDelayed(translateTask, DELAY_TIME);
            }
        };

        mTextInput.addTextChangedListener(inputTW);
    }

    private class TranslatorTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... msgTexts) {
            String translateResult = "";
            for (String msgText : msgTexts) {
                msgText = msgText.trim().replace(" ", "+");
                Log.d("NEW TASK", "getting direction");
                String translateDirection = getTranslateDirection(msgText);
                Log.d("NEW TASK", "getting translate");
                translateResult = translate(msgText, translateDirection);
            }
            return translateResult;
        }
    }

    private void updateTextView(String s) {
        mTextOutput.setText(s);
    }

    private String getTranslateDirection(String text) {
        String urlForDetection = YANDEX_DETECT_URL + "key=" + ACCESS_KEY + "&text=" + text;
        RequestTask detectRequest = new RequestTask();
        detectRequest.execute(urlForDetection);
        String translateDirection;
        String detectionResult;
        try {
            Log.d("NEW TASK", "before detecting");
            detectionResult = detectRequest.get();
            Log.d("NEW TASK", "after dee");
            JSONObject jsonObject = new JSONObject(detectionResult);
            if (jsonObject.getString("lang").equals("ru")) {
                translateDirection = "ru-en";
            } else {
                translateDirection = jsonObject.getString("lang") + "-ru";
            }
        } catch (Exception e) {
            e.printStackTrace();
            translateDirection = "en-ru";
        }
        return translateDirection;
    }

    private String translate(String text, String translateDirection) {
        String urlForTranslate = YANDEX_TRANSLATE_URL + "key=" + ACCESS_KEY +
                "&text=" + text + "&lang=" + translateDirection;
        RequestTask rt = new RequestTask();
        rt.execute(urlForTranslate);
        String response;
        JSONObject jsonResult;
        String resultText;
        try {
            response = rt.get();
            jsonResult = new JSONObject(response);
            resultText = jsonResult.getJSONArray("text").get(0).toString();
            Log.d("RESULT", response);
        } catch (Exception e) {
            resultText = "";
            e.printStackTrace();
        }
        return resultText;
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
