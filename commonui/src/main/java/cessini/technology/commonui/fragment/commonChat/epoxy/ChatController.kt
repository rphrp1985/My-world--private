package cessini.technology.commonui.fragment.commonChat.epoxy

import cessini.technology.commonui.viewmodel.commonChat.CommonChatPayload
import cessini.technology.commonui.viewmodel.commonChat.CommonChatPayload2
import com.airbnb.epoxy.EpoxyController




class ChatController() : EpoxyController(){

    var items : ArrayList<CommonChatPayload2>
    private var user_id=""
    init {
        items= ArrayList()
    }

    fun update(x:ArrayList<CommonChatPayload2>, user_id:String){
        items= x
        this.user_id= user_id

    }

    fun addChatPayLoad(chatPayload: CommonChatPayload2,user_id: String){
        items.add(0,chatPayload)
        this.user_id= user_id
    }

    override fun buildModels() {

        items.forEachIndexed { index, commonChatPayload ->

            ChatModel_().id(index).messageText(commonChatPayload.message).sentbyme(commonChatPayload.user_id==user_id).name(commonChatPayload.name).addTo(this)
        }
    }

}


