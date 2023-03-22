package cessini.technology.commonui.activity

//import cessini.technology.commonui.R2.id.cameraSwitch
import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cessini.technology.commonui.R
//import cessini.technology.commonui.R2.id.room_join_framelayout2
import cessini.technology.commonui.activity.live.PeerConnectionAdapter
import cessini.technology.commonui.activity.live.SdpAdapter
import cessini.technology.commonui.activity.live.SignalingClient
import cessini.technology.commonui.activity.services.screen_share.MediaProjectionService
import cessini.technology.commonui.adapter.RecAdapter
import cessini.technology.commonui.databinding.CommonChatSnackviewBinding
//import cessini.technology.commonui.fragment.RoomJoinRequestFragment
//import cessini.technology.commonui.fragment.RoomJoinWaiting
import cessini.technology.commonui.fragment.commonChat.CommonChatFragment
import cessini.technology.commonui.utils.networkutil.TAG
import cessini.technology.commonui.viewmodel.commonChat.CommonChatPayload
import cessini.technology.commonui.viewmodel.commonChat.CommonChatViewModel
import cessini.technology.model.Profile
import cessini.technology.newapi.services.commonChat.CommonChatSocketHandler
import cessini.technology.newapi.services.myspace.RoomSocket
import cessini.technology.newrepository.myworld.ProfileRepository
import com.airbnb.epoxy.EpoxyRecyclerView
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.slf4j.helpers.Util.report
import org.webrtc.*
import org.webrtc.PeerConnectionFactory.InitializationOptions
import java.io.File
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class GridActivity : AppCompatActivity() , SignalingClient.Callback {

    private lateinit var snackViewBinding : CommonChatSnackviewBinding
    private lateinit var snackbar: Snackbar
    private lateinit var customSnackView: CommonChatSnackviewBinding


    @Inject
    lateinit var roomScoket: RoomSocket

    @Inject
    lateinit var credentials: BasicAWSCredentials

    @Inject
    lateinit var clientConfiguration:ClientConfiguration

    @Inject
    lateinit var profileRepository: ProfileRepository


    private var mMediaProjectionPermissionResultData: Intent? = null
    private var mMediaProjectionPermissionResultCode = 0


    val KEY = true
    var epoxyset=false;

    val hubViewModel:HubViewModel by viewModels()

    private val audioManager by lazy { RTCAudioManager.create(this) }
    @Inject
    lateinit var eglBaseContext: EglBase.Context

    @Inject
    lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var localView: SurfaceViewRenderer
    private lateinit var  remoteViews: Array<SurfaceViewRenderer>
    private lateinit var camera: ImageView
    private lateinit var screenShare: ImageView
    private lateinit var raiseHand:Button
    private lateinit var audio: ImageView
    private lateinit var recAdapter: RecAdapter
    private var IsCameraOn= true
    private var isCreated=false
    lateinit var controller:EpoxyController
    lateinit var epoxyRecyclerView: EpoxyRecyclerView

    /** Storing data model of local user camera and screen to handle it in epoxy */
    private var userData : data? = null
    private var userDataScreen : data? = null

    private var endPointUrlBase: String = "rtmp://Ant-M-RTMPL-1G6LL9TH0KN27-3cbc7d0e3e5e05ad.elb.ap-south-1.amazonaws.com"

    val options = InitializationOptions.builder(this)
        .createInitializationOptions()

    @Inject
    lateinit var audioSource : AudioSource

    private val chatViewModel : CommonChatViewModel by viewModels()

    val recyclerDataArrayList:ArrayList<data> = ArrayList()

    lateinit var profile: Profile
    val socketUserMap = mutableMapOf<String,JSONObject>()
    private var isMic=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_ActionBar)
        setContentView(R.layout.activity_grid)
        (this@GridActivity).window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            navigationBarColor = ContextCompat.getColor(this@GridActivity,R.color.Dark)

        }
        val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels

         controller = EpoxyController(screenHeight, screenWidth, applicationContext)

        epoxyRecyclerView = findViewById<EpoxyRecyclerView>(R.id.idCourseRV)

        startService(Intent(baseContext, MediaProjectionService::class.java))

       screenShot.observe(this, androidx.lifecycle.Observer {
           streamShort()
       })

        Log.e("jsom",JSONObject(HubConstants.awsconfig).toString())
        /**
         * AWS Config
         */




        AWSMobileClient.getInstance().initialize(applicationContext,AWSConfiguration(JSONObject(HubConstants.awsconfig)),object : Callback<UserStateDetails?> {
            override fun onResult(result: UserStateDetails?) {
                Log.e("aws",result.toString())
            }

            override fun onError(e: java.lang.Exception?) {
                Log.e("aws error", "onError: ", e)
            }
        })


        val endcall=findViewById<Button>(R.id.btnSchedule)
        audio=findViewById<ImageView>(R.id.iv_audio)
        val ivMessage = findViewById<ImageView>(R.id.ivMessage)
        val share = findViewById<ImageView>(R.id.share)
        val cameraSwitch = findViewById<ImageView>(R.id.cameraSwitch)
        camera = findViewById<ImageView>(R.id.iv_video)
        screenShare = findViewById<ImageView>(R.id.shareScreen)
        raiseHand = findViewById(R.id.btnShareScreen)
