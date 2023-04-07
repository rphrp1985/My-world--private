package cessini.technology.myspace.create

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import cessini.technology.commonui.common.BaseBottomSheet
import cessini.technology.commonui.common.BottomSheetLevelInterface
import cessini.technology.commonui.utils.Constant
import cessini.technology.myspace.R
import cessini.technology.myspace.SearchAdapter
import cessini.technology.myspace.SearchView
import cessini.technology.myspace.databinding.FragmentSearchUsersBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchUsersFragment(private val listener: BottomSheetLevelInterface?) :
    BaseBottomSheet<FragmentSearchUsersBinding>(R.layout.fragment_search_users),
    BottomSheetLevelInterface
{
    val viewModel: SearchView by activityViewModels()
    var count=0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activity = requireActivity()
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.context = this

        binding.viewpager.currentItem = viewModel.viewPagerCounter.value!!

        viewModel.viewPagerCounter.observe(viewLifecycleOwner, Observer {
            binding.viewpager.currentItem = viewModel.viewPagerCounter.value!!
        })

        init()

    }
    private fun init() {
        // removing toolbar elevation
        binding.viewpager.adapter =
            SearchAdapter(viewModel.titles.value!!, childFragmentManager, lifecycle)

        // attaching tab mediator
//        TabLayoutMediator(
//            binding.tabLayout, binding.viewpager
//        ) { tab: TabLayout.Tab, position: Int ->
//            tab.text = viewModel.titles.value!![position]
//            binding.searchViewHeader.setQuery("", false)
//        }.attach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpFullScreen(bottomSheetDialog, Constant.settingBottomSheetHeight + 6)
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
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        try {
            listener?.onSheet1Dismissed()
        }catch (e:Exception){}
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSheet2Dismissed() {
        setLevel(0)
        listener?.onSheet2Dismissed()
    }

    override fun onSheet2Created() {
        setLevel(-1)
        listener?.onSheet2Created()
    }

    override fun onSheet1Dismissed() = Unit

    override fun getHeightOfBottomSheet(height: Int) {
        binding.searchCN.layoutParams.height = height + 10.toPx().toInt()
    }
    override fun onResume() {
        super.onResume()

        binding.searchViewHeader.queryHint = "Search Creators"

        /** Navigating user back to Explore Section. */
        binding.backButton.setOnClickListener {
            dismiss()
        }


    }


}