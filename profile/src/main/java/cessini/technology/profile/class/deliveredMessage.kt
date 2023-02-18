package cessini.technology.profile.`class`

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class deliveredMessage (
    @SerializedName("message_ids") val message_ids:List<String>
) {
    fun getMessages() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}