package cessini.technology.newapi.services.explore.model.response

import cessini.technology.model.LiveRoom
import cessini.technology.newapi.services.myspace.model.response.ApiRoom
import cessini.technology.newapi.services.video.model.response.ApiVideo
import com.google.gson.annotations.SerializedName

data class ApiExplore(
    val message: ApiExploreData?,
    val status: Boolean
)

data class ApiExploreData(
    @SerializedName(value = "public_events") val publicEvents: List<ApiPublicEvent> = emptyList(),
    @SerializedName(value = "rooms")val rooms: List<ApiRoom> = emptyList(),
    @SerializedName(value = "top_profiles") val topProfiles: List<ApiTopProfile> = emptyList(),
//    val live_rooms: List<LiveRoom> = emptyList(),

//    val videos: Map<String, List<ApiVideo>> = emptyMap(),
////val videos : List<String> = emptyList(),
)

data class ApiPublicEvent(
    @SerializedName(value = "id")val id: String,
    @SerializedName(value = "image_url") val image: String,
    @SerializedName(value = "title")val title: String="",
    @SerializedName(value = "description") val description: String="",

)
data class ApiTopProfile(
    @SerializedName(value = "_id") val id: String,
    @SerializedName(value = "channel_name") val channelName: String,
    val name: String,
    @SerializedName(value = "profile_picture") val profilePicture: String,
//    @SerializedName(value = "is_following") val is_following:Boolean?,
)
