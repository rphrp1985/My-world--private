package cessini.technology.commonui.viewmodel.commonChat

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cessini.technology.commonui.fragment.commonChat.epoxy.ChatController
//import cessini.technology.commonui.fragment.commonChat.epoxy.ChatController
import cessini.technology.newapi.services.commonChat.CommonChatConstants
import cessini.technology.newapi.services.commonChat.CommonChatSocketHandler
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlinx.coroutines.flow.collectLatest as collectLatest

class CommonChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<CommonChatPayload>>()
    val messages: LiveData<List<CommonChatPayload>> get() = _messages
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
        mSocket.on("connect_error", onConnectError)
        mSocket.on("connect_timeout", onConnectError)

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

    fun addMessage(chatPayload: CommonChatPayload){
        Log.d(TAG,"addmessage() user_id = $user_id")
        chatController.addChatPayLoad(chatPayload,user_id)
        chatController.requestModelBuild()
//        val chats= mutableListOf<CommonChatPayload>()
//        _messages.value?.let { chats.addAll(it) }
//        chats.add(chatPayload)
//        _messages.value= chats

    }


    fun listenTo(roomID: String){
        mSocket.on("message"){

            Log.d(TAG,"new message")
            fun listentochats() = viewModelScope.launch {
            val chats = mutableListOf<CommonChatPayload>()
            CommonChatSocketHandler.chats().collectLatest {
              val chatload= CommonChatPayload(

                  message = it.getString("message"),
                  room= it.getString("room"),
                  user_id = it.getString("user_id")
              )
            chats.add(chatload)

            }
                _messages.value= chats

        }
    }

        mSocket.on("delivered"){


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