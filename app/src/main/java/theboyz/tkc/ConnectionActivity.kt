package theboyz.tkc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import theboyz.tkc.comm.MemoryListener
import theboyz.tkc.comm.packet
import uni.proj.ec.Command
import kotlin.concurrent.thread

private const val TAG = "ConnectionActivity"

class ConnectionActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connection_activity_layout)

        Constants.Connection.onDisconnect = {
            runOnUiThread {
                setContentView(R.layout.connection_activity_disconnected)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Constants.Connection.onDisconnect = null
    }

    fun openRegistersView(view: View) {
        Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show()
        Constants.Connection.send(
            Command.fromString("hi{}").packet
        )
    }
    fun forceTerminate(view: View) {
        Constants.Connection.disconnect()
    }

    fun connectionExit(view: View) {
        finish()
    }


}
