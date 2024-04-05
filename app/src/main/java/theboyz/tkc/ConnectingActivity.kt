package theboyz.tkc

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import theboyz.tkc.comm.SharedMemory
import java.util.UUID
import kotlin.concurrent.thread


private const val TAG = "ConnectingActivity"
class ConnectingActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connecting_activity_layout);
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        thread {
            //start the connection
            val mac = intent.getStringExtra("mac") ?: throw NullPointerException()
            Log.i(TAG, "onCreate: Starting connection")
            try {
                val dev = bluetoothAdapter.getRemoteDevice(mac)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "onCreate: Failed to connect (no permission)")
                    throw IllegalStateException("no permission")
                }
                val socket = dev.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID))

                socket.connect()

                Constants.memory = SharedMemory(socket)
                startActivity(Intent(this , ConnectionActivity::class.java))
            }catch (e : Exception){
                Log.e(TAG, "onCreate: Error while connecting", e)
                runOnUiThread{
                    setContentView(R.layout.connecting_activity_failed)
                }
            }
        }
    }

    fun connectingReturn(view: View) {
        finish()
    }
}