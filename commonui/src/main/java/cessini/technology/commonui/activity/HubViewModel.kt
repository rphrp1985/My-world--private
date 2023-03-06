package cessini.technology.commonui.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cessini.technology.newrepository.myspace.RoomRepository
import com.amazonaws.util.IOUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.webrtc.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor(
    application: Application,
    private val roomRepository: RoomRepository
) :AndroidViewModel(application) {
    val REQUEST_EXTERNAL_STORAGe: Int= 205
    var iceServers: List<PeerConnection.IceServer> = listOf(

         PeerConnection.IceServer.builder("stun:coturn_server.joinmyworld.in:3478")
        .createIceServer(),

         PeerConnection.IceServer.builder("turn:coturn_server.joinmyworld.in:3478")
        .setUsername("testguest")
        .setPassword("secretpassword")
        .createIceServer(),

//    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
//        .setUsername("suraj@gmail.com")
//        .setPassword("98376683")
//        .createIceServer(),
//
//    PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302")
//        .setUsername("diya@gmail.com")
//        .setPassword("98376683")
//    .createIceServer(),
//
//    PeerConnection.IceServer.builder("turn:rooms-api.joinmyworld.live")
//    .setUsername("Admin")
//    .setPassword("password")
//    .createIceServer(),

    )

    var peerConnectionMap: HashMap<String, PeerConnection> = HashMap()
    var peerConnectionScreenMap: HashMap<String, PeerConnection> = HashMap()


    var STREAM_NAME_PREFIX = "android_device_stream"
    var videoFileName=""
    var fileName: String? = "VID" + System.currentTimeMillis() + ".mp4".also {
        videoFileName = it
    }

     lateinit var fileRenderer:FileRenderer
    lateinit var remoteVideoTrack:VideoTrack

    var hand= false

     var isFront = true
     val CAPTURE_PERMISSION_REQUEST_CODE = 1
     val REQUEST_CODE_STREAM = 179 //random num
     val REQUEST_CODE_RECORD = 180 //random num
     var isScreenShare=false
     var isCameraShare=false


    var remoteViewsIndex = 1
     var videoTrack: VideoTrack? = null
     var videoTrackScreen: VideoTrack? = null
     var localAudioTrack : AudioTrack? = null
     var localAudioTrackScreen : AudioTrack? = null
     var mediaStream: MediaStream?= null
    var rname="RoomLive"
    var user_id="user"




    // disabling all the running tracks
    fun endcall() {
        if (peerConnectionMap.isNotEmpty()) {
            for (i in peerConnectionMap.values) {
                i.close()
            }
        }
        if (this::remoteVideoTrack.isInitialized){
            remoteVideoTrack.removeSink(fileRenderer)
            videoTrack?.setEnabled(false)
            videoTrackScreen?.setEnabled(false)
            localAudioTrackScreen?.setEnabled(false)
            localAudioTrack?.setEnabled(false)
            fileRenderer.release()
        }

    }


     fun createSDPConstraints(): MediaConstraints? {

        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"))
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "maxFrameRate",
                60.toString()
            )
        )
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "minFrameRate",
                1.toString()
            )
        )
        return constraints
    }
    private fun createFile(context: Context, srcUri: Uri?, dstFile: File) {
        try {
            val inputStream = srcUri?.let { context.contentResolver.openInputStream(it) } ?: return
            val outputStream = FileOutputStream(dstFile)
            IOUtils.copy(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    fun showDeepLinkOffer(appLinkAction: String?, appLinkData: Uri?) {
        // 1
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            // 2
            val rCode = appLinkData.getQueryParameter("code")
            Log.e("deepLink",rCode.toString())
            if (rCode.isNullOrBlank().not()) {
                rname=rCode.toString()
            }
        } else {
        }
    }


    fun sendSnapshot(room:String,file: File){
        viewModelScope.launch {
            runCatching {
                roomRepository.sendSnapShot(
                    room,file
                )
            }.onFailure {

                Log.d("SnapShot","failed  ${it.message}")  }
                .onSuccess { Log.d("SnapShot","success") }

        }
    }









}