//        localView = findViewById(R.id.localView)
        handleIntent(intent)

        if(!IsCameraOn)
            camera.setImage(R.drawable.ic_removevideo)

        if(!isMic)
            audio.setImage(R.drawable.ic_removeaudio)



        if(hubViewModel.rname=="RoomLive") {
            try {
                hubViewModel.rname = intent.getStringExtra("Room Name").toString()


                Log.e("room name", hubViewModel.rname)

            } catch (e: Exception) {

            }
        }
        if(hubViewModel.user_id=="user"){
            try {
                hubViewModel.user_id = intent.getStringExtra("user_id").toString()

                Log.e("user_id", hubViewModel.user_id)

            } catch (e: Exception) {

            }
        }
        isCreated= intent.getBooleanExtra("created",false);

        lifecycleScope.launch {
            profileRepository.profile.collectLatest {
                profile= it
                hubViewModel.user_id= it.id
                hubViewModel.isFront = true
//                setUpEpoxy()
                showCamera()
                SignalingClient.get()?.init(this@GridActivity, hubViewModel.rname,profile,socketUserMap)

                if(isCreated)
                createRoomLink()

                chatViewModel.setSocket(hubViewModel.rname)
                chatViewModel.roomID= hubViewModel.rname
                chatViewModel.user_id = hubViewModel.user_id
                chatViewModel.listenTo(hubViewModel.rname)

                startStream(hubViewModel.rname,profile.email)

                return@collectLatest
            }
        }



//        showSnack(CommonChatPayload("mmm","mm",""))
        chatViewModel.messages.observe(this){
            showSnack(it)
        }


        cameraSwitch.setOnClickListener(){
            switchCamera(cameraSwitch)
        }

        audio.setOnClickListener() {
            switchAudio(audio)
            Log.e("closed", "connection Closed")
        }

        camera.setOnClickListener {
            switchVideo(camera)

        }
        share.setOnClickListener {
            createRoomLink()
        }
        endcall.setOnClickListener {
            onBackPressed()
        }

        screenShare.setOnClickListener {
            onScreenShare()
        }

        raiseHand.setOnClickListener {
             hand()
        }
        hubViewModel.peerConnectionMap = HashMap()

        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

        ivMessage.setOnClickListener {
            val commonChat = CommonChatFragment()
            commonChat.show(supportFragmentManager, CommonChatFragment.TAG)
        }


    }

    fun hand(){
        var status=true
        hubViewModel.hand= !hubViewModel.hand
        SignalingClient.get()?.sendHand(hubViewModel.hand)
        if(hubViewModel.hand){
            raiseHand.let {
                it.setTextColor(R.color.quantum_black_100)
                it.background = ContextCompat.getDrawable(this,R.drawable.circular_button_view_without_border)
                status=true
            }

        }else{
            raiseHand.background=null
            raiseHand.background= ContextCompat.getDrawable(this,R.drawable.ic_hubaskwhite)
            status=false


        }
//        for(i in 0 until recyclerDataArrayList.size){
//            if(recyclerDataArrayList[i].socketId=="socket"){
//                recyclerDataArrayList[i].handSwitch = status
//                controller.requestModelBuild()
////                setUpEpoxy()
//                break
//            }
//        }
    }



    override fun onJoinPermission(data: JSONObject, socket: Socket) {

        runOnUiThread {
            val useralias = data.getJSONObject("userAlias")
            val name = useralias.optString("name")
            val profile = useralias.optString("profilePicture")
            val id = data.optString("id")

            val promptsView = findViewById<View>(R.id.request_permission)
            promptsView.visibility= View.VISIBLE
            val image: ImageView = promptsView.findViewById(R.id.profile_image)
            val textView: TextView = promptsView.findViewById(R.id.caller_name)
            val allow:Button = promptsView.findViewById(R.id.answer_button)
            val decline : Button = promptsView.findViewById(R.id.decline_button)
            textView.text = name
//
            try {
                Glide.with(this)
                    .load(profile).centerCrop()
                    .into(image);
            } catch (e: Exception) {
            }

            allow.setOnClickListener {
                val jo = JSONObject()
                    jo.put("allowed", true)
                    jo.put("id", id)
                    Log.d("RoomJoin", "answere =$jo")
                    socket.emit("permit status", jo)

                promptsView.visibility= View.GONE
                return@setOnClickListener
            }

            decline.setOnClickListener {
                val jo = JSONObject()
                    jo.put("allowed", false)
                    jo.put("id", id)
                    Log.d("RoomJoin", "decline =$jo")
                    socket.emit("permit status", jo)
                promptsView.visibility= View.GONE
                return@setOnClickListener
            }
        }


    }

    override fun onPermissionWaiting() {

        runOnUiThread {
            val layout = findViewById<View>(R.id.included)
            val text = layout.findViewById<TextView>(R.id.room_name)
            text.text = hubViewModel.rname
            layout.visibility = View.VISIBLE

        }
    }

    override fun hidefragment() {
        runOnUiThread {
            val layout = findViewById<View>(R.id.included)
            layout.visibility = View.GONE
//            val layout2 = findViewById<FrameLayout>(R.id.room_join_framelayout2)
//            layout2.visibility = View.GONE
        }
    }

    /**
     * SnackBar Code
     */

    private fun showSnack(message: CommonChatPayload){
        customSnackView.tvUser.text = message.user_id
        customSnackView.tvText.text = message.message
        snackbar.show()
    }


    private fun setupCustomSnackBar(){
        snackViewBinding = CommonChatSnackviewBinding.inflate(layoutInflater)
        snackbar = Snackbar.make(snackViewBinding.root, "", Snackbar.LENGTH_LONG)
        customSnackView = snackViewBinding
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        val params : FrameLayout.LayoutParams= snackbar.view.layoutParams as FrameLayout.LayoutParams
        val width = Resources.getSystem().displayMetrics.widthPixels * 0.6
        params.width = width.toInt()
        snackbarLayout.layoutParams = params
        snackbarLayout.addView(customSnackView.root, 0)
    }


    /**
     * Handling the permissions for the user camera
     */
    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ),
            hubViewModel.CAPTURE_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Starting camera when permissions are granted
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == hubViewModel.CAPTURE_PERMISSION_REQUEST_CODE) {

            for(permission in grantResults){
                if(permission==PackageManager.PERMISSION_GRANTED) {
                    showCamera()
                    return
                }
            }
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCamera() {

        var cameraPermission= true
        var micPermission= true;
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            askPermission()
            cameraPermission= false
            turnOffVideo()
//            return
        }else

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            askPermission()
            micPermission= false
            turnOffAudio()
//            return
        }
        recyclerDataArrayList.remove(userData)

        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread-${System.currentTimeMillis()}", eglBaseContext)
        // create VideoCapturer
        val videoCapturer = createCameraCapturer(hubViewModel.isFront)
        val videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturer!!.initialize(
            surfaceTextureHelper,
            applicationContext,
            videoSource.capturerObserver)
        videoCapturer.startCapture(480, 640, 30)
        hubViewModel.videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)
        hubViewModel.localAudioTrack = peerConnectionFactory.createAudioTrack("100" + "_audio", audioSource)
        hubViewModel.mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream")
        hubViewModel.mediaStream?.addTrack(hubViewModel.videoTrack)
        hubViewModel.mediaStream?.addTrack(hubViewModel.localAudioTrack)

        if(!isMic) {
            micPermission = false

            hubViewModel.localAudioTrack?.setEnabled(false)

        }

        if(!IsCameraOn){
            hubViewModel.videoTrack?.setEnabled(false)
            cameraPermission= false
        }


