package cessini.technology.home.fragment

//import kotlinx.android.synthetic.main.user_save_video.*
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint.Join
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.activity.live.SignalingClient
import cessini.technology.commonui.activity.live.SocketEventCallback
import cessini.technology.commonui.activity.live.signallingserverData.JoinRoom
import cessini.technology.commonui.activity.live.signallingserverData.SGCUser
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.commonui.common.isInDarkTheme
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.commonui.viewmodel.basicViewModels.GalleryViewModel
import cessini.technology.home.R
import cessini.technology.home.controller.HomeEpoxyController
import cessini.technology.home.databinding.HomeRoomTopPopupBinding
import cessini.technology.home.databinding.NewHomeFragmentBinding
import cessini.technology.home.fragment.dialogs.JoinEvents
import cessini.technology.home.fragment.dialogs.RoomWaitingFragment
import cessini.technology.home.homeLoading
import cessini.technology.home.model.HomeEpoxyStreamsModel
import cessini.technology.home.model.JoinRoomSocketEventPayload
import cessini.technology.home.model.User
import cessini.technology.home.viewmodel.HomeFeedViewModel
import cessini.technology.home.viewmodel.SocketFeedViewModel
import cessini.technology.model.Profile
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.ToFlowNavigable
import cessini.technology.newapi.preferences.AuthPreferences
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.newrepository.video.VideoRepository
import cessini.technology.newrepository.websocket.video.VideoViewUpdaterWebSocket
import com.airbnb.epoxy.EpoxyRecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.android.synthetic.main.fragment_create_room.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<NewHomeFragmentBinding>(R.layout.new_home_fragment),
    LifecycleObserver,
    View.OnTouchListener {

    lateinit var fragmentStories: StoriesFragment

    @Inject lateinit var viewsUpdater: VideoViewUpdaterWebSocket

    @Inject lateinit var profileRepository: ProfileRepository

    lateinit var profile: Profile

//    lateinit var blurLayout: BlurLayout

    private val galleryViewModel by activityViewModels<GalleryViewModel>()



    companion object {
        private const val TAG = "HomeFragment"
        var canSendJoinReq=true
        var JOIN_ROOM_NAME=""
        var REQUEST_STATUS=0
//        0= nothing
//        1= waiting
//        2= joined
//        3= declined
    }

    @Inject lateinit var videoRepository: VideoRepository

    @Inject lateinit var userIdentifierPreferences: UserIdentifierPreferences

    @Inject lateinit var authPreferences: AuthPreferences

    private var isUserSignedIn = false
//    private val roomWaitingFragment = RoomWaitingFragment()

    private val homeFeedViewModel: HomeFeedViewModel by viewModels()
    private val socketFeedViewModel: SocketFeedViewModel by viewModels()
    var mode= true
    var recyclerView: EpoxyRecyclerView?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bottomNavigationView=requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cpDark))
        bottomNavigationView.itemIconTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        bottomNavigationView.itemTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "CREATED")
        authPreferences = AuthPreferences(requireContext())
        userIdentifierPreferences = UserIdentifierPreferences(requireContext(), authPreferences)

        /*homeFeedViewModel.activity = requireActivity()*/
        /**Setting up the viewModel to the Binding.*/
        binding.homeViewModel = homeFeedViewModel //attach your viewModel to xml
        /**Setting a lifecycleOwner as this Fragment.*/
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            profileRepository.profile.collectLatest {
                profile = it
            }
        }

        mode= Constant.home_fragment_live
        recyclerView= binding.recyclerView


        if(socketFeedViewModel.controller==null ) {
            socketFeedViewModel.controller = HomeEpoxyController(
                context = requireContext(),
                onJoinClicked = { joinRoomSocketEventPayload ->
                    if (!isUserSignedIn)
                        showSignInBottomSheet()
                    else {
                        joinRoomRequest(convertToJSONObject(joinRoomSocketEventPayload),joinRoomSocketEventPayload.room)
                    }
                },
                checkSignInStatus = {
                    if (!isUserSignedIn) {
                        showSignInBottomSheet()
                        false
                    } else true
                },
                canLoadMore = socketFeedViewModel.canLoadMore,

           warningFunction =  {
               onCanNotJoin()
            }
            )
        }

