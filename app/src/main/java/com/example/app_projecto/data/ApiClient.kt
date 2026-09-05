package com.example.app_projecto.data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    /**
     * MAUI usaba http://localhost:5179
     * - Emulador Android: usar http://10.0.2.2:5179/
     * - Celular físico: usar http://IP-DE-TU-PC:5179/  ej: http://192.168.1.10:5179/
     *   (tu API escucha en 0.0.0.0:5179 según launchSettings.json, así que es alcanzable por LAN)
     */
    var BASE_URL = "http://10.0.2.2:5179/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
