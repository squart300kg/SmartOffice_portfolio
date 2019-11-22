package com.example.smartoffice.retrifit_setup;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient
{
    public static final String BASE_URL = "http://54.180.2.52/"; // 정화씨 aws

    private static Retrofit retrofit;

    public static Retrofit getApiClient()
    {
        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder().
                    baseUrl(BASE_URL).
                    addConverterFactory(GsonConverterFactory.create()).
                    build();
        }
        return retrofit;
    }
}
