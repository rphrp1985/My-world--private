package cessini.technology.home.fragment.dialogs


import android.app.ActionBar
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import cessini.technology.home.R
import cessini.technology.home.databinding.HomeRoomTopPopupBinding
import cessini.technology.home.databinding.RoomJoinRequestBinding


class RoomJoinRequestFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

//    lateinit var pgbar:ProgressBar
//    lateinit var okButton:Button
//    lateinit var enterButton:Button
//    lateinit var roomTextView: TextView
//    lateinit var status:TextView

    lateinit var binding: RoomJoinRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.room_join_request, container, false)
        return binding.root

    }


}
