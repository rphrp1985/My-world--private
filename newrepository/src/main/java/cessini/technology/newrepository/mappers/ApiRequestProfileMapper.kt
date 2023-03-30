package cessini.technology.newrepository.mappers

import cessini.technology.model.PreviousProfile
import cessini.technology.model.RequestProfile
import cessini.technology.newapi.services.myspace.model.response.ApiRequestProfile
import cessini.technology.newapi.services.myspace.model.response.PreviousUserData

fun ApiRequestProfile.toModel() = RequestProfile(
    id = id,
    name = name,
    channel = channelName,
    picture = profilePicture,
)
fun PreviousUserData.toModel() = PreviousProfile(
    id = id,
    name = name,
    channel = channelName,
    picture = profilePicture,
    follower = follower,
    following = following,
    area_of_expert = area_of_expert
)
