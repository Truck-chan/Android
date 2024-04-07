package theboyz.tkc

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.core.Mat
import theboyz.tkc.comm.packet
import theboyz.tkc.ui.view.Overlay
import uni.proj.ec.Command
import uni.proj.ec.command
import kotlin.concurrent.thread

private const val TAG = "ConnectionActivity"

class ConnectionActivity : AppCompatActivity(){

    private lateinit var btnPlay: ImageButton
    private lateinit var btnChat: ImageButton
    private lateinit var btnReg : ImageButton

    private lateinit var cameraContainer: JavaCamera2View
    private lateinit var overlayView: Overlay
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatContent: RecyclerView
    private lateinit var chatInputArea: EditText

    private var currentOpenMenu: Int = 0 //0 = none , 1 = chat , 2 = reg
    private var gameRunning: Boolean = false
    private var imageBacked: Boolean = false

    private var CurrentFrame: Mat = Mat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connection_activity_layout)
        init()
        updateButtonsStats()
    }

    private fun init(){

        if (Constants.Connection != null) {
            Constants.Connection.onDisconnect = {
                runOnUiThread {
                    setContentView(R.layout.connection_activity_disconnected)
                }
            }
        }

        btnPlay = findViewById(R.id.btn_play)
        btnChat = findViewById(R.id.btn_chat)
        btnReg  = findViewById(R.id.btn_registers)

        cameraContainer = findViewById(R.id.camera_container)
        overlayView = findViewById(R.id.overlay_view)
        chatContainer = findViewById(R.id.chat_container)
        chatContent = findViewById(R.id.chat_content)
        chatInputArea = findViewById(R.id.chat_input_area)

        cameraContainer.setCvCameraViewListener(
            object: CameraBridgeViewBase.CvCameraViewListener2 {
                var temp: Mat = Mat()

                override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
                    if (inputFrame != null) {
                        CurrentFrame = inputFrame.rgba()
                        CurrentFrame.copyTo(temp)
                        temp = ImageProcessing.OnFrame(temp)
                        if (gameRunning){
                            temp = ImageProcessing.OnGameFrame(temp)
                        }
                        return temp
                    }
                    return Mat()
                }

                override fun onCameraViewStarted(width: Int, height: Int) {
                    ImageProcessing.onCameraSize(width , height)
                }

                override fun onCameraViewStopped() {

                }
            }
        )

        cameraContainer.setCameraPermissionGranted()

        EventLog.init(chatContent)

        EventLog.i("Event Logger Started")

        ImageProcessing.init()
    }

    private fun updateButtonsStats(){
        if (gameRunning){
            btnPlay.setImageResource(R.drawable.baseline_pause_24)
        }else{
            btnPlay.setImageResource(R.drawable.baseline_play_arrow_24)
        }

        btnChat.setColorFilter(Color.argb(255 , 0 , 0 , 0))
        btnReg.setColorFilter(Color.argb(255 , 0 , 0 , 0))

        when (currentOpenMenu){
            1 -> btnChat.setColorFilter(Color.argb(255 , 120 , 120 , 255))
            2 -> btnReg.setColorFilter(Color.argb(255 , 120 , 120 , 255))
        }

    }

    override fun onResume() {
        super.onResume()
        cameraContainer.enableView()
        //cameraContainer.enableFpsMeter()
    }

    override fun onPause() {
        super.onPause()
        cameraContainer.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()

        Constants.Connection.onDisconnect = null
    }

    fun openRegistersView(view: View) {
        Toast.makeText(this, "Will be added if needed.", Toast.LENGTH_SHORT).show()
        updateButtonsStats()
    }

    fun openChatView(view: View) {
        when(currentOpenMenu){
            1 -> {
                chatContainer.visibility = View.INVISIBLE
                currentOpenMenu = 0
            }

            else -> {
                chatContainer.visibility = View.VISIBLE
                currentOpenMenu = 1
            }
        }
        updateButtonsStats()
    }

    fun bakeAction(view: View) {
        var dialog = AlertDialog.Builder(this).create()
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.bake_loading_dialog, null))
        dialog.setCancelable(false)
        dialog.show()
        EventLog.w("Bake Process Started ..")
        thread {
            val temp = Mat()
            CurrentFrame.copyTo(temp)
            imageBacked = try {
                ImageProcessing.onBakeTrackImage(temp)
                true
            } catch (e: Exception){
                EventLog.e("Bake Process failed with error : ${e.message}")
                false
            }

            runOnUiThread {
                dialog.cancel()
                EventLog.w("Bake Process Finished")
            }
        }
    }
    fun playAction(view: View) {
        if (!imageBacked){
            Toast.makeText(this , "Bake the track first" , Toast.LENGTH_SHORT).show()
            return
        }

        try{
            ImageProcessing.onGameStarted()
            gameRunning = true
        }catch (e: Exception){
            e.printStackTrace()
            EventLog.e("Failed to start game mode : ${e.message}")
        }

        updateButtonsStats()
    }

    fun sendChatAction(view: View) {
        val text = chatInputArea.text.toString()
        try{
            val cmd = text.command
            Communicator.send(cmd)
            EventLog.i("Sent Command : $text")
        }catch (e: Exception){
            EventLog.e("Cannot compile : $text")
        }
    }

    fun forceTerminate(view: View) {
        Constants.Connection.disconnect()
    }

    fun connectionExit(view: View) {
        finish()
    }



}
