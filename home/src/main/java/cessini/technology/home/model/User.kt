package cessini.technology.home.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("channel_name")val channelName: String,
    val email: String,
    @SerializedName("_id")val id: String,
    @SerializedName("name")val name: String,
   @SerializedName("profile_picture") val profilePicture: String
)