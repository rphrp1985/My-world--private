package cessini.technology.model

data class RequestProfile(
    val id: String,
    val name: String,
    val channel: String,
    val picture: String,
)
data class PreviousProfile(
    val id: String,
    val name: String,
    val channel: String,
    val picture: String,
    val follower: Int,
    val following: Int,
    val area_of_expert: String
)