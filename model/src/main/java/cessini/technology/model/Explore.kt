package cessini.technology.model

import cessini.technology.model.recordmyspacegrid.recordGrid
import cessini.technology.model.recordmyspacegrid.viewpagerItem

data class Explore(
    val publicEvents: List<PublicEvent> = emptyList(),
    val topProfiles: List<TopProfile> = emptyList(),
    val rooms: List<Room> = emptyList(),
    var liveRooms: List<LiveRoom> = emptyList(),
    val videos: List<Pair<String, List<Video>>> = emptyList(),
    var items: Set<List<HealthFitness>?>? = null,
    var itemsInfo: Set<List<MessageI>>? = null,
    var categoryRooms : MutableList<SuggestionCategoryRooms> = mutableListOf(),
    var recordMyspcaeGrid : MutableList<viewpagerItem> =mutableListOf(),
    var recordCommonMyspcaeGrid : MutableList<viewpagerItem> =mutableListOf(),
    var visibleItemIndex: Int = 0,
    var trendingRooms: List<MessageT> = mutableListOf(),
    var itemRecorded: Set<List<HealthFitness>?>? = null,


    )

data class ExplorePagedData(
    val topProfilesModel:TopProfile?=null,
    val room:Room?= null,
    val trendingRoom: MessageT?=null
//    val live_room:SuggestionCategoryRooms?=null

)