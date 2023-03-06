package cessini.technology.commonui.activity.live

import android.util.Log
import cessini.technology.commonui.activity.live.signallingserverData.*
import cessini.technology.model.Profile
import com.google.gson.GsonBuilder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

class SignalingClient() {
    var rname = "LiveMySpaceActivity"
    private lateinit var socket: Socket

    private lateinit var callback: Callback
    var lastConnection = ""
    var room_code=""

//    lateinit var profile: Profile

    // TODO: REPLACE WITH "https://rooms-api.joinmyworld.live"
    private val socketUrl = "https://socket.joinmyworld.in/"
//    "https://socket.joinmyworld.in/"
//        "http://172.17.0.156:8000/"


    private val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session -> true }
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    val trustManager = trustAllCerts[0] as X509TrustManager
    fun init(
        callback: Callback,
        rname: String,
        profile: Profile,
        socketUserMap: MutableMap<String, JSONObject>
    ) {
        room_code= rname
        this.callback = callback
//        this.profile= profile
        try {

//            val sslContext = SSLContext.getInstance("TLS")
//            sslContext.init(null, trustAllCerts, null)

//            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
//            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
//                .hostnameVerifier(hostnameVerifier)
//                .sslSocketFactory(sslSocketFactory, trustManager)
//                .build()
// default settings for all sockets
//            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
//            IO.setDefaultOkHttpCallFactory(okHttpClient);

            // set as an option
            val opts = IO.Options()
            socket = IO.socket(socketUrl, opts).connect()


             socket.on("connect"){

              Log.d(TAG,"signalling client socket connected room = $rname")
//              val data = JSONObject("""{"room":"$rname"}""")
              val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)

//                 room_code= "Prianshu Prasad_vv_583149515846757_1677173321"
//              val data = JoinRoom("Prianshu Prasad_vv_583149515846757_1677173321",sgcUser,"rpinformationhub@gmail.com").getJson()
                 val data = JoinRoom(rname,sgcUser,profile.email).getJson()

                 Log.d(TAG,"join room = ${data}")
              socket.emit("join room", data )
          }

            socket.on("denied"){

                val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)

                val data = JoinRoom(rname,sgcUser,profile.email).getJson()

                Log.d(TAG,"permission = ${data}")
                socket.emit("permission", data )
                callback.onPermissionWaiting()

            }

            var temp=0;

            socket.on("all users", Emitter.Listener { args: Array<Any>? ->

                Log.d(TAG, "data= ${Arrays.toString(args)} socket id= ${socket.id()}")
                var x= args!![0].toString()
                val array = JSONArray(x)

                for( i in 0 until array.length()){
                    val user = array.getJSONObject(i)
                    val socketid = user.optString("socket")
                    socketUserMap[socketid] = user

                }
                if(temp==0) {
                    temp++;
                    for (i in 0 until array.length()) {
                        val user = array.getJSONObject(i)
                        val socketid = user.optString("socket")
//                    socketUserMap.put(socketid,user)
                        callback.onPeerJoined(socketid, false)
                    }
                }
                callback.updateUserDetails()

            })
            socket.on("allowed"){
                Log.d(TAG,"allowed $rname")
//              val data = JSONObject("""{"room":"$rname"}""")
                val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)