//        if( !mode ) {
//            socketFeedViewModel.controllerSuggestion = HomeEpoxyController(
//                context = requireContext(),
//                onJoinClicked = { joinRoomSocketEventPayload ->
//                    if (!isUserSignedIn)
//                        showSignInBottomSheet()
//                    else {
//                        joinRoomRequest(convertToJSONObject(joinRoomSocketEventPayload),joinRoomSocketEventPayload.room)
//                    }
//                },
//                checkSignInStatus = {
//                    if (!isUserSignedIn) {
//                        showSignInBottomSheet()
//                        false
//                    } else true
//                },
//                canLoadMore = socketFeedViewModel.canLoadMore,
//
//                warningFunction =  {
//                    onCanNotJoin()
//                }
//            )
//        }




        homeFeedViewModel.isUserSignedIn()
        binding.recyclerView.setController(socketFeedViewModel.controller!!)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

            showLoader()

//            binding.recyclerView.setController(socketFeedViewModel.controller!!)
//        }

        homeFeedViewModel.authFlag.observe(viewLifecycleOwner, Observer { isSignedIn->
            var temp = ""
            if (isSignedIn) {
                isUserSignedIn = true
                homeFeedViewModel.loadUserInfo()
                temp = userIdentifierPreferences.id

            }else{
            temp = userIdentifierPreferences.uuid
            }

            if(temp!=socketFeedViewModel.userID) {

                socketFeedViewModel.userID= temp
//                if(mode) {
                    socketFeedViewModel.setPaging(!mode)
                    requireActivity().runOnUiThread {

                        setupEpoxy()

                    }
//                }else {
//                    socketFeedViewModel.setPagingSuggestion()
////                    Toast.makeText(context,"suggestion created",Toast.LENGTH_LONG).show()
//
//                    requireActivity().runOnUiThread {
//
//                        setupEpoxySuggestion()
//
//                    }
//
//                }

            }else
            {
                binding.recyclerView.adapter = socketFeedViewModel.controller!!.adapter
            }

            return@Observer

        })

        setup()

        if(REQUEST_STATUS!=0){
            showFrag()
        }else
            hideWaiting()


//        lifecycleScope.launch {
//                profileRepository.profile.collect {
//
//                    var temp =""
//                    Log.d(TAG,"profile = $it")
//                    if(it.id!="")
//                        temp= it.id
//                    else
//                        temp=userIdentifierPreferences.uuid
//
//                    if(temp!=socketFeedViewModel.userID) {
//
//                        socketFeedViewModel.userID= temp
//                        socketFeedViewModel.setPaging(
//                            userIdentifierPreferences.uuid)
//
//                        requireActivity().runOnUiThread {
//                            setupEpoxy()
//                        }
//
//                    }else
//                    {
//                        binding.recyclerView.adapter = socketFeedViewModel.controller!!.adapter
//                    }
//
//
//                }
//            }






        //blurview
        // blurLayout = binding.blurLayout


        /*if (!isInDarkTheme()) {
            (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.camera_flow)
                .setIcon(R.drawable.ic_camera_white)
            (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.explore_flow)
                .setIcon(R.drawable.ic_search_white)
        }
        if ((activity as HomeActivity).profileDrawable != null) {
            (activity as HomeActivity).setUpNavProfileIcon(
                null, (activity as HomeActivity).profileDrawable, false
            )
        } else {
            if (!isInDarkTheme()) {
                (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.profile_flow)
                    .setIcon(R.drawable.ic_profile_white)
            }
        }*/

//        setUpNavViewColor("#121214")

        /*fragmentVideoSuggestion =
            childFragmentManager.findFragmentByTag("frag_video_suggestion") as VideoSuggestionFragment

        fragmentStories =
            childFragmentManager.findFragmentByTag("frag_stories") as StoriesFragment

        fragmentVideoSuggestion.view?.visibility = View.INVISIBLE
        fragmentStories.view?.visibility = View.VISIBLE

        binding.viewpager.offscreenPageLimit = 2*/

        /*ExoProvider.get(requireActivity())
            .register(this, memoryMode = MemoryMode.HIGH)
            .addBucket()*/

        /*showShimmer()

        setUpHomeFeed()*/

        // TODO: To be replaced with Pagination


        /*TODO:
           lifecycleScope.launch {
            viewModel.flow.collectLatest { pagingData: PagingData<home_data> ->
                controller.submitData(pagingData)
            }
        }*/



//        val DUMMY_HLS_FEED_LINK = "http://amssamples.streaming.mediaservices.windows.net/69fbaeba-8e92-4740-aedc-ce09ae945073/AzurePromo.ism/manifest(format=m3u8-aapl)"
//        val DUMMY_DATA = HomeEpoxyStreamsModel(
//            link = DUMMY_HLS_FEED_LINK,
//            title = "Nothing to display in feeds!",
//            room = "",
//            admin = "MyWorld",
//            user = User(
//                channelName = "",
//                email = "",
//                id = "",
//                name = "",
//                profilePicture = ""
//            ),
//            email = ""
//        )


