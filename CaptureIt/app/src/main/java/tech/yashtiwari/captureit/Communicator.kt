package tech.yashtiwari.captureit

import android.media.Image
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Communicator: ViewModel(){

    val message = MutableLiveData<Any>()

    public fun sendMsgCommunicator(msg: String){
        message.value = msg
    }


}