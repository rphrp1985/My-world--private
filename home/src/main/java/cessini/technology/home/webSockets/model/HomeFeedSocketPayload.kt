package cessini.technology.home.webSockets.model

data class HomeFeedSocketPayload(
    val User_id:String,
    val page:Int
)
data class HomeFeedSocketPayloadSuggestion(
    val User_id:String,
    val page:Int,
    val Keyword:String
)
