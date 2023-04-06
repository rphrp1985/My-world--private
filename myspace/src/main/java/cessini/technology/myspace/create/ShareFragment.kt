package cessini.technology.myspace.create

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.adapter.RecAdapter
import cessini.technology.commonui.common.*
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.PreviousProfile
import cessini.technology.model.Profile
import cessini.technology.model.RequestProfile
import cessini.technology.model.Room
import cessini.technology.myspace.FriendsModel
import cessini.technology.myspace.FriendsModel_
import cessini.technology.myspace.R
import cessini.technology.myspace.databinding.FragmentCreateRoomBinding
import cessini.technology.myspace.databinding.FragmentShareBinding
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.myspace.RoomRepository
import cessini.technology.newrepository.myworld.FollowRepository
import cessini.technology.newrepository.myworld.ProfileRepository
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.TypedEpoxyController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class ShareFragment(private val listener: BottomSheetLevelInterface?) :
    BaseBottomSheet<FragmentShareBinding>(R.layout.fragment_share),
    BottomSheetLevelInterface {
    var count=0

    companion object {
        private const val TAG = "ShareFragment"
    }
    private lateinit var epoxyControllerPrevious: MyEpoxyController
    private lateinit var epoxyControllerFollower: MyEpoxyController
    private val baseViewModel by activityViewModels<BaseViewModel>()

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences

    @Inject
    lateinit var roomRepository: RoomRepository

    @Inject
    lateinit var followRepository: FollowRepository

    @Inject
    lateinit var navigator: Navigator

    private val acceptedRequets: MutableMap<String, MutableList<String>> = mutableMapOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchPreviousUsers()
        fetchFollowers()
        binding.connectButton.setOnClickListener {
            val link="https://www.myworld.com/liveRoom?code=${CreateRoomFragment.current_room_code}"
            val pm: PackageManager = requireActivity().packageManager
            var finalLaunchables:ResolveInfo?=null
//            val recAdapter= RecAdapter(pm,finalLaunchables,requireContext(),shareBody)
            val main = Intent(Intent.ACTION_MAIN,null)
            main.addCategory(Intent.CATEGORY_LAUNCHER)
            val launchables: List<ResolveInfo> = pm.queryIntentActivities(main, 0)
            for (resolveInfo in launchables){
                val packageName=resolveInfo.activityInfo.packageName
                if (packageName.contains("com.whatsapp")) {
                    finalLaunchables=resolveInfo
                }
            }
            if (finalLaunchables!=null){
                val launchable = finalLaunchables as ResolveInfo
                val activity: ActivityInfo = launchable.activityInfo
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                val packageName = activity.packageName
                i.putExtra(Intent.EXTRA_TEXT, link)
                i.setPackage(packageName)
                context?.startActivity(i)
            }
            else{
                Toast.makeText(context,"Whatsapp is not installed in your device",Toast.LENGTH_SHORT).show()
            }

        }
        binding.editTextSearch.setOnClickListener {
            val searchFragment = SearchUsersFragment(this@ShareFragment)
            searchFragment.show(parentFragmentManager, searchFragment.tag)
        }

    }

    private fun setupRecyclerView() {
        binding.friends.apply {
            layoutManager = LinearLayoutManager(context)
        }
        epoxyControllerPrevious = MyEpoxyController(userIdentifierPreferences.id,requireContext())

        binding.friends.setController(epoxyControllerPrevious)
        binding.followers.apply {
            layoutManager = LinearLayoutManager(context)
        }
        epoxyControllerFollower = MyEpoxyController(userIdentifierPreferences.id,requireContext())

        binding.followers.setController(epoxyControllerFollower)

    }
    private fun fetchPreviousUsers(){
        Log.e(TAG, "Inside previousUsers()")
        val previousUsers = viewLifecycleOwner.lifecycleScope.async {
            Log.e(
                TAG,
                "fetchRooms: ${roomRepository.previousRoomUsers()}"
            )
            runCatching { roomRepository.previousRoomUsers() }
                .onFailure {
                    toast(it.message.orEmpty())
                }
                .getOrDefault(emptyList())
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val users = previousUsers.await()
            Log.e(TAG, "fetchRooms: $users")
            if (users.isNotEmpty()) {
                binding.apply {
                    rlFriends.visibility=View.VISIBLE
                }
                epoxyControllerPrevious.setData(users)
                //buildRoomModels(users)
            } else {
                binding.apply {
                    rlFriends.visibility=View.GONE
                }
            }
        }
    }
    private fun fetchFollowers(){
        Log.e(TAG, "Inside followers()")
        val followers = viewLifecycleOwner.lifecycleScope.async {
            Log.e(
                TAG,
                "fetchFollowers: ${followRepository.getFollowers(userIdentifierPreferences.id)}"
            )
            runCatching { followRepository.getFollowers(userIdentifierPreferences.id) }
                .onFailure {
                    toast(it.message.orEmpty())
                }
                .getOrDefault(emptyList())
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val users = followers.await()
            val data=ArrayList<PreviousProfile>()
            users.forEach {
                val profile=profileRepository.getProfile(it.id)
                data.add(PreviousProfile(it.id,it.name,it.channelName,it.profilePicture,profile.followerCount,profile.followingCount,profile.expertise))
            }
            Log.e(TAG, "fetchFollowers: $data")
            if (users.isNotEmpty()) {
                binding.apply {
                    rlFollowers.visibility=View.VISIBLE
                }
                epoxyControllerFollower.setData(data)
                //buildRoomModels(users)
            } else {
                binding.apply {
//                        manageRoomText.visibility = View.VISIBLE
//                        noRoomsLabel.visibility = View.VISIBLE
//                        manageRoomsRecyclerView.visibility = View.GONE
                }
            }
        }
    }
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
class MyEpoxyController(var id:String,var context: Context) : TypedEpoxyController<List<PreviousProfile>>() {
    override fun buildModels(data: List<PreviousProfile>) {
        data.forEach { myData ->
            FriendsModel_(id,context)
                .id(myData.id)
                .data(myData)
                .addTo(this)
        }
    }
}