//        if(!recyclerDataArrayList.contains(userData)){
            userData = data(profile.name, 0, hubViewModel.videoTrack!!,
                creater = true,
                microphoneSwitch = micPermission,
                videoSwitch = cameraPermission,
                profilepic = profile.profilePicture,
                handSwitch = false,
                userID = profile.id,
                isScreen = false,
                socketId = "socket",
                con = eglBaseContext,
                fileRenderer = null)
            recyclerDataArrayList.add(userData!!)
        setUpEpoxy()
//        controller.images = recyclerDataArrayList
//
//        controller.requestModelBuild()

    }

    /**
     * creating and returning camera capture accoring to the camera used
     */
    private fun createCameraCapturer(isFront: Boolean): VideoCapturer? {
        val enumerator = Camera1Enumerator(false)
        val deviceNames = enumerator.deviceNames

        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (if (isFront) enumerator.isFrontFacing(deviceName) else enumerator.isBackFacing(
                    deviceName
                )
            ) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    /**
     * Handling the permission for the screen share or starting the service for the same
     */
    @TargetApi(21)
    private fun startScreenCapture() {
        if(Build.VERSION.SDK_INT >=28) {
            // Start a foreground service and post notification regarding the screen share
            val intent = Intent(this, MediaProjectionService::class.java)
            startForegroundService(intent)
        }
        val mediaProjectionManager = application.getSystemService(
            Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), hubViewModel.CAPTURE_PERMISSION_REQUEST_CODE)
    }

    /**
     * For the case of screen capture handling the Intent result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==hubViewModel.REQUEST_EXTERNAL_STORAGe && resultCode!=0)
            streamShort()

        if(requestCode==hubViewModel.CAPTURE_PERMISSION_REQUEST_CODE && resultCode!=0 ){

        }
        if (requestCode != hubViewModel.CAPTURE_PERMISSION_REQUEST_CODE)
        {

        }else {

            if(resultCode==0){
                Toast.makeText(this,"permission not granted",Toast.LENGTH_SHORT).show()
            }else {
                mMediaProjectionPermissionResultCode = resultCode
                mMediaProjectionPermissionResultData = data
                screenCast()
            }
        }
        if ( requestCode == hubViewModel.REQUEST_CODE_STREAM && resultCode!=0  ) {
            Toast.makeText(this,"permission granted for stream",Toast.LENGTH_SHORT).show()
            mMediaProjectionPermissionResultCode = resultCode
            mMediaProjectionPermissionResultData = data
            val displayService: MediaProjectionService = MediaProjectionService.INSTANCE!!
            val endpoint: String = "rtmp://live.joinmyworld.in/live/${hubViewModel.stream_key}"
             displayService.prepareStreamRtp(endpoint, resultCode, data!!)
            displayService.startStreamRtp(endpoint)
        }
    }

    /**
     * capturing the screen cast stream and adding it to the mediaStream with peerConnectionFactory
     */
    private fun screenCast() {
        // create Video Capturer
        if(userDataScreen!=null){
            recyclerDataArrayList.remove(userDataScreen)

        }
        val videoCapturer = createScreenCapturer()
        val surfaceTextureHelper = SurfaceTextureHelper.create("ScreenThread", eglBaseContext)
        val videoSource = peerConnectionFactory.createVideoSource(
            videoCapturer!!.isScreencast  )
        videoCapturer.initialize(
            surfaceTextureHelper,
            applicationContext,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(480, 640, 30)
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        hubViewModel.videoTrackScreen = peerConnectionFactory.createVideoTrack("100", videoSource)
        hubViewModel.videoTrackScreen?.setEnabled(true)

        hubViewModel.localAudioTrackScreen = peerConnectionFactory.createAudioTrack("100" + "_audio", audioSource)

        hubViewModel.mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream")
        hubViewModel.mediaStream?.addTrack(hubViewModel.videoTrackScreen)
//        hubViewModel.mediaStream?.addTrack(hubViewModel.localAudioTrackScreen)
        userDataScreen = data("userScreen", 0, hubViewModel.videoTrackScreen!!,
            creater = true,
            microphoneSwitch = true,
            videoSwitch = true,
            profilepic = profile.profilePicture,
            handSwitch = false,
            userID = profile.id,
            socketId = "socket",
            con = eglBaseContext,
            fileRenderer = null,
            isScreen = true
        )
        recyclerDataArrayList.add(userDataScreen!!)
        for(x in socketUserMap){
            onPeerJoined(x.key,true)
        }
        setUpEpoxy()


//        SignalingClient.get()?.init(this,hubViewModel.rname,profile)
    }

    /**
     * Returning the screen Capturer
     */
    @TargetApi(21)
    private fun createScreenCapturer(): VideoCapturer? {
        if (mMediaProjectionPermissionResultCode !== Activity.RESULT_OK) {
            report("User didn't give permission to capture the screen.")
            return null
        }
        return ScreenCapturerAndroid(mMediaProjectionPermissionResultData, object : MediaProjection.Callback() {
            override fun onStop() {
                hubViewModel.videoTrackScreen?.setEnabled(false)
                report("Screen Share stopped")
            }

        })
    }


    /**
     * Handling the epoxy with the case of array size
     * of array size is small than the grid is with single span count
     * adding the data to the epoxy controller to display the videoTrack on UI thread
     */



    private fun setUpEpoxy() {

//        screenShot.clear()

        modelProcessed=0
        epoxyset= true
        val layoutManager: GridLayoutManager
        if (recyclerDataArrayList.size == 1 || recyclerDataArrayList.size == 2) {
            Log.d("recyclerDataArrayList", "size ${recyclerDataArrayList.size}")
            layoutManager = GridLayoutManager(this, 1)
        } else if (recyclerDataArrayList.size % 2 == 1) {
            Log.d("recyclerDataArrayList", "size 3 ${recyclerDataArrayList.size}")
            layoutManager = GridLayoutManager(this, 2)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    when (position) {
                        0 -> return 2
                        else -> return 1
                    }
                }
            }
        } else {
            Log.d("recyclerDataArrayList", "size ${recyclerDataArrayList.size}")
            layoutManager = GridLayoutManager(this, 2)
        }
        /**
         * Setting values to the epoxy controller
         */

        controller.images = recyclerDataArrayList

        runOnUiThread() {
            epoxyRecyclerView.layoutManager = layoutManager
            epoxyRecyclerView.setController(controller)

            controller.requestModelBuild()


        }
