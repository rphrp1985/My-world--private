package cessini.technology.profile.fragment

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.activity.HomeActivity
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.model.Profile
import cessini.technology.myspace.create.CreateRoomFragment
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.Navigator
import cessini.technology.profile.R
import cessini.technology.profile.adapter.PrivateProfileTabAdapter
import cessini.technology.profile.databinding.FragmentProfileBinding
import cessini.technology.profile.viewmodel.EditUserProfileViewModel
import cessini.technology.profile.viewmodel.ProfileViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import uz.jamshid.library.progress_bar.CircleProgressBar
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(R.layout.fragment_profile), MotionLayout.TransitionListener {
    companion object {
        private const val TAG = "ProfileFragment"
    }

    private val profileViewModel : ProfileViewModel by activityViewModels()
    private val editUserProfileViewModel : EditUserProfileViewModel by activityViewModels()
    private val baseViewModel:BaseViewModel by activityViewModels()

    private var tabLayout: TabLayout? = null
    private var shareMenuItem: MenuItem? = null

    @Inject
    lateinit var navigator: Navigator

    // FIXME: View states should be moved to view model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileViewModel.activity = requireActivity()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: ")
        ProfileConstants.public = false


        (activity as HomeActivity).setUpNavProfileIcon(null, (activity as HomeActivity).profileDrawable, true)

        //setting up the viewPager and the TabLayout
//        binding.viewPager2.isSaveEnabled = false
        tabLayout = binding.profileTab

        setUpToolbar()
        setHasOptionsMenu(true)

        if (profileViewModel.isNetworkAvailable()){
            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.profile.collectLatest {
                    if (it.id.isEmpty()) {
                        handleFirstTimeUser()
                        return@collectLatest
                    }
                    binding.profile = it

                    Log.d("Profile-id","profile id = ${it.id}")
                    val bio=SpannableString(it.bio+" ")
                    val expert=modifyFont(it.expertise)
                    expert.setSpan(ForegroundColorSpan(Color.rgb(35,153,234)),0,expert.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    val final=SpannableStringBuilder("")
                    final.append(bio)
                    final.append(expert)
                    binding.profileBio.setText(final,TextView.BufferType.SPANNABLE)

                    (activity as HomeActivity).setUpNavProfileIcon(it.profilePicture, null, true)
                    if(it.verified)
                        binding.imageView8.visibility=VISIBLE
                }
            }


            arguments?.getString("room_name")?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(100)
                    navigator.navigateToFlow(NavigationFlow.AccessRoomFlow(it))
                }
            }

            lifecycleScope.launch {
                profileViewModel.registerCategoriesAfterLogin(profileViewModel.userId)
            }

            fetchHasMessages()
        }

        (activity as HomeActivity).baseViewModel.authFlag.observe(viewLifecycleOwner) {
            if (it) {
                profileViewModel.loadProfile(true)
                binding.imageView8.isVisible = binding.profile?.verified == true
            } else {
                Log.d(TAG, "Auth")
                profileViewModel.goToAuth()
            }
        }

        //setting SwipeRefreshLayout
        binding.swipeRefreshLayout.apply {
            val cp = CircleProgressBar(requireContext())
            cp.setBorderWidth(2)
            binding.swipeRefreshLayout.setCustomBar(cp)

            setRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({

                    try {
                        showShimmer()
                        binding.swipeRefreshLayout.setRefreshing(false)
                    } catch (e: NullPointerException) {
                        //empty catch block
                    }
                }, 3000)
            }
        }

        binding.motionLayout.setTransitionListener(this)
        binding.viewPager2.adapter = PrivateProfileTabAdapter(this.childFragmentManager, lifecycle)


    }

    private fun fetchHasMessages() {
        if(!profileViewModel.isHasMessagesCalled) {
            profileViewModel.isHasMessagesCalled= true

            lifecycleScope.launch {
                kotlin.runCatching { profileViewModel.getMessageForRoom() }
                    .onSuccess {
                        profileViewModel.hasMessages = it.isNotEmpty()
                    }
            }
        }
    }

    private fun handleFirstTimeUser() {

        binding.profile = Profile(
            id = "", name = "Profile name", email = "", channelName = "channel name", provider = "",
            bio = "", profilePicture = "", location = "", followerCount = 0, followingCount = 0,
            following = false,expertise = "",verified = false
        )
    }

    override fun onResume() {
        super.onResume()

        hideShimmer()
        ProfileConstants.public = false
        /**Hide the keyboard.*/
        val inputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)


