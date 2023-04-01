package cessini.technology.myspace

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import cessini.technology.model.PreviousProfile
import cessini.technology.model.Profile
import cessini.technology.myspace.create.CreateRoomFragment
import cessini.technology.myspace.create.ShareFragment
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

@EpoxyModelClass()
abstract class FriendsModel(user_id:String,private val context: Context): EpoxyModelWithHolder<FriendsModel.FriendsModelHolder>() {

    lateinit var mSocket: io.socket.client.Socket

    @EpoxyAttribute
    lateinit var data: PreviousProfile

    @EpoxyAttribute
    lateinit var fragment: ShareFragment

    @EpoxyAttribute
    lateinit var userId: Profile
    val id=user_id
    override fun getDefaultLayout(): Int {
        return R.layout.myspace_view_holder_friends
    }

    override fun bind(holder: FriendsModelHolder) {
        super.bind(holder)
        setSocket(id)
        mSocket.on("connected"){

        }
        Glide.with(holder.picture.context)
            .load(data.picture)
            .into(holder.picture)
//        holder.picture.setImage(Uri.parse(data.picture).to)
        holder.name.text = data.name
        holder.channelName.text = data.channel
        holder.followers.text = data.follower.toString()
        holder.following.text = data.following.toString()

        holder.send.setOnClickListener {
            if (holder.send.text == "Send") {
                sendMessage()
                holder.send.text = "Undo"
                Toast.makeText(context,"Room link sent successfully",Toast.LENGTH_SHORT).show()
            } else {
                holder.send.text = "Send"
            }
        }

    }
    fun sendMessage(){
        if (mSocket.connected()) {
            Log.d("socket", "already connected ")
            mSocket.emit("message", message(id,
                data.id,"https://www.myworld.com/liveRoom?code=${CreateRoomFragment.current_room_code}").getMessage())

        }
    }

    inner class FriendsModelHolder : EpoxyHolder() {
        lateinit var picture: CircleImageView
        lateinit var name: TextView
        lateinit var channelName: TextView
        lateinit var followers: TextView
        lateinit var following: TextView
        lateinit var constraintLayout: ConstraintLayout
        lateinit var send: AppCompatButton

        override fun bindView(itemView: View) {
            send = itemView.findViewById(R.id.button4)
            constraintLayout = itemView.findViewById(R.id.cl_item)
            picture = itemView.findViewById(R.id.userImage)
            name = itemView.findViewById(R.id.user_name)
            channelName = itemView.findViewById(R.id.user_channel_search)
            followers = itemView.findViewById(R.id.profile_follower_count)
            following = itemView.findViewById(R.id.profile_following_count)
        }
    }

    fun setSocket(idme:String){
        Socket.setSocket(idme)
        Socket.establishConnection()
        mSocket = Socket.getSocket()
    }
}
class message(@SerializedName("user_id") val sender_id:String,
              @SerializedName("receiver_id")  val receiver_id:String,
              @SerializedName("message")  val message:String) {

    fun getMessage() : JSONObject {
        val jsonString = Gson().toJson(this)  // json string
        return JSONObject(jsonString)
    }


}
