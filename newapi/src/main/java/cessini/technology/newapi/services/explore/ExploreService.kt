package cessini.technology.newapi.services.explore

import cessini.technology.model.*
import cessini.technology.newapi.ApiParameters
import cessini.technology.newapi.model.ApiMessage
import cessini.technology.newapi.model.ApiNotification
import cessini.technology.newapi.model.ApiNotificationWhenLoggedIn
import cessini.technology.newapi.services.explore.model.body.RegisterAuthUserBody
import cessini.technology.newapi.services.explore.model.body.TokenBody
import cessini.technology.newapi.services.explore.model.body.UserRegistrationBody
import cessini.technology.newapi.services.explore.model.response.ApiExplore
import cessini.technology.newapi.services.explore.model.response.ApiExploreProfile
import cessini.technology.newapi.services.explore.model.response.ApiExploreRoom
import cessini.technology.newapi.services.explore.model.response.ApiSongs

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExploreService {

    @Headers(ApiParameters.NO_AUTH_HEADER)
    @GET("${ExploreConstants.EXPLORE_ENDPOINT_AUTH}")
    suspend fun exploreUnAuth(
//        @Path(value = "id") id: String
    ): ApiExplore

//    @GET("${ExploreConstants.EXPLORE_ENDPOINT_AUTH}")
//    suspend fun explorePaged(
////        @Path(value = "id") id: String
//    ): ApiExplore

//    @Headers(ApiParameters.AUTH_HEADER)
    @GET(value = "${ExploreConstants.EXPLORE_ENDPOINT_AUTH}")
    suspend fun explore(
//        @Path(value = "id") id: String,
    ): ApiExplore

    @GET( value = "${ExploreConstants.EXPLORE_ROOM}{page}" )
    suspend fun explore_room(
        @Path(value = "page") page:Int
    ): Response<ApiExploreRoom>

    @GET( value = "${ExploreConstants.EXPLORE_TOP_PROFILE}{page}" )
    suspend fun explore_top_profile(
        @Path(value = "page") id:Int
    ): Response<ApiExploreProfile>

    @GET( value = "${ExploreConstants.EXPLORE_TOP_PROFILE_AUTH}{id}/{page}" )
    suspend fun explore_top_profile_auth(
        @Path(value = "page") page:Int,
        @Path(value ="id") id: String
    ): Response<ApiExploreProfile>


    @GET(value = "${ExploreConstants.SUGGESTION_CAT_ROOMS}{id}/{page}")
    suspend fun getSuggestedRooms(
        @Path(value = "id") id: String,
        @Path(value="page") page: Int
    ):suggestionroomresponse

    @GET(value = "${ExploreConstants.COMPONENT_END_POINT}{id}")
    suspend fun getComponentAuth(
        @Path(value = "id") id: String
    ) : Component

    @GET(value = "${ExploreConstants.COMPONENT_END_POINT}{id}")
    suspend fun getComponentUnAuth(
        @Path(value = "id") id: String
    ) : Component

    @Headers(ApiParameters.NO_AUTH_HEADER)
    @GET(ExploreConstants.MUSIC_ENDPOINT)
    suspend fun getSongs(): Response<ApiSongs>

    @POST(ExploreConstants.NOTIFICATION_REGISTER_ENDPOINT)
    suspend fun registerNotification(
        @Body tokenBody: TokenBody,
    ): ApiMessage

    @GET(ExploreConstants.NOTIFICATION_GET_ENDPOINT)
    suspend fun getNotification(): ApiNotificationWhenLoggedIn

//   eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXAiOiJhY2Nlc3MiLCJleHAiOjE2NDc1OTQ0MjgsImlkIjoiSUQ4MjU1NjA3OTAwMDAwMDAxMDQiLCJqdGkiOiIzVzU2SEFmbHZpeDFIdXRjdHc4eng3Vk16OEFCWkllTXVLakczYmlpandZPSJ9.7l1zuNi0EzRvKAS6tHkE5SY3532vV6zaaD2HfIqeZAs

//    @Headers ("${ApiParameters.AUTH_HEADER}= ${ApiParameters.TOKEN_TYPE} eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXAiOiJhY2Nlc3MiLCJleHAiOjE2NDc1OTQ0MjgsImlkIjoiSUQ4MjU1NjA3OTAwMDAwMDAxMDQiLCJqdGkiOiIzVzU2SEFmbHZpeDFIdXRjdHc4eng3Vk16OEFCWkllTXVLakczYmlpandZPSJ9.7l1zuNi0EzRvKAS6tHkE5SY3532vV6zaaD2HfIqeZAs\n" )
    @Headers(ApiParameters.NO_AUTH_HEADER)
    @GET(ExploreConstants.NOTIFICATION_GET_ENDPOINT)
    suspend fun getNotificationPlease(

    ): Response<ApiNotification>

    @POST(ExploreConstants.REGISTER_VIDEO_CATEGORY_AUTH_ENDPOINT)
    suspend fun registerCategory(
        @Body registerAuthUserBody: RegisterAuthUserBody,
    ): Response<ApiMessage>

    @Headers(ApiParameters.NO_AUTH_HEADER)
    @POST(ExploreConstants.REGISTER_VIDEO_CATEGORY_PUBLIC_ENDPOINT)
    suspend fun registerCategory(
        @Body userRegistrationBody: UserRegistrationBody,
    ): Response<ApiMessage>

    @Headers(ApiParameters.NO_AUTH_HEADER)
    @GET(ExploreConstants.VIDEO_CATEGORIES_END_POINT)
    suspend fun getVideoCategories() : Category

//    @GET(ExploreConstants.EXPLORE_VIDEOS)
//    suspend fun getExploreVideos() : ExploreVideosResponse

//    @GET("suggested/video/{id}")
//    suspend fun getDetails(
//        @Path("id") id:String
//    ) : Response<Component>

    @GET(ExploreConstants.TRENDING_ROOMS)
    suspend fun getTrendingRooms(
    ) : TrendingRooms


    @GET(ExploreConstants.TRENDING_ROOMS)
    suspend fun getTrendingRoomsPaged(
        @Query("page") page:Int
    ) : TrendingRooms


}