//        Log.d(TAG,"value = ${profileViewModel.profileLoadProgress.value}")
        profileViewModel.profileLoadProgress.observe(viewLifecycleOwner) {

            when (it) {
                0 -> {
                    binding.viewPager2.visibility = View.GONE
                    Log.d(TAG, it.toString())
                }
                100 -> {
                    hideShimmer()
                    binding.viewPager2.visibility = View.VISIBLE
                    Log.d(TAG, it.toString())
                    profileDetails()
                }
                else -> {
                    hideShimmer()
                    binding.viewPager2.visibility = View.VISIBLE
//                    Toast.makeText(activity, "Profile Load Failed", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, it.toString())
                }
            }
        }


        binding.addStoryProfileFragment.setOnClickListener {
            val homeActivity = run { requireActivity() as HomeActivity }

            if (!profileViewModel.rooms.value.isNullOrEmpty() && profileViewModel.hasMessages) {
                val createRoomFragment = CreateRoomFragment(null)
                createRoomFragment.show(parentFragmentManager, createRoomFragment.tag)
                return@setOnClickListener
            }

            findNavController().navigate(ProfileFragmentDirections.toManageRoom(homeActivity.baseViewModel.id.value.orEmpty()))
        }


        /** resetting view model values in edit profile */
        editUserProfileViewModel.resetEditProfileData()

        if(binding.motionLayout.currentState == R.id.end) {
            binding.swipeRefreshLayout.isEnabled = false
        }
    }

    private fun profileDetails() {
        //Setting up the ViewPager Adapter and The TabLayout
        binding.viewPager2.isSaveEnabled =false
        TabLayoutMediator(tabLayout!!, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Hub"
                }
                1 -> {
                    tab.text = "Video"
                }
                2 -> {
                    tab.text = "Save"
                }

            }
        }.attach()

        /** Navigating user to the follow following fragment.*/
        binding.followContainer.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                profileViewModel.profile
                    .filter { it.id.isNotBlank() }
                    .collectLatest {
                        findNavController().navigate(
                            ProfileFragmentDirections.actionProfileFragmentToFollowerFollowing3(
                                it.id
                            )
                        )
                    }
            }
        }
    }

    private fun setUpToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.searchToolbar)
        binding.searchToolbar.showOverflowMenu()
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_toolbar_menu, menu)
        shareMenuItem = menu.findItem(R.id.action_profile_invite)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_profile_settings) {
            findNavController().navigate(R.id.action_profileFragment_to_profileSettingFragment)

        } else if (id == R.id.action_profile_invite) {
//            openCustoms()
//            openSharingVia()
            (activity as HomeActivity).baseViewModel.loadUserInfo()
            var bundle = Bundle()
            (activity as HomeActivity).baseViewModel.name.observe(viewLifecycleOwner){
                if (!(it == null)){
                    bundle.putString("username", it.toString())
                }
            }
            (activity as HomeActivity).baseViewModel.profileImage.observe(viewLifecycleOwner){
                if (!(it == null)){
                    bundle.putString("profileimg", it.toString())
                }
            }
            bundle.putString("sharelink","link")
            findNavController().navigate(R.id.action_global_share,bundle)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun invokeApplication(packageName: String, resolveInfo: ResolveInfo) {
        // if(packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana") || packageName.contains("com.kakao.story")) {
        val intent = Intent()
        intent.component = ComponentName(packageName, resolveInfo.activityInfo.name)
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Hi guys, I found amazing thing to share. Send your love and care in form of gifts to your loved ones from anywhere in world. Log on to giftjaipur.com or download app" + "https://goo.gl/YslIVT" + "and use coupon code app50 to get Rs 50 off on your first purchase."
        )
        intent.putExtra(Intent.EXTRA_SUBJECT, "GiftJaipur 50 Rs Coupon...")
        intent.setPackage(packageName)
        startActivity(intent)
        // }
    }

    private fun openSharingVia() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val message =
                "Let's talk with best creators who help you on Myworld! It's a fast, simple and secure app. Get it at "
            val link = "https://play.google.com/store/apps/details?id=cessini.technology.myworld"
            putExtra(Intent.EXTRA_TEXT, message + link)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Invite via")
        startActivity(shareIntent)
    }

    /**Function to show the Shimmer effect.*/
    private fun showShimmer() {

    }

    /**Function to Stop the Shimmer effect.*/
    private fun hideShimmer() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            delay(2000)
