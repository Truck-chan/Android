package theboyz.tkc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ConnectionViewModel : ViewModel(){
    companion object {
        const val STATE_CONNECTING   = 0
        const val STATE_DISCONNECTED = 1
        const val STATE_FAILED       = 2
        const val STATE_CONNECTED    = 3
    }

    //Screen state
    var state by mutableIntStateOf(STATE_CONNECTING)

    //Logs
    var log = mutableListOf<String>()


    //values
    var RegA by mutableIntStateOf(0)
    var RegB by mutableIntStateOf(0)
}