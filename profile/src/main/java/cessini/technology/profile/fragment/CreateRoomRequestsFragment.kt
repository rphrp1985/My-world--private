package cessini.technology.profile.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cessini.technology.profile.R
import cessini.technology.profile.databinding.FragmentCreateRoomRequestsBinding

class CreateRoomRequestsFragment : Fragment() {

    companion object {
        private const val TAG = "CreateRoomRequestsFragment"
    }

    private var _binding: FragmentCreateRoomRequestsBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_create_room_requests,
            container,
            false
        )
        return binding.root
    }
}