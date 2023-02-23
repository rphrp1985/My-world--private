package cessini.technology.commonui.activity

import org.webrtc.EglBase
import org.webrtc.VideoTrack

data class data(
    var title: String,
    var index: Int,
    var video: VideoTrack,
    val creater: Boolean,
    var microphoneSwitch: Boolean,
    var videoSwitch:Boolean,
    var profilepic:String,
    var handSwitch:Boolean,
    var userID:String,
    var socketId:String,
    val con: EglBase.Context,
    val fileRenderer: FileRenderer?
)
