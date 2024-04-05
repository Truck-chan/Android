package theboyz.tkc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import theboyz.tkc.comm.MemoryListener
import kotlin.concurrent.thread

private const val TAG = "ConnectionActivity"
const val SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb" //SPP UUID

class ConnectionActivity : AppCompatActivity() , MemoryListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.connection_activity_layout)
        Constants.memory.listener = this

        thread {
            var k = 0;
            while (true){
                Constants.memory[1] = k++
                Constants.memory.refresh()
            }
        }
    }

    fun openRegistersView(view: View) {}
    fun forceTerminate(view: View) {}
    @SuppressLint("SetTextI18n")
    override fun onDataChanged(index: Int) {
        runOnUiThread {
            Log.i(TAG, "onDataChanged: Data")
            if (index == 0) {
                var tv = findViewById<TextView>(R.id.testing_tv);
                tv.text = "Value : ${Constants.memory[index]}"
            }else{
                var tv = findViewById<TextView>(R.id.testing_tv2);
                tv.text = "K : ${Constants.memory[index]}"
            }
        }
    }

    override fun onDisconnected() {
        TODO("Not yet implemented")
    }

    override fun onConnected() {
        TODO("Not yet implemented")
    }
}
