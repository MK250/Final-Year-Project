package com.example.sugarsync;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EdamamApiService {
    @GET("api/nutrition-data")
    Call<NutritionResponse> getNutritionalInfo(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("ingr") String ingredients
    );
}

