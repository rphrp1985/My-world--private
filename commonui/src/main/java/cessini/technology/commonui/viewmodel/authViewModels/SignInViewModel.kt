package cessini.technology.commonui.viewmodel.authViewModels


import android.util.Log
import androidx.lifecycle.*
import cessini.technology.cvo.entity.AuthEntity
import cessini.technology.newapi.services.myworld.model.response.ApiProfile
import cessini.technology.newrepository.authRepository.AuthRepository
import cessini.technology.newrepository.explore.RegistrationRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val newAuthRepository: cessini.technology.newrepository.myworld.AuthRepository,
    private val registrationRepository: RegistrationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SignInViewModel"
    }

    private val _signInProgress = MutableLiveData(0)
    val signInProgress: LiveData<Int> get() = _signInProgress
    private var getToken=""
    fun getToken(): String {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            getToken=token
        })
        return getToken
    }

    fun signIn(
        account: GoogleSignInAccount,
        onSuccess: (AuthEntity, ApiProfile) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            val apiProfile = newAuthRepository.authenticate(account.idToken!!)
            val profile = apiProfile.profile
            val token = apiProfile.token
            try {
                registrationRepository.registerAuthUser()
            } catch (e: Exception) {
                Log.d(TAG, "${e.localizedMessage} : ${e.message}")
            }

            try {
                val authEntity = AuthEntity(
                    id = profile.id,
                    email = profile.email,
                    displayName = profile.name,
                    channelName = profile.channelName,
                    photoUrl = profile.profilePicture,
                    idToken = account.idToken!!,
                    bio = profile.bio,
                    access_token = token.access,
                    refresh_token = token.refresh,
                    location = profile.location
                )

                onSuccess(authEntity, profile)
                getToken()

            } catch (e: Exception) {
                Log.d(TAG, "Sign in failure, clearing half baked data")
                onError()
            }

        }
    }

    fun facebookSignIn(
        accessToken: AccessToken?,
        onSuccess: (AuthEntity, ApiProfile) -> Unit,
        onError: () -> Unit
    ) {
        Log.d("Login", "The login code is getting executed")
        viewModelScope.launch {
            val apiProfile = newAuthRepository.authenticate(accessToken.toString())
            val profile = apiProfile.profile
            val token = apiProfile.token
            registrationRepository.registerAuthUser()
            try {
                val authEntity = AuthEntity(
                    id = profile.id,
                    email = profile.email,
                    displayName = profile.name,
                    channelName = profile.channelName,
                    photoUrl = profile.profilePicture,
                    idToken = token.toString(),
                    bio = profile.bio,
                    access_token = token.access,
                    refresh_token = token.refresh,
                    location = profile.location
                )

                onSuccess(authEntity, profile)

            } catch (e: Exception) {
                Log.d(TAG, "Sign in failure, clearing half baked data")
                onError()
            }
        }
    }

    fun insertDataInDB(authEntity: AuthEntity, callback: (String) -> Unit) {
        viewModelScope.launch {
            val result = kotlin.runCatching {
                authRepository.insertAuth(authEntity)
            }

            result.onSuccess {
                Log.d("DB", "Auth Inserted in DB $it")
                _signInProgress.value = 100
                callback(it.channelName)
                val userId=it.id
<<<<<<< HEAD
                val displayName=it.displayName
                val profileImage=it.photoUrl
                updateFirebaseData(getToken,userId)
                addGlobalNotification(userId,profileImage,displayName)
=======
                val name=it.displayName
                updateFirebaseData(getToken,userId,name)
>>>>>>> show_name
            }

            result.onFailure {
                Log.d("DB", "Auth Inserted failed ${it.message}")
                _signInProgress.value = -1
            }
        }
    }
    fun updateFirebaseData(token: String,userId:String,name:String){
        val collectionRef = Firebase.firestore.collection("deviceArn")
        val oldDocRef = collectionRef.document(token)

        // Check whether document id exits or not. If not exits then return null
        oldDocRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful){
                val document=task.result
                if (document.exists()){
                    Log.e(TAG,"Document id token exits")
                }
                else{
                    Log.e(TAG,"Document id token does not exits")
                    return@addOnCompleteListener
                }
            }
            else{
                Log.e(TAG,"Failed to retrieve document")
            }
        }

        // Updating userId field value
        val data = hashMapOf("userId" to userId)
        oldDocRef.update(data as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG, "userId attribute updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating userId", e)
            }

        // Inserting name field value
        val displayName= hashMapOf("name" to name)
        oldDocRef.update(displayName as Map<String,Any>)
            .addOnSuccessListener {
                Log.e(TAG,"name attribute added successfully")
            }
            .addOnFailureListener{ e ->
                Log.w(TAG,"Error inserting name",e)
            }

        val newDocRef=Firebase.firestore.collection("deviceArn").document(userId)
        createNewDocument(oldDocRef,newDocRef)

    }
    fun createNewDocument(oldDocRef:DocumentReference,newDocRef:DocumentReference){
        oldDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot!=null){
                val data=documentSnapshot.data
                if (data != null) {
                    newDocRef.set(data).addOnSuccessListener {
                        oldDocRef.delete()
                    }
                }
            }
        }
    }
    fun addGlobalNotification(id:String,profile_image:String,username:String){
        Firebase.firestore.collection("GlobalNotifications").document("${id}").set(User(id,profile_image,username))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Data added to Firestore ${it.result}")
                } else {
                    Log.d(TAG, "Data added to Firestore ${it.exception}")
                }
            }
    }





}
data class User(val id: String,val profile_image: String,val username: String)
