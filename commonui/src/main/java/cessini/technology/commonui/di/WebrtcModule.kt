package cessini.technology.commonui.di

import android.content.Context
import cessini.technology.commonui.activity.RTCAudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.webrtc.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebrtcModule{

    @Provides
    @Singleton
    fun provideAudiomanager(@ApplicationContext context: Context):RTCAudioManager{
        return RTCAudioManager(context)

    }

    @Provides
    @Singleton
    fun provideegl(): EglBase.Context {
        return  EglBase.create().eglBaseContext
    }

    @Provides
    @Singleton
    fun providepeerconnection(
        eglBase: EglBase.Context,
        @ApplicationContext context: Context
    ): PeerConnectionFactory {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase,
                    true,
                    true
                )
            ).createPeerConnectionFactory()

    }

    @Provides
    @Singleton
    fun provideaudiosourse(peerConnectionFactory: PeerConnectionFactory): AudioSource {
        return peerConnectionFactory.createAudioSource(MediaConstraints())
    }



}