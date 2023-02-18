package cessini.technology.home.controller

import android.content.Context
import cessini.technology.commonui.activity.epoxy
import cessini.technology.home.EpoxyModelClasses.home
import cessini.technology.home.model.HomeEpoxyStreamsModel
import cessini.technology.home.model.JoinRoomSocketEventPayload
import cessini.technology.home.model.User
import com.airbnb.epoxy.AsyncEpoxyController

class HomeEpoxyController(
    private val context:Context,
    private val onJoinClicked: (JoinRoomSocketEventPayload)->Unit,
    private val checkSignInStatus: () -> Boolean
):AsyncEpoxyController() {
    var streams:List<HomeEpoxyStreamsModel> = emptyList()
    set(value){
        field=value
        requestModelBuild()
    }

    private val DUMMY_HLS_FEED_LINK = "http://amssamples.streaming.mediaservices.windows.net/69fbaeba-8e92-4740-aedc-ce09ae945073/AzurePromo.ism/manifest(format=m3u8-aapl)"

    override fun buildModels() {
       streams.forEachIndexed { index, it ->
            home {
                link(DUMMY_HLS_FEED_LINK)
                title(it.title)
                context(context)
                username(it.admin)
                joinRoomSocketEventPayload(
                    JoinRoomSocketEventPayload(
                        room = it.room,
                        user = User(
                            channelName = it.user.channelName,
                            email = it.user.email,
                            id = it.user.id,
                            name = it.user.name,
                            profilePicture = it.user.profilePicture
                        ),
                        email = it.email
                    )
                )
                signInStatus {
                    checkSignInStatus()
                }
                onJoinButtonClicked { joinRoomSocketEventPayload->
                    if (checkSignInStatus()) {
                        onJoinClicked(joinRoomSocketEventPayload)
                    }
                }
                likeButtonClick {}
                commentButtonClick {}
                roomButtonClick {}
                id("HomeFeed$index")
            }
       }
    }
}
/*TODO:
   class HomeEpoxyController:PagingDataEpoxyController<home_data>() {
    override fun buildItemModel(currentPosition: Int, item: home_data?): EpoxyModel<*> {


    }
}*/