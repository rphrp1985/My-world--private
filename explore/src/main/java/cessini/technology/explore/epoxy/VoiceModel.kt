package cessini.technology.explore.epoxy

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.cvo.exploremodels.SearchCreatorApiResponse
import cessini.technology.explore.R
import cessini.technology.explore.states.ExploreOnClickEvents
import cessini.technology.profile.viewmodel.PublicProfileViewModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import java.util.HashMap

@EpoxyModelClass
abstract class VoiceModel : EpoxyModelWithHolder<VoiceModel.Holder>() {


    @EpoxyAttribute
    lateinit var img: String

    @EpoxyAttribute
    lateinit var expertise:String

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var title1: String

    @EpoxyAttribute
     var vid: String=""

    @EpoxyAttribute
    lateinit var channelName: String

    @EpoxyAttribute
     var activity: FragmentActivity?=null

    @EpoxyAttribute
    lateinit var followingStatusId: HashMap<String, Boolean>

    @EpoxyAttribute
    lateinit var onClickEvents: (event: ExploreOnClickEvents) -> Unit

    override fun getDefaultLayout(): Int {
        return R.layout.child_item_1
    }

    override fun bind(holder: Holder) {
        activity?.let {
            super.bind(holder)
            with(holder)
            {
                val publicProfileViewModel = activity?.run {
                    ViewModelProvider(this)[PublicProfileViewModel::class.java]
                }
                var signedIn = (activity as HomeActivity).baseViewModel.authFlag.value
                followButton(btn)
                Glide.with(this.image.context).load(img).into(this.image)
                this.title.text = title1
                image.setOnClickListener {
                    ProfileConstants.creatorModel = SearchCreatorApiResponse(
                        vid, title1, channelName, img
                    )
                    onClickEvents(ExploreOnClickEvents.ToGlobalProfileFlow)
                }
                btn.setOnClickListener {
                    publicProfileViewModel?.activity = activity!!
                    Log.e("VoiceModel", "ButtonClickListenerCalled Signed=${signedIn}")
                    publicProfileViewModel!!.onFollowClickExplore(vid)
                    if (signedIn == true)
                        followButtonClick(btn)
                }
            }

//            holder.expert.text= expertise
            val expertString=modifyFont(expertise,30)
            expertString.setSpan(ForegroundColorSpan(Color.rgb(35,153,234)),0,expertString.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            val final=SpannableStringBuilder("")
            final.append(expertString)
            holder.expert.setText(final,TextView.BufferType.SPANNABLE)



        }
    }

    class Holder : EpoxyHolder() {
        lateinit var image: ImageView
        lateinit var title: TextView
        lateinit var btn: Button
        lateinit var expert:TextView
        override fun bindView(itemView: View) {
            image = itemView.findViewById(R.id.imageView)
            title = itemView.findViewById(R.id.textView)
            btn = itemView.findViewById(R.id.follow)
            expert= itemView.findViewById(R.id.textView2)
        }
    }


    private fun followButton(btn: Button) {
        activity?.let {
            if (followingStatusId.get(vid) != null && followingStatusId.get(vid) == true) {
                btn.text = "Unfollow"
                btn.setBackgroundResource(R.drawable.unfollow_btn)

                btn.setTextColor(ContextCompat.getColor(it, R.color.cpTextDark))
                //btn.setTextColor(Color.rgb(118,118,118))
            } else {
                btn.text = "Follow"
                btn.setTextColor(ContextCompat.getColor(it, R.color.btnfllow))
                //btn.setTextColor(Color.rgb(239,239,239))
                btn.setBackgroundResource(R.drawable.join_myspace_upcoming)
            }
        }
    }

    private fun followButtonClick(btn: Button) {
        activity?.let {
            if (followingStatusId.get(vid) == false) {
                btn.text = "Unfollow"
                btn.setTextColor(ContextCompat.getColor(it, R.color.cpTextDark))
                //btn.setTextColor(Color.rgb(118,118,118))
                btn.setBackgroundResource(R.drawable.unfollow_btn)
                followingStatusId.put(vid, true)
            } else {
                btn.text = "Follow"
                btn.setTextColor(ContextCompat.getColor(it, R.color.btnfllow))
                //btn.setTextColor(Color.rgb(239,239,239))
                btn.setBackgroundResource(R.drawable.join_myspace_upcoming)
                followingStatusId.put(vid, false)
            }
        }
    }

    private fun modifyFont(expertise: String, height: Int): SpannableStringBuilder {
        val arr=expertise.toCharArray()
        val parts= mutableListOf<String>()
        var i=0
        while(i<arr.size){
            var temp=""
            while(i<arr.size && arr[i]!=' ' && arr[i]!=','){
                while(i<expertise.length && arr[i]=='#'){
                    i++
                }
                if (i<expertise.length){
                    temp += arr[i]
                    i++
                }
            }
            i++
            parts.add(temp)
        }
        var ans=SpannableStringBuilder("")
        for (str in parts){
            val spannableString = SpannableString(" $str ")
            val drawable = ContextCompat.getDrawable(context, R.drawable.experties)
            val lineHeight = height
            drawable?.setBounds(0, 0, lineHeight, lineHeight)
            val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_BOTTOM)
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ans.append(spannableString)
        }
        return ans
    }










}
