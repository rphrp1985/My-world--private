package cessini.technology.profile.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cessini.technology.commonui.common.dateTime
import cessini.technology.commonui.common.navigateToProfile
import cessini.technology.commonui.common.toast
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.Room
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.profile.R
import cessini.technology.profile.databinding.FragmentRoomsBinding
import cessini.technology.profile.databinding.FragmentScheduledBinding
import cessini.technology.profile.listItemRoom
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledFragment : Fragment() {
    companion object {
        private const val TAG = "ScheduledFragment"
    }

    private val baseViewModel by activityViewModels<BaseViewModel>()

    private var _binding: FragmentScheduledBinding? = null
    val binding get() = _binding!!

    @Inject
    lateinit var profileRepository: ProfileRepository
    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences
    @Inject
    lateinit var roomRepository: RoomRepository
    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_scheduled,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        runCatching { fetchRooms() }
    }
    private fun setupRecyclerView() {
        binding.manageRoomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchRooms() {
        Log.e(TAG, "Inside fetchRooms()")
        val roomsDeffered = viewLifecycleOwner.lifecycleScope.async {
            Log.e(
                TAG,
                "fetchRooms: ${profileRepository.getScheduledRooms(userIdentifierPreferences.id)}"
            )
            runCatching { profileRepository.getScheduledRooms(userIdentifierPreferences.id) }
                .onFailure {
                    toast(it.message.orEmpty())
                }
                .getOrDefault(emptyList())
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val rooms = roomsDeffered.await()
            Log.e(TAG, "fetchRooms: $rooms")
            if (rooms != null) {
                if (rooms.isNotEmpty()) {
                    buildRoomModels(rooms)
                } else {
                    binding.apply {
                        manageRoomText.visibility = View.VISIBLE
                        noRoomsLabel.visibility = View.VISIBLE
                        manageRoomsRecyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun buildRoomModels(rooms: List<Room>) {
        Log.e("buildModelRoomsFunc", rooms.toString())
        binding.manageRoomsRecyclerView.withModels {
            rooms.forEach { room ->
                listItemRoom {
                    id(room.id)
                    Log.i("Room data", "$room")
                    onGoLiveClick { v ->
                        val intent = Intent()
                        intent.setClassName(
                            requireContext(),
                            "cessini.technology.commonui.activity.GridActivity"
                        )
                        startActivity(intent)

                        viewLifecycleOwner.lifecycleScope.launch {
                            runCatching { roomRepository.startRoom(room.name) }.onSuccess {
                                navigator.navigateToFlow(NavigationFlow.AccessRoomFlow(room.name))
                                binding.apply {
                                    manageRoomText.visibility = View.GONE
                                    noRoomsLabel.visibility = View.GONE
                                    manageRoomsRecyclerView.visibility = View.VISIBLE
                                }
/////////////////////////////////////////////////////////
                                toast(message = "You are Live.")

                            }.onFailure {
                                toast(message = "Unable to go live.")
                                binding.apply {
                                    manageRoomText.visibility = View.VISIBLE
                                    noRoomsLabel.visibility = View.VISIBLE
                                    manageRoomsRecyclerView.visibility = View.GONE
                                }
                            }
                        }
                    }
                    onDetail { _ ->
                        navigator.navigateToFlow(NavigationFlow.AccessRoomFlow(room.name))
                    }
                    userPicture(room.creator.profilePicture)
                    roomName(room.title)
                    userName(room.time.dateTime())
                    onProfileClick { _ ->
                        navigateToProfile(
                            room.creator.id,
                            baseViewModel.id.value.orEmpty()
                        )
                    }
                }
            }
        }
    }

}