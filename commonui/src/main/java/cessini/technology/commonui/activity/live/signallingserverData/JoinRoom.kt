package cessini.technology.commonui.activity.live.signallingserverData

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class JoinRoom (

    val room_code: String,
    val user: SGCUser,
   val email: String,

        ){
     fun getJson() : JSONObject {
         val jsonString = Gson().toJson(this)  // json string
         return JSONObject(jsonString)
     }
 }

class SGCUser(
     val id: String,
     val name: String,
     val email: String,
     val channelName: String,
     val profilePicture: String

){
    fun getJson() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}

class Mic(
    room: String,
    value: Boolean,
    url: String
){
    fun getJson() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}

class Camera(
    room: String,
    value: Boolean,
    url: String
)
{
    fun getJson() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }
}






