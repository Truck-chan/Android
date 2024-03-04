package theboyz.tkc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import theboyz.tkc.ui.component.Avatar
import kotlin.random.Random

class MainViewModel : ViewModel() {
    var usersList = mutableListOf<Avatar<String>>()
        private set

    var state by mutableIntStateOf(0)

    companion object {
        const val STATE_IDLE = 0
        const val STATE_SEARCHING = 1
        const val STATE_OFF = 2
        const val STATE_PERMISSIONS = 2
    }

    fun addUser(name: String, mac: String, minDistance: Float = 0.1f) {

        for (k in usersList){
            if (k.data == mac){
                return
            }
        }

        var x = Random.nextFloat()
        var y = Random.nextFloat()

        while (true){
            var b = true
            for (k in usersList){
                if ((k.x - x) * (k.x - x) + (k.y - y) * (k.y - y) < minDistance){
                    x = Random.nextFloat()
                    y = Random.nextFloat()
                    b = false;
                    break;
                }
            }
            if (b) break
        }
        usersList.add(Avatar(name, Random.nextFloat() , Random.nextFloat() , 0, mac))

    }
}