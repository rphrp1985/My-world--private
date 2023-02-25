package cessini.technology.commonui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cessini.technology.commonui.R
import cessini.technology.commonui.databinding.RoomWaitingBinding
import com.bumptech.glide.Glide
import io.socket.client.Socket
import org.json.JSONObject

class RoomJoinWaiting : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    var rname=""
    lateinit var binding: RoomWaitingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.room_waiting, container, false)

         binding.roomName.text= rname
        return binding.root

    }

    fun update(room:String){
        rname= room
    }



}
