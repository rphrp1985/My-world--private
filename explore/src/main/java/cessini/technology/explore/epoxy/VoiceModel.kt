package cessini.technology.explore.epoxy

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
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
import androidx.lifecycle.viewModelScope
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.cvo.exploremodels.SearchCreatorApiResponse
import cessini.technology.explore.R
import cessini.technology.explore.states.ExploreOnClickEvents
import cessini.technology.profile.viewmodel.PublicProfileViewModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    lateinit var baseViewModel: BaseViewModel

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
        var parts= mutableListOf<String>()
        var i=0
//        while(i<arr.size){
//            var temp=""
//            while(i<arr.size && arr[i]!=' ' && arr[i]!=','){
//                while(i<expertise.length && arr[i]=='#'){
//                    i++
//                }
//                if (i<expertise.length){
//                    temp += arr[i]
//                    i++
//                }
//            }
//            i++
//            parts.add(temp)
//        }

        parts= expertise.split(',',' ').toMutableList()

        val temporary_map= mutableMapOf<Int,String>()

        for (str in parts) {
            if(str.trim()=="")
                continue

            val it = baseViewModel.getSubCatCode(str)

            val x= temporary_map[it]

            if(x==null)
                temporary_map[it]= " ${str.trim()}"
            else
                temporary_map[it]= "$x ${str.trim()}"

        }

        var ans=SpannableStringBuilder("")
        for(categories in temporary_map){

            val str= categories.value
            val cat_number= categories.key

            val spannableString = SpannableString(" $str ")
//            val drawable = ContextCompat.getDrawable(context, R.drawable.experties)
            val shape= ShapeDrawable(  OvalShape())
            shape.paint.color = Color.BLACK

                    when(cat_number){
                        1-> shape.paint.color= Color.RED
                        2-> shape.paint.color= Color.BLUE
                        3-> shape.paint.color= Color.GREEN
                        4-> shape.paint.color= Color.MAGENTA
                        5-> shape.paint.color= Color.YELLOW
                        6-> shape.paint.color= Color.CYAN
                        7-> shape.paint.color= Color.GRAY
                        else-> shape.paint.color= Color.BLACK

                    }


            val lineHeight = height
            shape?.setBounds(0, 0, lineHeight, lineHeight)
            val imageSpan = ImageSpan(shape!!, ImageSpan.ALIGN_CENTER)
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ans.append(spannableString)
        }
        return ans
    }










}
