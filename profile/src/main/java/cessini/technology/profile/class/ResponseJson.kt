package cessini.technology.profile.`class`

import cessini.technology.profile.fragment.publicProfile.ResponseMessageJson

data class ResponseJson (

    val  messages: List<ResponseMessageJson>,
    val meta: Meta

)

data class Meta(
    val messages_count:Int,
    val page:Int ,
    val can_load_more:Boolean
)