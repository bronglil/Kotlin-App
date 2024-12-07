package com.example.automacorp.services

import com.example.automacorp.models.RoomCommandDto
import com.example.automacorp.models.RoomDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory

const val API_USERNAME = "user"
const val API_PASSWORD = "password"

object ApiServices {

    // Declare the loggingInterceptor inside the object scope
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Lazy initialization of the RoomsApiService
    val roomsApiService: RoomsApiService by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val client = try {

            // Disable SSL verification for testing (unsafe)
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    // No validation
                }

                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    // No validation
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), null)

            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)  // Add logging interceptor
                .sslSocketFactory(sslContext.socketFactory, trustManager)  // Disable SSL verification (unsafe)
                .hostnameVerifier { _, _ -> true }  // Accept all hostnames (useful for development)
                .addInterceptor(BasicAuthInterceptor(API_USERNAME, API_PASSWORD))  // Add Basic Authentication
                .build()
        } catch (e: Exception) {
            throw RuntimeException("Error setting up SSL", e)
        }

        val baseUrl = "https://automacorp.devmind.cleverapps.io/api/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(RoomsApiService::class.java)
    }

    interface RoomsApiService {
        @GET("rooms")
        fun findAll(): Call<List<RoomDto>>

        @GET("rooms/{id}")
        fun findById(@Path("id") id: Long): Call<RoomDto>

        @PUT("rooms/{id}")
        fun updateRoom(@Path("id") id: Long, @Body room: RoomCommandDto): Call<RoomDto>
    }

    class BasicAuthInterceptor(private val username: String, private val password: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain
                .request()
                .newBuilder()
                .header("Authorization", Credentials.basic(username, password))  // Add Basic Authentication
                .build()
            return chain.proceed(request)
        }
    }
}
