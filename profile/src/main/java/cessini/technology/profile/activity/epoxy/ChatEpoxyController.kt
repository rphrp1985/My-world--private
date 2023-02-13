package cessini.technology.profile.activity.epoxy

import android.util.Log
import androidx.lifecycle.MutableLiveData
import cessini.technology.profile.ItemChatMeBindingModel_
import cessini.technology.profile.ItemChatOtherBindingModel_
import cessini.technology.profile.ItemChatShimmerBindingModel_
import cessini.technology.profile.databinding.ItemChatMeBinding
import cessini.technology.profile.fragment.publicProfile.ResponseMessageJson
import cessini.technology.profile.fragment.publicProfile.SimpleModel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController


/**
 * EpoxyController which works with PagedLists
 */
class ChatEpoxyController(val idMe: String,val idOth: String,val chatMessageState: CanLoad) : PagedListEpoxyController<ResponseMessageJson>() {

    val allmessages:ArrayList<ResponseMessageJson> = ArrayList()


    override fun buildItemModel(currentPosition: Int, item: ResponseMessageJson?): EpoxyModel<*> {
        item?.let {
            allmessages.add(item)
            return  if(item.user_id==idMe)
            ItemChatMeBindingModel_().id(item._id).sent(item.message)
            else
                ItemChatOtherBindingModel_().id(item._id).recieved(SimpleModel(item.user_id,item.message)).sentPhoto(item.profile_picture)

        } ?: run {
            return ItemChatShimmerBindingModel_().id(currentPosition)
        }
    }



    override fun addModels(models: List<EpoxyModel<*>>) {
        Log.d("EPOXYCONTROLLER","addmodels called")
        if (chatMessageState.canLoadMore) {
            super.addModels(
                models.plus(
                    ItemChatShimmerBindingModel_().id("loading")
                ).distinct()
            )
         } else {
            super.addModels(models.distinct())
         }
    }

    override fun add(model: EpoxyModel<*>) {
        Log.d("EPOXYCONTROLLER","add called")
        super.add(model)
    }






    override fun onExceptionSwallowed(exception: RuntimeException) {}
    }
