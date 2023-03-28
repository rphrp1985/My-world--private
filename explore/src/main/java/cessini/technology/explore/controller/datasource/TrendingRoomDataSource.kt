package cessini.technology.explore.controller.datasource

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import cessini.technology.commonui.activity.data
import cessini.technology.commonui.utils.Constant
import cessini.technology.home.viewmodel.SocketFeedViewModel
import cessini.technology.home.webSockets.HomeFeedWebSocket
import cessini.technology.home.webSockets.model.DataResponse
import cessini.technology.home.webSockets.model.HomeFeedSocketPayload
import cessini.technology.home.webSockets.model.HomeFeedSocketPayloadSuggestion
import cessini.technology.model.*
import cessini.technology.newrepository.explore.ExploreRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking


private const val TAG = "TrendingRoomDataSource"


//class CanLoad(var canLoadMore: Boolean)


class TrendingRoomDataSource(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    PageKeyedDataSource<Int, ExplorePagedData>() {



    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int,ExplorePagedData>) {

        Log.d(TAG,"arr initial sending")
        runBlocking {
            val room_flow = exploreRepository.getTrendingRoomsPaged(1)

            Log.d(TAG,"explore =$room_flow")
            room_flow.collectLatest {

                val TrendingRooms: TrendingRooms? = it.data

                TrendingRooms?.let {
                    val arr= mutableListOf<ExplorePagedData>()



                    for( data in it.message) {

                        arr.add(ExplorePagedData(trendingRoom = data))

                    }
                    Log.d(TAG,"arr initial =$arr")
                    callback.onResult(arr, 0, 2)
                }


            }

        }



    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ExplorePagedData>) {

        val page= params.key

        Log.d(TAG,"arr after sending")
        runBlocking {
            val room_flow = exploreRepository.getTrendingRoomsPaged(page)

            Log.d(TAG,"explore =$room_flow")
            room_flow.collectLatest {

                val TrendingRooms: TrendingRooms? = it.data

                TrendingRooms?.let {
                    val arr= mutableListOf<ExplorePagedData>()



                    for( data in it.message) {

                        arr.add(ExplorePagedData(trendingRoom = data))

                    }
                    Log.d(TAG,"arr initial =$arr")
                    callback.onResult(arr, page+1)
                }


            }

        }



    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int,ExplorePagedData>) {
    }

    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        super.addInvalidatedCallback(onInvalidatedCallback)
    }
    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {

        super.removeInvalidatedCallback(onInvalidatedCallback)
    }


}

class TrendingRoomDataFactory(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    DataSource.Factory<Int,ExplorePagedData>() {

    private val DataSource = TrendingRoomDataSource( compositeDisposable , exploreRepository)


    override fun create(): DataSource<Int, ExplorePagedData> {
        return DataSource
    }

    fun invalidiate(){
//        DataSource.
    }

}
