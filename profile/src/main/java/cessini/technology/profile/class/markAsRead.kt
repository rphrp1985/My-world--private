package cessini.technology.profile.`class`

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class markAsRead (
    @SerializedName("user_id") val userid:String, @SerializedName("message_id") val messagID:String) {
    fun getMessages() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}