package cessini.technology.home.webSockets.model

data class HomeFeedSocketPayload(
    val user_id:String,
    val page:Int
)
data class HomeFeedSocketPayloadSuggestion(
    val user_id:String,
    val page:Int,
    val Keyword:String
)
