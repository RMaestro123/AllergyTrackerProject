package com.example.myapplication.meteo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MeteoApi {
    @GET("v1/forecast")
    Call<MeteoResponse> getForecast(
            @Query("latitude")  double lat,
            @Query("longitude") double lon,
            @Query("daily")     String dailyFields,
            @Query("timezone")  String tz
    );
}
