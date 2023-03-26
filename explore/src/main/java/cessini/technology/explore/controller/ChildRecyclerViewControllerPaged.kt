package cessini.technology.explore.controller

import android.content.Context
import android.provider.Settings.Global.getString
import android.provider.Settings.System.getString
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.utils.networkutil.TAG
import cessini.technology.commonui.viewmodel.basicViewModels.GalleryViewModel
import cessini.technology.cvo.exploremodels.CategoryModel
import cessini.technology.cvo.exploremodels.ProfileModel
import cessini.technology.explore.ChildItemRoomBindingModel_
import cessini.technology.explore.ExploreLoadingBindingModel_
import cessini.technology.explore.R
import cessini.technology.explore.UpcomingMyspaceBindingModel_
import cessini.technology.explore.childItem2
import cessini.technology.explore.childItemRoom
import cessini.technology.explore.epoxy.VoiceModel_
import cessini.technology.explore.epoxy.voice
import cessini.technology.explore.fragment.ExploreSearchFragmentDirections
import cessini.technology.explore.states.ExploreOnClickEvents
import cessini.technology.explore.upcomingMyspace
import cessini.technology.explore.viewmodel.SearchViewModel
import cessini.technology.home.HomeLoadingBindingModel_
import cessini.technology.model.*
import cessini.technology.navigation.MainNavGraphDirections
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.ToFlowNavigable
import cessini.technology.newapi.preferences.AuthPreferences
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.newrepository.websocket.video.RoomViewerUpdaterWebSocket
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChildRecyclerViewControllerPaged(

    val roomRepository: RoomRepository,
    val userIdentifierPreferences: UserIdentifierPreferences,
    var parentIndex: Int,
    var viewModel: SearchViewModel,
    var context: Context,
    var activity: FragmentActivity,
    var fragment: Fragment,
    var onClickListener: ((ExploreOnClickEvents) -> Unit)
) : PagedListEpoxyController<ExplorePagedData>() {


    var followingStatusID: HashMap<String, Boolean> = HashMap<String, Boolean>()
//    private val galleryViewModel by fragment.activityViewModels<GalleryViewModel>()

//    @Inject
//     var authPreferences: AuthPreferences?=null

    //    val authPreferences = AuthPreferences(context)
    @Inject
    lateinit var roomViewerUpdaterWebSocket: RoomViewerUpdaterWebSocket
    var find = true


    var categoryVideoList: MutableList<Video> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }
    var category: CategoryModel = CategoryModel(0, "")

    var topProfiles: MutableList<TopProfile> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }

    var rooms: MutableList<Room> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }

    var liveRooms: MutableList<LiveRoom> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }

    var trendingRooms: MutableList<List<MessageT>> = mutableListOf()
        set(value) {
            field = value
            requestModelBuild()
        }

      fun buildModelsi() {

    }

    private fun getColoredSpanned(text: String, color: String): String {
        return "<font color=$color>$text</font>"
    }

    interface ChildRecyclerViewRippleListener {
        fun onRipple()
    }

    fun addFollowingStatus() {
        activity?.let {
            viewModel.following.observe(it) { it ->
                it?.forEachIndexed { index, user ->
                    followingStatusID[user.id] = true
                }
                requestModelBuild()
            }
        }
    }

    private fun setupLiveRoom() {
        Log.d(TAG, "Trending room = $trendingRooms")
        trendingRooms.forEach { room ->

        }
    }

    fun openLive(){
        Constant.home_fragment_live= false
        fragment?.findNavController()?.navigate(ExploreSearchFragmentDirections.actionExploreSearchFragmentToLiveFragment())

    }

