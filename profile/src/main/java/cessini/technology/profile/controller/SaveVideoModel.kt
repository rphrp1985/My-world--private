package cessini.technology.profile.controller

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cessini.technology.profile.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide

@EpoxyModelClass
abstract class SaveVideoModel : EpoxyModelWithHolder<SaveVideoModel.Holder>(){

    @EpoxyAttribute
    lateinit var gridimgId :String
    @EpoxyAttribute
    lateinit var title1 :String
    @EpoxyAttribute
    lateinit var category1:String
    @EpoxyAttribute
    lateinit var subcategory1:String


    override fun getDefaultLayout(): Int
    {

        return R.layout.user_save_video
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        with(holder)
        {
            Glide.with(this.gridImage.context).load(gridimgId).into(this.gridImage)
            this.title.text= title1
            this.category.text= category1
            this.hostName.text= subcategory1

        }

    }

    class Holder : EpoxyHolder() {
        lateinit var gridImage : ImageView
        lateinit var title: TextView
        lateinit var category: TextView
        lateinit var subcategory: TextView
        lateinit var hostName: TextView
        override fun bindView(itemView: View) {
            gridImage= itemView.findViewById(R.id.gridImage_container)
            title= itemView.findViewById(R.id.title)
            category= itemView.findViewById(R.id.category)
            subcategory= itemView.findViewById(R.id.subcategory)
            hostName = itemView.findViewById(R.id.hostname)
        }
    }
}