package com.example.sugarsync;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class EdamamApi {

    private static final String API_URL = "https://api.edamam.com/api/nutrition-details";
    private static final String APP_ID = "49a44627";
    private static final String APP_KEY = "3481c10231e891ca386bd204ed44ab88";

    public static String getNutritionalInfo(String query) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, query);

        Request request = new Request.Builder()
                .url(API_URL + "?app_id=" + APP_ID + "&app_key=" + APP_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected response code: " + response);
            }
        }
    }
}
