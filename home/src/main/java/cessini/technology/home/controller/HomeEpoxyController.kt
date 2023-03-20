package cessini.technology.home.controller

import android.content.Context
import android.util.Log
import cessini.technology.home.EpoxyModelClasses.HomeEpoxyModel_
import cessini.technology.home.HomeLoadingBindingModel_
import cessini.technology.home.epoxy.CanLoad
import cessini.technology.home.model.JoinRoomSocketEventPayload
import cessini.technology.home.model.User
import cessini.technology.home.webSockets.model.DataResponse
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController


class HomeEpoxyController(
    private val context: Context,
    private val onJoinClicked: (JoinRoomSocketEventPayload) -> Unit,
    private val checkSignInStatus: () -> Boolean,
    val canLoadMore: CanLoad,
  private val warningFunction: () -> Unit,
): PagedListEpoxyController<DataResponse>(){

//    private val DUMMY_HLS_FEED_LINK = "http://amssamples.streaming.mediaservices.windows.net/69fbaeba-8e92-4740-aedc-ce09ae945073/AzurePromo.ism/manifest(format=m3u8-aapl)"

    override fun buildItemModel(currentPosition: Int, it: DataResponse?): EpoxyModel<*> {

        it?.let {
        return HomeEpoxyModel_().link(it?.hls).title(it?.title).context(context).roomcode(it?.room)
            .username(it!!.creator.name).
        joinRoomSocketEventPayload(
            JoinRoomSocketEventPayload(
                room = it?.room.toString(),
                user = User(
                    channelName = it!!.creator.channelName,
                    email = it.admin,
                    id = it.creator.id,
                    name = it.creator.name,
                    profilePicture = it.creator.profilePicture
                ),
                email = it.admin
            )
        ).

        signInStatus {
            checkSignInStatus()
        }.
        onJoinButtonClicked { joinRoomSocketEventPayload ->
            if (checkSignInStatus()) {
                onJoinClicked(joinRoomSocketEventPayload)
            }
        }.warning{
                    warningFunction()
                }.
        likeButtonClick {}.
        commentButtonClick {}.
        roomButtonClick {}.

        id("HomeFeed$currentPosition")



        } ?: run {
            return HomeLoadingBindingModel_().id("HomeLoading$currentPosition")
        }

//        home {
//            link(it?.hls)
//            title(it?.title)
//            context(context)
//            username(it?.admin)
//            joinRoomSocketEventPayload(
//                JoinRoomSocketEventPayload(
//                    room = it?.room.toString(),
//                    user = User(
//                        channelName = it!!.creator.channelName,
//                        email = it.creator.email,
//                        id = it.creator.id,
//                        name = it.creator.name,
//                        profilePicture = it.creator.profilePicture
//                    ),
//                    email = it.admin
//                )
//            )
//            signInStatus {
//                checkSignInStatus()
//            }
//            onJoinButtonClicked { joinRoomSocketEventPayload ->
//                if (checkSignInStatus()) {
//                    onJoinClicked(joinRoomSocketEventPayload)
//                }
//            }
//            likeButtonClick {}
//            commentButtonClick {}
//            roomButtonClick {}
//            id("HomeFeed$currentPosition")
//        }


    }
    override fun addModels(models: List<EpoxyModel<*>>) {
        Log.d("EPOXYCONTROLLER","addmodels called  ${canLoadMore.canLoadMore}")
        if (canLoadMore.canLoadMore) {
            super.addModels(
                models.plus(
                    HomeLoadingBindingModel_().id("loading")
                ).distinct()
            )
        } else {
            super.addModels(models.distinct())
        }
    }
}
/*TODO:
   class HomeEpoxyController:PagingDataEpoxyController<home_data>() {
    override fun buildItemModel(currentPosition: Int, item: home_data?): EpoxyModel<*> {


    }
}*/