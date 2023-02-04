package cessini.technology.newapi.interceptors

import android.util.Log
import cessini.technology.newapi.ApiParameters.AUTH_HEADER
import cessini.technology.newapi.ApiParameters.NO_AUTH
import cessini.technology.newapi.ApiParameters.TOKEN_TYPE
import cessini.technology.newapi.preferences.AuthPreferences
import cessini.technology.newapi.services.myworld.MyWorldConstants.BASE_ENDPOINT
import cessini.technology.newapi.services.myworld.MyWorldConstants.REFRESH_TOKEN_ENDPOINT
import cessini.technology.newapi.services.myworld.model.body.RefreshBody
import cessini.technology.newapi.services.myworld.model.response.ApiAccessToken
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        Log.d("TAG","token required ${chain.request().body}")

        Log.d("TAG","token required ${chain.request().headers}")
        Log.d("TAG","Auth url = ${chain.request().url}")
        // Skip requests with "No-Auth" header
        if (chain.request().headers[NO_AUTH] != null) {
            return chain.proceed(chain.request())
        }

        Log.d("TAG","access token = ${authPreferences.accessToken}")

        Log.d("TAG","refresh token = ${authPreferences.refreshToken}")

        if (!authPreferences.tokenExpire) {
            return chain.proceedDeleteTokenIfUnauthorized(
                chain.createAuthRequest(authPreferences.accessToken)
            )
        }


        updateAccessToken(chain)

        return chain.proceedDeleteTokenIfUnauthorized(
            chain.createAuthRequest(authPreferences.accessToken)
        )
    }

    private fun updateAccessToken(chain: Interceptor.Chain) {
        authPreferences.accessToken =
            Gson().fromJson(
                chain.refreshAccessToken().run {
                    body!!.string().also { close() }
                },
                ApiAccessToken::class.java
            )?.data?.access ?: ""
    }

    private fun Interceptor.Chain.proceedDeleteTokenIfUnauthorized(request: Request): Response {
        val response = proceed(request)

        if (response.code == 401) {
            authPreferences.deleteAccessToken()
        }

        Log.d("TAG","request= $request")

        Log.d("TAG","response= $response")

        return response
    }

    private fun Interceptor.Chain.createAuthRequest(accessToken: String): Request {

        Log.d("TAG","AUth req created ")
        return request()
            .newBuilder()
            .addHeader(AUTH_HEADER, TOKEN_TYPE + accessToken)
            .build()
    }

    private fun Interceptor.Chain.refreshAccessToken(): Response {
        val body = Gson().toJson(RefreshBody(authPreferences.refreshToken))
            .toRequestBody("application/json".toMediaType())

        Log.d("TAG","Refreshing token    ")
        val tokenRefresh = request()
            .newBuilder()
//            .url("http://ec2-13-232-34-39.ap-south-1.compute.amazonaws.com/$REFRESH_TOKEN_ENDPOINT")
            .url("$BASE_ENDPOINT$REFRESH_TOKEN_ENDPOINT")
            .post(body)
            .build()

        return proceedDeleteTokenIfUnauthorized(tokenRefresh)
    }
}
