package cessini.technology.commonui

import android.util.Log
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.createPlatformEndpoint
import aws.sdk.kotlin.services.sns.model.PublishRequest
import aws.sdk.kotlin.services.sns.model.SubscribeRequest
import aws.smithy.kotlin.runtime.util.asyncLazy
import cessini.technology.newapi.model.MyWorldNotification
import cessini.technology.newrepository.preferences.UserIdentifierPreferences
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmazonSNSImpl @Inject constructor(userIdentifierPreferences: UserIdentifierPreferences) {
    companion object {
        private const val TAG = "AmazonSNSImpl"
    }
    private val userId = userIdentifierPreferences.id
    private var endpoint = ""
    private var displayName=""
    init {
        getEndpoint(userId)
    }



    suspend fun createNewEndpoint(devtoken: String): String {



        Log.d(TAG, "createNewEndpoint: $userId")

        if (endpoint.isEmpty()) {

            try {

//                val endpointRequest = CreatePlatformEndpointRequest {
//                    platformApplicationArn = "arn:aws:sns:ap-south-1:454502946350:app/GCM/MyWorld"
//                    token = devtoken
//                }
                val endPointArn =
                    SnsClient {
                        credentialsProvider = AmazonModule
                        region = "ap-south-1"
                    }.createPlatformEndpoint {
                        platformApplicationArn = "arn:aws:sns:ap-south-1:523652883143:app/GCM/MyWorld"
                        token = devtoken
                    }.endpointArn.toString()

                Log.d(TAG, "Endpoint arn $endPointArn")

                addToFirebase(UserArn(userId, devtoken, endPointArn,displayName))
                return endPointArn
            } catch (e: Exception) {
                Log.d(TAG, "Error ${e.stackTrace}")
            }
            return endpoint
        } else
            return endpoint

    }

    fun addToFirebase(userArn: UserArn) {
        Firebase.firestore.collection("deviceArn").document("${userArn.devtoken}").set(userArn)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Device added to Firestore ${it.result}")
                } else {
                    Log.d(TAG, "Device added to Firestore ${it.exception}")
                }
            }
    }

    fun getEndpoint(userId: String) {

        Log.d(TAG, "getEndpoint: $userId")
        if (userId.isEmpty())
            return
        Firebase.firestore.collection("deviceArn").document("$userId").get()
            .addOnSuccessListener { doc ->
                endpoint = doc.toObject<UserArn>()?.arn.toString()
                displayName=doc.toObject<UserArn>()?.name.toString()
                if (endpoint.isNotEmpty())
                    Log.d(TAG, "Device present in Firestore ${doc.data}")
            }
            .addOnFailureListener {
                Log.d(TAG, "Device not present in Firestore ${it.message}")
            }
    }

    suspend fun sendFollowNotification(userId: String) {
        val message="$displayName followed you"
        Log.d(TAG, "sendFollowNotification: ")



        Log.d(TAG, "sendFollowNotification: $endpoint")

        if (userId.isEmpty())
            return
        else {
            Firebase.firestore.collection("deviceArn").document("$userId").get()
                .addOnSuccessListener { doc ->
                    val endpoint = doc.toObject<UserArn>()?.arn.toString()
                    if (endpoint.isNotEmpty())
                        Log.d(TAG, "Device present in Firestore ${doc.data}")

                    if (endpoint.isNotEmpty() && endpoint!="null") {

                        val request = PublishRequest {
                            this.messageStructure
                            this.messageAttributes //= Map<String, MessageAttributeValue>("", )
                            this.subject = ""
                            this.message = message
                            this.targetArn = endpoint
                            this.subject = "New follower"
                        }
                        Log.e(TAG, "endpoint: $endpoint")
//            var cred = BasicAWSCredentials("AKIAWTUT4SYXBNPNSG4S", "xmtzf8lVqiz+/tH95VCuD3zKf/KOUWszbCg5z6yK")
                        var msg = ""
                        try {
                            GlobalScope.launch {
                                msg = SnsClient {
                                    credentialsProvider = AmazonModule
                                    region = "ap-south-1"
                                }.use { snsClient ->
                                    snsClient.publish(request)
                                }.messageId.toString()

                                Log.d(TAG, "sendFollowNotification: $msg")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "sendFollowNotification: ${e.message}")
                        }

                        Log.d(TAG, "amazonsns $msg")
                    } else {
                        Log.d(TAG, "User endpoint not found")
                    }

                }
                .addOnFailureListener {
                    Log.d(TAG, "Device not present in Firestore ${it.message}")
                }
        }
        addGlobalData(message,userId)
//        if (endpoint.isNotEmpty()) {
//
//            val request = PublishRequest {
//                this.subject = ""
//                this.message = "You were followed."
//                this.targetArn = endpoint
//                this.subject = "New follower"
//            }
////            var cred = BasicAWSCredentials("AKIAWTUT4SYXBNPNSG4S", "xmtzf8lVqiz+/tH95VCuD3zKf/KOUWszbCg5z6yK")
//            var msg = ""
//            try {
//                msg = SnsClient {
//                    credentialsProvider = AmazonModule
//                    region = "ap-south-1"
//                }.use { snsClient ->
//                    snsClient.publish(request)
//                }.messageId.toString()
//            } catch (e: Exception) {
//                Log.d(TAG, "sendFollowNotification: ${e.message}")
//            }
//
//            Log.d(TAG, "amazonsns $msg")
//        }
//        else{
//            Log.d(TAG, "User endpoint not found")
//        }

    }
    suspend fun subscribe(snsTopicArn:String,endpointArn:String){
        val snsClient=SnsClient {
            credentialsProvider = AmazonModule
            region = "ap-south-1"
        }

        val subscribe=SubscribeRequest{
            this.topicArn=snsTopicArn
            this.protocol="application"
            this.endpoint=endpointArn
        }

        snsClient.subscribe(subscribe)

    }
    fun addGlobalData(message:String,id:String){
        var profile_image=""
        var username=""
        val colRef=Firebase.firestore.collection("GlobalNotifications")
        val olddocRef=colRef.document("${userId}")
        val newdocRef=colRef.document("${id}")
        olddocRef.get()
            .addOnSuccessListener { doc ->
                profile_image=doc.getString("profile_image").toString()
                username=doc.getString("username").toString()
                Log.e(TAG,profile_image)
                Log.e(TAG,username)
                newdocRef.collection("NotificationData")
                    .document(userId)
                    .set(MyWorldNotification(userId,message,username,profile_image))
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Data added to Firestore ${it.result}")
                        } else {
                            Log.d(TAG, "Data added to Firestore ${it.exception}")
                        }
                    }
            }
            .addOnFailureListener {
                Log.d(TAG, "Data not present in Firestore ${it.message}")
            }




    }
    fun deleteDataUnfollow(id:String){
        val colRef=Firebase.firestore.collection("GlobalNotifications")
        val newdocRef=colRef.document("${id}")
        newdocRef.collection("NotificationData")
            .document(userId)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }
}

data class UserArn(val userId: String = "", val devtoken: String = "", val arn: String = "",val name:String= "")