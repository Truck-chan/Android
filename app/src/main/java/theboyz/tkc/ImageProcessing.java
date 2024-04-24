package theboyz.tkc;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import kotlin.NotImplementedError;
import theboyz.tkc.ip.AdjustGamma;
import theboyz.tkc.ip.ApplyOtsuThreshold;
import theboyz.tkc.ip.ConvertToGray;
import theboyz.tkc.ip.PadImage;
import theboyz.tkc.ip.Sharingan;
import uni.proj.ec.Command;

public class ImageProcessing {

    public static void send(String command){
        //Communicator.Companion.send(command);
    }

    public static void send(Command command){
       // Communicator.Companion.send(command);
    }

    /**
     * will be called only once after the connection with arduino has been established
     * this will run on the UI thread, so you better not make it performance intensive
     * */

    private static Sharingan sharingan;
    public static void init(){
        sharingan = new Sharingan();
        sharingan.addPreprocessorComponent(new ConvertToGray());
        sharingan.addPreprocessorComponent(new AdjustGamma());
        sharingan.addPreprocessorComponent(new ApplyOtsuThreshold());

        //fixme: Padding doesn't work for now ..
        //sharingan.addPreprocessorComponent(new PadImage());
    }

    /**
     * this will be called only once after the camera has been obtained
     * this will run on the UI thread, so you better not make it performance intensive
     * */
    public static void onCameraSize(int width, int height){}

    /**
     * read the name :)
     * this will run on the UI thread, so you better not make it performance intensive
     * */
    public static void onGameStarted(){}

    /**
     * this will be called every frame even if the game is not running
     * if you want to do any preprocessing on the frame before sending
     * it to the actual track processing function do it here, the output
     * of this function will be displayed on the screen or sent to OnGameFrame
     * then displayed on the screen, depending on the mode.
     *
     * this will run on the UI thread, so you better not make it performance intensive
     * */

    static float x = 40;
    public static void OnFrame(Mat frame){}


    public static void OnPreview(int id, Mat frame){
        x += 3;
        if (x > 400){
            x = 40;
        }

        Imgproc.circle(frame, new Point(x , 150) , 40 , new Scalar(1,1,1,1) , 15);
    }

    /**
     * same as OnFrame, but it will only be called when the game is running
     *
     * this will run on the UI thread, so you better not make it performance intensive
     * */
    public static void OnGameFrame(Mat frame){
        //sharingan.loadImage(frame);
        //sharingan.startPreprocessing();
//        sharingan.loadImage(frame);
//        sharingan.startPreprocessing();
//        sharingan.analyseMap();
        sharingan.mapImage.copyTo(frame);
    }


    /**
     * this function will be called before the game starts, "frame" should contain an image
     * of the empty track to pre-process it before the game starts.
     *
     * this will run on its own thread, so it has no problem being performance intensive
     * */
    public static void onBakeTrackImage(Mat frame){
        send("let_me_cook{}");
        sharingan.loadImage(frame);
        sharingan.startPreprocessing();
        sharingan.analyseMap();
        //sharingan.connectContours();
    }
}
