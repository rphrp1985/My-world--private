package cessini.technology.myspace.create

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cessini.technology.commonui.common.BaseBottomSheet
import cessini.technology.commonui.common.BottomSheetLevelInterface
import cessini.technology.commonui.common.navigateToProfile
import cessini.technology.commonui.common.toast
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.RequestProfile
import cessini.technology.model.Room
import cessini.technology.myspace.R
import cessini.technology.myspace.databinding.FragmentCreateRoomBinding
import cessini.technology.myspace.databinding.FragmentShareBinding
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.airbnb.epoxy.EpoxyController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShareFragment(private val listener: BottomSheetLevelInterface?) :
    BaseBottomSheet<FragmentShareBinding>(R.layout.fragment_share),
    BottomSheetLevelInterface {
    var count=0

    companion object {
        private const val TAG = "ShareFragment"
    }

    private val baseViewModel by activityViewModels<BaseViewModel>()

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences

    @Inject
    lateinit var roomRepository: RoomRepository

    @Inject
    lateinit var navigator: Navigator

    private val acceptedRequets: MutableMap<String, MutableList<String>> = mutableMapOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchPreviousUsers()
    }

    private fun setupRecyclerView() {
        binding.friends.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }
    private fun fetchPreviousUsers(){
        Log.e(TAG, "Inside fetchRooms()")
        val roomsDeffered = viewLifecycleOwner.lifecycleScope.async {
            Log.e(
                TAG,
                "fetchRooms: ${profileRepository.getProfileRooms(userIdentifierPreferences.id)}"
            )
            runCatching { profileRepository.getProfileRooms(userIdentifierPreferences.id) }
                .onFailure {
// toast(it.message.orEmpty())
                }
                .getOrDefault(emptyList())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val rooms = roomsDeffered.await()

            val roomWithRequests =
                rooms?.map {
                    async { Pair(it, roomRepository.roomRequests(it.name)) }
                }?.awaitAll()

            if (roomWithRequests != null) {
                Log.e("build model room", roomWithRequests.toString())
                buildRequestModels(roomWithRequests)
            }
        }
    }
    private fun buildRequestModels(roomWithRequests: List<Pair<Room, List<RequestProfile>>>) {
        binding.friends.withModels {
            roomWithRequests
                .filter { it.second.isNotEmpty() }
                .onEach { (room, requests) ->
//                    buildRequestSeperator(room.title, room.name)
//                    buildRequests(room.title, requests)
                }
        }
    }

//    private fun EpoxyController.buildRequestSeperator(roomName: String, roomCode: String) {
//         {
//            id(roomName.hashCode())
//            title("${roomName.trimEnd()} requests")
//
//            if (acceptedRequets[roomName].isNullOrEmpty()) {
//                liveStatus(false)
//            } else {
//                liveStatus(true)
//            }
//
//            onClick { _ ->
//                if (acceptedRequets[roomName].isNullOrEmpty()) {
//                    toast(message = "Please accept one request.")
//                    return@onClick
//                }
//
//                viewLifecycleOwner.lifecycleScope.launch {
//                    runCatching {
//                        roomRepository.acceptRoomRequests(
//                            roomCode,
//                            acceptedRequets[roomName].orEmpty()
//                        )
//                    }.onSuccess {
//                        acceptedRequets[roomName] = mutableListOf()
//
//                        runCatching { fetchRequests() }
//                        toast(message = "Request accepted.")
//                    }.onFailure {
//                        toast(message = it.message.orEmpty())
//                    }
//                }
//            }
//        }
//    }
//
//    private fun EpoxyController.buildRequests(roomName: String, requests: List<RequestProfile>) {
//        requests.forEach {
//            listItemRequest {
//                id(it.id)
//                reqUserImage(it.picture)
//                reqUserChannel(it.channel)
//                reqUserName(it.name)
//                onReqProfileClick { _ ->
//                    navigateToProfile(
//                        it.id,
//                        baseViewModel.id.value.orEmpty()
//                    )
//                }
//                onAccept { _ -> addOrRemoveUser(roomName, it.id) }
//            }
//        }
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpFullScreen(bottomSheetDialog, Constant.settingBottomSheetHeight + 6)
        }

        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_SETTLING) {
                    count++
                    if (count % 2 == 0) {
                        dismiss()
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        try {
            listener?.onSheet1Dismissed()
        }catch (e:Exception){}
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSheet2Dismissed() {
        setLevel(0)
        listener?.onSheet2Dismissed()
    }

    override fun onSheet2Created() {
        setLevel(-1)
        listener?.onSheet2Created()
    }

    override fun onSheet1Dismissed() = Unit

    override fun getHeightOfBottomSheet(height: Int) {
        binding.shareConstraint.layoutParams.height = height + 10.toPx().toInt()
    }


}