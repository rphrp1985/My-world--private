package cessini.technology.commonui.viewmodel.authViewModels


import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.*
import cessini.technology.cvo.entity.AuthEntity
import cessini.technology.newapi.services.myworld.model.response.ApiProfile
import cessini.technology.newrepository.authRepository.AuthRepository
import cessini.technology.newrepository.explore.RegistrationRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
    var tokenS=""
    fun token(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            tokenS=token
            Log.e("DNYAN",tokenS)
            // Log and toast
        })
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
                token()

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
                fireB(tokenS,it.id)
            }

            result.onFailure {
                Log.d("DB", "Auth Inserted failed ${it.message}")
                _signInProgress.value = -1
            }
        }
    }
    fun fireB(token: String,userId:String){
        val collectionRef = Firebase.firestore.collection("deviceArn")
        val docRef = collectionRef.document(token)
        val data = hashMapOf("userId" to userId)

        docRef.update(data as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG, "Document name updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document name", e)
            }

        val newDocRef=Firebase.firestore.collection("deviceArn").document(userId)


        move(docRef,newDocRef)



        Log.e("FIRE_SANJAY","SAIIII")
    }
    fun move(oldDoc:DocumentReference,newDoc:DocumentReference){
        oldDoc.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot!=null){
                val data=documentSnapshot.data
                if (data != null) {
                    newDoc.set(data).addOnSuccessListener {
                        oldDoc.delete()
                    }
                }
            }
        }
    }





}
