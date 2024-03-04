package theboyz.tkc

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

import theboyz.tkc.comm.SharedMemory
import theboyz.tkc.ui.theme.TruckKunTheme
import java.lang.IllegalStateException
import java.util.UUID
import kotlin.concurrent.thread

private const val TAG = "ConnectionActivity"

class ConnectionActivity : ComponentActivity() {
    lateinit var sharedMemory: SharedMemory
    val viewModel by viewModels<ConnectionViewModel>()
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        thread {
            //start the connection
            var mac = intent.getStringExtra("mac") ?: throw NullPointerException()
            Log.i(TAG, "onCreate: Starting connection")
            try {
                var dev = bluetoothAdapter.getRemoteDevice(mac)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "onCreate: Failed to connect (no permission)")
                    throw IllegalStateException("no permission")
                }
                val socket = dev.createInsecureRfcommSocketToServiceRecord(UUID.randomUUID())
                socket.connect();
                sharedMemory = SharedMemory(socket) {
                    //TODO handle data change
                }

                viewModel.state = ConnectionViewModel.STATE_CONNECTED

            }catch (e : Exception){
                Log.e(TAG, "onCreate: Error while connecting", e)
                viewModel.state = ConnectionViewModel.STATE_FAILED
            }
        }
        setContent {
            ConnectionLayout()
        }
    }
}

@Composable
fun ConnectionContent(){
    val ctx = LocalContext.current as ConnectionActivity
    val viewModel = ctx.viewModel

    when (viewModel.state){
        ConnectionViewModel.STATE_CONNECTING -> {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box (
                    modifier = Modifier
                        .width(45.dp)
                        .height(45.dp)
                ){
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Connecting ...")
            }
        }

        ConnectionViewModel.STATE_DISCONNECTED -> {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_error_outline_24),
                    contentDescription = "",
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Connection was terminated.")
                Spacer(modifier = Modifier.height(5.dp))

                OutlinedButton(onClick = {
                    //TODO: return code
                }) {
                    Text(text = "Return")
                }
            }
        }

        ConnectionViewModel.STATE_FAILED -> {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_wifi_tethering_error_24),
                    contentDescription = "",
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Failed to connect to the device.")
                Spacer(modifier = Modifier.height(5.dp))

                OutlinedButton(onClick = {
                    ctx.startActivity(Intent(ctx , MainActivity::class.java))
                    ctx.finish()
                }) {
                    Text(text = "Return")
                }
            }
        }

        ConnectionViewModel.STATE_CONNECTED -> {

        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:parent=Nexus S,orientation=landscape"
)
@Composable
fun ConnectionLayout() {
    TruckKunTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ConnectionContent()
        }
    }
}