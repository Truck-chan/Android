package theboyz.tkc

import android.Manifest
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import org.opencv.android.OpenCVLoader
import theboyz.tkc.ui.component.UsersPreview
import theboyz.tkc.ui.theme.TruckKunTheme
import theboyz.tkc.Constants.START_CAMERA


private const val TAG = "MainActivity";


class MainActivity : ComponentActivity() {
    val viewModel by viewModels<MainViewModel>()
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            uiManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
        }
        actionBar?.hide()

        OpenCVLoader.initLocal()

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, filter)

        if (bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.state = MainViewModel.STATE_IDLE
            } else {
                viewModel.state =
                    if (bluetoothAdapter.isDiscovering) MainViewModel.STATE_SEARCHING else MainViewModel.STATE_IDLE
            }
        }else{
            viewModel.state = MainViewModel.STATE_OFF
        }



        setContent {
            MainLayout()
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == 1001){
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        var r = 0;
        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(
                    arrayOf( Manifest.permission.CAMERA),
                    1001
                )
        } else r++

        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf( Manifest.permission.BLUETOOTH),
                1001
            )
        } else r++

        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(
                    arrayOf( Manifest.permission.BLUETOOTH_CONNECT),
                    1001
                )
            }else {
                Toast.makeText(this, "android issues ..", Toast.LENGTH_SHORT).show()
            }
        } else r++

        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
        } else r++


        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf( Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else r++


        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf( Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1001
            )
        } else r++


        if (r == 6){
            if (START_CAMERA) {
                this.startActivity(
                    Intent(this, ConnectionActivity::class.java)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            Log.i(TAG, "onReceive: Intent received")
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    Log.i(TAG, "Action found")
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device == null) {
                        Log.e(TAG, "onReceive: Device is null")
                        return
                    }
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.i(TAG, "onReceive: Device : ${deviceName} & mac = ${deviceHardwareAddress}")
                    if (deviceName != null) {
                        viewModel.addUser(deviceName ?: "not-named", deviceHardwareAddress)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(TAG, "Action started")
                    viewModel.state = MainViewModel.STATE_SEARCHING
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "Action idle")
                    viewModel.state = MainViewModel.STATE_IDLE
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.i(TAG, "Action state changed")

                    if (!bluetoothAdapter.isEnabled){
                        viewModel.state = MainViewModel.STATE_OFF
                    }else {
                        if (bluetoothAdapter.isDiscovering){
                            viewModel.state = MainViewModel.STATE_SEARCHING
                        }else{
                            viewModel.state = MainViewModel.STATE_IDLE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}

@Composable
fun MainContent() {
    val ctx = LocalContext.current as MainActivity
    val users = ctx.viewModel.usersList
    val viewModel = ctx.viewModel

    Log.i(TAG, "MainContent: Update")

    Row {
        Box (
            Modifier
                .weight(0.5f)
                .fillMaxSize()
                .padding(Dp(4f))) {
            AnimatedContent(
                targetState = viewModel.state,
                label = "",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90))
                            + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))
                            + slideIntoContainer(
                        animationSpec = tween(200, easing = EaseIn),
                        towards = if (viewModel.state == MainViewModel.STATE_IDLE) AnimatedContentTransitionScope.SlideDirection.End else AnimatedContentTransitionScope.SlideDirection.Start
                    ))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                }
            ) {
                if (it != MainViewModel.STATE_SEARCHING){
                    Column (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ){
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Truck-chan",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Cyan,
                            softWrap = true,
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = -25.dp),
                            text = "Doko ?",
                            fontSize = 64.sp,
                            softWrap = true,
                        )

                        if (viewModel.state != MainViewModel.STATE_OFF) {
                            Button(
                                onClick = {
                                    if (ctx.bluetoothAdapter.isEnabled) {
                                        if (ActivityCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.BLUETOOTH_SCAN
                                            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                                        ) {
                                            Toast.makeText(
                                                ctx,
                                                "Ayo gimme permission",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                ActivityCompat.requestPermissions(
                                                    ctx,
                                                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                                                    10,
                                                )
                                            }

                                        } else {
                                            Log.i(TAG, "MainContent: Starting Discovery")
                                            ctx.bluetoothAdapter.startDiscovery()
                                            ctx.viewModel.usersList.clear()
                                        }
                                    } else {
                                        Toast.makeText(
                                            ctx,
                                            "you know you need bluetooth to USE bluetooth right ? ACTIVATE IT!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                elevation = ButtonDefaults.elevatedButtonElevation()
                            ) {
                                Text(
                                    text = "Search",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Enable Bluetooth",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red,
                                modifier = Modifier
                                    .border(1.dp, Color.Gray, RectangleShape)
                                    .padding(4.dp)
                            )
                        }
                    }
                }else{
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp),
                        )
                    }
                }
            }

        }
        Divider(thickness = Dp(1f), modifier = Modifier
            .fillMaxHeight()  //fill the max height
            .width(1.dp))

        Box (
            Modifier
                .weight(0.5f)
                .fillMaxSize()) {

            if (viewModel.state == MainViewModel.STATE_IDLE){
                Text(
                    text = "Press the search button to start searching ..",
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 40.dp),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }else{
                Text(
                    text = "Searching ..",
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 40.dp),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }

            UsersPreview(
                ripple = viewModel.state == MainViewModel.STATE_SEARCHING || viewModel.usersList.isNotEmpty(),
                pulse = false,
                users,
            ) {
                ctx.bluetoothAdapter.cancelDiscovery()
                ctx.startActivity(Intent(ctx , ConnectingActivity::class.java).putExtra("mac" , viewModel.usersList[it].data))
            }
        }
    }
}



@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun MainLayout() {
    TruckKunTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainContent()
        }
    }
}