//        runOnUiThread { streamShort() }
    }

    fun createVideoPath(context: Context, fileName: String?): String? {
        val imageThumbsDirectory: File = context.getExternalFilesDir("FOLDER")!!
        if (!imageThumbsDirectory.exists()) {
            imageThumbsDirectory.mkdir()
        }
        val appDir: String = context.getExternalFilesDir("FOLDER")!!.absolutePath
        val file = File(appDir, fileName)
        return file.absolutePath
    }


    /**
     * Creating the PeerConnection to establish connection between two devices
     */
    @Synchronized
    private fun getOrCreatePeerConnection(socketId: String,screeni: Boolean,screenType:String): PeerConnection {
        var peerConnection:PeerConnection?
        if (!screeni) {
            peerConnection = hubViewModel.peerConnectionMap[socketId]
            if (peerConnection != null) {
                return peerConnection
            }
        } else {
            peerConnection = hubViewModel.peerConnectionScreenMap[socketId]
            if (peerConnection != null) {
                return peerConnection
            }
        }
        peerConnection =
            peerConnectionFactory.createPeerConnection(hubViewModel.iceServers, object : PeerConnectionAdapter(
                "PC:$socketId"
            ) {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    SignalingClient.get()?.sendIceCandidate(iceCandidate, socketId, screeni)
                }

                /**
                 * Adding new data to the recyclerDataArrayList whenever new mediaStream is added to the network
                 */
                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)


                    if (screenType != "offer" && screeni)
                        return

                        hubViewModel.fileName =
                            createVideoPath(applicationContext, hubViewModel.fileName)
                        hubViewModel.remoteVideoTrack = mediaStream.videoTracks[0]

                    var name= "user"
                    var profile=""
                    var ishandraised= false;
                    var iscamera= true;
                    var ismic = true;

                    val remoteUser=socketUserMap[socketId]

                    if(remoteUser!=null){

                        profile=      remoteUser.optString("profilePicture")
                        name= remoteUser!!.optString("name")
                        ishandraised=  remoteUser.optBoolean("isHandRaised")
                        iscamera=  remoteUser.optBoolean("isNotCamera")
                        ismic=   remoteUser!!.optBoolean("isMuted")

                    }


                        recyclerDataArrayList.add(
                            data(
                                name,
                                hubViewModel.remoteViewsIndex++,
                                mediaStream.videoTracks[0],
                                false,
                                microphoneSwitch = ismic,
                                videoSwitch = iscamera,
                                profilepic = profile,
                                handSwitch = ishandraised,
                                userID = "",
                                isScreen = screeni,
                                socketId = socketId,
                                con = eglBaseContext,
                                fileRenderer = null
                            )
                        )
                        setUpEpoxy()
                        Log.e("stream", "stream added ${mediaStream.videoTracks.size}")
                        Log.e("path", createVideoPath(applicationContext, hubViewModel.fileName).toString())
//                    }catch (e:Exception){
//                        e.printStackTrace()
//                    }
                }

            })


        Log.d("peer","${peerConnection.toString()}")
        peerConnection!!.addStream(hubViewModel.mediaStream)
        if (!screeni)
            hubViewModel.peerConnectionMap[socketId] = peerConnection!!
        else
            hubViewModel.peerConnectionScreenMap[socketId] = peerConnection!!

        return peerConnection
    }

    override fun onCreateRoom(id: String) {
    }

    /**
     * Creating the Peer Connection
     */

    override fun onPeerJoined(socketId: String,screen:Boolean) {
        val peerConnection = getOrCreatePeerConnection(socketId,screen,"send")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        peerConnection.createOffer(object : SdpAdapter("createOfferSdp:$socketId") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection.setLocalDescription(
                    SdpAdapter("setLocalSdp:$socketId"),
                    sessionDescription
                )
                SignalingClient.get()?.sendSessionDescription(sessionDescription, socketId,"ok",screen)
            }
        }, constraints)
    }

    override fun onSelfJoined() {
    }

    override fun onPeerLeave(data: String) {

        for(user in recyclerDataArrayList){
            if(user.socketId==data){
                 recyclerDataArrayList.remove(user)
                setUpEpoxy()
                break;
            }
        }

    }


    /**
     * Initiates the creation of an SDP offer for the purpose of starting a new WebRTC connection to a remote peer.
     */
    override fun onOfferReceived(data: JSONObject) {
        runBlocking {


            val obj: JSONObject = JSONObject(data.optString("type"))
            val socketId= obj.optString("socket")
            val screen = obj.optBoolean("screen")
            Log.d("screen data","screen = $screen")



            val peerConnection = getOrCreatePeerConnection(socketId, screen,"offer")
            val jsonObject = Gson().fromJson(data.optString("sdp").toString(), JsonObject::class.java)
            val sdp = Gson().fromJson(jsonObject, SessionDescription::class.java)

            peerConnection.setRemoteDescription(SdpAdapter("setRemoteASdp:$socketId"),sdp)

//            peerConnection.setRemoteDescription(
//                SdpAdapter("setRemoteSdp:$socketId"),
//                SessionDescription(SessionDescription.Type.OFFER, data.optString("sdp") )
//            )
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }


            /**
             * Initiates the creation an SDP answer to an offer received from a remote peer
             * during the offer/answer negotiation of a WebRTC connection.
             */
            peerConnection.createAnswer(object : SdpAdapter("localAnswerSdp") {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    super.onCreateSuccess(sessionDescription)
                    peerConnection.setLocalDescription(SdpAdapter("setLocalSdp:$socketId"), sessionDescription)
                    SignalingClient.get()?.sendSessionDescription(sessionDescription, socketId,"ans",screen)
                }
            }, constraints)



        }
    }

    override fun updateUserDetails(){
        runBlocking {
            for(user in recyclerDataArrayList) {
                val socketId= user.socketId
                val remoteUser = socketUserMap[socketId]
                if (remoteUser == null) {
//                    runOnUiThread {
////                        Toast.makeText(this@GridActivity, "undefined user", Toast.LENGTH_SHORT)
//                            .show()
//                    }fU
                } else {

//                    runOnUiThread {
//                        Toast.makeText(this@GridActivity, "user =$remoteUser", Toast.LENGTH_SHORT)
//                            .show()
//                    }
                    for (i in 0 until recyclerDataArrayList.size) {
                        if (recyclerDataArrayList[i].socketId == socketId) {
//                            runOnUiThread {
//                                Toast.makeText(this@GridActivity, "user found", Toast.LENGTH_SHORT)
//                                    .show()
//                            }
                            recyclerDataArrayList[i].title = remoteUser!!.optString("name")
                            recyclerDataArrayList[i].microphoneSwitch =
                                remoteUser!!.optBoolean("isMuted")
                            recyclerDataArrayList[i].videoSwitch =
                                remoteUser.optBoolean("isNotCamera")
                            recyclerDataArrayList[i].profilepic =
                                remoteUser.optString("profilePicture")
                            recyclerDataArrayList[i].handSwitch =
                                remoteUser.optBoolean("isHandRaised")
                            recyclerDataArrayList[i].userID = remoteUser.optString("id")
//                    break;
                        }
                    }
                }
            }
            runOnUiThread { controller.requestModelBuild() }
        }
    }


    /**
     *
     */
    override fun onAnswerReceived(data: JSONObject) {
        val obj: JSONObject = JSONObject(data.optString("type"))

        val socketId= obj.optString("socket")
        val screen = obj.optBoolean("screen")



        val peerConnection = getOrCreatePeerConnection(socketId, screen,"offer")

        val jsonObject = Gson().fromJson(data.optString("sdp").toString(), JsonObject::class.java)
        val sdp = Gson().fromJson(jsonObject, SessionDescription::class.java)

          peerConnection.setRemoteDescription(SdpAdapter("setRemoteASdp:$socketId"),sdp)

        setUpEpoxy()


//        if(data.optString("part")=="not ok")
//        {
//            recyclerDataArrayList.removeLast()
//            setUpEpoxy()
//        }
    }

    /**
     *
     */

    override fun onIceCandidateReceived(data: JSONObject) {
        val obj: JSONObject = JSONObject(data.optString("type"))

        val socketId= obj.optString("socket")
        val screen = obj.optBoolean("screen")


        val peerConnection = getOrCreatePeerConnection(socketId, screen,"offer")
//        val obj: JSONObject = JSONObject(data.optString("candidate"))
        val jsonObject = Gson().fromJson(data.optString("candidate").toString(), JsonObject::class.java)
       val candidate = Gson().fromJson(jsonObject, IceCandidate::class.java)
        peerConnection.addIceCandidate(candidate)
        Log.e("ice candidate","Added ice candidate")
    }


    /**
     *
     * UI Button Click handled
     *
     *
     */


    fun turnOffVideo(){
        val it = findViewById<ImageView>(R.id.iv_video)
        it.setImage(R.drawable.ic_removevideo)
        IsCameraOn= false
        hubViewModel.videoTrack?.setEnabled(false)
        SignalingClient.get()?.sendCamera(false)
        controller.requestModelBuild()
//        for(i in 0 until recyclerDataArrayList.size){
//            if(recyclerDataArrayList[i].socketId=="socket"){
//                recyclerDataArrayList[i].videoSwitch = false
//                setUpEpoxy()
//                break
//            }
//        }
    }

    // Handling the video switch
    fun switchVideo(it: ImageView) {

        var status=true
        if(!IsCameraOn) {

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                askPermission()
              return
            }

            it.setImage(R.drawable.ic_addvideo)
            IsCameraOn= true
            hubViewModel.videoTrack?.setEnabled(true)
            SignalingClient.get()?.sendCamera(true)
            status=true
        }else{
            it.setImage(R.drawable.ic_removevideo)
            IsCameraOn= false
            hubViewModel.videoTrack?.setEnabled(false)
            SignalingClient.get()?.sendCamera(false)
            status=false
        }
        for(i in 0 until recyclerDataArrayList.size){
            if(recyclerDataArrayList[i].socketId=="socket" && !recyclerDataArrayList[i].isScreen){
                recyclerDataArrayList[i].videoSwitch = status
                controller.requestModelBuild()
//                setUpEpoxy()
                break
            }

        }
