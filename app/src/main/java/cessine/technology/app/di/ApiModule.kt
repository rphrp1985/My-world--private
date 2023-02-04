package cessine.technology.app.di

import android.content.Context
import cessini.technology.newapi.interceptors.AuthInterceptor
import cessini.technology.newapi.preferences.AuthPreferences
import cessini.technology.newapi.services.explore.ExploreConstants
import cessini.technology.newapi.services.explore.ExploreInfoService
import cessini.technology.newapi.services.explore.ExploreRecordService
import cessini.technology.newapi.services.explore.ExploreService
import cessini.technology.newapi.services.myspace.MySpaceConstants
import cessini.technology.newapi.services.myspace.MySpaceService
import cessini.technology.newapi.services.myworld.MyWorldConstants
import cessini.technology.newapi.services.myworld.MyWorldService
import cessini.technology.newapi.services.story.StoryConstants
import cessini.technology.newapi.services.story.StoryGetService
import cessini.technology.newapi.services.story.StoryService
import cessini.technology.newapi.services.timeline_room.LiveRoomService
import cessini.technology.newapi.services.timeline_room.RoomConstants
import cessini.technology.newapi.services.timeline_room.RoomTimelineService
import cessini.technology.newapi.services.video.VideoConstants
import cessini.technology.newapi.services.video.VideoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
//import dagger.hilt.internal.aggregatedroot.codegen._cessine_technology_app_MainApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.*

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {


    @Provides
    @Singleton
    fun provideGetStoryApi(
        retrofit: Retrofit.Builder,
    ): StoryGetService = retrofit
        .baseUrl(StoryConstants.GET_STORY_BASE_ENDPOINT)
        .build()
        .create(StoryGetService::class.java)

    @Provides
    @Singleton
    fun provideStoryApi(
        retrofit: Retrofit.Builder,
    ): StoryService = retrofit
        .baseUrl(StoryConstants.BASE_ENDPOINT)
        .build()
        .create(StoryService::class.java)

    @Provides
    @Singleton
    fun provideVideoApi(
        retrofit: Retrofit.Builder,
    ): VideoService = retrofit
        .baseUrl(VideoConstants.BASE_ENDPOINT)
        .build()
        .create(VideoService::class.java)

    @Provides
    @Singleton
    fun provideExploreApi(
        retrofit: Retrofit.Builder,okHttpClient: OkHttpClient
    ): ExploreService = retrofit
        .baseUrl(ExploreConstants.BASE_ENDPOINT)
        .client(okHttpClient)
        .build()
        .create(ExploreService::class.java)

    @Provides
    @Singleton
    fun provideExploreInfoApi(
        retrofit: Retrofit.Builder,okHttpClient: OkHttpClient
    ): ExploreInfoService = retrofit
        .client(okHttpClient)
        .baseUrl(ExploreConstants.BASE_ENDPOINT_INFO)
        .build()
        .create(ExploreInfoService::class.java)

    @Provides
    @Singleton
    fun provideExploreRecordApi(
        retrofit: Retrofit.Builder,okHttpClient: OkHttpClient
    ): ExploreRecordService=retrofit
        .client(okHttpClient)
        .baseUrl(ExploreConstants.BASE_ENDPOINT_RECORDED)
        .build()
        .create(ExploreRecordService::class.java)

    @Provides
    @Singleton
    fun provideMySpaceApi(
        retrofit: Retrofit.Builder,okHttpClient: OkHttpClient
    ): MySpaceService = retrofit
        .baseUrl(MySpaceConstants.BASE_ENDPOINT)
        .build()
        .create(MySpaceService::class.java)




    @Provides
    @Singleton
    fun provideMyWorldApi(
        retrofit: Retrofit.Builder, okHttpClient: OkHttpClient
    ): MyWorldService {
        return retrofit
            .baseUrl(MyWorldConstants.BASE_ENDPOINT)
            .client(okHttpClient)
            .build()
            .create(MyWorldService::class.java)
    }

    @Provides
    @Singleton
    fun provideRoomTimelineApi( okHttpClient: OkHttpClient
    ): RoomTimelineService {
        return Retrofit.Builder().baseUrl(RoomConstants.ROOM_TIMELINE_BASE_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build().create(RoomTimelineService::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideLiveRoomApi(okHttpClient: OkHttpClient): LiveRoomService {
//        return Retrofit.Builder().baseUrl(RoomConstants.LIVE_ROOM_BASE_ENDPOINT)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build().create(LiveRoomService::class.java)
//    }

    @Provides
    @Singleton
    fun provideLiveRoomApi(
        retrofit: Retrofit.Builder,
    ): LiveRoomService = retrofit
        .baseUrl(RoomConstants.LIVE_ROOM_BASE_ENDPOINT)
        .build()
        .create(LiveRoomService::class.java)


    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
    ): Retrofit.Builder = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session -> true }
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<X509Certificate>()
            }
        })
        val trustManager = trustAllCerts[0] as X509TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, null)

        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        val okhttpclient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .readTimeout(5000, TimeUnit.SECONDS)
            .writeTimeout(5000, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .hostnameVerifier(hostnameVerifier)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .build()
        return okhttpclient
    }
}
