package cessini.technology.commonui.fragment


import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cessini.technology.commonui.R
import coil.load
import com.bumptech.glide.Glide
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI


class RoomJoinRequestFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    lateinit var binding: cessini.technology.commonui.databinding.RoomJoinRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.room_join_request, container, false)




        return binding.root

    }

    fun showRequest(name: String, pic: String, socket: Socket){

        binding.callerName.text= name
        Glide.with(requireContext())
            .load(pic)
            .into(binding.profileImage);

        binding.answerButton.setOnClickListener {
            val jo = JSONObject()
            jo.put("allowed",true)
            jo.put("id","")
          socket.emit("permit status",jo)
        }

        binding.declineButton.setOnClickListener {
            val jo = JSONObject()
            jo.put("allowed",false)
            jo.put("id","")
            socket.emit("permit status",jo)
        }

    }


}
