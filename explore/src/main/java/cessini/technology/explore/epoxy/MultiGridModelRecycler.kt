package cessini.technology.explore.epoxy

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import cessini.technology.explore.R
import cessini.technology.home.fragment.HomeFragment
import cessini.technology.model.roomInfo
//import cessini.technology.myspace.generated.callback.OnClickListener
import com.airbnb.epoxy.*
import com.airbnb.epoxy.EpoxyController
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily


@EpoxyModelClass
abstract class MultiGridModelRecycler : EpoxyModelWithHolder<MultiGridModelRecycler.Holder>() {
    @EpoxyAttribute
    lateinit var controller: EpoxyController
  @EpoxyAttribute
  var size :Int =0
    @EpoxyAttribute
    lateinit var roomTitle :String

    @EpoxyAttribute
    lateinit var thumbnail :String

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener
    @EpoxyAttribute
    lateinit var ontouch:View.OnTouchListener
    override fun bind(holder: Holder) {
//        holder.recyclerView.setController(controller)
//        var layoutManager: GridLayoutManager = GridLayoutManager(holder.recyclerView.context, 2)
//        if (size <= 2)
//            layoutManager = GridLayoutManager(holder.recyclerView.context, 1)
//        else if (size % 2 != 0) {
//            layoutManager = GridLayoutManager(holder.recyclerView.context, 2)
//
//            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//                override fun getSpanSize(position: Int): Int {
//                    when (position) {
//                        0 -> return 2
//                        else -> return 1
//                    }
//                }
//            }
//        }
//        holder.recyclerView.layoutManager= layoutManager


try {
    Glide
        .with(context)
        .load(thumbnail)
        .centerCrop()
        .into(holder.recyclerView);
}catch (e:Exception){}
        holder.textView.text= roomTitle
        holder.textView.setOnClickListener(clickListener)
        holder.recyclerView.isClickable=true
        holder.recyclerView.setOnClickListener(clickListener)

        holder.recyclerView.shapeAppearanceModel =
            holder.recyclerView.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, convertDpToPixel(10F))
                .setTopRightCorner(CornerFamily.ROUNDED, convertDpToPixel(10F))
                .setBottomLeftCorner(CornerFamily.ROUNDED, convertDpToPixel(10F))
                .setBottomRightCorner(CornerFamily.ROUNDED, convertDpToPixel(10F))
                .build()

    }


    private fun convertDpToPixel(dp: Float): Float {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val px: Float = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }


    override fun getDefaultLayout(): Int {
        return R.layout.gridepoxy
    }
    class Holder : EpoxyHolder() {
        lateinit var recyclerView: ShapeableImageView
        lateinit var textView: TextView
        lateinit var cardView: MaterialCardView
        override fun bindView(itemView: View) {
            recyclerView = itemView.findViewById(R.id.samplepoxyview)
            textView= itemView.findViewById(R.id.roomtitle)
            cardView = itemView.findViewById(R.id.cardview)
        }
    }
}