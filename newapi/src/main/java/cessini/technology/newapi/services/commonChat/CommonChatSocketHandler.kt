package cessini.technology.newapi.services.commonChat

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.concurrent.Flow

object CommonChatSocketHandler {

    lateinit var mSocket: Socket
    const val TAG = "CommonChatSocketHandler"
    @Synchronized
    fun setSocket(roomID: String) {
        try {
            val param : IO.Options = IO.Options()
            param.query = "room=$roomID"
            mSocket = IO.socket(CommonChatConstants.BASE_ENDPOINT,param)
        } catch (e: URISyntaxException) {
                Log.d(TAG,e.localizedMessage,e)
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }

    fun chats() = callbackFlow {
        mSocket.on("message"){
            val json= it[0] as JSONObject
            trySendBlocking(json)
        }
        awaitClose { mSocket.off("updatechat") }
    }


}