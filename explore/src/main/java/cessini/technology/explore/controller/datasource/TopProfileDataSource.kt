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
import cessini.technology.model.ExplorePagedData
import cessini.technology.model.TopProfile
import cessini.technology.newrepository.explore.ExploreRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking


private const val TAG = "TopProfileDataSource"


//class CanLoad(var canLoadMore: Boolean)


class TopProfileDataSource(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    PageKeyedDataSource<Int, ExplorePagedData>() {



    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ExplorePagedData>) {

        Log.d(TAG,"arr initial sending")
        runBlocking {
            val profiles = exploreRepository.exploreProfilePaging(1)

            Log.d(TAG,"explore =$profiles")
            profiles?.let {

                val arr= mutableListOf<ExplorePagedData>()

                for(profile in it.message.top_profiles){

                    arr.add(
                        ExplorePagedData(
                            topProfilesModel = TopProfile(
                                id= profile.id,
                                profilePicture = profile.profilePicture,
                                name= profile.name,
                                channelName = profile.channelName,
                                area_of_expert = profile.area_of_expert

                            )
                        )
                    )


                }


//                for( data in it.rooms) {
//                    arr.add(ExplorePagedData(
//                        room = data
//                    ))
//                }
                Log.d(TAG,"arr initial =$arr")
                callback.onResult(arr, 0, 2)
            }

        }



    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ExplorePagedData>) {

        val page= params.key

        Log.d(TAG,"arr later =$page")
        runBlocking {
            val profiles = exploreRepository.exploreProfilePaging(page)

            profiles?.let {

                val arr= mutableListOf<ExplorePagedData>()

                for(profile in it.message.top_profiles){

                    arr.add(
                        ExplorePagedData(
                            topProfilesModel = TopProfile(
                                id= profile.id,
                                profilePicture = profile.profilePicture,
                                name= profile.name,
                                channelName = profile.channelName,
                                area_of_expert = profile.area_of_expert

                            )
                        )
                    )


                }


//                for( data in it.rooms) {
//                    data.id= System.currentTimeMillis().toString()
//                    arr.add(ExplorePagedData(
//                        room = data
//                    ))
//                }
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

class TopProfileDataFactory(

    val compositeDisposable: CompositeDisposable,
    val exploreRepository: ExploreRepository

) :
    DataSource.Factory<Int, ExplorePagedData>() {

    private val DataSource = TopProfileDataSource( compositeDisposable , exploreRepository)


    override fun create(): DataSource<Int, ExplorePagedData> {
        return DataSource
    }

    fun invalidiate(){
//        DataSource.
    }

}
