package cessini.technology.newapi.services.myspace.model.response

import com.google.gson.annotations.SerializedName

data class ApiRoomRequests(
    val message: String,
    val data: List<RoomRequestData> = emptyList(),
    val status: Boolean,
)

data class RoomRequestData(
    @SerializedName(value="_id") val _id:ID,
    @SerializedName(value="id") val id:String,
    @SerializedName(value = "listeners_pending") val requests : List<ApiRequestProfile>,
)
data class ID(
    @SerializedName(value = "\$oid") val id:String
)

data class ApiRequestProfile(
    @SerializedName(value = "socket") val socket: String,
    @SerializedName(value = "id") val id: String,
    @SerializedName(value = "name") val name: String,
    @SerializedName(value = "email") val email: String,
    @SerializedName(value = "channelName") val channelName: String,
    @SerializedName(value = "profilePicture") val profilePicture: String,
    @SerializedName(value = "isMuted") val isMuted: Boolean,
    @SerializedName(value = "isNotCamera") val isNotCamera: Boolean,
    @SerializedName(value = "isHandRaised") val isHandRaised: Boolean,
    @SerializedName(value = "isScreenShared") val isScreenShared: Boolean,
    @SerializedName(value = "isHost") val isHost: Boolean,
)

data class ApiStreamKey(
    @SerializedName(value = "data") val data:String
)