//        socketFeedViewModel.homeFeeds.observe(viewLifecycleOwner, Observer { homeFeedSocketResponse->
//            val homeEpoxyStreams = mutableListOf<HomeEpoxyStreamsModel>()
//
//            homeFeedSocketResponse.data
//            homeFeedSocketResponse.data.map {
//
////                homeEpoxyStreams.add(
////                    link= it.hls,
////                    title= it.title,
////                    room = it.room,
////                    admin = it.admin,
////                    user = it.creator,
////                    email = it.admin
////
////                )
//
//
//                homeEpoxyStreams.add(
//                    HomeEpoxyStreamsModel(
//                        link = it.hls,
//                        title = it.title,
//                        room = it.room,
//                        admin = it.admin,
//                        user = User(
//                            channelName = homeFeedViewModel.channelName.toString(),
//                            email = homeFeedViewModel.email.toString(),
//                            id = homeFeedViewModel.userId.toString(),
//                            name = homeFeedViewModel.username.toString(),
//                            profilePicture = homeFeedViewModel.profilePicture.toString()
//                        ),
//                        email = homeFeedViewModel.email.toString()
//                    )
//                )
//            }
//            homeFeedViewModel.homeEpoxyStreamsList.value = homeEpoxyStreams
////            controller.streams = homeFeedViewModel.homeEpoxyStreamsList.value!!
////            controller.requestModelBuild()
//        })
//        if(!homeFeedViewModel.homeEpoxyStreamsList.value.isNullOrEmpty()) {
//            controller.streams = homeFeedViewModel.homeEpoxyStreamsList.value!!
//        } else {
//            controller.streams = mutableListOf(DUMMY_DATA)
//        }


        binding.backButton.setOnClickListener {
            (activity)?.onBackPressed()
        }




        systemBarInsetsEnabled = false

//        setUpNavIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_homeactive))
    }

    override fun onDestroyView() {
        val bottomNavigationView=requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.cpWhite))
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.itemTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.drawable.bottom_nav_bar_item_color))
        super.onDestroyView()
    }

    override fun onDestroy() {
        Constant.home_fragment_live= true
        super.onDestroy()
    }

    fun setupEpoxy(){

        try{
        socketFeedViewModel.fetchPages({ list ->

            Log.d(TAG, "fetch page = $list")

            if(recyclerView!=null && list.size>0) {
                socketFeedViewModel.controller!!.submitList(list)
                if(recyclerView?.adapter!=socketFeedViewModel.controller!!.adapter)
                recyclerView?.adapter= socketFeedViewModel.controller!!.adapter

            }

        }, { error ->
            Log.e(TAG, "error: ${error}")
        })
        }catch (e:Exception){}



    }
