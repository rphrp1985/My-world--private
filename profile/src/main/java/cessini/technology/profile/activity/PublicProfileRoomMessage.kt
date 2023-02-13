package cessini.technology.profile.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.item
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.Navigator
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import cessini.technology.profile.*
import cessini.technology.profile.`class`.*
import cessini.technology.profile.activity.epoxy.ChatEpoxyController
import cessini.technology.profile.databinding.FragmentPublicprofileroommessageBinding
import cessini.technology.profile.fragment.publicProfile.ResponseMessageJson
import cessini.technology.profile.viewmodel.ProfileViewModel
import cessini.technology.profile.viewmodel.PublicProfileRoomMessageViewModel
import cessini.technology.profile.viewmodel.PublicProfileViewModel
import com.google.gson.GsonBuilder
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_publicprofileroommessage.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.List


@AndroidEntryPoint
class PublicProfileRoomMessage : Fragment() {
    companion object {
        private const val TAG = "PublicProfileRoomMessage"
    }

    private val pArgs: PublicProfileRoomMessageArgs by navArgs()
    private lateinit var binding: FragmentPublicprofileroommessageBinding
    private val messageViewModel: PublicProfileRoomMessageViewModel by activityViewModels()
    private lateinit var viewModel: PublicProfileViewModel
    private lateinit var profileViewModel: ProfileViewModel
    var messageList: PagedList<ResponseMessageJson>?=null

//    val allMessages = ArrayList<ResponseMessageJson>()
    var currentPage=0
    var currentRequests=0;

    lateinit var chatUserMe: JSONObject
    lateinit var chatUserId: JSONObject

    private lateinit var baseViewModel: BaseViewModel

    @Inject
    lateinit var userIdentifierPreferences: UserIdentifierPreferences

