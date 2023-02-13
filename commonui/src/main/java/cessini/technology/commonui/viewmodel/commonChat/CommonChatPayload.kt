package cessini.technology.commonui.viewmodel.commonChat

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class CommonChatPayload(
    @SerializedName("message")val message : String,
    @SerializedName("user_id")val user_id: String,
    @SerializedName("room")val room: String

){
    fun getChatPayload() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
 }
