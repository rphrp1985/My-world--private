package cessini.technology.newrepository.myspace

import cessini.technology.model.PreviousProfile
import cessini.technology.model.RequestProfile
import cessini.technology.model.Room
import cessini.technology.model.UserUpiData
import cessini.technology.model.search.UserLikes
import cessini.technology.newapi.extensions.getOrThrow
import cessini.technology.newapi.services.myspace.MySpaceService
import cessini.technology.newapi.services.myspace.RoomSocket
import cessini.technology.newapi.services.myspace.model.body.AcceptRoomRequestBody
import cessini.technology.newapi.services.myspace.model.body.RoomBody
import cessini.technology.newapi.services.myspace.model.body.UserLikeBody
import cessini.technology.newapi.services.myspace.model.response.ApiStreamKey
import cessini.technology.newapi.services.myworld.MyWorldService
import cessini.technology.newrepository.extensions.createMultipartBody
import cessini.technology.newrepository.mappers.toModel
import cessini.technology.newrepository.mappers.toModels
import cessini.technology.newrepository.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val roomApi: MySpaceService,
    private val profileApi: MyWorldService,
    private val Roomsocket: RoomSocket
    ) {
    suspend fun createRoom(
        title: String,
        time: Long,
        private: Boolean = false,
        roomCode: String,

        categories: MutableSet<String>
    ) = roomApi.createRoom(RoomBody(title =title, schedule = time, private = private, category = categories, room_code= roomCode))
        .getOrThrow().data.name

    suspend fun getRoom(name: String): Room =
        roomApi.getRoom(name).getOrThrow().data.room.toModel()

    suspend fun joinRoom(roomName: String) {
        roomApi.joinRoom(roomName)
    }

    suspend fun roomRequests(roomName: String): List<RequestProfile> {
        return roomApi.roomRequest(roomName).getOrThrow().data.firstOrNull()?.
            requests?.map { it.toModel() }.orEmpty()
    }
    suspend fun previousRoomUsers(): List<PreviousProfile>{
        return roomApi.getPreviousUsers().getOrThrow().message.map { it.toModel() }.orEmpty()
    }

    suspend fun sendSnapShot(roomName: String,file: File): String {
        file.createMultipartBody(
            formName = "thumbnail",
            contentType = "image/*"
        )?.let {
            return roomApi.sendSnapShot(roomName,
                it
            ).getOrThrow().message
        }

       return "Error in sendsnapshot"
    }

    suspend fun getStreamKey(room: String, email: String): String = Roomsocket.getKey(room,email).getOrThrow().data



    suspend fun acceptRoomRequests(roomName: String, listenersid: List<String>) {
        roomApi.acceptRequest(AcceptRoomRequestBody(roomName, listenersid))
    }

    suspend fun getLiveRooms(): List<cessini.technology.model.LiveRoom>? =
        roomApi.getLiveRooms().getOrThrow()

    suspend fun startRoom(roomCode: String) : Boolean{
        return roomApi.startRoom(roomCode).isSuccessful
    }

    suspend fun deleteRoom(roomCode: String) : Boolean{
        return roomApi.deleteRoom(roomCode).isSuccessful
    }

    suspend fun removeListener(roomCode: String, profileID: String) : Boolean{
        return roomApi.removeListener(roomCode, profileID).isSuccessful
    }

    suspend fun getCreatorUPIData(userId: String): UserUpiData {
        return profileApi.getCreatorUPIData(userId).data.toModels()
    }

    suspend fun getLikes(id: String): Flow<Resource<UserLikes>> {
        return flow {

            try {
                val response =  roomApi.getLikes(id)
                emit(Resource.Success(data = response))
            } catch (e: IOException) {
                e.printStackTrace()
                //emit custom error message here according to exception or error that may be displayed to the user
                emit(Resource.Error("Couldn't load data"))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            }

        }
    }

    suspend fun likeRoom(roomName: String, userId:String) {
        roomApi.likeRoom(UserLikeBody(userId,roomName))
    }
}
