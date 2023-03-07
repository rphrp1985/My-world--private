package cessini.technology.commonui.activity

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cessini.technology.commonui.R
import coil.load
import coil.transform.CircleCropTransformation
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import de.hdodenhof.circleimageview.CircleImageView
import org.kurento.client.RecorderEndpoint
import org.kurento.client.WebRtcEndpoint
import org.webrtc.*

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
    var tL: Float = 0f

    @EpoxyAttribute
    var tR: Float = 0f

    @EpoxyAttribute
    var bL: Float = 0f

    @EpoxyAttribute
    var bR: Float = 0f


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

//                    GridActivity.modelProcessed++

//                    holder.imgPerson.addFrameListener(
//                        object :
//                            EglRenderer.FrameListener {
//                            override fun onFrame(p0: Bitmap?) {
//
//                                ThreadUtils.runOnUiThread {
//
//                                    GridActivity.screenShot.value!!.add(p0)
//                                    GridActivity.screenShot.value = GridActivity.screenShot.value
//
//                                    holder.imgPerson.removeFrameListener(this)
//                                }
//                            }
//
//                        }, 1.0f
//
//                    )




                } catch (e: Exception) {
                }

                holder.cardView.layoutParams.height = height1.toInt()
                holder.cardView.layoutParams.width = width1.toInt()
                holder.name.text = name1
                holder.profilePic.load(profile_picture){
                    transformations(CircleCropTransformation())
                }

                if (!videoSwitch){
                    holder.contraint_layout.visibility=View.VISIBLE

                }
                else{
                    holder.contraint_layout.visibility=View.INVISIBLE
                }

                if (!microphoneSwitch){
                    holder.audio.setImage(R.drawable.ic_removeaudio)
                }
                else{
                    holder.audio.setImage(R.drawable.ic_addaudio)
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
        lateinit var contraint_layout:ConstraintLayout
        override fun bindView(itemView: View) {
            cardView = itemView.findViewById(R.id.cardView)
            imgPerson = itemView.findViewById(R.id.localView)
            name = itemView.findViewById(R.id.name)
            profilePic=itemView.findViewById(R.id.profile_pic)
            audio=itemView.findViewById(R.id.audio)
            handRaise=itemView.findViewById(R.id.iv_hand)
            contraint_layout=itemView.findViewById(R.id.layout)
//          profession = itemView.findViewById(R.id.rView)
        }
    }
}



