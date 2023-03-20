package cessini.technology.commonui.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cessini.technology.newrepository.myspace.RoomRepository
import com.amazonaws.util.IOUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.webrtc.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HubViewModel @Inject constructor(
    application: Application,
    private val roomRepository: RoomRepository
) :AndroidViewModel(application) {

    var peerConnectionMap: HashMap<String, PeerConnection> = HashMap()
    var peerConnectionScreenMap: HashMap<String, PeerConnection> = HashMap()


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
    var stream_key=""




    // disabling all the running tracks
    fun endcall() {
        if (peerConnectionMap.isNotEmpty()) {
            for (i in peerConnectionMap.values) {
                i.close()
            }
        }
        if (this::remoteVideoTrack.isInitialized){
//            remoteVideoTrack.removeSink(fileRenderer)
            videoTrack?.setEnabled(false)
            videoTrackScreen?.setEnabled(false)
            localAudioTrackScreen?.setEnabled(false)
            localAudioTrack?.setEnabled(false)
//            fileRenderer?.release()
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


    fun streamShort(size:Int){


        Log.d("SnapShot","size = $size")
        if( GridActivity.modelProcessed< size )
            return

        val file= screenshot("snapshot")

        file?.let {
            Log.d("SnapShot","stream short file success")

            sendSnapshot(rname, it) }

    }

    fun screenshot( filename: String): File? {
        val date = Date()
        Log.d("SnapShot","insode screeenshot")
        // Here we are initialising the format of our image name
        val format = "snapshot"
        try {
//            // Initialising the directory of storage
            val dirpath: String = Environment.getExternalStorageDirectory().toString()+"/"+ Environment.DIRECTORY_DOWNLOADS.toString()
            val file = File(dirpath)
            if (!file.exists()) {
                val mkdir = file.mkdir()
            }

            // File name
            val path = "$dirpath/$filename-$format.jpeg"

            var bitmap: Bitmap?= null

            when(GridActivity.screenShot.value?.size){
                0-> bitmap= null
                1->bitmap= GridActivity.screenShot.value!![0]
                2-> bitmap= combineBitmapTopDown(GridActivity.screenShot.value!![0]!!, GridActivity.screenShot.value!![1]!!)
                3-> bitmap= combineBitmapTopDown(GridActivity.screenShot.value!![0]!!,combineBitmapLeftRight(
                    GridActivity.screenShot.value!![1]!!, GridActivity.screenShot.value!![2]!!)!!)
                else-> bitmap = combineBitmapTopDown(combineBitmapLeftRight(GridActivity.screenShot.value!![1]!!, GridActivity.screenShot.value!![0]!!)!!, combineBitmapLeftRight(
                    GridActivity.screenShot.value!![2]!!, GridActivity.screenShot.value!![3]!!)!!)!!
            }

            val imageurl = File(path)
            val outputStream = FileOutputStream(imageurl)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("SnapShot","file success")
            GridActivity.screenShot.value!!.clear()
            return imageurl
            return null
        } catch (io: FileNotFoundException) {
            io.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    private fun combineBitmapLeftRight(left: Bitmap, right: Bitmap): Bitmap? {
        val width = left.width + right.width
        val height = if (left.height > right.height) left.height else right.height
        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        canvas.drawBitmap(left, 0f, 0f, null)
        canvas.drawBitmap(right, left.width.toFloat(), 0f, null)
        return combined
    }

    private fun combineBitmapTopDown(top: Bitmap, bottom: Bitmap): Bitmap? {
        val width = top.width.coerceAtMost(bottom.width)
        val height = top.height+ bottom.height
        val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        canvas.drawBitmap(top, 0f, 0f, null)
        canvas.drawBitmap(bottom, 0f, top.height.toFloat(), null)
        return combined
    }


    suspend fun getStreamKey(room: String, email: String): String{
        //    Log.d("HUBVM","res = ${}")

        return roomRepository.getStreamKey(room, email)

    }











}