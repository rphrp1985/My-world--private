package cessini.technology.commonui.activity.live

import android.util.Log
import cessini.technology.commonui.activity.live.signallingserverData.JoinRoom
import cessini.technology.commonui.activity.live.signallingserverData.SGCUser
import cessini.technology.model.Profile
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
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
import kotlin.collections.ArrayList

class SignalingClient() {
    var rname = "LiveMySpaceActivity"
    private lateinit var socket: Socket

    private lateinit var callback: Callback
    var lastConnection = ""

    // TODO: REPLACE WITH "https://rooms-api.joinmyworld.live"
    private val socketUrl = "https://socket.joinmyworld.in/"

    private val hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session -> true }
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    val trustManager = trustAllCerts[0] as X509TrustManager
    fun init(callback: Callback, rname: String,profile:Profile) {
        this.callback = callback
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

//              val data = JoinRoom("surajpisal_ee_123855667896512_1676786614",sgcUser,"surajpisal113@gmail.com").getJson()
                 val data = JoinRoom(rname,sgcUser,profile.email).getJson()

                 Log.d(TAG,"join room = ${data}")
              socket.emit("join room", data )
          }

//            socket.on("created", Emitter.Listener { args: Array<Any?>? ->
//                Log.e("chao", "room created:" + args.toString() + " sid- ${socket.id()}")
//                callback.onCreateRoom(socket.id())
//            })
//            Log.d("Working", "one")

//            socket.on("full",
//                Emitter.Listener { args: Array<Any?>? ->
//                    Log.e(
//                        "chao",
//                        "room full"
//                    )
//                })

//            try {
//                Log.d("id", socket.id().toString())
//            } catch (e: Exception) {
//                Log.d("id", e.toString())
//            }

            socket.on("all users", Emitter.Listener { args: Array<Any>? ->

                Log.d(TAG, "data= ${Arrays.toString(args)} socket id= ${socket.id()}")


                    var x= args!![0].toString()

                    x=x.replace('[',' ')
                    x=x.replace(']',' ')
                    x= x.trim()
                    val arr= x.split(',')
                    for(ids in arr){
                     var z=  ids.replace('"',' ')
                     z= z.trim()
                        Log.d(TAG,"id = $z")
                        callback.onPeerJoined(z)
                    }

//                    for (element in jsonArray) {
//                        callback.onPeerJoined(element)
//                    }

            })

            socket.on("new-ice-candidate"){

//                Log.d(TAG,"ice candidate recived = $")
                val json= JSONObject(it!![0].toString())

                Log.d(TAG,"ice candidate recived = $json")
                callback.onIceCandidateReceived(json)

            }

            socket.on("offer"){
                val json= JSONObject(it!![0].toString())

                Log.d(TAG,"offer recived = $json")
                callback.onIceCandidateReceived(json)
            }

//            Log.d("Working", "second")
//            socket.on("joined", Emitter.Listener { args: Array<Any?>? ->
//                Log.e("chao", "self joined:" + socket.id())
//
//                callback.onSelfJoined()
//            })
//            socket.on("log",
//                Emitter.Listener { args: Array<Any?>? ->
//                    Log.e(
//                        "chao",
//                        "log call " + Arrays.toString(args)
//                    )
//                })

//            Log.d("SHIVAM3", "ASJ")
//            socket.on("bye", Emitter.Listener { args: Array<Any> ->
//                Log.e("chao", "bye " + args[0])
//                callback.onPeerLeave(args[0] as String)
//            })

//            Log.d("Working", "third")
//            socket.on("message response") { args ->
//                Log.e("chao arg", "message " + Arrays.toString(args))
//
//                val arg = args[0]
//                Log.d("arg", arg.toString())
//                if (arg is String) {
//                } else if (arg is JSONObject) {
//                    var data = arg
//                    Log.e("data", data.javaClass.name)
//                    try {
//                        val type = data.optString("type")
//                        if ("offer" == type) {
//                            callback.onOfferReceived(data)
//                        } else if ("answer" == type) {
//                            callback.onAnswerReceived(data)
//                        } else
//                            callback.onIceCandidateReceived(data)
//                    } catch (e: Exception) {
//                        Log.e("exception", "$e ")
//                    }
//                }
//
//            }

            //Other Methods
//            socket.connect()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: KeyManagementException) {
            throw RuntimeException(e)
        }
    }

    fun destroy() {
        socket.disconnect()
        socket.close()
//        instance = null
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, to: String?) {
        val jo = JSONObject()
        try {


            jo.put("type","new-ice-candidate")
            jo.put("candidate",iceCandidate.sdp)
            jo.put("target",to)

//            jo.put("type", "candidate")
//            jo.put("label", iceCandidate.sdpMLineIndex)
//            jo.put("id", iceCandidate.sdpMid)
//            jo.put("candidate", iceCandidate.sdp)
//            jo.put("from", socket.id())
//            jo.put("to", to)
            socket.emit("new-ice-candidate", jo)
//

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun sendSessionDescription(sdp: SessionDescription, to: String?, p: String?) {
        Log.e("in session desc", "in session desc")
        val jo = JSONObject()
        try {
            jo.put("name",socket.id())
            jo.put("target",to)
            jo.put("type","offer")
            jo.put("sdp",sdp.description)

//            jo.put("type", sdp.type.canonicalForm())
//            jo.put("sdp", sdp.description)
//            jo.put("from", socket.id())
//            jo.put("part", p)
//            jo.put("to", to)
            socket.emit("offer", jo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    interface Callback {
        fun onCreateRoom(id: String)
        fun onPeerJoined(socketId: String)
        fun onSelfJoined()
        fun onPeerLeave(data: String)
        fun onOfferReceived(data: JSONObject)
        fun onAnswerReceived(data: JSONObject)
        fun onIceCandidateReceived(data: JSONObject)
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


