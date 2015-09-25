package ru.bondar.russify;


import com.google.gson.JsonObject;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import ru.bondar.russify.ApiInterface.TranslateAPI;
import rx.Observable;

public class TranslateAdapter {
    private static TranslateAdapter instance;
    private TranslateAPI service;
    private final String SERVICE_ENDPOINT = "https://translate.yandex.net";
    private final String ACCESS_KEY = "trnsl.1.1.20150911T085342Z.79f8b7b676e2face.b1fca036ecb27cb8260a55bf9704ff61a0e006b9";


    private TranslateAdapter() {
        service = new Retrofit.Builder()
                .baseUrl(SERVICE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(TranslateAPI.class);
    }

    public static TranslateAdapter getInstance() {
        if (instance == null) {
            instance = new TranslateAdapter();
        }
        return instance;
    }

    public Observable<JsonObject> getTranslate(String text, String transDirect) {
        return service.getTranslate(ACCESS_KEY, text, transDirect);
    }

    public Observable<JsonObject> getDirection(String text) {
        return service.detectLanguage(ACCESS_KEY, text);
    }
}
