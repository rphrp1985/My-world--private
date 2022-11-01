package cessini.technology.newrepository.myworld

import android.app.Application
import android.provider.Settings
import cessini.technology.newapi.services.myworld.MyWorldService
import cessini.technology.newapi.services.myworld.model.body.OnBoardingSubmission
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class OnBoardingRepository @Inject constructor(
    private val service: MyWorldService,
    application: Application,
) {

    private val deviceId = Settings.Secure.getString(
        application.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    suspend fun submitOnBoardingSelection(
        language: String,
        subcategories: List<String>
    ): Boolean {
        val result = service.submitOnBoardingSelection(
            OnBoardingSubmission(
                language = language,
                subCategories = subcategories,
                deviceId = deviceId
            )
        )
        return result.isSuccessful
    }
}