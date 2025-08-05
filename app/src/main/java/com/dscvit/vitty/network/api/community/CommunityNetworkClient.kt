package com.dscvit.vitty.network.api.community

import com.dscvit.vitty.util.WebConstants.COMMUNITY_BASE_URL
import com.dscvit.vitty.util.WebConstants.TIMEOUT
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CommunityNetworkClient {
    private var retrofit: Retrofit? = null

    val retrofitClientCommunity: Retrofit
        get() {
            if (retrofit == null) {
                val loggingInterceptor =
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }

                val okHttpClient =
                    OkHttpClient
                        .Builder()
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                        .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                        .writeTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                        .build()

                retrofit =
                    Retrofit
                        .Builder()
                        .baseUrl(COMMUNITY_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build()
            }
            return retrofit!!
        }
}
