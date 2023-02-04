package cessini.technology.commonui.viewmodel.commonChat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cessini.technology.newapi.services.commonChat.CommonChatConstants
import cessini.technology.newapi.services.commonChat.CommonChatSocketHandler
import io.socket.client.Socket
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest as collectLatest

class CommonChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<CommonChatPayload>>()
    val messages: LiveData<List<CommonChatPayload>> get() = _messages

    private lateinit var mSocket: Socket

    fun setSocket(roomID: String){
        CommonChatSocketHandler.setSocket(roomID)
        if(!CommonChatSocketHandler.getSocket().connected()){
            CommonChatSocketHandler.establishConnection()
        }
        mSocket = CommonChatSocketHandler.getSocket()
    }



    fun listenTo(roomID: String){
        mSocket.on("message"){

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
    }}

    fun emitMessage(message: CommonChatPayload){
        mSocket.emit("message")
        //TODO Emit
    }


    companion object{
        const val TAG = "CommonChatVM"
    }


}