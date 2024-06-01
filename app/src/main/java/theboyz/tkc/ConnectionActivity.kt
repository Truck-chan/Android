package theboyz.tkc

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.core.Mat
import theboyz.tkc.ip.utils.GlobalParameters
import theboyz.tkc.ui.view.ControllerView
import theboyz.tkc.ui.view.OnOvalMoveListener
import theboyz.tkc.ui.view.Overlay
import uni.proj.ec.Command
import uni.proj.ec.command
import kotlin.concurrent.thread

private const val TAG = "ConnectionActivity"

class ConnectionActivity : AppCompatActivity(){

    private lateinit var btnPlay: ImageButton
    private lateinit var btnChat: ImageButton
    private lateinit var btnReg : ImageButton
    private lateinit var btnPreview : ImageButton

    private lateinit var cameraContainer: JavaCamera2View
    private lateinit var overlayView: Overlay
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatContent: RecyclerView
    private lateinit var variablesContainer: LinearLayout
    private lateinit var chatInputArea: EditText
    private lateinit var currentPreview: TextView

    private var currentOpenMenu: Int = 0 //0 = none , 1 = chat , 2 = reg
    private var gameRunning: Boolean = false
    private var imageBacked: Boolean = false

    private var currentPreviewIndex: Int = 0
    private var previewRunning: Boolean = false

    private var CurrentFrame: Mat = Mat()
    private val FrameLock: Any = Object()

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
        btnPreview  = findViewById(R.id.btn_preview)

        // M => index 2 for wide camera

        cameraContainer = findViewById(R.id.camera_container)
        cameraContainer.setCameraIndex(2);

        overlayView = findViewById(R.id.overlay_view)
        chatContainer = findViewById(R.id.chat_container)
        chatContent = findViewById(R.id.chat_content)
        chatInputArea = findViewById(R.id.chat_input_area)
        currentPreview = findViewById(R.id.currentPreview)
        variablesContainer = findViewById(R.id.variables_container)

