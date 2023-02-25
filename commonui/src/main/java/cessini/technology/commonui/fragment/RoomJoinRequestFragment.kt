package cessini.technology.commonui.fragment


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cessini.technology.commonui.R
import cessini.technology.commonui.activity.GridActivity
import coil.load
import com.bumptech.glide.Glide
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI


class RoomJoinRequestFragment(val listner:GridActivity) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    lateinit var binding: cessini.technology.commonui.databinding.RoomJoinRequestBinding

    var nameI: String=""
    var picI: String=""
    lateinit var socketI: Socket
    var idI:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.room_join_request, container, false)

        binding.callerName.text= nameI

        try {
            Glide.with(requireContext())
                .load(picI).centerCrop()
                .into(binding.profileImage);
        }catch (e:Exception){}

        binding.answerButton.setOnClickListener {
            val jo = JSONObject()
            jo.put("allowed",true)
            jo.put("id",idI)
            Log.d("RoomJoin","answere =$jo")
            socketI.emit("permit status",jo)
            listner.hidefragment()
        }

        binding.declineButton.setOnClickListener {
            val jo = JSONObject()
            jo.put("allowed",false)
            jo.put("id",idI)
            Log.d("RoomJoin","decline =$jo")
            socketI.emit("permit status",jo)
            listner.hidefragment()
        }






    return binding.root

    }

    fun showRequest(name: String, pic: String, socket: Socket,id:String){

        nameI= name
        picI= pic

        idI= id
        socketI= socket

    }


}
