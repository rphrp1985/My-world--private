package cessini.technology.commonui.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.amazonaws.util.IOUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import org.webrtc.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor(
    application: Application
) :AndroidViewModel(application) {
     var iceServers: List<PeerConnection.IceServer> = listOf(
    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
    .createIceServer(),

    PeerConnection.IceServer.builder("stun:rooms-api.joinmyworld.live")
    .createIceServer(),

    PeerConnection.IceServer.builder("turn:rooms-api.joinmyworld.live")
    .setUsername("Admin")
    .setPassword("password")
    .createIceServer(),

    )

    var peerConnectionMap: HashMap<String, PeerConnection> = HashMap()


    var STREAM_NAME_PREFIX = "android_device_stream"
    var videoFileName=""
    var fileName: String? = "VID" + System.currentTimeMillis() + ".mp4".also {
        videoFileName = it
    }

     lateinit var fileRenderer:FileRenderer
    lateinit var remoteVideoTrack:VideoTrack

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










}