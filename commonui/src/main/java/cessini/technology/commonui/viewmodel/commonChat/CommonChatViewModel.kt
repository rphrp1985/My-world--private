package cessini.technology.commonui.viewmodel.commonChat

//import cessini.technology.commonui.fragment.commonChat.epoxy.ChatController
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cessini.technology.commonui.fragment.commonChat.epoxy.ChatController
import cessini.technology.newapi.services.commonChat.CommonChatSocketHandler
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class CommonChatViewModel : ViewModel() {

     val messages = MutableLiveData<CommonChatPayload>()
//    val messages: LiveData<CommonChatPayload> get() = _messages
    val chatController:ChatController = ChatController()
    var roomID:String="Room Live"
    var user_id:String ="user_id"

    private lateinit var mSocket: Socket

    fun setSocket(roomID: String){
        CommonChatSocketHandler.setSocket(roomID)

        if(!CommonChatSocketHandler.getSocket().connected()){
            Log.d(TAG,"not Connected")
            CommonChatSocketHandler.establishConnection()
        }else{
            Log.d(TAG,"Connected")
        }
        mSocket = CommonChatSocketHandler.getSocket()

        mSocket.on("connect", onConnect)
//        mSocket.on("connect_error", onConnectError)
//        mSocket.on("connect_timeout", onConnectError)

    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG,"onconnect socket   ")
        for(i in it){
        Log.d(TAG,"onconnect  $i ")
    }

    }

    private val onConnectError = Emitter.Listener {

        for(i in it) {
            Log.d(TAG, "onconnection error $i")
        }

    }

    fun addMessage(chatPayload: CommonChatPayload2){
        Log.d(TAG,"addmessage() user_id = $user_id")
        chatController.addChatPayLoad(chatPayload,user_id)
        chatController.requestModelBuild()
        if(chatPayload.user_id!=user_id){
//            Handler(Looper.getMainLooper()).post {
//                messages.value= chatPayload
//            }
        }

//        val chats= mutableListOf<CommonChatPayload>()
//         chats.addAll(chatController.items)
//        _messages.value= chats
//        chats.add(chatPayload)
//        _messages.value= chats

    }


    fun listenTo(roomID: String){
        mSocket.on("message"){

            if(!it.isEmpty()){

                val json= JSONObject(it!![0].toString())

                if(json.optString("user_id")!=user_id){

                addMessage(CommonChatPayload2(json.optString("message"),json.optString("user_id"),roomID,json.optString("name")))
                }
            }


//            Log.d(TAG,"new message")
//            fun listentochats() = viewModelScope.launch {
//            val chats = mutableListOf<CommonChatPayload>()
//            CommonChatSocketHandler.chats().collectLatest {
//              val chatload= CommonChatPayload(
//
//                  message = it.getString("message"),
//                  room= it.getString("room"),
//                  user_id = it.getString("user_id")
//              )
//            chats.add(chatload)
//

//                _messages.value= chats


    }


    }


    fun emitMessage(message: CommonChatPayload){

        mSocket.emit("message",message.getChatPayload())
        Log.d(TAG,"emit Message")
        //TODO Emit
    }


    companion object{
        const val TAG = "CommonChatVM"
    }


}