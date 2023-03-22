package cessini.technology.home.webSockets.model

import cessini.technology.home.model.User
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class DataResponse(
    @SerializedName("__v") val __v: Int,//
    @SerializedName("_id") val _id: String,//
    @SerializedName("admin") val admin: String,//
    @SerializedName("adminSocket") val adminSocket: String,//
    @SerializedName("allowedUsers") val allowedUsers: List<AllowedUserResponse>,//
    @SerializedName("chat") val chat: String,
    @SerializedName("createdAt") val createdAt: String,//
    @SerializedName("description") val description: String,
    @SerializedName("live") val live: Boolean,//
    @SerializedName("messages") val messages: List<MessageResponse>,
    @SerializedName("otherUsers") val otherUsers: List<Any>,
    @SerializedName("room_code") val room: String,//
    @SerializedName("sub_category") val sub_category: List<String>,
    @SerializedName("title") val title: String,//
    @SerializedName("schedule") val schedule:Long,//
    @SerializedName("private") val private:Boolean,//
    @SerializedName("category") val category: List<String>,//
    @SerializedName("creator") val creator: User,//
    @SerializedName("dash") val dash:String,//
    @SerializedName("hls") val hls:String,
    @SerializedName("flv") val flv:String



)


//"listeners": [],
//"sub_category": [
//"1",
//"5",
//"3",
//"4"
//],


//"otherUsers": [],
//"screenShared": [],
//"message": [],


//"messages": [],
//"hls": "https://stream.joinmyworld.in/hls/Prianshu_Prasad_bjh_837937400574636_1677515605.m3u8",
//"dash": "https://stream.joinmyworld.in/dash/Prianshu_Prasad_bjh_837937400574636_1677515605.mpd"
