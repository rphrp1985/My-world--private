package cessini.technology.home.fragment.dialogs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cessini.technology.home.R
import cessini.technology.home.databinding.HomeRoomTopPopupBinding


class RoomWaitingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    var roomName=""
    lateinit var binding:HomeRoomTopPopupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_room_top_popup, container, false)
        binding.waitingBar.visibility= View.VISIBLE
        binding.enterButton.visibility= View.GONE
        binding.okButton.visibility= View.GONE
        binding.roomName.text= roomName
        binding.joinStatus.text="Waiting for Accept"

        return binding.root

    }

    fun setWaiting(roomName:String){
        this.roomName= roomName

    }

    fun setApproved(roomName: String){
        binding.waitingBar.visibility= View.GONE
        binding.enterButton.visibility= View.VISIBLE
        binding.okButton.visibility= View.GONE
        binding.roomName.text= roomName
        binding.joinStatus.text="Request Approved"
    }

    fun setDenied(roomName: String){
        binding.waitingBar.visibility= View.GONE
        binding.enterButton.visibility= View.GONE
        binding.okButton.visibility= View.VISIBLE
        binding.roomName.text= roomName
        binding.joinStatus.text="Request Denied"
    }

    fun setshowanimation(){}

    fun setEventCallback(joinEvents: JoinEvents){

        binding.okButton.setOnClickListener { joinEvents.hide() }

        binding.enterButton.setOnClickListener {  joinEvents.join() }

    }
}

interface JoinEvents{
    fun join()

    fun hide()
}
