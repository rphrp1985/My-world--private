package cessini.technology.home.epoxy

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import cessini.technology.home.viewmodel.SocketFeedViewModel
import cessini.technology.home.webSockets.HomeFeedWebSocket
import cessini.technology.home.webSockets.model.DataResponse
import cessini.technology.home.webSockets.model.HomeFeedSocketPayload
import cessini.technology.home.webSockets.model.HomeFeedSocketPayloadSuggestion
import io.reactivex.disposables.CompositeDisposable


private const val TAG = "HomeEpoxyDataSource"


 class CanLoad(var canLoadMore: Boolean)


class HomeEpoxyDataSource(
    val service: HomeFeedWebSocket,
    val compositeDisposable: CompositeDisposable,
    var userID: String,
    val socketFeedViewModel: SocketFeedViewModel,
    var suggestion: Boolean
) :
    PageKeyedDataSource<Int, DataResponse>() {


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, DataResponse>) {

//        suggestion= false
        socketFeedViewModel.initialCallback(callback)

        if(!suggestion) {
            Log.d(TAG,"send initia send")
            service.sendInitial(HomeFeedSocketPayload(
                user_id = userID,
                page = 1
            ), callback)
        }else{
            Log.d(TAG,"send initial send")
            service.sendInitialSuggestion(HomeFeedSocketPayloadSuggestion(
                user_id = userID,
                page = 1,
                keyword = "1"
            ), callback)
        }
//        runBlockin0g {
//            profile.collectLatest {
//                if(it.id=="") {
//                    userID=uuid
//                }else
//                {
//                    userID= it.id
//                }
//                    service.sendInitial(HomeFeedSocketPayload(
//                        user_id = userID,
//                        page = 1
//                    ), callback)
//
//            }
//        }



    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, DataResponse>) {

        socketFeedViewModel.afterCallback(callback)

        if(!suggestion){
        service.send(HomeFeedSocketPayload(
            user_id = userID ,
            page = params.key
        ))}
        else{
            Log.d(TAG,"send initial send  after")
            service.sendSuggestion(HomeFeedSocketPayloadSuggestion(
                user_id = userID ,
                page = params.key,
                keyword = "1"
            ))
        }


    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, DataResponse>) {
    }

    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        super.addInvalidatedCallback(onInvalidatedCallback)
    }
    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {

        super.removeInvalidatedCallback(onInvalidatedCallback)
    }


}

class HomeDatasourceFactory(
    val service: HomeFeedWebSocket,
    val compositeDisposable: CompositeDisposable,
    val idMe: String,
    socketFeedViewModel: SocketFeedViewModel,
    suggestion:Boolean

    ) :
    DataSource.Factory<Int, DataResponse>() {

    private val DataSource = HomeEpoxyDataSource( service,compositeDisposable,idMe, socketFeedViewModel,suggestion)



    override fun create(): DataSource<Int, DataResponse> {
        return DataSource
    }



    fun invalidiate(){
//        DataSource.
    }

}
