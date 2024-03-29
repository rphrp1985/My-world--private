package cessini.technology.commonui.fragment.videodisplay

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.R
import cessini.technology.commonui.`class`.ExoProvider
import cessini.technology.commonui.adapter.videodisplay.CommonVideoDisplayAdapter
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.commonui.databinding.FragmentCommonVideoDisplayBinding
import cessini.technology.commonui.utils.ProfileConstants
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.commonui.viewmodel.CommentsViewModel
import cessini.technology.commonui.viewmodel.basicViewModels.GalleryViewModel
import cessini.technology.cvo.exploremodels.SearchCreatorApiResponse
import cessini.technology.model.Video
import cessini.technology.model.VideoProfile
import cessini.technology.navigation.NavigationFlow
import cessini.technology.navigation.ToFlowNavigable
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kohii.v1.core.MemoryMode
import kohii.v1.exoplayer.Kohii


@AndroidEntryPoint
class CommonVideoDisplayFragment :
    BaseFragment<FragmentCommonVideoDisplayBinding>(R.layout.fragment_common_video_display),
    CommonVideoDisplayAdapter.Listener {

    companion object {
        private val TAG = CommonVideoDisplayFragment::class.java.simpleName
    }

    private val commentsViewModel by activityViewModels<CommentsViewModel>()
    private val galleryViewModel by activityViewModels<GalleryViewModel>()
    private val baseViewModel by activityViewModels<BaseViewModel>()

    private val exoProvider: Kohii by lazy { ExoProvider.get(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = galleryViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.profileViewpager.offscreenPageLimit = 2

        exoProvider.register(this, MemoryMode.HIGH)
            .addBucket(binding.profileViewpager)

//        val dataSourceFactory: DataSource.Factory =
//            DefaultDataSourceFactory(requireContext(),Util.getUserAgent(requireContext(), "ExoPlayer"))
//        val uri: Uri = Uri.parse(galleryViewModel.videoDisplay.value?.upload_file)
//        val dashMediaSource = DashMediaSource(uri, dataSourceFactory,
//            DefaultDashChunkSource.Factory(dataSourceFactory), null, null)
//
//        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
//        val trackSelector: TrackSelector =
//            DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
//
//        val simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext(), trackSelector)


//        simpleExoPlayer.prepare(dashMediaSource)
        val videoAdapter by lazy { CommonVideoDisplayAdapter(this, exoProvider) }

        /**Flags for transition from different modules-
         * 0->Incoming from Explore Search Module
         * 1->Incoming from Profile Video Module**/
        when (galleryViewModel.videoFlow.value) {
            0 -> galleryViewModel.videoDisplay.observe(this) {
                if (it == null) return@observe
                Log.d(TAG, "Value of 0 : $it")

                val videos = listOf(
                    Video(
                        id = it.id.orEmpty(),
                        profile = VideoProfile(
                            id = it.profile?.id.orEmpty(),
                            name = it.profile?.name.orEmpty(),
                            channel = it.profile?.channel_name.orEmpty(),
                            picture = it.profile?.profile_picture.orEmpty(),
                        ),
                        title = it.title.orEmpty(),
                        description = it.description.orEmpty(),
                        thumbnail = it.thumbnail.orEmpty(),
                        duration = it.duration ?: 0,
                        category = convertToInt(it.category?.category),
                        //category = it.category?.category.orEmpty(),
                        url = it.upload_file.orEmpty(),
                        timestamp = it.timestamp?.toFloatOrNull() ?: 0F,
                    )
                )
                videoAdapter.submitList(videos)
            }
            1 -> galleryViewModel.videoDisplayList.observe(this) {
                if (it == null) return@observe
                Log.d(TAG, "Value of 1 : ${it.size}")
                videoAdapter.submitList(it)
            }
        }

        /** Setting up the ViewPager for the home fragment. */
        binding.profileViewpager.apply {
            Log.i(TAG, "Entering in adapter")
            adapter = videoAdapter
            setCurrentItem(galleryViewModel.videoPos.value!!, false)
        }

        /** Sending User Back to the Profile Fragment.*/
        binding.profileVideoBackNavigationButton.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun convertToInt(category: String?): List<String> {
        val res = mutableListOf<String>()
        category?.forEach { res.add(it.toString()) }
        return res
    }

    override fun onStop() {
        super.onStop()
        /** If the Fragment is stopped. Then, the instance of the player is released by calling the method.*/
        galleryViewModel.setCommonVideoProfile(null)
    }

    override fun getLikedStatus(videoId: String, callback: (Boolean) -> Unit) {
        return galleryViewModel.getLikedStatus(videoId, callback)
    }

    override fun onProfileVideoUserNameOrImgClicked(video: Video) {
        if (video.profile.id == baseViewModel.id.value) {
            (activity as ToFlowNavigable).navigateToFlow(NavigationFlow.ProfileFlow)
            return
        }
        openUserProfile(video)
    }

    override fun onCommentButtonPressed(videoId: String) {
        commentsViewModel.setCurrVideoId(videoId)
        commentsViewModel.getCommentsList(videoId)
        findNavController().navigate(CommonVideoDisplayFragmentDirections.actionCommonVideoDisplayFragmentToCommentFragment())
    }

    override fun onPlaybackPaused(videoId: String, duration: Int) {
        galleryViewModel.updatePlayerOnPaused(videoId, duration)
    }

    override fun onLikeButtonPressed(videoId: String) {
        if (baseViewModel.authFlag.value == true) {
            galleryViewModel.postLikeOnVideo(videoId)
            return
        }
        (activity as ToFlowNavigable).navigateToFlow(
            NavigationFlow.AuthFlow
        )
    }

    fun openUserProfile(video: Video) {
        ProfileConstants.creatorModel = SearchCreatorApiResponse(
            id = video.profile.id,
            name = video.profile.name,
            channel_name = video.profile.channel,
            profile_picture = video.profile.picture,
        )

        (activity as ToFlowNavigable).navigateToFlow(
            NavigationFlow.GlobalProfileFlow
        )
    }

}
