package cessini.technology.explore.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.paging.RxPagedListBuilder
import cessini.technology.commonui.utils.networkutil.NetworkUtils
import cessini.technology.explore.controller.ChildRecyclerViewControllerPaged
import cessini.technology.explore.controller.datasource.Live_RoomDataFactory
import cessini.technology.explore.controller.datasource.TopProfileDataFactory
import cessini.technology.explore.controller.datasource.UpcomingRoomDataFactory
import cessini.technology.explore.epoxy.MultiGridController
import cessini.technology.explore.fragment.ExploreFragmentDirections
import cessini.technology.explore.states.ExploreOnClickEvents
import cessini.technology.model.*
import cessini.technology.model.search.UserLikes
import cessini.technology.newrepository.explore.ExploreRepository
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.myworld.FollowRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.newrepository.timlineRoomRepo.TimelineRepository
import cessini.technology.newrepository.utils.Resource
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val userIdentifierPreferences: UserIdentifierPreferences,
    private val roomRepository: RoomRepository,
    private val application: Application,
    private val timelineRepository: TimelineRepository,
    private val followRepository: FollowRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SearchViewModel"
    }

    val testList = MutableLiveData<MutableList<ViewerX>>()
    var viewerList = ArrayList<ViewerX>()
    var allCategory = MutableLiveData<Explore>()
    var liveRooms = MutableLiveData<List<LiveRoom>?>()
    var allComponent = MutableLiveData<Component>()

    var allTrendingRooms = MutableLiveData<TrendingRooms>()

    var likedRooms = MutableLiveData<UserLikes>()

    var following = MutableLiveData<List<User>?>()
    var roomMessage: MutableLiveData<SuggestionRoomMessage> = MutableLiveData()
    val allInfo = MutableLiveData<Info>()
    var allRecordVideo = MutableLiveData<RecordedVideo>()
    var allCommonRecordVideo = MutableLiveData<RecordedVideo>()
    var suggestedroomresponse: MutableLiveData<MutableList<SuggestionCategoryRooms>> =
        MutableLiveData(
            mutableListOf()
        )
    var liveRoomsTest: MutableStateFlow<PagingData<LiveRoom>> = MutableStateFlow(PagingData.empty())

    init {
        if (isNetworkAvailable()) {
            fetchCategoriesAPI()
            fetchAllComponent()
            fetchAllInfo()
            //problems------
            fetchSuggestedRooms()
            fetchAllTrendingRooms()
            fetchAllRecordedVideos()
            fetchAllCommonRecordedVideos()
            getFollowing()

//            SuggestedLiveRoom()
//            UpcomingRooms()
//            Top_ptfiles()
        }
    }


    var top_profilechildRecyclerViewController :ChildRecyclerViewControllerPaged?=null

    fun Top_profiles(
        context: Context,
        fragment: Fragment,
        onClickListener: (event: ExploreOnClickEvents) -> Unit,
        activity: FragmentActivity
    ): ChildRecyclerViewControllerPaged? {


        if(top_profilechildRecyclerViewController!=null) {
            return top_profilechildRecyclerViewController
        }

         top_profilechildRecyclerViewController =
            ChildRecyclerViewControllerPaged(
                roomRepository,
                userIdentifierPreferences,
                1,
                this,
                context,
                activity,
                fragment,
                onClickListener

            )
        val top_profilecompositeDisposable : CompositeDisposable  = CompositeDisposable()
        var  top_profileconfig  : PagedList.Config = PagedList.Config.Builder()
            .setPageSize(2)
            .setInitialLoadSizeHint(15)
            .setEnablePlaceholders(true)
            .build()

        val top_profiledatasourceFactory =
            TopProfileDataFactory(
                top_profilecompositeDisposable,
                exploreRepository
            )// false

        val top_profilerxPageList: Observable<PagedList<ExplorePagedData>> = RxPagedListBuilder(top_profiledatasourceFactory,top_profileconfig).buildObservable()




        top_profilecompositeDisposable.add(
            top_profilerxPageList
                .subscribeWith(object : DisposableObserver<PagedList<ExplorePagedData>>() {
                    override fun onComplete() {

                    }
                    override fun onNext(pagedList: PagedList<ExplorePagedData>) {
                        Log.d("Top Profile","page list = $pagedList")
                        top_profilechildRecyclerViewController!!.submitList(pagedList)
                    }
                    override fun onError(e: Throwable) {
                        Log.e("Home WebSocket", "error: ${e}")
//                        onPageLoadFailed(e)
                    }

                })
        )
     return top_profilechildRecyclerViewController
    }


    var multiGridController : MultiGridController?=null
    fun SuggestedLiveRoom(fragment: Fragment, activity: FragmentActivity): MultiGridController? {

        if(multiGridController!=null)
            return multiGridController

        multiGridController = MultiGridController( "Trending Technology",activity,fragment)
        val Live_RoomcompositeDisposable : CompositeDisposable = CompositeDisposable()
        var  Live_Roomconfig  : PagedList.Config = PagedList.Config.Builder()
            .setPageSize(2)
            .setInitialLoadSizeHint(15)
            .setEnablePlaceholders(true)
            .build()

        val Live_RoomdatasourceFactory =
            Live_RoomDataFactory(
                Live_RoomcompositeDisposable,
                exploreRepository
            )// false

        val Live_RoomrxPageList: Observable<PagedList<roomInfo>> = RxPagedListBuilder(Live_RoomdatasourceFactory,Live_Roomconfig).buildObservable()



        Live_RoomcompositeDisposable.add(
            Live_RoomrxPageList
                .subscribeWith(object : DisposableObserver<PagedList<roomInfo>>() {
                    override fun onComplete() {

                    }
                    override fun onNext(pagedList: PagedList<roomInfo>) {
                        Log.d("Live Room","page list = $pagedList")
                        multiGridController?.submitList(pagedList)
                    }
                    override fun onError(e: Throwable) {
                        Log.e("Home WebSocket", "error: ${e}")
//                        onPageLoadFailed(e)
                    }

                })
        )
return multiGridController
    }


    var UpcommingchildRecyclerViewController :ChildRecyclerViewControllerPaged?=null
    fun UpcomingRooms( context: Context,
                       fragment: Fragment,
                       onClickListener: (event: ExploreOnClickEvents) -> Unit,
                       activity: FragmentActivity): ChildRecyclerViewControllerPaged? {

        if(UpcommingchildRecyclerViewController!=null)
            return UpcommingchildRecyclerViewController


        UpcommingchildRecyclerViewController =
            ChildRecyclerViewControllerPaged(
                roomRepository,
                userIdentifierPreferences,
                5,
                this,
                context,
                activity,
                fragment,
                onClickListener

            )
        val UpcomingroomcompositeDisposable : CompositeDisposable  = CompositeDisposable()
        var  upcomingconfig  : PagedList.Config = PagedList.Config.Builder()
            .setPageSize(2)
            .setInitialLoadSizeHint(15)
            .setEnablePlaceholders(true)
            .build()

        val upcomigdatasourceFactory =
            UpcomingRoomDataFactory(
                UpcomingroomcompositeDisposable,
                exploreRepository
            )// false

        val UpcomingrxPageList: Observable<PagedList<ExplorePagedData>> = RxPagedListBuilder(upcomigdatasourceFactory,upcomingconfig).buildObservable()



        UpcomingroomcompositeDisposable.add(
            UpcomingrxPageList
                .subscribeWith(object : DisposableObserver<PagedList<ExplorePagedData>>() {
                    override fun onComplete() {

                    }
                    override fun onNext(pagedList: PagedList<ExplorePagedData>) {
                        Log.d("Upcoming","page list = $pagedList")
                        UpcommingchildRecyclerViewController?.submitList(pagedList)
                    }
                    override fun onError(e: Throwable) {
                        Log.e("Home WebSocket", "error: ${e}")
//                        onPageLoadFailed(e)
                    }

                })
        )
return UpcommingchildRecyclerViewController
    }

    fun getFollowing() {
        viewModelScope.launch {

            val response = followRepository.getFollowing(userIdentifierPreferences.id)
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        following.postValue(result.data)
                    }

                    is Resource.Error -> {
                        // Handle Error state here if data requires then maybe set empty data
                    }
                }
            }

        }
    }


    private fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isInternetAvailable(application)
    }

    fun fetchAllTrendingRooms() {
        viewModelScope.launch {
            val response = exploreRepository.getTrendingRooms()
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        allTrendingRooms.value = result.data
                        Log.e("TrendingRooms", result.data.toString())
                    }
                    is Resource.Error -> {


                        Log.e(
                            "TrendingRooms",
                            "Error in fetching Trending Rooms Explore ${result.data}"
                        )
                    }
                }
            }
        }
    }

    fun fetchAllRecordedVideos() {
        viewModelScope.launch {
            if (userIdentifierPreferences.loggedIn) {

                val response = exploreRepository.getRecordedVideos()
                response.collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            allRecordVideo.value = result.data
                            Log.e("Recorded Videos", result.data.toString())
                        }

                        is Resource.Error -> {

                            Log.e(
                                "Recorded Videos",
                                "Error in fetching Recorded Videos  ${result.data}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun fetchAllCommonRecordedVideos() {
        viewModelScope.launch {

            val response = exploreRepository.getCommonRecordedVideos()
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        allCommonRecordVideo.value = result.data
                        Log.e("Common Recorded Videos", result.data.toString())
                    }

                    is Resource.Error -> {
                        Log.e(
                            "Recorded Videos",
                            "Error in fetching Recorded Videos  ${result.data}"
                        )
                    }
                }
            }

        }
    }

    fun fetchAllComponent() {

        viewModelScope.launch {
            if (userIdentifierPreferences.loggedIn) {

                val response = exploreRepository.componentSignedUser()
                response.collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {

                            allComponent.value = result.data


                            Log.e("ComponentRegister", result.data.toString())
                        }

                        is Resource.Error -> {
                            Log.e(
                                "Component",
                                "Error in fetching component when signed in  ${result.data}"
                            )
                        }
                    }
                }

            } else {

                val response = exploreRepository.componentRegisterUser()
                response.collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            allComponent.value = result.data
                            Log.e("ComponentRegister", result.data.toString())
                        }

                        is Resource.Error -> {
                            Log.e(
                                "Component",
                                "Error in fetching component when not signed in  ${result.data}"
                            )
                        }
                    }
                }

            }

        }
    }

    fun fetchLikedRooms() {
        viewModelScope.launch {

            val response = roomRepository.getLikes(userIdentifierPreferences.id)
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        likedRooms.value = result.data

                    }
                    is Resource.Error -> {

                    }
                }
            }

        }
    }

    fun fetchSuggestedRooms() {
        viewModelScope.launch {
            val response = exploreRepository.getsuggestedrooms(1)
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.message?.let { temp ->

                            temp.TrendingNews.forEach {
                                if (it.listeners.isNotEmpty()) {
                                    val array: ArrayList<Listeners> = arrayListOf()
                                    val listen = it.listeners.toSet()
                                    array.addAll(listen)
                                    it.listeners = array
                                }
                            }
//
//                            temp.TrendingTechnology.forEach {
//                                if (it.listeners.isNotEmpty()) {
//                                    val array: ArrayList<Listeners> = arrayListOf()
//                                    val listen = it.listeners.toSet()
//                                    array.addAll(listen)
//                                    it.listeners = array
//
//                                }
//                            }
//                            temp.HealthFitness.forEach {
//                                if (it.listeners.isNotEmpty()) {
//                                    val array: ArrayList<Listeners> = arrayListOf()
//                                    val listen = it.listeners.toSet()
//                                    array.addAll(listen)
//                                    it.listeners = array
//
//                                }
//                            }
//                            temp.Knowledgecarrers.forEach {
//                                if (it.listeners.isNotEmpty()) {
//                                    val array: ArrayList<Listeners> = arrayListOf()
//                                    val listen = it.listeners.toSet()
//                                    array.addAll(listen)
//                                    it.listeners = array
//
//                                }
//                            }
                            roomMessage.value = temp
                            addSuggestedRooms()

                        }
                    }

                    is Resource.Error -> {

                    }
                }
            }

        }
    }

    private fun addSuggestedRooms() {
        suggestedroomresponse.value = mutableListOf()
        roomMessage.value?.let {

            Log.d(TAG,"trending room size = ${it.TrendingTechnology.size}")

            if (it.TrendingTechnology.size > 0) {
                val catrooms: MutableList<roomInfo> = getSuggestionInfo(it.TrendingTechnology)
//                suggestedroomresponse.value=  SuggestionCategoryRooms("Trending Technology", catrooms)
                suggestedroomresponse.value.let {
                    if (it == null) {
                        Log.d("Hemlo", "fails here")
                    } else {
                        Log.d("Hemlo", "not failing")
                        it.add(SuggestionCategoryRooms("Trending Technology", catrooms))
                    }
                }

            }
            Log.d(TAG,"suggested room size = ${suggestedroomresponse.value}")
//            if (it.TrendingNews.size > 0) {
//                val catrooms: MutableList<roomInfo> = getSuggestionInfo(it.TrendingNews)
//
//                suggestedroomresponse.value.let {
//                    if (it == null) {
//                        Log.d("Hemlo", "fails here")
//                    } else {
//                        Log.d("Hemlo", "not failing")
//                        it.add(SuggestionCategoryRooms("Trending News", catrooms))
//                    }
//                }
//
//            }
//            if (it.HealthFitness.size > 0) {
//                val catrooms: MutableList<roomInfo> = getSuggestionInfo(it.HealthFitness)
//
//                suggestedroomresponse.value.let {
//                    if (it == null) {
//                        Log.d("Hemlo", "fails here")
//                    } else {
//                        Log.d("Hemlo", "not failing")
//                        it.add(SuggestionCategoryRooms("Health & Fitness", catrooms))
//                    }
//                }
//
//            }
//            if (it.Knowledgecarrers.size > 0) {
//                val catrooms: MutableList<roomInfo> = getSuggestionInfo(it.Knowledgecarrers)
//
//                suggestedroomresponse.value.let {
//                    if (it == null) {
//                        Log.d("Hemlo", "fails here")
//                    } else {
//                        Log.d("Hemlo", "not failing")
//                        it.add(SuggestionCategoryRooms("Knowldege and Carrers", catrooms))
//                    }
//                }
//
//            }

            allCategory.value?.categoryRooms = suggestedroomresponse.value!!
//            allCategory.value= allCategory.value

        }
    }

    private fun getSuggestionInfo(category: ArrayList<RoomResponse>): MutableList<roomInfo> {
        val catrooms: MutableList<roomInfo> = mutableListOf()
        category.forEachIndexed { index, room ->
            val currList: MutableList<RoomUsers> = mutableListOf()
            room.creator.let { cre ->
                currList.add(
                    RoomUsers(
                        cre.id,
                        room.title,
                        cre.name,
                        cre.profilePicture,
                        cre.channelName
                    )
                )
            }
            //TODO Uncomment these lines
//            room.listeners?.forEach { list ->
//                currList.add(
//                    RoomUsers(
//                        list.Id,
//                        room.title,
//                        list.name,
//                        list.profilePicture,
//                        list.channelName
//                    )
//                )
//            }
            catrooms.add(roomInfo(room.title, room.roomCode, currList,room.thumbnail))

        }
        return catrooms
    }

    fun fetchAllInfo() {
        viewModelScope.launch {

            val response = exploreRepository.getInfo()
            response.collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        allInfo.value = result.data
                        Log.e("ComponentInfo", result.data.toString())
                    }
                    is Resource.Error -> {

                    }
                }
            }

        }
    }


    fun fetchCategoriesAPI() {
        viewModelScope.launch {

            val response = exploreRepository.exploreUser()
            response.collectLatest { result ->
                Log.d(TAG,"result explore = $response")
                when (result) {
                    is Resource.Success -> {
                        allCategory.value = result.data
                        liveRooms.postValue(result.data?.liveRooms)
                    }

                    is Resource.Error -> {

                    }
                }
            }

        }
    }

    @ExperimentalPagingApi
    fun fetchLiveRoomsTest() {
        viewModelScope.launch {
            kotlin.runCatching {
                with(exploreRepository) {
                    if (userIdentifierPreferences.loggedIn) {
                        getLiveRoomsSignedUser()
                    } else {
                        getLiveRoomsRegisterUser()
                    }
                }
            }.onSuccess {
                it.map { pagedData ->
                    Log.e("ExploreFragmentTLR", "fetchLiveRoomsTest From ViewModel: $pagedData ")
                    liveRoomsTest.value = pagedData
                }

            }
        }
    }

    private fun fetchLiveRooms() {
        viewModelScope.launch {
            runCatching {
                with(roomRepository) {
                    getLiveRooms()
                }
            }.onSuccess {
                liveRooms.postValue(it)
//                allCategory.value!!.liveRooms = it
            }.onFailure { Log.e("explore viewmodel", "Failed while fetching live rooms", it) }
        }
    }

    fun onClickSearch(view: View) {
        Log.i("On Click Search", "Clicked")
        view.findNavController().navigate(ExploreFragmentDirections.actionExploreFragmentToExploreSearchFragment())
    }

    fun setChild123Title(parentPosition: Int, position: Int): String? {
        return when (parentPosition) {
            0 -> allCategory.value!!.publicEvents!![position].id

            1 -> "Top Profiles"
            else -> allCategory.value!!.videos!![position].first
        }
    }

    fun getRoomViews() {
        viewModelScope.launch {
            Log.e(
                "roomviews",
                "inside grid viewmodel: ${timelineRepository.getRoomViews().body()?.data?.viewers}"
            )
            val gson = GsonBuilder().create()
//             viewerList=gson.fromJson(repo.getRoomViews().body()?.data?.viewers.toString(),Array<ViewerX>::class.java).toList()
            val res = timelineRepository.getRoomViews().body()?.data?.viewers
            res?.forEach {
                viewerList.add(it)
            }
            viewerList.add(ViewerX("1", ProfileX("Tabishk111", "")))

            Log.e("roomviews", "viewer list is : $viewerList")
            testList.value = viewerList
        }

    }
}