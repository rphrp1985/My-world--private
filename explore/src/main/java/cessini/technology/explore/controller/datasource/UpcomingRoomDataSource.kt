package cessini.technology.explore.controller.datasource

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import cessini.technology.commonui.utils.Constant
import cessini.technology.home.viewmodel.SocketFeedViewModel
import cessini.technology.home.webSockets.HomeFeedWebSocket
import cessini.technology.home.webSockets.model.DataResponse
import cessini.technology.home.webSockets.model.HomeFeedSocketPayload
import cessini.technology.home.webSockets.model.HomeFeedSocketPayloadSuggestion
import cessini.technology.model.Creator
import cessini.technology.model.ExplorePagedData
import cessini.technology.model.Listener
import cessini.technology.model.Room
import cessini.technology.newrepository.explore.ExploreRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking


private const val TAG = "UpcomingRoomDataSource"


 class CanLoad(var canLoadMore: Boolean)


class UpcomingRoomDataSource(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    PageKeyedDataSource<Int, ExplorePagedData>() {



    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ExplorePagedData>) {

        Log.d(TAG,"arr initial sending")
        runBlocking {
            val explore = exploreRepository.explorePaging(1)

            Log.d(TAG,"explore =$explore")
            explore?.let {

                val arr= mutableListOf<ExplorePagedData>()


                for( data in it.message.rooms) {
                    val listener= mutableListOf<Listener>()


                    for(items in data.listeners){
                        listener.add(
                            Listener(
                                _id=items.profileId,
                                name= items.profileName,
                                profile_picture = items.profilePicture,
                                channel_name = items.profileChannelName,
                                isHandsOn = false
                            )
                        )
                    }
                    arr.add(ExplorePagedData(
                        room = Room(
                            id = data.id.oid,
                            name = data.code,
                            title = data.title,
                            creator = Creator(
                                id= data.creator.id,
                                name = data.creator.name,
                                channelName = data.creator.channelName,
                                profilePicture = data.creator.profilePicture
                            ),
                            time= data.schedule,
                            private = data.private,
                            live = data.live,
                            categories = data.category,
                            listeners = listener
                        )
                    ))
                }
                Log.d(TAG,"arr initial =$arr")
                callback.onResult(arr, 0, 2)
            }

        }



    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ExplorePagedData>) {

        val page= params.key

        Log.d(TAG,"arr later =$page")
        runBlocking {
            val explore = exploreRepository.explorePaging(page)
            explore?.let {

                val arr= mutableListOf<ExplorePagedData>()

                for( data in it.message.rooms) {
                    val listener= mutableListOf<Listener>()


                    for(items in data.listeners){
                        listener.add(
                            Listener(
                                _id=items.profileId,
                                name= items.profileName,
                                profile_picture = items.profilePicture,
                                channel_name = items.profileChannelName,
                                isHandsOn = false
                            )
                        )
                    }
                    arr.add(ExplorePagedData(
                        room = Room(
                            id = data.id.oid,
                            name = data.code,
                            title = data.title,
                            creator = Creator(
                                id= data.creator.id,
                                name = data.creator.name,
                                channelName = data.creator.channelName,
                                profilePicture = data.creator.profilePicture
                            ),
                            time= data.schedule,
                            private = data.private,
                            live = data.live,
                            categories = data.category,
                            listeners = listener
                        )
                    ))
                }
                Log.d(TAG,"arr later $page =$arr")
                callback.onResult(arr, page+1 )
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

class UpcomingRoomDataFactory(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

    ) :
    DataSource.Factory<Int, ExplorePagedData>() {

    private val DataSource = UpcomingRoomDataSource( compositeDisposable , exploreRepository)


    override fun create(): DataSource<Int, ExplorePagedData> {
        return DataSource
    }

    fun invalidiate(){
//        DataSource.
    }

}
