package cessini.technology.explore.controller

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.cvo.exploremodels.CategoryModel
import cessini.technology.explore.R
import cessini.technology.explore.adapter.MainRecyclerHeaderAdapter
import cessini.technology.explore.childViewItem
import cessini.technology.explore.childViewItemPaged
import cessini.technology.explore.databinding.HeaderRefreshExploreBinding
import cessini.technology.explore.epoxy.*
import cessini.technology.explore.headerViewItem
import cessini.technology.explore.states.ExploreOnClickEvents
import cessini.technology.explore.viewmodel.ExploreSearchViewModel
import cessini.technology.explore.viewmodel.SearchViewModel
import cessini.technology.model.*
import cessini.technology.newrepository.explore.ExploreRepository
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.airbnb.epoxy.*
import kotlin.math.min

class MainRecyclerViewController(
    var context: Context?,
    var viewModel: SearchViewModel?,
    var activity: FragmentActivity?,
    val fragment: Fragment,
    val roomRepository: RoomRepository,
    val userIdentifierPreferences: UserIdentifierPreferences,
    var videoViewModel: ExploreSearchViewModel,
    val exploreRepository: ExploreRepository,
    val baseViewModel: BaseViewModel,
    var onClickListener: (event: ExploreOnClickEvents) -> Unit,

) : TypedEpoxyController<Explore>() {

//    var allCategory = Explore()
//        set(value) {
//            field = value.copy(videos = value.videos.filter { it.second.isNotEmpty() })
//            //requestModelBuild()
//        }


    override fun buildModels(allCategory: Explore) {

        setHeader(allCategory)
        // Live
        // Rooms

        val TrendingRoomsCongroller= viewModel!!.TrendingRooms(context!!,fragment!!,onClickListener,activity!!, baseViewModel)

        childViewItemPaged {
            id("room")
            categoryTitle("Trending Rooms")
            visible(1)
            size(" ")
            childController(TrendingRoomsCongroller)
        }

        //Live rooms with thumbnal




        val multiGridController= viewModel!!.SuggestedLiveRoom(fragment!!, activity!!, baseViewModel)

//        allCategory.categoryRooms.forEachIndexed { index, room ->
//
//            multiGridController.allRooms = room.rooms
            suggestedLiveMyspaceModelRecycler {
                id("1" + "Trending Technology ")
                maincontroller(multiGridController)
                categoryTitle("Trending Technology")
                onClickListener { _ ->
                    var t1 = ""
                    Log.e("MainRVController", "category rooms on click listener called")

                    onClickListener(
                        ExploreOnClickEvents.ExploreFragmentToLiveFragment(
                            "Trending Technology","Trending Technology"
                        )
                    )
                }

            }
//        }


        // List Of Video Stories
        val showVideos: (List<Pair<String, List<Video>>>) -> Unit = {
            it.forEachIndexed { index, categoryItem ->
                childViewItem {
                    id(index)
                    visible(0)
                    categoryTitle(categoryItem.first)
                    size("${categoryItem.second.size} videos")
                    val childRecyclerViewController =
                        ChildRecyclerViewController(
                            context!!,
                            2,
                            viewModel!!,
                            activity!!,
                            fragment,
                            roomRepository,
                            userIdentifierPreferences,
                            onClickListener
                        )
                    childRecyclerViewController.categoryVideoList =
                        categoryItem.second.toMutableList()
                    childRecyclerViewController.category =
                        CategoryModel(categoryItem.first.hashCode(), categoryItem.first)
                    childController(childRecyclerViewController)
                }
            }
        }

        // For sorting all categories by their list size
        val videos = allCategory.videos.sortedByDescending {
            it.second.size
        }

        videos.subList(0, min(2, allCategory.videos.size)).let(showVideos)

        // upcoming room epoxy

        val upcomingCongroller= viewModel!!.UpcomingRooms(context!!,fragment!!,onClickListener,activity!!, baseViewModel)

        childViewItemPaged {
            Log.e("Upcoming", "It is here")
            id("upcoming")
            categoryTitle("Upcoming Hub")
            visible(0)
            size(" ")
            childController(upcomingCongroller)
        }

//        videos.subList(min(2, allCategory.videos.size), min(4, allCategory.videos.size - 2))
//            .let(showVideos)


        // voices to follow pages epoxy

     val top_profilechildRecyclerViewController= viewModel!!.Top_profiles(context!!,fragment,onClickListener,activity!!, baseViewModel)

        childViewItemPaged {
            Log.e("Voices", allCategory.topProfiles.toMutableList().toString())
            id("1")
            categoryTitle("Voices to Follow")
            visible(0)
            size(" ")
//            childRecyclerViewController.topProfiles = allCategory.topProfiles.toMutableList()
            childController(top_profilechildRecyclerViewController)
        }



        videos.subList(min(4, allCategory.videos.size), allCategory.videos.size)
            .let(showVideos)


//        for(i in 0..4){
//            var item = emptyList<HealthFitness>()
//            if(i==0)
//            {
//            item=data.items.trendingTechnology!!
//            }
//            if(i==1)
//                item=data.items.healthFitness!!

//            carousel {
//                id("This is a ViewPager.")
//                hasFixedSize(true)
//                paddingRes(R.dimen.view_pager_item_padding)
//                Log.e("View Pager","inside viewpager component")
//                        models(allCategory.items!!.mapIndexed() { index, item ->
//                            ViewPagerItem_()
//                                .id(index)
//                                .title(item)
//                                .context(this@MainRecyclerViewController.context)
//                                .onVisibilityStateChanged { _, _, visibilityState ->
//                                    if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                                        allCategory.visibleItemIndex = index
//                                        this@MainRecyclerViewController.setData(allCategory)
//                                        //this@MainRecyclerViewController.setData(this@MainRecyclerViewController.allCategory)
//                                    }
//                                }
//
//                        })
//            }

        carousel {
            id("Expert Video")
            hasFixedSize(true)
            models(allCategory.items!!.mapIndexed { index, item ->
                val expertController = ExpertVideoController(
                    this@MainRecyclerViewController.activity,
                    this@MainRecyclerViewController.videoViewModel,
                )
                expertController.items = item!!
                val title = this@MainRecyclerViewController.setTitle(item)
                ExpertViewPagerModel_()
                    .id(index)
                    .categoryTitle(title)
                    .controller(expertController)
                    .position(index)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }


            })
        }


        carousel {
            id("redorded room video")
            hasFixedSize(true)
            models(allCategory.itemRecorded!!.mapIndexed { index, item ->
                val expertController = ExpertVideoController(
                    this@MainRecyclerViewController.activity,
                    this@MainRecyclerViewController.videoViewModel,
                )
                expertController.items = item!!
                val title = this@MainRecyclerViewController.setTitle(item)
                ExpertViewPagerModel_()
                    .id(index)
                    .categoryTitle(title)
                    .controller(expertController)
                    .position(index)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }


            })
        }

        carousel {
            id("Common Record Grid view pager.")
            hasFixedSize(true)
            models(allCategory.recordCommonMyspcaeGrid.mapIndexed { index, viewpageritem ->
                val recordMySpaceController =
                    RecordMySpaceController(this@MainRecyclerViewController.context)
                recordMySpaceController.recordGrids = viewpageritem.grids
                RecordViewPagerModel_()
                    .id(index)
                    .categoryTitle("${viewpageritem.categoryName}")
                    .controller(recordMySpaceController)
                    .position(index)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }
            })
        }

        carousel {
            id("Record Grid view pager.")
            hasFixedSize(true)
            models(allCategory.recordMyspcaeGrid.mapIndexed { index, viewpageritem ->
                val recordMySpaceController =
                    RecordMySpaceController(this@MainRecyclerViewController.context)
                recordMySpaceController.recordGrids = viewpageritem.grids
                RecordViewPagerModel_()
                    .id(index)
                    .categoryTitle(viewpageritem.categoryName)
                    .controller(recordMySpaceController)
                    .position(index)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }
            })
        }
        /*
        shareMyworldExplore {
            id("share")
            onClick { _ ->
//                val data = Intent().apply {
//                    this.action = Intent.ACTION_SEND
//                    this.putExtra(
//                        Intent.EXTRA_TEXT, "Let's talk with best creators who help you on Myworld! " +
//                            "It's a fast, simple and secure app. " +
//                            "Get it at https://play.google.com/store/apps/details?id=cessini.technology.myworld")
//                    this.type = "text/plain"
//                }
//                fragment.startActivity(data)
                onClickListener(ExploreOnClickEvents.ShareEvent)
            }
        }
        */