//        localView.isEnabled = !(localView.isEnabled)
    }

    //handling the audio switch

    fun turnOffAudio(){
        hubViewModel.localAudioTrack?.setEnabled(false)
        SignalingClient.get()?.sendMicrophone(false)
        val it= findViewById<ImageView>(R.id.iv_audio)
        it.setImage(R.drawable.ic_removeaudio)
        controller.requestModelBuild()
//        for(i in 0 until recyclerDataArrayList.size){
//            if(recyclerDataArrayList[i].socketId=="socket"){
//                recyclerDataArrayList[i].microphoneSwitch = false
//                setUpEpoxy()
//                break
//            }
//        }
    }

    fun switchAudio(it: ImageView) {
        var status=true
        if (hubViewModel.localAudioTrack != null) {
            if (hubViewModel.localAudioTrack?.enabled() == true) {
                hubViewModel.localAudioTrack?.setEnabled(false)
                SignalingClient.get()?.sendMicrophone(false)
                it.setImage(R.drawable.ic_removeaudio)
                status=false
            }
            else {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    askPermission()
                   return
                }

                hubViewModel.localAudioTrack?.setEnabled(true)
                SignalingClient.get()?.sendMicrophone(true)
                it.setImage(R.drawable.ic_addaudio)
                status=true
            }
        }
        for(i in 0 until recyclerDataArrayList.size){
            if(recyclerDataArrayList[i].socketId=="socket"){
                recyclerDataArrayList[i].microphoneSwitch = status
                controller.requestModelBuild()
//                setUpEpoxy()
                break
            }
        }
    }

    // handling the camera used either front or back

    fun switchCamera(it: ImageView){
        if(!IsCameraOn){
            Toast.makeText(this,"Video is Off",Toast.LENGTH_SHORT).show()
            return
        }
        hubViewModel.isFront = !hubViewModel.isFront
        showCamera()
    }

    // handling the screen share button click function
    private fun onScreenShare() {

        if(!hubViewModel.isScreenShare){
            startScreenCapture()
            SignalingClient.get()?.sendScreen(true)

            hubViewModel.isScreenShare=true
        }else{
            SignalingClient.get()?.sendScreen(false)
            hubViewModel.videoTrackScreen?.setEnabled(false)
            hubViewModel.localAudioTrackScreen?.setEnabled(false)
            hubViewModel.isScreenShare = false
            recyclerDataArrayList.remove(userDataScreen)
            setUpEpoxy()
        }
    }


    // ending the call
    override fun onBackPressed() {

        hubViewModel.endcall()
        SignalingClient.get()?.endcall()
        overridePendingTransition(R.anim.slide_in_animation, R.anim.slide_out_animation)
        CommonChatSocketHandler.closeConnection()
        CommonChatSocketHandler.mSocket.close()
        if (hubViewModel.isScreenShare){
            hubViewModel.endcall()
            SignalingClient.get()?.destroy()
        }
        stopService(Intent(baseContext, MediaProjectionService::class.java))

//        finish()
        super.onBackPressed()

    }


    // on activity destroyed
    override fun onDestroy() {
        super.onDestroy()
        hubViewModel.endcall()
        SignalingClient.get()?.endcall()
        overridePendingTransition(R.anim.slide_in_animation, R.anim.slide_out_animation)
        CommonChatSocketHandler.closeConnection()
        CommonChatSocketHandler.mSocket.close()
        if (hubViewModel.isScreenShare){
            hubViewModel.endcall()
            SignalingClient.get()?.destroy()
        }
        stopService(Intent(baseContext, MediaProjectionService::class.java))
//        val displayService: MediaProjectionService? = MediaProjectionService.INSTANCE
//        if (displayService != null && !displayService.isStreaming() && !displayService.isRecording()) {
//            //stop service only if no streaming or recording
//            stopService(Intent(this, MediaProjectionService::class.java))
//        }
    }





    companion object {
        private val BUCKET="kurento-recorded-files"

        val regions = Regions.fromName("ap-south-1")
        private var s3Client: AmazonS3Client? = null
        private val CHOOSING_IMAGE_REQUEST = 1234

        private var fileUri: Uri? = null
        var bitmap: Bitmap? = null

        var modelProcessed=0
        var screenShot =  MutableLiveData<MutableList<Bitmap?>>(mutableListOf())

    }


    fun onConnectionStartedRtmp(rtmpUrl: String?) {

    }

    fun onConnectionSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@GridActivity,
                "Connection success",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            Toast.makeText(
                this@GridActivity,
                "Connection failed. $reason", Toast.LENGTH_SHORT
            )
                .show()
            MediaProjectionService.INSTANCE?.stopStream()
        }
    }

    fun onNewBitrateRtmp(bitrate: Long) {}

    fun onDisconnectRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@GridActivity,
                "Disconnected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onAuthErrorRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@GridActivity,
                "Auth error",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onAuthSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@GridActivity,
                "Auth success",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // Creating and sharing the room link on socail media
    private fun createRoomLink() {
        Log.d("Intent","The intent that I am looking at is called")
        val shareBody="https://www.myworld.com/liveRoom?code=${hubViewModel.rname}"
        val bottomSheetDialog=BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.fragment_share_myworld)
        val link=bottomSheetDialog.findViewById<TextView>(R.id.textView16)
        link?.text = shareBody
        val name= bottomSheetDialog.findViewById<TextView>(R.id.textView14)
        name?.text= profile.name
        val image= bottomSheetDialog.findViewById<ImageView>(R.id.imageView9)
        image?.let {
            Glide.with(this)
                .load(profile.profilePicture)
                .into(it)
        };

        val pm: PackageManager = this.packageManager
        val finalLaunchables:MutableList<ResolveInfo> = ArrayList()
        val recyclerView=bottomSheetDialog.findViewById<RecyclerView>(R.id.myrecview)
        recAdapter=RecAdapter(pm,finalLaunchables,applicationContext,shareBody)
        recyclerView?.adapter=recAdapter
        val linearLayoutManager: LinearLayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL,false)
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
        runOnUiThread {
            recAdapter.notifyDataSetChanged()
        }
