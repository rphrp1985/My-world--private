package cessini.technology.home.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import cessini.technology.commonui.common.BaseViewModel
import cessini.technology.commonui.utils.networkutil.TAG
import cessini.technology.home.controller.HomeEpoxyController
import cessini.technology.home.epoxy.CanLoad
import cessini.technology.home.epoxy.HomeDatasourceFactory
import cessini.technology.model.Video
import cessini.technology.home.viewmodel.SocketFeedViewModel.Event
import cessini.technology.home.webSockets.HomeFeedWebSocket
import cessini.technology.home.webSockets.model.DataResponse
import cessini.technology.home.webSockets.model.HomeFeedSocketResponse
import cessini.technology.model.Profile
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.newrepository.websocket.suggestion.SuggestionWebSocket
import cessini.technology.newrepository.websocket.timeline.TimelineWebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocketFeedViewModel @Inject constructor(
    userIdentifierPreferences: UserIdentifierPreferences,
) : BaseViewModel<Event>() {

    private val _videos = MutableStateFlow(emptyList<Video>())
    val videos = _videos.asStateFlow()

    val homeFeeds = MutableLiveData<HomeFeedSocketResponse>()

    private val deviceId = userIdentifierPreferences.uuid

    private val _suggestedVideos = MutableStateFlow(emptyList<Video>())
    val suggestedVideos = _suggestedVideos.asStateFlow()
    var controller: HomeEpoxyController? = null
    var controllerSuggestion: HomeEpoxyController?=null

    private val timelineSocketBlock: (List<Video>) -> Unit = {
        _videos.value += it
    }

    val canLoadMore: CanLoad = CanLoad(true)
    private var timelineWebSocket = TimelineWebSocket(deviceId, timelineSocketBlock)

    private val suggestionWebSocket = SuggestionWebSocket { _suggestedVideos.value = it }

    var userID=""
//    var isInitiated= false


    lateinit var rxPageList: Observable<PagedList<DataResponse>>
    lateinit var rxPageListSuggestion: Observable<PagedList<DataResponse>>

    lateinit var compositeDisposable : CompositeDisposable
     var compositeDisposableSuggestion : CompositeDisposable?=null

    val webResponce: MutableLiveData<HomeFeedSocketResponse> = MutableLiveData()

    lateinit var  config  :PagedList.Config
    lateinit var  configSuggestion  :PagedList.Config
    lateinit var  datasourceFactory: HomeDatasourceFactory

    lateinit var  datasourceFactorySuggestion: HomeDatasourceFactory
   var callbackIni: PageKeyedDataSource.LoadInitialCallback<Int, DataResponse>? = null
    var callbackAfter: PageKeyedDataSource.LoadCallback<Int, DataResponse>? = null

    private val homeFeedWebSocket = HomeFeedWebSocket {
      Log.d(TAG,"viewmodel data = $it")
        callbackIni?.onResult(it.data, 0, it.meta.page + 1)
        callbackAfter?.onResult(it.data,it.meta.page+1)
        canLoadMore.canLoadMore = it.data.isNotEmpty()

        callbackAfter=null
        callbackAfter=null


    }

        fun setPaging( uuid: String) {
            config= PagedList.Config.Builder()
                .setPageSize(2)
                .setInitialLoadSizeHint(15)
                .setEnablePlaceholders(true)
                .build()
//            if(!isInitiated) {
//                isInitiated= true
                compositeDisposable = CompositeDisposable()
                datasourceFactory =
                    HomeDatasourceFactory(homeFeedWebSocket,
                        compositeDisposable,
                        userID,
                        this,
                        false)
                rxPageList = RxPagedListBuilder(datasourceFactory, config).buildObservable()
//            }


        }

    fun invalidiate(){
        datasourceFactory.invalidiate()
    }

    fun setPagingSuggestion( ) {
        configSuggestion= PagedList.Config.Builder()
            .setPageSize(2)
            .setInitialLoadSizeHint(15)
            .setEnablePlaceholders(true)
            .build()
        if(compositeDisposableSuggestion==null)
        compositeDisposableSuggestion = CompositeDisposable()
        else
            compositeDisposable.clear()

        datasourceFactorySuggestion =
            HomeDatasourceFactory(homeFeedWebSocket,
                compositeDisposableSuggestion!!,
                userID,
                this, false)
        rxPageListSuggestion = RxPagedListBuilder(datasourceFactorySuggestion, configSuggestion).buildObservable()

    }



    fun fetchPages(
        onPageReady: (PagedList<DataResponse>) -> Unit,
        onPageLoadFailed: (Throwable) -> Unit
    ) {

        compositeDisposable.add(
            rxPageList
                .subscribeWith(object : DisposableObserver<PagedList<DataResponse>>() {
                    override fun onComplete() {

                    }
                    override fun onNext(pagedList: PagedList<DataResponse>) {
                        onPageReady(pagedList)
                    }
                    override fun onError(e: Throwable) {
                        Log.e("Home WebSocket", "error: ${e}")
                        onPageLoadFailed(e)
                    }

                })
        )
    }

    fun fetchPagesSuggestion(
        onPageReady: (PagedList<DataResponse>) -> Unit,
        onPageLoadFailed: (Throwable) -> Unit
    ) {
        compositeDisposableSuggestion!!.add(
            rxPageListSuggestion
                .subscribeWith(object : DisposableObserver<PagedList<DataResponse>>() {
                    override fun onComplete() {

                    }
                    override fun onNext(pagedList: PagedList<DataResponse>) {
                        onPageReady(pagedList)
                    }
                    override fun onError(e: Throwable) {
                        Log.e("Home WebSocket", "error: ${e}")
                        onPageLoadFailed(e)
                    }

                })
        )
    }


//    fun sendUserPayload(payload: HomeFeedSocketPayload) {
//        homeFeedWebSocket.send(payload)
//    }

    fun fetchMoreVideos() {
        timelineWebSocket.next()
    }

    fun suggest(videoId: String) {
        suggestionWebSocket.suggest(videoId)
    }

    fun suggestedVideoClicked(position: Int) {
        Event.SuggestionClicked.send()

        viewModelScope.launch {
            delay(100)
            with(_suggestedVideos.value) {
                _videos.value = subList(position, size)
            }
        }
    }

    fun resetTimeline() {
        timelineWebSocket.close()
        _videos.value = emptyList()

        timelineWebSocket = TimelineWebSocket(deviceId, timelineSocketBlock)
    }

    override fun onCleared() {
        super.onCleared()

        timelineWebSocket.close()
        suggestionWebSocket.close()
    }

    fun initialCallback(callback: PageKeyedDataSource.LoadInitialCallback<Int, DataResponse>) {
        callbackAfter= null
     callbackIni= callback
    }

    fun afterCallback(callback: PageKeyedDataSource.LoadCallback<Int, DataResponse>) {
      callbackIni= null
        callbackAfter= callback
    }

    sealed class Event {
        object SuggestionClicked : Event()
    }
}
