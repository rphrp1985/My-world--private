package cessini.technology.myspace.create

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cessini.technology.commonui.common.BaseBottomSheet
import cessini.technology.commonui.common.BottomSheetLevelInterface
import cessini.technology.commonui.utils.Constant
import cessini.technology.myspace.R
import cessini.technology.myspace.databinding.FragmentSearchBinding
import cessini.technology.myspace.databinding.FragmentShareBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment(private val listener: BottomSheetLevelInterface?) :
    BaseBottomSheet<FragmentSearchBinding>(R.layout.fragment_search),
    BottomSheetLevelInterface {
    var count=0
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
        binding.searchConstraint.layoutParams.height = height + 10.toPx().toInt()
    }
}