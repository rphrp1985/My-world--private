package cessini.technology.commonui.fragment.suggestion

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.R
import cessini.technology.commonui.common.BaseFragment
import cessini.technology.commonui.databinding.FragmentLanguageBinding
import cessini.technology.commonui.viewmodel.suggestionViewModel.LanguageSelectionFragmentViewModel
import cessini.technology.navigation.MainNavGraphDirections
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@AndroidEntryPoint
class LanguageFragment : BaseFragment<FragmentLanguageBinding>(R.layout.fragment_language) {

    companion object {
        const val TAG = "LanguageFragment"
    }

    private val viewModel: LanguageSelectionFragmentViewModel by viewModels()

    // TODO: Extend to other supported languages
    private val localeChipMap = mapOf(
        R.id.english_language to "en",
        R.id.hindi_language to "hi",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener { onClickNext() }
    }

    private fun onClickNext() {
        binding.progressBar.isVisible = true
        val locale = localeChipMap[binding.chipGroup.checkedChipId]
        val defaultAppLanguage = Locale.getDefault().language

        if (locale != null && defaultAppLanguage != locale /*check if user selected language is not the default language*/) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(locale)
            )
        }

        try {
            lifecycleScope.launch {
                val success = async { viewModel.submitOnBoardingSelection() }
                if (success.await())
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(
                            MainNavGraphDirections.actionGlobalHomeFlow()
                        )
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to submit on-boarding selection", e)
            Toast.makeText(
                requireContext(),
                getString(R.string.error_on_boarding_submission),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            binding.progressBar.isGone = true
        }
    }
}