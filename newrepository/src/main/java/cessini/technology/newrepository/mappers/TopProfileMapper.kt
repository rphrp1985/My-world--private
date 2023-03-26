package cessini.technology.newrepository.mappers

import cessini.technology.model.TopProfile
import cessini.technology.newapi.services.explore.model.response.ApiTopProfile

fun ApiTopProfile.toModel() = TopProfile(
    id = id,
    name = name,
    channelName = channelName,
    profilePicture = profilePicture,
    area_of_expert= area_of_expert

//    is_following = is_following
)
