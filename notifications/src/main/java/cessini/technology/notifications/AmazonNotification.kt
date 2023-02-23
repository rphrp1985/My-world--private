package cessini.technology.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import cessini.technology.commonui.AmazonSNSImpl
import cessini.technology.newapi.model.MyWorldNotification
import cessini.technology.newrepository.explore.RegistrationRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    @Inject
    lateinit var registrationRepository: RegistrationRepository

    @Inject
    lateinit var amazonSNSImpl: AmazonSNSImpl

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token acquired: $token")

        coroutineScope.launch {
            launch {
                try {
                    val endpoint=amazonSNSImpl.createNewEndpoint(token)
                    amazonSNSImpl.subscribe("arn:aws:sns:ap-south-1:523652883143:All",endpoint)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            launch {
                try {
                    registrationRepository.registerNotification(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, remoteMessage.data.toString())
        val body=remoteMessage.notification?.body.toString()
        val title=remoteMessage.notification?.title.toString()
        addData(MyWorldNotification("3",body,"Myworld","https://my-world-bucket-alpha.s3.ap-south-1.amazonaws.com/notification/72.png"))
        showNotification(body,title)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun showNotification(body: String,title: String) {

        val mBuilder: NotificationCompat.Builder = NotificationCompat
            .Builder(this, "Channel_Id")
            .setSmallIcon(R.drawable.notifications_icon)
            .setContentText(body)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // creating notification channel
        val notificationChannel =
            NotificationChannel("Channel_Id", "message", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(123, mBuilder.build())
    }
    private fun addData(myWorldNotification: MyWorldNotification){
        Firebase.firestore.collection("GlobalNotifications").document().set(myWorldNotification)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Data added to Firestore ${it.result}")
                } else {
                    Log.d(TAG, "Data added to Firestore ${it.exception}")
                }
            }
    }
}