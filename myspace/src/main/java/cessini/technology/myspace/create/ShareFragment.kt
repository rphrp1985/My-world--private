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
import cessini.technology.myspace.R
import cessini.technology.myspace.databinding.FragmentCreateRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareFragment(private val listener: BottomSheetLevelInterface?) :
    BaseBottomSheet<FragmentCreateRoomBinding>(R.layout.fragment_share),
    BottomSheetLevelInterface {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSheet2Dismissed() {
        TODO("Not yet implemented")
    }

    override fun onSheet2Created() {
        TODO("Not yet implemented")
    }

    override fun onSheet1Dismissed() {
        TODO("Not yet implemented")
    }

    override fun getHeightOfBottomSheet(height: Int) {
        TODO("Not yet implemented")
    }


}