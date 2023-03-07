package cessini.technology.home.epoxy

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import cessini.technology.home.viewmodel.SocketFeedViewModel
import cessini.technology.home.webSockets.HomeFeedWebSocket
import cessini.technology.home.webSockets.model.DataResponse
import cessini.technology.home.webSockets.model.HomeFeedSocketPayload
import cessini.technology.model.Profile
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking


private const val TAG = "HomeEpoxyDataSource"


 class CanLoad(var canLoadMore: Boolean)


class HomeEpoxyDataSource(
    val service: HomeFeedWebSocket,
    val compositeDisposable: CompositeDisposable,
    var userID: String,
    val socketFeedViewModel: SocketFeedViewModel,
    val uuid: String
) :
    PageKeyedDataSource<Int, DataResponse>() {


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, DataResponse>) {

        socketFeedViewModel.initialCallback(callback)
        service.sendInitial(HomeFeedSocketPayload(
            user_id = userID,
            page = 1
        ), callback)
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

        service.send(HomeFeedSocketPayload(
            user_id = userID ,
            page = params.key
        ))

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
    uuid: String,

    ) :
    DataSource.Factory<Int, DataResponse>() {

    private val chatDataSource = HomeEpoxyDataSource( service,compositeDisposable,idMe, socketFeedViewModel,uuid)



    override fun create(): DataSource<Int, DataResponse> {
        return chatDataSource
    }



    fun invalidiate(){
//        chatDataSource.invalidate()
    }

}
