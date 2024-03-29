package cessini.technology.myspace.create

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cessini.technology.commonui.common.BaseViewModel
import cessini.technology.commonui.common.request
import cessini.technology.commonui.utils.networkutil.TAG
import cessini.technology.model.Subcategory
import cessini.technology.myspace.create.CreateRoomSharedViewModel.Event
import cessini.technology.myspace.create.CreateRoomSharedViewModel.Event.Failed
import cessini.technology.myspace.create.CreateRoomSharedViewModel.Event.RoomCreated
import cessini.technology.myspace.databinding.FragmentCreateRoomBinding
import cessini.technology.newrepository.explore.RegistrationRepository
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoomSharedViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userIdentifierPreferences: UserIdentifierPreferences,
    private val registrationRepository: RegistrationRepository
) : BaseViewModel<Event>() {
    val bottomSheetDraggedState = MutableLiveData<Float>()
    val roomTitle = MutableLiveData<String>()
    val time = MutableLiveData<Long>()
    var data= MutableLiveData<Map<String, List<Subcategory>>>()
    var flag=true

    var categorySet = mutableSetOf<String>()
    var subCategorySet = mutableSetOf<Int>()
    var loggedIn = MutableLiveData<Boolean>()

    private val _requestInProgress = MutableLiveData(false)

    val selectedRoomCategories = mutableSetOf<Int>()

    fun updateDraggedState(state:Float) {
        bottomSheetDraggedState.value = state
    }

    fun isLoggedIn(){
        loggedIn.value = userIdentifierPreferences.loggedIn
    }
    fun createRoom() {
        validate(reason = "Enter Title!", { return }) { roomTitle.value != null && roomTitle.value!!.isNotBlank() && roomTitle.value!!.isNotEmpty()}
        validate(reason = "Choose Time!", { return }) { time.value != null }
        validate(reason = "Choose Topic of Hub!", { return }) { !categorySet.isNullOrEmpty() }
        validate(reason = "Log in to Create Hub", { return }) { userIdentifierPreferences.loggedIn }

        return request(
            _requestInProgress,
            block = {
                roomRepository.createRoom(
                    title = roomTitle.value!!,
                    time = time.value!!,
                    private = false,
                    categories = categorySet,
                    roomCode = CreateRoomFragment.current_room_code

                )
            },
            onSuccess = { RoomCreated(name = it).send() },
            onFailure = { Failed(it.message.orEmpty()).send() },
        )
    }
    fun createInstantRoom(binding: FragmentCreateRoomBinding) {
        validate(reason = "Enter Title!", { return }) { roomTitle.value != null && roomTitle.value!!.isNotBlank() && roomTitle.value!!.isNotEmpty()}
        validate(reason = "Choose Time!", { return }) { time.value != null }
        validate(reason = "Choose Topic of Hub!", { return }) { !categorySet.isNullOrEmpty() }
        validate(reason = "Log in to Create Hub", { return }) { userIdentifierPreferences.loggedIn }

        binding.btnNext.visibility= View.GONE
        binding.permissionProgress.visibility= View.VISIBLE

        Log.d(TAG,"category set = $categorySet")

        Log.d(TAG,"milisecond = ${System.currentTimeMillis()}")
        return request(
            _requestInProgress,
            block = {
                roomRepository.createRoom(
                    title = roomTitle.value!!,
                    time =System.currentTimeMillis(),
                    private = false,

                    categories = categorySet,
                    roomCode = CreateRoomFragment.current_room_code

                )
            },
            onSuccess = { Event.InstantRoomCreated(roomID = it).send() },
            onFailure = { Failed(it.message.orEmpty()).send() },
        )
    }


    private inline fun validate(
        reason: String,
        runIfConditionFalse: () -> Unit,
        condition: () -> Boolean,
    ) {
        if (!condition()) {
            Failed(reason).send()
            runIfConditionFalse()
        }
    }

    fun addData(){
        if(flag) {
            viewModelScope.launch {
                try {

                    var response = registrationRepository.getVideoCategories()
                    data.value = response.data
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            flag=false
        }
    }

    sealed class Event {
        class RoomCreated(val name: String) : Event()
        class InstantRoomCreated(val roomID:String):Event()
        class Failed(val reason: String) : Event()
        class ShowToast(val message: String) : Event()
    }
}
