package cessini.technology.profile.`class`

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class getMessage(
    @SerializedName("page") val page:Int ,
    @SerializedName("user_id") val userid:String, @SerializedName("receiver_id") val with_userid:String) {
    fun getMessages() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}