//    private fun setupUpcomingHub() {
//
//        rooms.forEach {
//            if (it.listeners.isNotEmpty()) {
//                it.listeners = it.listeners.toSet().toList()
//            }
//        }
//        rooms.forEach { room ->
//            //code for removing all myspaces
////                    if(!room.time.compareDate(System.currentTimeMillis()))
////                        return@forEach
//            var text = ""
//            var categorytext = ""
//            authPreferences = context?.let { AuthPreferences(it) }!!
//            roomViewerUpdaterWebSocket =
//                RoomViewerUpdaterWebSocket(authPreferences, userIdentifierPreferences)
//
//            Log.d(
//                "RoomViewerSocket",
//                "inside room pager adapter:  ${
//                    roomViewerUpdaterWebSocket.invoke(
//                        room.id,
//                        1
//                    )
//                }"
//            )
//            upcomingMyspace {
//                id(room.id)
//                creatorName(room.creator.name)
//                time(room.time)
//
//                if (room.categories.isNotEmpty()) {
//                    val category = StringBuilder()
//                    room.categories.forEach {
//
//                        if(it.isNotEmpty())
//                            category.append("$it#")
//
//                    }
//                    val fh = category.indexOf("#")
//                    val sh = category.indexOf("#", fh + 1)
//                    val th = category.indexOf("#", sh + 1)
//                    if(fh>1)
//                    categorytext += "#" + category.substring(1, fh) + " "
//                    if (sh != -1) {
//                        categorytext += "#" + category.substring(fh + 2, sh) + " "
//
//                    } else
//
//                        if (th != -1) {
//                            categorytext += "#" + category.substring(sh + 2, th) + " "
//                        }
//
//                }
//
//                text = context?.getString(R.string.some_text, room.title, categorytext).toString()
//
//                span(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
//                listener1Image(room.creator.profilePicture)
//                listener2Image(room.listeners.getOrNull(0)?.profile_picture ?: "")
//                listener3Image(room.listeners.getOrNull(1)?.profile_picture ?: "")
//
//                onClick { _ ->
//                    Log.e("RoomName", room.name)
//
//                    onClickListener?.let { it(ExploreOnClickEvents.ToAccessRoomFlow(room)) }
//
//                }
//                onJoin { view ->
//                    if (!userIdentifierPreferences.loggedIn) {
//                        (activity as ToFlowNavigable).navigateToFlow(NavigationFlow.AuthFlow)
//
//                    } else {
//                        activity?.lifecycleScope?.launch {
//                            runCatching { roomRepository.joinRoom(room.name) }
//                                .onSuccess {
//                                    Toast.makeText(
//                                        activity,
//                                        "Join request sent",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    view.setBackgroundResource(R.drawable.round_enable_viewbutton)
//                                }
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun setupVoicesToFollow() {
        topProfiles.forEachIndexed { index, topProfile ->
            followingStatusID.put(topProfile.id, false)
        }

        topProfiles.forEachIndexed { index, topProfilesModel ->
            if (topProfilesModel.id != (activity as HomeActivity).baseViewModel.id.value) {
                voice {
                    id(topProfilesModel.id)
                    img(topProfilesModel.profilePicture)
                    title1(topProfilesModel.name)
                    vid(topProfilesModel.id)
                    channelName(topProfilesModel.channelName)
                    activity(activity)
                    followingStatusId(followingStatusID)
                    onClickListener?.let {
                        onClickEvents(it)
                    }
                }
            }
        }
    }

    override fun buildItemModel(currentPosition: Int, item: ExplorePagedData?): EpoxyModel<*> {
        item?.let {

        when (parentIndex) {
            1 -> {

               return VoiceModel_().
                id(item?.topProfilesModel?.id).
                img(item?.topProfilesModel?.profilePicture).
                title1(item?.topProfilesModel?.name).
                vid(item.topProfilesModel?.id.toString()).
                channelName(item?.topProfilesModel?.channelName).
                activity(activity).
                followingStatusId(followingStatusID)
                   .onClickEvents(onClickListener)




//                return voice {
//                    id(item?.topProfilesModel?.id)
//                    img(item?.topProfilesModel?.profilePicture)
//                    title1(item?.topProfilesModel?.name)
//                    vid(item?.topProfilesModel?.id)
//                    channelName(item?.topProfilesModel?.channelName)
//                    activity(activity)
//                    followingStatusId(followingStatusID)
//                    onClickEvents(onClickListener)
//                }
            }
            2 -> {

//                val room=Room()
//                val moreThan3Listener = if (room[0].allowedUser.size < 4) {
//                    "0"
//                } else {
//                    (5 - room[0].allowedUser.size).toString()
//                }
//
//                // Touch effect
//                val touchTimeFactor = 200
//                var touchDownTime = 0L
//
//                ChildItemRoomBindingModel_().
//                id(room[0].room).
//                roomTitle(room.get(0).title).
//                listenerCount(moreThan3Listener).
//                listener1Image(room[0].creator.profilePicture).
//                listener2Image(room[0].allowedUser.getOrNull(0)?.profilePicture ?: "").
//                listener3Image(room[0].allowedUser.getOrNull(1)?.profilePicture ?: "").
//                listener4Image(room[0].allowedUser.getOrNull(2)?.profilePicture ?: "").
//                listener5Image(room[0].allowedUser.getOrNull(3)?.profilePicture ?: "").
//                searchViewModel(viewModel).
//                fragment(fragment).
//                onTouchDetected { view, event ->
//
//                    if (event.action == MotionEvent.ACTION_DOWN) {
//                        view.animate().scaleX(0.98f).scaleY(0.98f).duration = 20
//                        Log.i("ChildRecyclerViewRoom", "Pressed")
//                        touchDownTime = System.currentTimeMillis()
//
//                    } else if (event.action == MotionEvent.ACTION_UP) {
//                        view.animate().scaleX(1f).scaleY(1f).duration = 20
//                        Log.i("ChildRecyclerViewRoom", "Released")
//                        val isClickTime =
//                            System.currentTimeMillis() - touchDownTime < touchTimeFactor
//                        if (isClickTime) {
//                            view.performClick()
//                            Log.i("ChildRecyclerViewRoom", "Clicked")
////                                    val intent = Intent(activity, LiveMyspaceActivity::class.java)
////                                    intent.putExtra("ROOM_CODE",room.get(0).roomCode)
////                                    activity.startActivity(intent)
////                                    activity.overridePendingTransition(R.anim.slide_out_animation, R.anim.slide_in_animation)
//////                                        (activity as ToFlowNavigable).navigateToFlow(
////                                            NavigationFlow.AccessRoomFlow(room.room_code!!)
////                                        )
//
//                            onClickListener(
//
////                                openLive()
//                                ExploreOnClickEvents.ExploreFragmentToLiveFragment(
//                                    "Trending Rooms",
//                                    "abc"
//                                )
//                            )
//
//                        }
//
//                    }
//                    false
//                }



//                childItemRoom {
//                    id(room[0].room)
//                    roomTitle(room.get(0).title)
//
//                    listenerCount(moreThan3Listener)
//
//                    listener1Image(room[0].creator.profilePicture)
//                    listener2Image(room[0].allowedUser.getOrNull(0)?.profilePicture ?: "")
//                    listener3Image(room[0].allowedUser.getOrNull(1)?.profilePicture ?: "")
//                    listener4Image(room[0].allowedUser.getOrNull(2)?.profilePicture ?: "")
//                    listener5Image(room[0].allowedUser.getOrNull(3)?.profilePicture ?: "")
//                    searchViewModel(viewModel)
//                    fragment(fragment)
//
//
//
//                    onTouchDetected { view, event ->
//
//                        if (event.action == MotionEvent.ACTION_DOWN) {
//                            view.animate().scaleX(0.98f).scaleY(0.98f).duration = 20
//                            Log.i("ChildRecyclerViewRoom", "Pressed")
//                            touchDownTime = System.currentTimeMillis()
//
//                        } else if (event.action == MotionEvent.ACTION_UP) {
//                            view.animate().scaleX(1f).scaleY(1f).duration = 20
//                            Log.i("ChildRecyclerViewRoom", "Released")
//                            val isClickTime =
//                                System.currentTimeMillis() - touchDownTime < touchTimeFactor
//                            if (isClickTime) {
//                                view.performClick()
//                                Log.i("ChildRecyclerViewRoom", "Clicked")
////                                    val intent = Intent(activity, LiveMyspaceActivity::class.java)
////                                    intent.putExtra("ROOM_CODE",room.get(0).roomCode)
////                                    activity.startActivity(intent)
////                                    activity.overridePendingTransition(R.anim.slide_out_animation, R.anim.slide_in_animation)
//////                                        (activity as ToFlowNavigable).navigateToFlow(
////                                            NavigationFlow.AccessRoomFlow(room.room_code!!)
////                                        )
//
//                                onClickListener(
//
////                                openLive()
//                                    ExploreOnClickEvents.ExploreFragmentToLiveFragment(
//                                        "Trending Rooms",
//                                        "abc"
//                                    )
//                                )
//
//                            }
//
//                        }
//                        false
//                    }

            }
            5 -> {
//                setupUpcomingHub()

                var text = ""
                var categorytext = ""
//                authPreferences = context?.let { it1 -> AuthPreferences(it1) }!!
//                roomViewerUpdaterWebSocket =
//                    RoomViewerUpdaterWebSocket(authPreferences, userIdentifierPreferences)

//                Log.d(
//                    "RoomViewerSocket",
//                    "inside room pager adapter:  ${
//                        item?.room?.id?.let {
//                            roomViewerUpdaterWebSocket.invoke(
//                                it,
//                                1
//                            )
//                        }
//                    }"
//                )


                if (item?.room?.categories?.isNotEmpty() == true) {
                    val category = StringBuilder()
                    item?.room?.categories?.forEach {

                        if(it.isNotEmpty())
                            category.append("$it#")

                    }
                    val fh = category.indexOf("#")
                    val sh = category.indexOf("#", fh + 1)
                    val th = category.indexOf("#", sh + 1)
                    if(fh>1)
                        categorytext += "#" + category.substring(1, fh) + " "
                    if (sh != -1) {
                        categorytext += "#" + category.substring(fh + 2, sh) + " "

                    } else

                        if (th != -1) {
                            categorytext += "#" + category.substring(sh + 2, th) + " "
                        }

                }

//                text = item?.room?.title.toString() + categorytext
                      text= context?.getString(R.string.some_text, item?.room?.title, categorytext).toString()


                return UpcomingMyspaceBindingModel_().
                id(item?.room?.id).
                creatorName(item?.room?.creator?.name).
                time(item?.room?.time).
                span(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)).
                listener1Image(item?.room?.creator?.profilePicture).
                listener2Image(item?.room?.listeners?.getOrNull(0)?.profile_picture ?: "").
                listener3Image(item?.room?.listeners?.getOrNull(1)?.profile_picture ?: "").
                onClick { _ ->
                    item?.room?.name?.let { Log.e("RoomName", it) }

                    item?.room?.let { ExploreOnClickEvents.ToAccessRoomFlow(it) }
                        ?.let { onClickListener?.let { it1 -> it1(it) } }

                }.
                onJoin { view ->
                    if (!userIdentifierPreferences.loggedIn) {
                        (activity as ToFlowNavigable).navigateToFlow(NavigationFlow.AuthFlow)

                    } else {
                        activity?.lifecycleScope?.launch {
                            runCatching { item?.room?.name?.let { roomRepository.joinRoom(it) } }
                                .onSuccess {
                                    Toast.makeText(
                                        activity,
                                        "Join request sent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    view.setBackgroundResource(R.drawable.round_enable_viewbutton)
                                }
                        }
                    }
                }





//                upcomingMyspace {
//                    id(item?.room?.id)
//                    creatorName(item?.room?.creator?.name)
//                    time(item?.room?.time)
//
//                    if (item?.room?.categories?.isNotEmpty() == true) {
//                        val category = StringBuilder()
//                        item?.room?.categories?.forEach {
//
//                            if(it.isNotEmpty())
//                                category.append("$it#")
//
//                        }
//                        val fh = category.indexOf("#")
//                        val sh = category.indexOf("#", fh + 1)
//                        val th = category.indexOf("#", sh + 1)
//                        if(fh>1)
//                            categorytext += "#" + category.substring(1, fh) + " "
//                        if (sh != -1) {
//                            categorytext += "#" + category.substring(fh + 2, sh) + " "
//
//                        } else
//
//                            if (th != -1) {
//                                categorytext += "#" + category.substring(sh + 2, th) + " "
//                            }
//
//                    }
//
//                    text = context.getString(R.string.some_text, item?.room?.title, categorytext)
//
//                    span(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
//                    listener1Image(item?.room?.creator?.profilePicture)
//                    listener2Image(item?.room?.listeners?.getOrNull(0)?.profile_picture ?: "")
//                    listener3Image(item?.room?.listeners?.getOrNull(1)?.profile_picture ?: "")
//
//                    onClick { _ ->
//                        item?.room?.name?.let { Log.e("RoomName", it) }
//
//                        item?.room?.let { ExploreOnClickEvents.ToAccessRoomFlow(it) }
//                            ?.let { onClickListener(it) }
//
//                    }
//                    onJoin { view ->
//                        if (!userIdentifierPreferences.loggedIn) {
//                            (activity as ToFlowNavigable).navigateToFlow(NavigationFlow.AuthFlow)
//
//                        } else {
//                            activity.lifecycleScope.launch {
//                                runCatching { item?.room?.name?.let { roomRepository.joinRoom(it) } }
//                                    .onSuccess {
//                                        Toast.makeText(
//                                            activity,
//                                            "Join request sent",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        view.setBackgroundResource(R.drawable.round_enable_viewbutton)
//                                    }
//                            }
//                        }
//                    }
//                }



            }

            else -> {
                categoryVideoList.forEachIndexed { index, categoryVideo ->
                    childItem2 {
                        id(index)
                        positionType(categoryVideo.positionType)
                        childItem2(categoryVideo)
                        onClickChild2 { _, _, _, position ->
                            val galleryViewModel =
                                activity?.run {
                                    ViewModelProvider(this)[GalleryViewModel::class.java]
                                } ?: throw Exception("Invalid Activity")

                            Log.i("ChildRecycler", "Entering")

                            galleryViewModel.setVideoPos(position)
                            galleryViewModel.setVideoFlow(1)
                            galleryViewModel.setVideoDisplayList(ArrayList(categoryVideoList))

                            galleryViewModel.setCommonVideoProfile(
                                ProfileModel(
                                    id = categoryVideo.profile.id,
                                    channel_name = categoryVideo.profile.channel,
                                    name = categoryVideo.profile.name,
                                    profile_picture = categoryVideo.profile.picture,
                                )
                            )

                            fragment?.findNavController()?.navigate(
                                MainNavGraphDirections.actionGlobalGlobalVideoDisplayFlow()
                            )
                        }
                    }
                }
            }
        }
        }

        return ExploreLoadingBindingModel_().id("loading$currentPosition")
    }



    override fun addModels(models: List<EpoxyModel<*>>) {


            super.addModels(
                models.plus(
                    ExploreLoadingBindingModel_().id("loading")
                ).distinct()
            )

    }
}










//val categories = listOf(
//    " Film and animation ",
//    " Cars and vehicles ",
//    " Music ",
//    " Pets and animals ",
//    " Sport ",
//    " Travel and events ",
//    " Gaming ",
//    " People and blogs ",
//    " Comedy ",
//    " Entertainment ",
//    " News and politics ",
//    " How-to and style ",
//    " Education ",
//    " Science and technology ",
//    " Non-profits and activism ",
//)