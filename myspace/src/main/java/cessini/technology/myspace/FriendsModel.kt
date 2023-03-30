package cessini.technology.myspace

import android.net.Uri
import android.view.View
import android.widget.TextView
import cessini.technology.commonui.activity.setImage
import cessini.technology.model.PreviousProfile
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

@EpoxyModelClass()
abstract class FriendsModel : EpoxyModelWithHolder<FriendsModel.FriendsModelHolder>() {

    @EpoxyAttribute
    lateinit var data: PreviousProfile

    override fun getDefaultLayout(): Int {
        return R.layout.myspace_view_holder_friends
    }
    override fun bind(holder: FriendsModelHolder) {
        Glide.with(holder.picture.context)
            .load(data.picture)
            .into(holder.picture)
//        holder.picture.setImage(Uri.parse(data.picture).to)
        holder.name.text = data.name
        holder.channelName.text=data.channel
        holder.followers.text=data.follower.toString()
        holder.following.text=data.following.toString()
    }

    inner class FriendsModelHolder : EpoxyHolder() {
        lateinit var picture: CircleImageView
        lateinit var name: TextView
        lateinit var channelName: TextView
        lateinit var followers: TextView
        lateinit var following: TextView

        override fun bindView(itemView: View) {
            picture=itemView.findViewById(R.id.userImage)
            name=itemView.findViewById(R.id.user_name)
            channelName=itemView.findViewById(R.id.user_channel_search)
            followers=itemView.findViewById(R.id.profile_follower_count)
            following=itemView.findViewById(R.id.profile_following_count)
        }
    }
}
