package ru.bondar.russify;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface TranslateAPI {
    @GET("/api/v1.5/tr.json/translate")
    Observable<JsonObject> getTranslate(@Query("key") String key
            , @Query("text") String text
            , @Query("lang") String lang);

    @GET("/api/v1.5/tr.json/detect")
    Observable<JsonObject> detectLanguage(@Query("key") String key,
                                          @Query("text") String text
                                        );
}
