package cessini.technology.explore.epoxy

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.utils.Constant
import cessini.technology.explore.ExploreLoadingBindingModel_
import cessini.technology.explore.fragment.ExploreFragmentDirections
import cessini.technology.home.HomeLoadingBindingModel_
import cessini.technology.home.fragment.HomeFragmentDirections
import cessini.technology.model.ExplorePagedData
import cessini.technology.model.roomInfo
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController


class MultiGridController(val category:String,
                          var activity: FragmentActivity,
                          var fragment:Fragment
                          ):
    PagedListEpoxyController<roomInfo>() {

    var allRooms: MutableList<roomInfo> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }
//     fun buildModel() {
//        Log.d("Hemlo", "I entered")
//        allRooms.forEachIndexed { index, roomInfo ->
//            val data = roomInfo.datas
//            val size = data.size
//            //initial height=308 and weight=191
//            val controller = EpoxyController(154, 95,15f,category,fragment,true)
//            controller.roomUsers = data
//            multiGridModelRecycler {
//                id("$allRooms $index")
//                controller(controller)
//                context(fragment.context)
//              size(data.size)
//                roomTitle(roomInfo.title)
//                thumbnail(roomInfo.thumbnail)
//                clickListener{ _, ->
//                    var t1=""
//                    Log.e("MultiGridController","category rooms on click listener called")
//                    fragment.let {
//                        Constant.home_fragment_live= false
////                        Constant.keyword= roomInfo.
//
////                        it.findNavController().navigate("cessini.technology.home.fragment.homefragment")
//                        it.findNavController().navigate(
//                            ExploreFragmentDirections.actionExploreFragmentToLiveFragment()
//                        )
//
//                    }
//                }
//            }
//        }
//
//    }

    override fun buildItemModel(index: Int, roomInfo: roomInfo?): EpoxyModel<*> {



        roomInfo?.let {
            val data = roomInfo.datas
            val size = data.size
            //initial height=308 and weight=191
            val controller = EpoxyController(154, 95,15f,category,fragment,true)
            controller.roomUsers = data
           return MultiGridModelRecycler_().id(" $index").controller(controller)
                .context(fragment?.context).size(data.size).roomTitle(roomInfo.title)
                .thumbnail(roomInfo.thumbnail).clickListener { _, ->
                var t1 = ""
                Log.e("MultiGridController", "category rooms on click listener called")
                fragment.let {
                    Constant.home_fragment_live = false
//                        Constant.keyword= roomInfo.

//                        it.findNavController().navigate("cessini.technology.home.fragment.homefragment")
                    it?.findNavController()?.navigate(
                        ExploreFragmentDirections.actionExploreFragmentToLiveFragment()
                    )

                }
            }

        }
        return ExploreLoadingBindingModel_().id("Loading$index")


//        multiGridModelRecycler {
//            id("$allRooms $index")
//            controller(controller)
//            context(fragment.context)
//            size(data.size)
//            roomTitle(roomInfo.title)
//            thumbnail(roomInfo.thumbnail)
//            clickListener{ _, ->
//                var t1=""
//                Log.e("MultiGridController","category rooms on click listener called")
//                fragment.let {
//                    Constant.home_fragment_live= false
////                        Constant.keyword= roomInfo.
//
////                        it.findNavController().navigate("cessini.technology.home.fragment.homefragment")
//                    it.findNavController().navigate(
//                        ExploreFragmentDirections.actionExploreFragmentToLiveFragment()
//                    )
//
//                }
//            }
//        }

    }



    override fun addModels(models: List<EpoxyModel<*>>) {


        super.addModels(
            models.plus(
                ExploreLoadingBindingModel_().id("loading")
            ).distinct()
        )

    }
}
