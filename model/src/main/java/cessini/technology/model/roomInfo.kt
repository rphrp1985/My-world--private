package cessini.technology.model

data class roomInfo(
    val title:String,
    val roomCode: String?,
    val datas :MutableList<RoomUsers>,
    var thumbnail:String?= null
)

data class RoomUsers(
    val id:String,
    val title: String,
    val name:String,
val url :String,
val channelName:String
)