    @Inject
    lateinit var navigator: Navigator
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = activity?.run {
            ViewModelProvider(this)[PublicProfileViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        viewModel.activity=requireActivity()
        profileViewModel = activity?.run {
            ViewModelProvider(this)[ProfileViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        baseViewModel = activity?.run {
            ViewModelProvider(this)[BaseViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_publicprofileroommessage,
            container,
            false
        )
        binding.lifecycleOwner = this
        verifyLogin()
        viewModel.loadProfile(false)

        binding.recyclerGchat.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val position: Int = getCurrentItem()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("DERE", "onScrollStateChanged: "+getCurrentItem())
                    messageList?.let {
                        if(it.size>position)
                            setDateLabel(it[position]!!)
                    }


                }
            }

        })
        val rootView = binding.root
        val emojiPopup= EmojiPopup( rootView,binding.editGchatMessage)
        binding.emojiButton.setOnClickListener {
            if (emojiPopup.isShowing){
                emojiPopup.toggle()
            }
            else{
                emojiPopup.show()
            }
        }
        binding.editGchatMessage.setOnClickListener {
            if (emojiPopup.isShowing){
                emojiPopup.toggle()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.chatAction.setOnClickListener {
            findNavController().navigate(
                PublicProfileRoomMessageDirections.actionPublicProfileRoomMessageToChatBottomSheet()
            )
        }

        Log.d(TAG,"onViewCreate created")
        if(viewModel.displayName.value!=null)
            binding.userName = viewModel.displayName.value
        else
            binding.userName=""
        if(viewModel.photoUrl.value!=null)
            binding.profileImage = viewModel.photoUrl.value
        else
            binding.profileImage=""
//        binding.headText.setText(binding.userName)
        Log.d(TAG,"This function is called and the username is ${viewModel.displayName.value}")
        viewModel.photoUrl.observe(viewLifecycleOwner, Observer {
            Log.e("MessageFragment","ObserverCalled")
            binding.userName = viewModel.displayName.value
            binding.profileImage = viewModel.photoUrl.value
//            binding.headText.setText(binding.userName)
        })

        //other user
        messageViewModel.idOther = pArgs.profileId

        // me user
        messageViewModel.idMe = userIdentifierPreferences.id
        messageViewModel.userMe = baseViewModel.authEntity.channelName

        showLoader()
        messageViewModel.setSocket(messageViewModel.idMe)
        messageViewModel.setPaging()

        Log.d(TAG,"The status of the socket is ${messageViewModel.mSocket.isActive}")

        back_navigation_button_discovery_profile.setOnClickListener {
//            findNavController().navigate(PublicProfileRoomMessageDirections.actionPublicProfileRoomMessageToPublicProfileFragment())
            findNavController().navigateUp()
        }

        binding.editGchatMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                verifyLogin()
                if (s.toString().trim().isNotEmpty()) {
                    binding.sendMessage.visibility = View.VISIBLE
                } else {
                    binding.sendMessage.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.sendMessage.setOnClickListener {
            sendMessage()
        }

        binding.buttonSendEmoji.setOnClickListener {
            sendMessage()
        }


        messageViewModel.mSocket.on("connect") {
            Log.d(TAG, "connected")
            Log.d(TAG,"idme = ${messageViewModel.idMe}  idother= ${messageViewModel.idOther}")
        }

        //Epoxy - Paging
        val pagedListController = ChatEpoxyController(messageViewModel.idMe,messageViewModel.idOther,messageViewModel.chatMessageState())
//        binding.recyclerGchat.adapter = pagedListController.adapter


        messageViewModel.fetchPages({ list ->
            messageList= list

            pagedListController.submitList(list)
            if(list.size==0){
                showHeader()
            }else{
                binding.recyclerGchat.adapter= pagedListController.adapter
            }

        }, { error ->
            Log.e(TAG, "error: ${error}")
        })

        val onnewmessage = Emitter.Listener {
            Log.d(TAG,"new message recieved   ${it[0]}")
            val gson = GsonBuilder().create()
            val responseJson =
                gson.fromJson(it[0].toString(), ResponseMessageJson::class.java)

            messageViewModel.datasourceFactory.invalidiate()
            messageViewModel.compositeDisposable.clear()

            messageViewModel.fetchPages({ list ->

                messageList= list
                Log.d(TAG,"list= $list")
                pagedListController.submitList(list)

                Handler().postDelayed({
                    binding.recyclerGchat.scrollToPosition(0)

                },500)

                if(list.size==0){
                    showHeader()
                }else{
                    binding.recyclerGchat.adapter= pagedListController.adapter
                }


        }, { error ->
                Log.e(TAG, "error: ${error}")
            })


        }
        messageViewModel.mSocket.on("message",onnewmessage)


    }

    private fun getCurrentItem(): Int {
        return (binding.recyclerGchat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }
    private fun setDateLabel(item: ResponseMessageJson) {
        var time= item.created_at
        if(time=="")
            return
        time= time.substring(0,10)
        val dd = convertDateFormat("yyyy-MM-dd", "yyyy MMM dd", time)
        binding.dateLabel.text = dd
        binding.dateLabel.visibility = View.VISIBLE


        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                binding.dateLabel.visibility = View.INVISIBLE
            }, 3000)
        }

    }

    fun convertDateFormat(FormFormat: String, ToFormat: String, value: String): String {
        var returnValue = value
        val formFormat = SimpleDateFormat(FormFormat, Locale.getDefault())
        val toFormat = SimpleDateFormat(ToFormat, Locale.getDefault())
        returnValue = formFormat.parse(value)?.let { toFormat.format(it) }.toString()
        return returnValue
    }
    fun sendMessage(){
    if (messageViewModel.mSocket.connected()) {
            Log.d("socket", "already connected ")

            lifecycleScope.launch {
                var sendMsgText = binding.editGchatMessage.text.toString().trim()
                binding.editGchatMessage.setText("")

                      Log.d(TAG,"send json = ${message(messageViewModel.idMe, messageViewModel.idOther, sendMsgText).getMessage()}")
                messageViewModel.mSocket.emit("message", message(messageViewModel.idMe, messageViewModel.idOther, sendMsgText).getMessage())

          }

        } else {
            Toast.makeText(activity, "Error sending message", Toast.LENGTH_LONG).show()
            Log.d(TAG, "not connected ")
        }
    }

    private fun verifyLogin() {
        if (!userIdentifierPreferences.loggedIn) {
            navigator.navigateToFlow(NavigationFlow.AuthFlow)
        }
    }

    fun showHeader(){
        binding.recyclerGchat.withModels {
            itemChatHeader {
                id("Header")
            }
        }
    }
    fun showLoader(){
        binding.recyclerGchat.withModels {
            itemChatShimmer {
                id("Loading- Shimmer")
            }
        }
    }

}
