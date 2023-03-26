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


private const val TAG = "LiveRoomDataSource"


//class CanLoad(var canLoadMore: Boolean)


class Live_roomDataSource(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    PageKeyedDataSource<Int, roomInfo>() {



    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, roomInfo>) {

        Log.d(TAG,"arr initial sending")
        runBlocking {
            val room_flow = exploreRepository.getsuggestedrooms(1)

            Log.d(TAG,"explore =$room_flow")
            room_flow.collectLatest {

                val live_room: suggestionroomresponse? = it.data

                live_room?.let {
                    val arr= mutableListOf<roomInfo>()



                    for( data in it.message.TrendingTechnology) {
                        val listener= mutableListOf<RoomUsers>()

//                        for(users in data.listeners){
//                            listener.add(RoomUsers(
//
//                            ))
//                        }

                        arr.add(roomInfo(
                            title = data.title,
                            roomCode = data.roomCode,
                            thumbnail = data.thumbnail,
                            datas = mutableListOf()
                        ))

                    }
                    Log.d(TAG,"arr initial =$arr")
                    callback.onResult(arr, 0, 2)
                }


            }

        }



    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, roomInfo>) {

        val page= params.key

        Log.d(TAG,"arr after sending")
        runBlocking {
            val room_flow = exploreRepository.getsuggestedrooms(page)

            Log.d(TAG,"explore =$room_flow")
            room_flow.collectLatest {

                val live_room: suggestionroomresponse? = it.data

                live_room?.let {
                    val arr= mutableListOf<roomInfo>()



                    for( data in it.message.TrendingTechnology) {
                        val listener= mutableListOf<RoomUsers>()

//                        for(users in data.listeners){
//                            listener.add(RoomUsers(
//
//                            ))
//                        }

                        arr.add(roomInfo(
                            title = data.title,
                            roomCode = data.roomCode,
                            thumbnail = data.thumbnail,
                            datas = mutableListOf()
                        ))

                    }
                    Log.d(TAG,"arr initial =$arr")
                    callback.onResult(arr, page+1 )
                }


            }

        }



    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int,roomInfo>) {
    }

    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        super.addInvalidatedCallback(onInvalidatedCallback)
    }
    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {

        super.removeInvalidatedCallback(onInvalidatedCallback)
    }


}

class Live_RoomDataFactory(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    DataSource.Factory<Int, roomInfo>() {

    private val DataSource = Live_roomDataSource( compositeDisposable , exploreRepository)


    override fun create(): DataSource<Int, roomInfo> {
        return DataSource
    }

    fun invalidiate(){
//        DataSource.
    }

}
