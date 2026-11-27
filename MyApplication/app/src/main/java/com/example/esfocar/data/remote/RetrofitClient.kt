package com.example.esfocar.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 apunta al localhost de tu PC desde el emulador Android
    private const val BASE_URL_USUARIOS = "http://127.0.0.1:8081/"
    private const val BASE_URL_RUBRICAS = "http://127.0.0.1:8082/"
    private const val BASE_URL_EXTERNA = "https://date.nager.at/"
    val externalApi: ExternalRetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EXTERNA)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExternalRetrofitService::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val usuariosApi: UsuariosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USUARIOS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApiService::class.java)
    }

    val rubricasApi: RubricasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_RUBRICAS)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RubricasApiService::class.java)
    }
}