//            binding.containerProfile.getConstraintSet(R.id.start).getConstraint(binding.profileStoryShimmer.id).propertySet.mVisibilityMode = 1; // 1 - ignore or 0 - normal
//            binding.profileStoryShimmer.visibility = View.GONE
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        tabLayout = null
    }

    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
        binding.swipeRefreshLayout.isEnabled = false
    }

    override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) = Unit

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        binding.swipeRefreshLayout.isEnabled = motionLayout?.currentState == R.id.start
        binding.profilePictureContainerSmall.isVisible = motionLayout?.currentState == R.id.end
        shareMenuItem?.isVisible = motionLayout?.currentState == R.id.start
    }

    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit

    private fun modifyFont(expertise:String):SpannableStringBuilder{
        val arr=expertise.toCharArray()
        var parts= mutableListOf<String>()
//        var i=0
//        while(i<arr.size){
//            var temp=""
//            while(i<arr.size && arr[i]!=' ' && arr[i]!=','){
//                while(i<expertise.length && arr[i]=='#'){
//                    i++
//                }
//                if (i<expertise.length){
//                    temp += arr[i]
//                    i++
//                }
//            }
//            i++
//            parts.add(temp)
//        }

        parts= expertise.split(',',' ').toMutableList()

        var ans=SpannableStringBuilder("")
        val temporary_map= mutableMapOf<Int,String>()

        Log.d(TAG,"parts size = ${parts.size}")
        for (str in parts) {

            Log.d(TAG,"parts  = ${str}")
            if(str.trim()=="") {
                continue
            }
            Log.d(TAG,"temp_map 1 = ${temporary_map}")


            Log.d(TAG,"temp_map 2 = ${temporary_map}")

                val it = baseViewModel.getSubCatCode(str)

                    val x= temporary_map[it]

                    if(x==null)
                        temporary_map[it]= " ${str.trim()}"
                    else
                        temporary_map[it]= "$x ${str.trim()}"


        }

        Log.d(TAG,"temp_map = ${temporary_map}")
        for(categories in temporary_map){

            val str= categories.value
            val cat_number= categories.key
          val shape = ShapeDrawable(  OvalShape())
            shape.paint.color = Color.RED
                    when(cat_number){
                    1-> shape.paint.color= Color.RED
                    2-> shape.paint.color= Color.BLUE
                    3-> shape.paint.color= Color.GREEN
                     4-> shape.paint.color= Color.MAGENTA
                     5-> shape.paint.color= Color.YELLOW
                        6-> shape.paint.color= Color.CYAN
                        7-> shape.paint.color= Color.GRAY
                        else-> shape.paint.color= Color.BLACK

                    }

            val spannableString = SpannableString(" $str ")
            val lineHeight = binding.profileBio.lineHeight

            shape?.setBounds(0, 0, lineHeight, lineHeight)
            val imageSpan = ImageSpan(shape!!, ImageSpan.ALIGN_CENTER)
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            ans.append(spannableString)
        }
        return ans
    }

}
