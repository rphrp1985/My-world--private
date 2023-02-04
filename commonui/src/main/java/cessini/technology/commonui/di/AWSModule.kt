package cessini.technology.commonui.di

import cessini.technology.commonui.activity.HubConstants
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.google.android.gms.common.api.Api.Client
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kurento.client.Hub
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AWSModule {
    @Provides
    @Singleton
    fun provideawasc()= BasicAWSCredentials(HubConstants.key,HubConstants.secret)

    @Provides
    @Singleton
    fun provideawsconfig():ClientConfiguration{
        val c= ClientConfiguration()
        c.apply {
            maxErrorRetry = HubConstants.maxerror
            maxConnections = HubConstants.maxtimeout
        }
return  c
    }



}