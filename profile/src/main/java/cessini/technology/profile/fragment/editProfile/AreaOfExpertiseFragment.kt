package cessini.technology.profile.fragment.editProfile

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cessini.technology.commonui.common.BaseBottomSheet
import cessini.technology.commonui.common.BottomSheetLevelInterface
import cessini.technology.commonui.utils.Constant
import cessini.technology.commonui.viewmodel.BaseViewModel
import cessini.technology.commonui.viewmodel.suggestionViewModel.SuggestionViewModel
import cessini.technology.model.User
import cessini.technology.profile.R
import cessini.technology.profile.controller.AreaOfExpertiseController
import cessini.technology.profile.controller.FollowerFollowingController
import cessini.technology.profile.databinding.FragmentAreaOfExpertiseBinding
import cessini.technology.profile.viewmodel.FollowerFollowingViewModel
import cessini.technology.profile.viewmodel.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AreaOfExpertiseFragment(private val listener: BottomSheetLevelInterface):
BaseBottomSheet<FragmentAreaOfExpertiseBinding>(R.layout.fragment_area_of_expertise) {

    private val viewModel: SuggestionViewModel by activityViewModels()
    private val baseViewModel by activityViewModels<BaseViewModel>()

    var controller: AreaOfExpertiseController? = null
    var categories = ArrayList<String>()


    var count = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener.onSheet2Created()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collectLatest { category ->
                    category?.map { category_name ->
                        categories.add(category_name.key)
                    }
                }
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setRecyclerView()
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpFullScreen(bottomSheetDialog, Constant.settingBottomSheetHeight + 12)
        }
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_SETTLING) {
                    count++
                    if (count % 2 == 0) {
                        dismiss()
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
        return dialog
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onSheet1Dismissed()
//        controller!!.activity = null
//        controller!!.imageGalleryBottomSheetFragment = null
//
//        controller = null
    }
    private fun setRecyclerView() {

        binding.etCategoryRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            controller = AreaOfExpertiseController(this@AreaOfExpertiseFragment)
            binding.etCategoryRV.setController(controller!!)

            controller!!.categoryList = categories

        }
    }

}