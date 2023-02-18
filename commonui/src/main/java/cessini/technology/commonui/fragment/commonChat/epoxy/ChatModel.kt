package cessini.technology.commonui.fragment.commonChat.epoxy

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cessini.technology.commonui.R
import com.airbnb.epoxy.*



@EpoxyModelClass()
abstract class ChatModel :   EpoxyModelWithHolder<ChatModel.Holder>(){


    @EpoxyAttribute
    var messageText:String = "";
    @EpoxyAttribute
    var sentbyme:Boolean=false;


    override fun bind(holder: Holder) {
        holder.messageView.text= messageText

    }

    inner class Holder : EpoxyHolder(){

        lateinit var messageView: TextView

        override fun bindView(itemView: View) {

            messageView= itemView?.findViewById(R.id.message)!!



        }

    }

    override fun getDefaultLayout(): Int {
        return if(sentbyme) {
            R.layout.common_chat_item_user
        }else
         return  R.layout.common_chat_other

    }


}