//        if(recAdapter.)
        bottomSheetDialog.show()
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data
        hubViewModel.showDeepLinkOffer(appLinkAction, appLinkData)
    }

    private fun uploadFile(){
        val upload=object:Thread(){
            override fun run() {
                clientConfiguration.maxErrorRetry = 1
                clientConfiguration.connectionTimeout = 3600000
                clientConfiguration.socketTimeout = 3600000

                s3Client = AmazonS3Client(credentials)
                s3Client!!.setRegion(Region.getRegion(regions))

                TransferNetworkLossHandler.getInstance(applicationContext)
                val transferUtility = TransferUtility.builder()
                    .context(applicationContext)
                    .s3Client(s3Client)
                    .awsConfiguration(AWSConfiguration(JSONObject(HubConstants.awsconfig)))
                    .build()

                val listener = object: TransferListener {
                    override fun onProgressChanged(id: Int, curr: Long, tot: Long) {
                        Log.e("progress",curr.toString())
                    }
                    override fun onStateChanged(id: Int, state: TransferState?) {
                        when (state) {
                            TransferState.COMPLETED -> { Log.e("Demo", "Upload succeeded.") }
                            TransferState.FAILED -> { Log.e("Demo", "Upload failed.")/* handle err */ }
                            TransferState.WAITING-> { Log.e("Demo", "Upload waiting.") }

                            TransferState.IN_PROGRESS-> { Log.e("Demo", "Upload in progress.") }

                            TransferState.PAUSED-> { Log.e("Demo", "Upload paused.") }

                            TransferState.RESUMED_WAITING-> { Log.e("Demo", "Upload resume waiting.") }

                            TransferState.CANCELED-> { Log.e("Demo", "Upload cancel.") }

                            TransferState.WAITING_FOR_NETWORK-> { Log.e("Demo", "Upload wait for network.") }

                            TransferState.PART_COMPLETED-> { Log.e("Demo", "Upload part completed.") }

                            TransferState.PENDING_CANCEL-> { Log.e("Demo", "Upload pending cancel.") }

                            TransferState.PENDING_PAUSE-> { Log.e("Demo", "Upload pending pause.") }

                            TransferState.PENDING_NETWORK_DISCONNECT-> { Log.e("Demo", "Upload pending network disconnect.") }

                            TransferState.UNKNOWN-> { Log.e("Demo", "Upload unknown.") }

                            else -> { Log.e("Demo", "Upload btw.")
                                /* handle cases... */ }

                        }
                    }
                    override fun onError(id: Int, ex: Exception?) { /* handle err */ }
                }
                val no=(0..10).random()
                transferUtility.upload(BUCKET,"${hubViewModel.rname} - {$no}",File(hubViewModel.fileName))
                    .setTransferListener(listener)
            }
        }
        upload.start()

    }

    override fun onCallerMicrophoneSwitch(data: JSONObject) {
      val status= data.optBoolean("muted")
        val socketid= data.optString("id")

        for(i in 0 until recyclerDataArrayList.size){
            if(recyclerDataArrayList[i].socketId==socketid){
                recyclerDataArrayList[i].microphoneSwitch = status
                runOnUiThread {
                    Toast.makeText(this,
                        "${recyclerDataArrayList[i].title} ${status} mic",
                        Toast.LENGTH_LONG).show()
                }
                controller.requestModelBuild()
//                setUpEpoxy()
                break;
            }
        }
    }

    override fun onCallerVideoSwitch(data: JSONObject) {
        val status= data.optBoolean("camera")
        val socketid= data.optString("id")

        for(i in 0 until recyclerDataArrayList.size){
            if(recyclerDataArrayList[i].socketId==socketid){
                recyclerDataArrayList[i].videoSwitch = status
                runOnUiThread {
                    Toast.makeText(this,
                        "${recyclerDataArrayList[i].title} ${status} camera",
                        Toast.LENGTH_LONG).show()
                }
                controller.requestModelBuild()
//                setUpEpoxy()
                break
            }
        }


    }

    override fun onCallerScreenShare(data: JSONObject) {
        val status= data.optBoolean("value")
        val socketid= data.optString("id")
       runOnUiThread {
            Toast.makeText(this,
                " ${status} screen",
                Toast.LENGTH_LONG).show()
        }
        if(!status) {
            for (i in 0 until recyclerDataArrayList.size) {
                if (recyclerDataArrayList[i].socketId == socketid && recyclerDataArrayList[i].isScreen) {
                     recyclerDataArrayList.remove( recyclerDataArrayList[i])

//                    setUpEpoxy()
                }
            }
            runOnUiThread { setUpEpoxy() }
        }


    }

    override fun onCallerHandSwitch(data: JSONObject) {
        val status= data.optBoolean("hand")
        val socketid= data.optString("id")

        for(i in 0 until recyclerDataArrayList.size){
            if(recyclerDataArrayList[i].socketId==socketid){
                recyclerDataArrayList[i].handSwitch = status
                runOnUiThread {
                    Toast.makeText(this,
                        "${recyclerDataArrayList[i].title} ${status} hand",
                        Toast.LENGTH_LONG).show()
                }
                controller.requestModelBuild()
//                setUpEpoxy()
                break;
            }
        }


    }

    fun streamShort(){
//        lifecycleScope.launch {
//            hubViewModel.streamShort(recyclerDataArrayList.size)
//        }
    }

    fun startStream(room:String, email:String){
        lifecycleScope.launch {
            runCatching { hubViewModel.getStreamKey(room,email) }
                .onSuccess {
                    Log.d(TAG,"stream key $it")
                    hubViewModel.stream_key = it.toString()
                    val mediaProjectionService: MediaProjectionService? = MediaProjectionService.INSTANCE
                    if(mediaProjectionService!=null) {
                        val sendIntent = mediaProjectionService.sendIntent()

                        if (sendIntent != null) {
                            startActivityForResult(sendIntent, hubViewModel.REQUEST_CODE_STREAM)
                        }
                    }

                }
                .onFailure {
                    runOnUiThread {
                        Log.e("GRIDACT","stream error = ${it.message}")
//                        Toast.makeText(this@GridActivity,"Can not start stream ${it.message}",Toast.LENGTH_LONG).show()
                    }
                }
        }

    }




}