//    fun setupEpoxySuggestion(){
//
//        try{
//            socketFeedViewModel.fetchPagesSuggestion({ list ->
//
//                Log.d(TAG, "fetch page = $list")
//
//                if(recyclerView!=null) {
//                    socketFeedViewModel.controllerSuggestion!!.submitList(list)
//                    if(recyclerView?.adapter!=socketFeedViewModel.controllerSuggestion!!.adapter)
//                        recyclerView?.adapter= socketFeedViewModel.controllerSuggestion!!.adapter
//
//                }
//
//            }, { error ->
//                Log.e(TAG, "error: ${error}")
//            })
//        }catch (e:Exception){}
//
//
//
//    }

    fun showLoader(){
        binding.recyclerView.withModels {
            homeLoading {
             id("loading")
            }
        }
    }



    private fun convertToJSONObject(joinRoomSocketEventPayload: JoinRoomSocketEventPayload):JSONObject {
        var jsonObject = JSONObject()
        val userJSONObject = JSONObject()
        try {


            val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)

            jsonObject = JoinRoom(joinRoomSocketEventPayload.room,sgcUser,profile.email).getJson()

        } catch (e:JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    private fun joinRoomRequest(data:JSONObject,roomName:String) {
//        val homeSignallingClient = HomeSignallingClient()


            val top_notification: HomeRoomTopPopupBinding = binding.topRoom
            top_notification.enterButton.setOnClickListener {
                enterRoom(roomName)
            }

            top_notification.okButton.setOnClickListener {
                hideWaiting()
            }


        Toast.makeText(context, "Join Room Request Sent", Toast.LENGTH_LONG).show()
        JOIN_ROOM_NAME= roomName
        showWaiting()
       canSendJoinReq= false
        SignalingClient.get()?.requestJoinRoom(object : SocketEventCallback {
            override fun onJoinRequestAccepted(socketId: String) {
                (activity as HomeActivity)?.runOnUiThread {
                    Toast.makeText(context, "Room Joined", Toast.LENGTH_LONG).show()
                }





//                roomWaitingFragment.setEventCallback(object :JoinEvents{
//                    override fun join() {
//
//                        enterRoom(roomName)
//                    }
//
//                    override fun hide() {
//                        hideWaiting()
//                    }

//                })



                setApproved()

//                roomWaitingFragment.setApproved(roomName)
                canSendJoinReq= true

            }

            override fun onJoinRequestDenied(msg: String) {
                (activity as HomeActivity)?.runOnUiThread {

                    Toast.makeText(context,"Join Request Denied", Toast.LENGTH_LONG).show()
                }

//                roomWaitingFragment.setEventCallback(object :JoinEvents{
//                    override fun join() {
//
//                        enterRoom(roomName)
//                    }
//
//                    override fun hide() {
//                        hideWaiting()
//                    }
//
//                })
                setDenied()
//                roomWaitingFragment.setDenied(roomName)
                canSendJoinReq= true

            }
        }, data)
    }

    private fun setDenied() {
        (activity as HomeActivity)?.runOnUiThread {
            REQUEST_STATUS=3
            val top_notification: HomeRoomTopPopupBinding = binding.topRoom
            top_notification?.waitingBar?.visibility = View.GONE
            top_notification?.enterButton?.visibility = View.GONE
            top_notification?.okButton?.visibility = View.VISIBLE
            top_notification?.roomName?.text = JOIN_ROOM_NAME
            top_notification?.joinStatus?.text = "Request Denied"
        }
    }

    private fun setApproved() {
        (activity as HomeActivity)?.runOnUiThread {
            REQUEST_STATUS= 2
            val top_notification: HomeRoomTopPopupBinding = binding.topRoom
            top_notification?.waitingBar?.visibility = View.GONE
            top_notification?.enterButton?.visibility = View.VISIBLE
            top_notification?.okButton?.visibility = View.GONE
            top_notification?.roomName?.text = JOIN_ROOM_NAME
            top_notification?.joinStatus?.text = "Request Approved"
        }
    }

    fun showFrag(){
        (activity as HomeActivity).runOnUiThread{

            binding.topRoom.cardView.visibility= View.VISIBLE
            when(REQUEST_STATUS){
                0->hideWaiting()
                1-> showWaiting()
                2-> setApproved()
                3-> setDenied()

            }

        }

//        binding.frameLayout.visibility= View.VISIBLE
//        childFragmentManager.beginTransaction()
//            .replace(R.id.frame_layout, roomWaitingFragment)
//            .commit()
    }
    fun removeFragment() {
//        val transaction = childFragmentManager.beginTransaction()
//        if (roomWaitingFragment != null) {
//            transaction.remove(roomWaitingFragment)
//            transaction.commit()
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
////            yourFragment = null
//        }
    }
    fun showWaiting(){

        (activity as HomeActivity).runOnUiThread {
            binding.topRoom.cardView.visibility= View.VISIBLE
            REQUEST_STATUS= 1
            binding.topRoom?.waitingBar?.visibility= View.VISIBLE
            binding.topRoom?.enterButton?.visibility= View.GONE
            binding.topRoom?.okButton?.visibility= View.GONE
            binding.topRoom?.roomName?.text= JOIN_ROOM_NAME
            binding.topRoom?.joinStatus?.text="Waiting for Accept"
        }
//        roomWaitingFragment.setWaiting(roomName)
//            childFragmentManager.beginTransaction()
//                .replace(R.id.frame_layout, roomWaitingFragment)
//                .commit()
    }

    fun enterRoom(roomName: String){

        (activity as HomeActivity)?.runOnUiThread {

            val intent: Intent = Intent()

            intent.setClassName(
                requireContext(),
                "cessini.technology.commonui.activity.GridActivity"
            )
//            intent.setClassName(requireContext(),"cessini.technology.myspace.live.LiveMyspaceActivity")

            intent.putExtra("Room Name", "${roomName}")

            viewLifecycleOwner.lifecycleScope.launch {

                intent.putExtra("user_id", profile.id)

                startActivity(intent)

            }
        }

        hideWaiting()
    }


    fun hideWaiting(){
        REQUEST_STATUS=0
(activity as HomeActivity).runOnUiThread {
    binding.topRoom.cardView.visibility= View.GONE
        }
    }

    private fun showSignInBottomSheet() {
        (requireActivity() as ToFlowNavigable).navigateToFlow(
            NavigationFlow.AuthFlow
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()

        activity?.run {
            galleryViewModel.setStoryPos(0)
        }

        ProfileConstants.story = false


        /** Loading data*/
        //binding.progrssBarNoVideo.visibility = View.VISIBLE

        homeFeedViewModel.loadHomeFeed()
    }

    /**Function to setup the HOME FEED into the UI Window.*/
//    @RequiresApi(Build.VERSION_CODES.M)
    /*private fun setUpHomeFeed() {
        val feedAdapter = getAdapter()

        binding.viewpager.adapter = feedAdapter.also { adapter ->
            socketFeedViewModel.videos.onEach {
                hideShimmer()
                val previousSize = adapter.homeResponse.size

                adapter.homeResponse.addAll(it)

                adapter.notifyItemRangeInserted(previousSize, it.size)

            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }

        socketFeedViewModel.events.onEach {
            when (it) {
                SuggestionClicked -> {
                    feedAdapter.homeResponse = mutableSetOf()
                    feedAdapter.notifyDataSetChanged()
                    hideShimmer()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                homeFeedViewModel.homePos.value = position

                with(socketFeedViewModel) {
                    videos.value.getOrNull(position)?.id?.let { suggest(it) }
                    feedAdapter.suggestionVisible(position)

                    if (position == videos.value.size - 3) fetchMoreVideos()
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            socketFeedViewModel.resetTimeline()
            showShimmer()
            binding.swipeRefreshLayout.isRefreshing = false
            feedAdapter.homeResponse = mutableSetOf()
            feedAdapter.notifyDataSetChanged()
        }

        binding.viewpager.setCurrentItem(homeFeedViewModel.homePos.value ?: 0, false)
    }

    private fun getAdapter(): FeedAdapter {
        return FeedAdapter(
            fragmentVideoSuggestion,
            fragmentStories,
            binding.viewpager.context,
            playerPlayBackPosition,
            mutableSetOf(),
            activity,
            binding.storySuggestionViewButton,
            homeFeedViewModel,
            socketFeedViewModel,
            videoRepository,
            this,
            profileRepository
        )
    }*/

    private fun setUpNavViewColor(color: String) {
        if (!isInDarkTheme()) {
            (activity as HomeActivity).binding.navBarView.background =
                ColorDrawable(Color.parseColor(color))
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.onTouchEvent(event)
        /*binding.viewpager.onInterceptTouchEvent(event)*/
        return false
    }

    private fun setUpNavIcon(icon: Drawable?) {
        if (!isInDarkTheme()) {
            if (icon != null) {
                (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.home_flow).icon =
                    icon
            } else {
                (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.home_flow).icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_inactivehome)
            }
        }
    }

    fun onCanNotJoin() {
        requireActivity().runOnUiThread {
            Toast.makeText(context,
                "A join Request is pending. Can not send another request",
                Toast.LENGTH_LONG).show()
        }
    }

    /**Function to Stop the Shimmer effect.*/
//    private fun hideShimmer() {
//        binding.homeFeedShimmer.visibility = View.GONE
//    }
//
//    private fun showShimmer() {
//        binding.homeFeedShimmer.visibility = View.VISIBLE
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
        /*if (!isInDarkTheme()) {
            (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.camera_flow)
                .setIcon(R.drawable.ic_camera)
            (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.explore_flow)
                .setIcon(R.drawable.navigation_search_selector)
            if ((activity as HomeActivity).profileDrawable == null) {
                (activity as HomeActivity).binding.bottomNavigation.menu.findItem(R.id.profile_flow)
                    .setIcon(R.drawable.navigation_profile_selector)
            }

        }*/

        /*setUpNavIcon(null)*/
        /*setUpNavViewColor("#4D000000")*/
//    }


//    override fun onStart() {
//        super.onStart()
//        blurLayout.startBlur()
//    }

//    override fun onStop() {
//        super.onStop()
//        blurLayout.pauseBlur()
//    }

    fun setup(){

//        Toast.makeText(context,"user ID = ${socketFeedViewModel.userID}",Toast.LENGTH_LONG).show()
        if(!Constant.home_fragment_live){
//            Constant.home_fragment_live= true
            (activity as HomeActivity).binding.bottomNavigation.visibility = View.GONE
//            binding.translucentBackground.visibility= View.GONE
            binding.backButton.visibility=View.VISIBLE
//            socketFeedViewModel.setPagingSuggestion()
//            setupEpoxySuggestion()

        }else
        {
            (activity as HomeActivity).binding.bottomNavigation.visibility = View.VISIBLE
//            binding.translucentBackground.visibility= View.GONE
            binding.backButton.visibility=View.GONE

        }
    }







}