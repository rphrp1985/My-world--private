package cessini.technology.newapi.services.myspace

import cessini.technology.newapi.ApiParameters
import cessini.technology.newapi.services.timeline_room.RoomConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RoomSocket {

//    @Headers("Accept: */*")
    @GET(value =  RoomConstants.GET_STREAM_KEY)
    suspend fun getKey(
        @Query(value = "room_code")
        room: String,
        @Query(value = "email")
        email: String): Call<String>
}