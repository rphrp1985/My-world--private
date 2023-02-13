package cessini.technology.profile.activity.epoxy

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import cessini.technology.profile.`class`.ResponseJson
import cessini.technology.profile.`class`.deliveredMessage
import cessini.technology.profile.`class`.getMessage
import cessini.technology.profile.fragment.publicProfile.ResponseMessageJson
import com.google.gson.GsonBuilder
import io.reactivex.disposables.CompositeDisposable
import io.socket.client.Socket
import io.socket.emitter.Emitter


private const val TAG = "ChatDataSource"


 class CanLoad(var canLoadMore: Boolean)


class ChatDataSource(
    val service: Socket,
    val compositeDisposable: CompositeDisposable,
    val idMe: String,
    val idOth: String
) :
    PageKeyedDataSource<Int, ResponseMessageJson>() {

    val canLoadMore: CanLoad= CanLoad(true)


    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, ResponseMessageJson>) {

        Log.d("SOCKET","load initial ${service.connected()}")

        service.emit("previous_messages", getMessage(1,idMe, idOth).getMessages())

        val onUpdateChat = Emitter.Listener {
//                Log.d(TAG,"inside emitter : ${it[0]}")
            Log.d(TAG, "previous message repsonse ${it[0]}")
            val gson = GsonBuilder().create()
            val responseJson =
                gson.fromJson(it[0].toString(), ResponseJson::class.java)


            callback.onResult(responseJson.messages,0,responseJson.meta.page+1)

            canLoadMore.canLoadMore= responseJson.meta.can_load_more

            service.off("previous_messages");

        }
        service.on("previous_messages", onUpdateChat)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ResponseMessageJson>) {

        service.emit("previous_messages", getMessage(params.key,idMe, idOth).getMessages())

        val onUpdateChat = Emitter.Listener {
//                Log.d(TAG,"inside emitter : ${it[0]}")
            Log.d(TAG, "previous message repsonse ${it[0]}")
            val gson = GsonBuilder().create()
            val responseJson =
                gson.fromJson(it[0].toString(), ResponseJson::class.java)


            callback.onResult(responseJson.messages,responseJson.meta.page+1)

            for(message in responseJson.messages){
                emitDelivered(message)
            }

            canLoadMore.canLoadMore= responseJson.meta.can_load_more

            service.off("previous_messages");
        }
        service.on("previous_messages", onUpdateChat)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, ResponseMessageJson>) {


//        service.emit("previous_messages", getMessage(params.key,"ID626826540000000674", "ID709749170000000606").getMessages())
//
//        val onUpdateChat = Emitter.Listener {
////                Log.d(TAG,"inside emitter : ${it[0]}")
//            Log.d(TAG, "previous message repsonse ${it[0]}")
//            val gson = GsonBuilder().create()
//            val responseJson =
//                gson.fromJson(it[0].toString(), ResponseJson::class.java)
//
//
//            callback.onResult(responseJson.messages,responseJson.meta.page-1)
//
//            canLoadMore.canLoadMore= responseJson.meta.can_load_more
//
//            service.off("previous_messages");
//        }
//        service.on("previous_messages", onUpdateChat)


    }

    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        super.addInvalidatedCallback(onInvalidatedCallback)
    }
    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {

        super.removeInvalidatedCallback(onInvalidatedCallback)
    }
    fun emitDelivered(item:ResponseMessageJson){
        if(!item.is_delivered){
            service.emit("delivered", deliveredMessage(listOf(item._id)).getMessages())
        }
    }

}

class ChatDatasourceFactory(val service: Socket, val compositeDisposable: CompositeDisposable,val idMe:String,val idOth:String) :
    DataSource.Factory<Int, ResponseMessageJson>() {

    private val chatDataSource = ChatDataSource(service, compositeDisposable,idMe,idOth)



    override fun create(): DataSource<Int, ResponseMessageJson> {
        return chatDataSource
    }

    fun getMessageState()= chatDataSource.canLoadMore

    fun invalidiate(){
//        chatDataSource.invalidate()
    }

}
