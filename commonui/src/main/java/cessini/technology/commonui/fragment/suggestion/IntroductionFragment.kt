package cessini.technology.commonui.fragment.suggestion

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.R
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.commonui.databinding.FragmentIntroductionBinding
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroductionFragment : BaseFragment<FragmentIntroductionBinding>(R.layout.fragment_introduction) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        systemBarInsetsEnabled = false
        binding.btnNext.setOnClickListener { onClickNext() }
    }

    private fun onClickNext() {
        findNavController().navigate(
            IntroductionFragmentDirections.actionIntroductionToCategorySelection()
        )
    }
}