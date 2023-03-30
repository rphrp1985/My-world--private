package cessini.technology.commonui.activity

//import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cessini.technology.commonui.R
import cessini.technology.commonui.utils.networkutil.TAG
import coil.load
import coil.transform.CircleCropTransformation
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import de.hdodenhof.circleimageview.CircleImageView
import org.webrtc.*
import java.util.Objects


@EpoxyModelClass
abstract class EpoxyModel : EpoxyModelWithHolder<EpoxyModel.Holder>() {
    @EpoxyAttribute
    var creator: Boolean= false
    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    lateinit var context:EglBase.Context
    @EpoxyAttribute
     lateinit var track:VideoTrack

    @EpoxyAttribute
    lateinit var height1: Integer

    @EpoxyAttribute
    lateinit var width1: Integer

    @EpoxyAttribute
    lateinit var name1: String

    @EpoxyAttribute
    lateinit var profile_picture:String

    @EpoxyAttribute
    var videoSwitch:Boolean=true

    @EpoxyAttribute
    var handSwitch:Boolean=false

    @EpoxyAttribute
    var microphoneSwitch:Boolean=true

    @EpoxyAttribute
     var imageId: Int =-1

    @EpoxyAttribute
    lateinit var context1: Context

    @EpoxyAttribute
    lateinit var gridActivity: GridActivity

    @EpoxyAttribute
    var tL: Float = 0f

    @EpoxyAttribute
    var tR: Float = 0f

    @EpoxyAttribute
    var bL: Float = 0f

    @EpoxyAttribute
    var bR: Float = 0f

    var isVideoLoaded= false

    override fun getDefaultLayout(): Int
    {
        if(creator==true)
            return R.layout.card_layout_host
        else
            return R.layout.card_layout
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
            if (imageId != -1) {
                holder.imgPerson.setMirror(true)
                try {
                    holder.imgPerson.release()
                    holder.imgPerson.init(context, null)
                    holder.imgPerson.setEnableHardwareScaler(true)
                    holder.imgPerson.setZOrderMediaOverlay(true)
                    track.addSink(holder.imgPerson)

//                    val thread= Thread.currentThread()
                    holder.imgPerson.addFrameListener({
                          isVideoLoaded= true
                gridActivity.runOnUiThread {
                    holder.gif_image.visibility= View.GONE
                    holder.name.visibility= View.VISIBLE
                    holder.name_loading.visibility= View.GONE
                    holder.imgPerson.removeFrameListener {  }
                }
                    },1f)


                } catch (e: Exception) {
                    Log.d(TAG,e.message.toString())
                }

                holder.cardView.layoutParams.height = height1.toInt()
                holder.cardView.layoutParams.width = width1.toInt()
                holder.name.text = name1
                holder.name.visibility= View.GONE
                holder.name_loading.text= name1
                holder.profilePic.load(profile_picture){
                    transformations(CircleCropTransformation())
                }

                try {

                    Glide.with(context1).load(cessini.technology.commonui.R.drawable.gif_temp).centerCrop().into(holder.gif_image)
                }catch (e:Exception) {
                    Log.d(TAG, e.message.toString())
                }


                if (!videoSwitch){
                    holder.profilePic.visibility=View.VISIBLE
                    holder.gif_image.visibility= View.GONE
//                    holder.imgPerson.visibility= View.GONE
                }
                else{
                    holder.profilePic.visibility=View.INVISIBLE
                    if(!isVideoLoaded)
                    holder.gif_image.visibility= View.VISIBLE
//                    holder.imgPerson.visibility= View.VISIBLE
                }

                if (!microphoneSwitch){
                    holder.audio.setImage(cessini.technology.commonui.R.drawable.ic_removeaudio)
                }
                else{
                    holder.audio.setImage(cessini.technology.commonui.R.drawable.ic_addaudio)
                }

                if (!handSwitch){
                    holder.handRaise.visibility=View.INVISIBLE
                }
                else{
                    holder.handRaise.visibility=View.VISIBLE
                }

                holder.cardView.shapeAppearanceModel =
                    holder.cardView.shapeAppearanceModel.toBuilder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, tL)
                        .setTopRightCorner(CornerFamily.ROUNDED, tR)
                        .setBottomLeftCorner(CornerFamily.ROUNDED, bL)
                        .setBottomRightCorner(CornerFamily.ROUNDED, bR)
                        .build()

            }

    }




//    override fun unbind(holder: Holder) {
//        track.removeSink(holder.imgPerson)
//        super.unbind(holder)
//
//    }

    class Holder() : EpoxyHolder() {
        lateinit var imgPerson: SurfaceViewRenderer
        lateinit var name: TextView
//        lateinit var profession: SurfaceViewRenderer
        lateinit var cardView: MaterialCardView
        lateinit var profilePic: CircleImageView
        lateinit var audio:ImageView
        lateinit var handRaise:ImageView
        lateinit var gif_image:ImageView
        lateinit var name_loading:TextView
        lateinit var contraint_layout:ConstraintLayout
        override fun bindView(itemView: View) {
            cardView = itemView.findViewById(R.id.cardView)
            imgPerson = itemView.findViewById(R.id.localView)
            name = itemView.findViewById(R.id.name)
            profilePic=itemView.findViewById(R.id.profile_pic)
            audio=itemView.findViewById(R.id.audio)
            handRaise=itemView.findViewById(R.id.iv_hand)
            contraint_layout=itemView.findViewById(R.id.layout)
            gif_image= itemView.findViewById(R.id.gif_image)
            name_loading = itemView.findViewById(R.id.name_loading)
//          profession = itemView.findViewById(R.id.rView)
        }
    }
}



