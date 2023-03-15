package cessini.technology.newapi.services.myspace.model.body

data class RoomBody(
    val title: String,
    val category: MutableSet<String>,
    val schedule: Long,
    val private: Boolean,
//    val users: List<String>,

)
