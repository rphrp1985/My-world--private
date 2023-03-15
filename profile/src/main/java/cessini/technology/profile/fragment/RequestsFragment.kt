package cessini.technology.profile.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cessini.technology.commonui.common.navigateToProfile
import cessini.technology.commonui.common.toast
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.RequestProfile
import cessini.technology.model.Room
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.profile.R
import cessini.technology.profile.databinding.FragmentRequestsBinding
import cessini.technology.profile.listItemRequest
import cessini.technology.profile.listItemSepRequest
import com.airbnb.epoxy.EpoxyController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    companion object {
        private const val TAG = "RequestsFragment"
    }

    private var _binding: FragmentRequestsBinding? = null
    val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_requests,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchRequests()
    }
    private fun setupRecyclerView() {
        binding.requestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    private fun fetchRequests(){
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
        binding.requestsRecyclerView.withModels {
            roomWithRequests
                .filter { it.second.isNotEmpty() }
                .onEach { (room, requests) ->
                    buildRequestSeperator(room.title, room.name)
                    buildRequests(room.title, requests)
                }
        }
    }

    private fun EpoxyController.buildRequestSeperator(roomName: String, roomCode: String) {
        listItemSepRequest {
            id(roomName.hashCode())
            title("${roomName.trimEnd()} requests")

            if (acceptedRequets[roomName].isNullOrEmpty()) {
                liveStatus(false)
            } else {
                liveStatus(true)
            }

            onClick { _ ->
                if (acceptedRequets[roomName].isNullOrEmpty()) {
                    toast(message = "Please accept one request.")
                    return@onClick
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        roomRepository.acceptRoomRequests(
                            roomCode,
                            acceptedRequets[roomName].orEmpty()
                        )
                    }.onSuccess {
                        acceptedRequets[roomName] = mutableListOf()

                        runCatching { fetchRequests() }
                        toast(message = "Request accepted.")
                    }.onFailure {
                        toast(message = it.message.orEmpty())
                    }
                }
            }
        }
    }

    private fun EpoxyController.buildRequests(roomName: String, requests: List<RequestProfile>) {
        requests.forEach {
            listItemRequest {
                id(it.id)
                reqUserImage(it.picture)
                reqUserChannel(it.channel)
                reqUserName(it.name)
                onReqProfileClick { _ ->
                    navigateToProfile(
                        it.id,
                        baseViewModel.id.value.orEmpty()
                    )
                }
                onAccept { _ -> addOrRemoveUser(roomName, it.id) }
            }
        }
    }

    private fun addOrRemoveUser(roomName: String, id: String) {
        val acceptedUsers = acceptedRequets[roomName].orEmpty().toMutableList()
            .apply { if (contains(id)) remove(id) else add(id) }

        acceptedRequets[roomName] = acceptedUsers

        runCatching { fetchRequests() }
    }
}