//        carousel {
//            id("This is a ViewInfo.")
//            hasFixedSize(true)
//            paddingRes(R.dimen.view_pager_item_padding)
//            models(allCategory.itemsInfo!!.mapIndexed { index, item ->
//                ViewPagerInfo_()
//                    .id(index)
//                    .title(item)
//                    .context(this@MainRecyclerViewController.context)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }
//            })
//        }

        carousel {
            id("Info")
            hasFixedSize(true)
            models(allCategory.itemsInfo!!.mapIndexed { index, item ->
                val infoController = InfoController()
                infoController.items = item!!
                InfoViewPagerModel_()
                    .id(index)
                    .controller(infoController)
                    .position(index)
//                    .onVisibilityStateChanged { _, _, visibilityState ->
//                        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
//                            allCategory.visibleItemIndex = index
//                            this@MainRecyclerViewController.setData(allCategory)
//                        }
//                    }


            })
        }
            FooterEpoxyModel("Saud",onClickListener).id("_footer").addTo(this)
    }
    // data class for footer
    data class FooterEpoxyModel(
       val footerText1 : String,
       var onClickListener: (event: ExploreOnClickEvents) -> Unit
    ):ViewBindingKotlinModel<HeaderRefreshExploreBinding>(R.layout.header_refresh_explore){
        override fun HeaderRefreshExploreBinding.bind() {
            goToHomefeed.setOnClickListener{
                onClickListener(ExploreOnClickEvents.RefreshFeed) }
        }

    }
    private fun setHeader(allCategory: Explore) {
        headerViewItem {
            id("0")

            val adapter = MainRecyclerHeaderAdapter(
                viewModel!!,
                allCategory.publicEvents,
                0
            )
            headAdapter(adapter)
        }
    }

    private fun setTitle(item: List<HealthFitness>): String? {
        return "Technology"
    }


}
