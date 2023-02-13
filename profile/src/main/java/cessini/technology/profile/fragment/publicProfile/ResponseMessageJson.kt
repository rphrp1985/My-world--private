package cessini.technology.profile.fragment.publicProfile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseMessageJson (

//    @SerializedName("message")
//    @Expose
    val message:String ,
//    @SerializedName("user_id")
//    @Expose
    val user_id:String ,
//    @SerializedName("receiver_id")
//    @Expose
    val receiver_id: String,
//    @SerializedName("name")
//    @Expose
    val name:String ,
//    @SerializedName("profile_picture")
//    @Expose
    val profile_picture: String ,
//    @SerializedName("mark_as_read")
//    @Expose
    val mark_as_read:Boolean ,
//    @SerializedName("is_delivered")
//    @Expose
    val is_delivered: Boolean,
//    @SerializedName("created_at")
//    @Expose
    val created_at:String ,
//    @SerializedName("_id")
//    @Expose
    val _id:String
)