//                 room_code= "Prianshu Prasad_vv_583149515846757_1677173321"
//              val data = JoinRoom("Prianshu Prasad_vv_583149515846757_1677173321",sgcUser,"rpinformationhub@gmail.com").getJson()
                val data = JoinRoom(rname,sgcUser,profile.email).getJson()
                callback.hidefragment()
                Log.d(TAG,"join room = ${data}")
                socket.emit("join room", data )

            }
            socket.on("new-ice-candidate"){

                val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)
                val data = JoinRoom(rname,sgcUser,profile.email).getJson()
                callback.hidefragment()
                Log.d(TAG,"join room = ${data}")

                socket.emit("join room", data )

                Log.d(TAG,"ice candidate recived")
                val json= JSONObject(it!![0].toString())

                Log.d(TAG,"ice candidate recived = $json")
                callback.onIceCandidateReceived(json)

            }

            socket.on("offer"){
                val json= JSONObject(it!![0].toString())

                if( json.get("type").toString()== "ans"){
                    Log.d(TAG,"ans recived = $json")
                    callback.onAnswerReceived(json)
                }else{
                Log.d(TAG,"offer recived = $json")
                callback.onOfferReceived(json)
                }
            }

            socket.on("mic"){
                val json= JSONObject(it!![0].toString())
                Log.d(TAG,"mic = $json")
                callback.onCallerMicrophoneSwitch(json)
            }

            socket.on("camera"){
                val json= JSONObject(it!![0].toString())
                Log.d(TAG,"camera = $json")
                callback.onCallerVideoSwitch(json)
            }

            socket.on("hand"){
                val json= JSONObject(it!![0].toString())
                Log.d(TAG,"hand = $json")
                callback.onCallerHandSwitch(json)
            }

            socket.on("screen"){
                val json= JSONObject(it!![0].toString())
                Log.d(TAG,"screen = $json")
                callback.onCallerScreenShare(json)
            }
            socket.on("permit?"){
                val json= JSONObject(it!![0].toString())
                Log.d("permt?",json.toString())
                callback.onJoinPermission(json,socket)
            }
            socket.on("user left"){
                val json= JSONObject(it!![0].toString())
                Log.d("leave",json.toString())
                callback.onPeerLeave(json.optString("id"))

            }




        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: KeyManagementException) {
            throw RuntimeException(e)
        }
    }

    fun destroy() {
        socket.emit("leave")
        socket.disconnect()
        socket.close()
//        instance = null
    }

    fun endcall(){
        socket.emit("leave")
    }



    fun sendIceCandidate(iceCandidate: IceCandidate, to: String?,screen:Boolean) {
        val jo = JSONObject()
//        val sgcUser= SGCUser(profile.id,profile.name,profile.email,profile.channelName,profile.profilePicture)

        try {
            val jo2 = JSONObject()
            jo2.put("socket",socket.id())
            jo2.put("screen",screen)
//            jo2.put("user",sgcUser.getJson())

            val jsonString = GsonBuilder().create().toJson(iceCandidate)
            jo.put("type",jo2.toString())
            jo.put("candidate",jsonString)
            jo.put("target",to)
            Log.d(TAG,"ice send $jo")
            socket.emit("new-ice-candidate", jo)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun sendSessionDescription(sdp: SessionDescription, to: String?, p: String,screen:Boolean) {
        Log.e("in session desc", "in session desc")
        val jo = JSONObject()
        val jo2 = JSONObject()
        try {

            jo2.put("socket",socket.id())
            jo2.put("screen",screen)

            val jsonString = GsonBuilder().create().toJson(sdp)
            jo.put("name",p)
            jo.put("target",to)
            jo.put("type",jo2.toString())
            jo.put("sdp",jsonString)
            Log.d(TAG,"sdp send $jo")
            socket.emit("offer", jo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sendMicrophone(it:Boolean){
        val jo = JSONObject()
        jo.put("room_code",room_code)
        jo.put("value",it)
        jo.put("url","")
        Log.d("Rp-Event","mic= ${jo}")

        socket.emit("mic", jo )
    }

    fun sendCamera(it: Boolean){
        val jo = JSONObject()
        jo.put("room_code",room_code)
        jo.put("value",it)
        jo.put("url","")
        Log.d("Rp-Event","camera= $jo")
        socket.emit("camera",jo)
    }

    fun sendHand(it:Boolean){
        val jo = JSONObject()
        jo.put("room_code",room_code)
        jo.put("value",it)
//        jo.put("url","")
        Log.d("Rp-Event","hand= ${jo}")
        socket.emit("hand",jo)
    }

    fun sendScreen(it:Boolean){
        val jo = JSONObject()
        jo.put("room_code",room_code)
        jo.put("value",it)
        jo.put("url","")
//        jo.put("url","")
        Log.d("Rp-Event","hand= ${jo}")
        socket.emit("screen",jo)
    }


    interface Callback {
        fun onCreateRoom(id: String)
        fun onPeerJoined(socketId: String,screen:Boolean)
        fun onSelfJoined()
        fun onPeerLeave(data: String)
        fun onOfferReceived(data: JSONObject)
        fun onAnswerReceived(data: JSONObject)
        fun onIceCandidateReceived(data: JSONObject)
        fun onCallerMicrophoneSwitch(data: JSONObject)
        fun onCallerVideoSwitch(data: JSONObject)
        fun onCallerScreenShare(data: JSONObject)
        fun onCallerHandSwitch(data: JSONObject)
        fun onJoinPermission(data: JSONObject, socket: Socket)
        fun onPermissionWaiting()
        fun hidefragment()
        fun updateUserDetails()

    }

    companion object {
       const val TAG= "SignallingClient"

        private var instance: SignalingClient? = null
        fun get(): SignalingClient? {
            if (instance == null) {
                synchronized(SignalingClient::class.java) {
                    if (instance == null) {
                        instance = SignalingClient()
                    }
                }
            }
            return instance
        }
    }



}


