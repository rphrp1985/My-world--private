package cessini.technology.myspace.access

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.AmazonSNSImpl
import cessini.technology.commonui.adapter.RecAdapter
import cessini.technology.commonui.common.BaseBottomSheet
import cessini.technology.commonui.common.navigateToProfile

import cessini.technology.commonui.common.toast
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.Listener
import cessini.technology.model.Room
import cessini.technology.myspace.R
import cessini.technology.myspace.access.MySpaceFragmentViewModel.Event.ShowToast
import cessini.technology.myspace.databinding.FragmentMyspaceBinding
import cessini.technology.myspace.databinding.LayoutListenerBinding
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class MySpaceFragment :
    BaseBottomSheet<FragmentMyspaceBinding>(R.layout.fragment_myspace) {

    private val myspaceArgs: MySpaceFragmentArgs by this.navArgs()

    private val viewModel by activityViewModels<MySpaceFragmentViewModel>()
    private val baseViewModel by activityViewModels<BaseViewModel>()

    private var currentroom:Room?= null

    private var roomListeners = mutableListOf<Listener>()

    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var navigator: Navigator

    lateinit var roomInformation: Room

    private var audioStatus = true
    private var videoStatus = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mySpaceFragmentState.value = -3
        fetchRooms()
        setupUi()
        setupShareButton()
        leaveRoom()
        listenEvents()
        collectRoom()
        setupCreatorOnClick()
        subscribeObservers()
        setupAudioVideoListener()
        spinner()
        viewModel.updateRoom(myspaceArgs.roomName)
        roomInformation = viewModel.room.value

        binding.view4.setOnClickListener {
            findNavController().navigate(
                MySpaceFragmentDirections.actionMySpaceFragmentToBottomSheetAcessRoomFragment()
            )
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpFullScreen(bottomSheetDialog, Constant.settingBottomSheetHeight + 6)
        }
        (dialog as BottomSheetDialog).behavior.isDraggable = false
        return dialog
    }

    private fun leaveRoom() {
        binding.btnLeaveRoom.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                profileRepository.profile.collectLatest {
                    Log.i("ProfileIDXYZ", it.id)
                    viewModel.removeListener(myspaceArgs.roomName, it.id)
                }
            }
            findNavController().navigateUp()
        }
    }

    private fun spinner() {
        val a = ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.room_option,
            R.layout.spinner_list
        ).also {
            it.setDropDownViewResource(R.layout.spinner_list)
        }

        binding.delete.adapter = a

        val click = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 1) {
                    viewModel.deleteRoom(myspaceArgs.roomName)
                    Toast.makeText(
                        requireContext(),
                        "Room deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.delete.onItemSelectedListener = click
    }

    private fun subscribeObservers() {
        viewModel.isMicOn
            .onEach {
                audioStatus = it
                //toggleMic(audioStatus)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.isVideoOn
            .onEach {
                videoStatus = it
                //toggleVideo(videoStatus)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun collectRoom() {
        viewModel.room
            .filter { it.id.isNotEmpty() }
            .take(count = 2)
            .onEach {
                if (it.name == myspaceArgs.roomName) {
                    updateRoomUi(it)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupUi() {
        val room = Room()
        binding.room = room
        binding.myspaceDropdownIcon.setOnClickListener { dismiss() }
    }

    private fun updateRoomUi(room: Room) {
        binding.room = room
        currentroom= room
        roomListeners.clear()
        roomListeners.addAll(room.listeners)
        room.listeners.map {
            createListener(it.name, it.profile_picture) {
                navigateToProfile(it._id, baseViewModel.id.value.orEmpty())
            }
        }.forEach(binding.listenersContainer::addView)

        // For setting visibility of hands on icon ac. to profile id
        val profileIds = room.listeners.map { it._id }


        addJoinButton(room)
    }

    private fun setupCreatorOnClick() {
        binding.usertham.setOnClickListener {
            navigateToProfile(
                binding.room?.creator?.id.orEmpty(),
                baseViewModel.id.value.orEmpty()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun addJoinButton(room: Room) {
        if (room.listeners.size == 5) return
        if (userIdentifierPreferences.id == room.creator.id) return
        if (room.listeners.any { it._id == userIdentifierPreferences.id }) return

        binding.listenersContainer.addView(createListener {
            if (!userIdentifierPreferences.loggedIn) {
                navigator.navigateToFlow(NavigationFlow.AuthFlow)
                return@createListener
            }
            if (roomListeners.find { it._id == userIdentifierPreferences.id } == null && room.creator.id != userIdentifierPreferences.id) {
                viewModel.joinRoom(room.name)
                addRequestData(room.creator.id,userIdentifierPreferences.id,room.title)
            }
        })
    }
    private fun addRequestData(creatorId:String,listenerId:String,title: String){
        var profile_image=""
        var username=""
        var message=""
        val colRef=Firebase.firestore.collection("GlobalNotifications")
        val olddocRef=colRef.document("${listenerId}")
        val newdocRef=colRef.document("${creatorId}")
        olddocRef.get()            .addOnSuccessListener { doc ->
                profile_image=doc.getString("profile_image").toString()
                username=doc.getString("username").toString()
                message="$username is requested to join in room : $title"
                newdocRef.collection("NotificationData")
                    .document()
                    .set(MyWorldNotification(listenerId,message,username,profile_image))
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                        } else {
                        }
                    }
            }
            .addOnFailureListener {
//                Log.d(AmazonSNSImpl.TAG, "Data not present in Firestore ${it.message}")
            }

    }

    private fun createListener(
        name: String = "Join",
        image: String = "",
        onClick: () -> Unit,
    ): View {
        return DataBindingUtil.inflate<LayoutListenerBinding>(
            layoutInflater,
            R.layout.layout_listener,
            binding.listenersContainer,
            false,
        ).also { it ->
            it.name = name
            it.image = image
            it.root.setOnClickListener { _ ->
                onClick()
                it.listenerImage.setBackgroundResource(R.drawable.round_enable_viewbutton)
            }
        }.root
    }

    private fun setupShareButton() {
        binding.share.setOnClickListener {

            createRoomLink()
//            val bundle = Bundle()
//
//            bundle.putString("sharelink", "https://www.myworld.in/room_${myspaceArgs.roomName}")
//            findNavController().navigate(R.id.action_global_share,bundle)
        }
    }

    private fun createRoomLink() {
        Log.d("Intent","The intent that I am looking at is called")
        val shareBody="https://www.myworld.com/liveRoom?code=${myspaceArgs.roomName}"
        val bottomSheetDialog=BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(cessini.technology.commonui.R.layout.fragment_share_myworld)
        val link=bottomSheetDialog.findViewById<TextView>(cessini.technology.commonui.R.id.textView16)
        link?.text = shareBody
        val name= bottomSheetDialog.findViewById<TextView>(cessini.technology.commonui.R.id.textView14)
        name?.text= currentroom?.name
        val image= bottomSheetDialog.findViewById<ImageView>(cessini.technology.commonui.R.id.imageView9)
//        image?.let {
////            Glide.with(this)
////                .load(profile.profilePicture)
////                .into(it)
//        };

        val pm: PackageManager = requireActivity().packageManager
        val finalLaunchables:MutableList<ResolveInfo> = ArrayList()
        val recyclerView=bottomSheetDialog.findViewById<RecyclerView>(cessini.technology.commonui.R.id.myrecview)
        val recAdapter= RecAdapter(pm,finalLaunchables,requireContext(),shareBody)
        recyclerView?.adapter=recAdapter
        val linearLayoutManager: LinearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        recyclerView?.layoutManager = linearLayoutManager
        val main = Intent(Intent.ACTION_MAIN,null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables: List<ResolveInfo> = pm.queryIntentActivities(main, 0)
        for (resolveInfo in launchables){
            val packageName=resolveInfo.activityInfo.packageName
            if (packageName.contains("om.viber.voip") ||
                packageName.contains("com.twitter.android") ||
                packageName.contains("com.google.android.gm") ||
                packageName.contains("com.whatsapp") ||
                packageName.contains("com.facebook.katana") ||
                packageName.contains("com.instagram.android")
            ) {
                finalLaunchables.add(resolveInfo)
            }
        }
        Collections.sort(
            finalLaunchables,
            ResolveInfo.DisplayNameComparator(pm)
        )
        requireActivity().runOnUiThread {
            recAdapter.notifyDataSetChanged()
        }
//        if(recAdapter.)
        bottomSheetDialog.show()
    }




    private fun listenEvents() {
        viewModel.events.onEach {
            when (it) {
                is ShowToast -> toast(it.message)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    // For mic thing
//    private fun toggleMic(status: Boolean) {
//        if (status) binding.ivAudio.setImageResource(R.drawable.ic_addaudio)
//        else binding.ivAudio.setImageResource(R.drawable.ic_removeaudio)
//    }

    private fun setupAudioVideoListener() {
        binding.setAudioListener {
            viewModel.isMicOn.value = !audioStatus
        }
        binding.setVideoListener {
            viewModel.isVideoOn.value = !videoStatus
        }
    }

    // For listener's video on feature

//    private fun toggleVideo(status: Boolean) {
//        if (status) binding.ivVideo.setImageResource(R.drawable.ic_addvideo)
//        else binding.ivVideo.setImageResource(R.drawable.ic_removevideo)
//    }

    private fun fetchRooms() {
        val roomsDeffered = viewLifecycleOwner.lifecycleScope.async {
            runCatching { profileRepository.getProfileRooms(userIdentifierPreferences.id) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val rooms = roomsDeffered.await()
            rooms.map {
                if (it != null) {
                    setupRoomUi(it)
                }
            }
        }
    }

    private fun setupRoomUi(rooms: List<Room>) {
        rooms.forEach {
            if (it.name == myspaceArgs.roomName) {
                binding.delete.visibility = View.VISIBLE
                binding.view4.visibility = View.VISIBLE
            }

            if (viewModel.room.value.live) {
                binding.isLive.visibility = View.VISIBLE
                binding.roomTime.visibility = View.GONE
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.mySpaceFragmentState.value = -2
    }

}
data class MyWorldNotification (
    val id: String,
    val message: String,
    val username: String,
    val profile_image: String
)