        var b1: SeekBar = findViewById(R.id.variable_GAMMA_VALUE)
        b1.setOnSeekBarChangeListener(
            object: OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    GlobalParameters.GAMMA_VALUE = progress.toDouble() / 100.0f
                    var text: TextView = findViewById(R.id.variable_GAMMA_VALUE_value)
                    text.text = GlobalParameters.GAMMA_VALUE.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        var b2: SeekBar = findViewById(R.id.variable_MIN_AREA_PERCENTAGE)
        b2.setOnSeekBarChangeListener(
            object: OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    GlobalParameters.MIN_AREA_PERCENTAGE = progress.toDouble() / 1000.0f
                    var text: TextView = findViewById(R.id.variable_MIN_AREA_PERCENTAGE_value)
                    text.text = GlobalParameters.MIN_AREA_PERCENTAGE.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        var b3: SeekBar = findViewById(R.id.variable_MAX_AREA_PERCENTAGE)
        b3.setOnSeekBarChangeListener(
            object: OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    GlobalParameters.MAX_AREA_PERCENTAGE = progress.toDouble() / 100.0f
                    var text: TextView = findViewById(R.id.variable_MAX_AREA_PERCENTAGE_value)
                    text.text = GlobalParameters.MAX_AREA_PERCENTAGE.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        cameraContainer.setCvCameraViewListener(
            object: CameraBridgeViewBase.CvCameraViewListener2 {
                var temp: Mat = Mat()

                override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
                    if (gameMode){
                        if (System.currentTimeMillis() - lastGameCommandTS >= GameCommandDelta){
                            lastGameCommandTS = System.currentTimeMillis()
                            Log.i(TAG, "onCameraFrame: Sending Game Command ${gameXValue} , ${gameYValue}")
                            Communicator.send(
                                Command("game" , arrayOf(
                                    Command.CommandArgument("x" , (gameXValue * 300 + 300).toInt()) ,
                                    Command.CommandArgument("y" , (gameYValue * 300 + 300).toInt())
                                ))
                            )
                        }
                    }

                    if (inputFrame != null) {

                        synchronized(FrameLock) {
                            CurrentFrame = inputFrame.rgba()
                            CurrentFrame.copyTo(temp)
                            overlayView.refresh(CurrentFrame.width() , CurrentFrame.height())
                        }

                        ImageProcessing.OnFrame(temp)
                        synchronized(FrameLock) {
                            temp.copyTo(CurrentFrame);
                        }

                        if (gameRunning){
                            //FIXME: some redundant copying is happening here
                            ImageProcessing.OnGameFrame(temp)
                        } else if (previewRunning){
                            ImageProcessing.OnPreview(currentPreviewIndex, temp)
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

        if (previewRunning){
            btnPreview.setColorFilter(Color.argb(255 , 255 , 255 , 255))
        }else{
            btnPreview.setColorFilter(Color.argb(255 , 10 , 10 , 10))
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

        when(currentOpenMenu){
            2 -> {
                variablesContainer.visibility = View.INVISIBLE
                currentOpenMenu = 0
            }

            else -> {
                chatContainer.visibility = View.INVISIBLE
                variablesContainer.visibility = View.VISIBLE
                currentOpenMenu = 2
            }
        }

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
                variablesContainer.visibility = View.INVISIBLE
                currentOpenMenu = 1
            }
        }
        updateButtonsStats()
    }

    fun bakeAction(view: View) {
        if (gameRunning){
            Toast.makeText(this, "Stop the game first", Toast.LENGTH_SHORT);
            return;
        }

        val dialog = AlertDialog.Builder(this).create()
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.bake_loading_dialog, null))
        dialog.setCancelable(false)
        dialog.show()
        EventLog.w("Bake Process Started ..")

        val temp = Mat()
        synchronized(FrameLock) {
            CurrentFrame.copyTo(temp)
        }

        thread {
            imageBacked = try {
                ImageProcessing.onBakeTrackImage(temp)
                true
            } catch (e: Exception){
                Log.i("Exception Debug", "bakeAction: " + e.message);
                EventLog.e("Bake Process failed with error : ${e.message}")
                false
            }

            EventLog.w("Bake Process Finished")

            runOnUiThread {
                dialog.cancel()

            }
        }
    }
    fun playAction(view: View) {
        if (gameRunning){
            gameRunning = false
            updateButtonsStats()
            return
        }

        if (previewRunning){
            previewRunning = false
            updateButtonsStats()
            //stop the preview if its running
        }

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

    fun preivewInc(view: View) {
        currentPreviewIndex++
        currentPreview.text = currentPreviewIndex.toString();
    }
    fun preivewDec(view: View) {
        currentPreviewIndex--
        currentPreview.text = currentPreviewIndex.toString();
    }
    fun previewAction(view: View) {
        if (gameRunning){
            Toast.makeText(this, "Please stop the game first" , Toast.LENGTH_SHORT).show()
            return
        }

        previewRunning = !previewRunning
        updateButtonsStats()
    }

    var gameMode = false
    var gameXValue = 0.0f
    var gameYValue = 0.0f
    var lastGameCommandTS: Long = 0
    var GameCommandDelta = 100 //in ms
    fun openGameMode(view: View) {
        if (!gameMode){
            findViewById<Overlay>(R.id.overlay_view).visibility = View.INVISIBLE
            findViewById<ImageButton>(R.id.btn_play).visibility = View.INVISIBLE
            findViewById<ImageButton>(R.id.btn_bake).visibility = View.INVISIBLE

            var xAxis = findViewById<ControllerView>(R.id.controller_x_axis);
            xAxis.visibility = View.VISIBLE
            xAxis.onOvalMoveListener = object : OnOvalMoveListener {
                override fun onOvalMove(x: Float, y: Float) {
                    gameXValue = x
                }
            }

            var yAxis = findViewById<ControllerView>(R.id.controller_y_axis);
            yAxis.visibility = View.VISIBLE
            yAxis.onOvalMoveListener = object : OnOvalMoveListener {
                override fun onOvalMove(x: Float, y: Float) {
                    gameYValue = -y
                }
            }

            gameMode = true

            (view as ImageButton).setImageResource(R.drawable.baseline_videogame_asset_off_24)

        } else {
            findViewById<Overlay>(R.id.overlay_view).visibility = View.VISIBLE
            findViewById<ImageButton>(R.id.btn_play).visibility = View.VISIBLE
            findViewById<ImageButton>(R.id.btn_bake).visibility = View.VISIBLE
            findViewById<ControllerView>(R.id.controller_y_axis).visibility = View.INVISIBLE
            findViewById<ControllerView>(R.id.controller_x_axis).visibility = View.INVISIBLE

            gameMode = false
            (view as ImageButton).setImageResource(R.drawable.baseline_videogame_asset_24)
        }
    }


}
