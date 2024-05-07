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

    /**
     * these two utility functions will send a commands to the arduino
     * a command string would look something like this: "hi_there{}"
     *                                              or  "hi_there{var=1}"
     * */
    public static void send(String command){
        Communicator.Companion.send(command);
    }
    public static void send(Command command){
        Communicator.Companion.send(command);
    }



    private static Sharingan sharingan;

    /**
     * this will only be called once
     * use it to initialize stuff
     * will be the first function to be called
     * */
    public static void init(){
        sharingan = new Sharingan();
        sharingan.addPreprocessorComponent(new ConvertToGray());
        sharingan.addPreprocessorComponent(new AdjustGamma());
        sharingan.addPreprocessorComponent(new ApplyOtsuThreshold());

        //fixme: Padding doesn't work for now ..
        //sharingan.addPreprocessorComponent(new PadImage());
    }

    /**
     * this will also be called once after init()
     * width, height are the size of the phone screen
     * */
    public static void onCameraSize(int width, int height){}

    /**
     * will be called when the play button is pressed
     * will be called once, and it will always come after init & onCameraSize
     * */
    public static void onGameStarted(){}

    /**
     * this will be called every time the camera sends a frame to the application
     * if you want to apply any post processing that affects every single function
     * do it here
     * call order : init -> onCameraSize -> (loop) { OnFrame }
     * */
    public static void OnFrame(Mat frame){}

    static float x = 40;

    /**
     * this will be called when the preview mode is active (the "eye" button is toggled ON)
     * call order : init -> onCameraSize -> (loop) { OnFrame -> (preview button ON ?) OnPreview }
     *
     * @param id is the number showing next to the "eye" button
     * you can think of id as the number of the preview you want to see
     *
     * @param frame is the current frame
     * */
    public static void OnPreview(int id, Mat frame){
        x += 3;
        if (x > 400){
            x = 40;
        }

        switch (id) { // example on how to view more than one preview
            case 0:
                Imgproc.circle(frame, new Point(x, 150), 40, new Scalar(255, 255, 255, 255), 15);
                break;
            case 1:
                Imgproc.circle(frame, new Point(x, 150), 40, new Scalar(255, 0, 255, 255), 15);
                break;
            case 2:
                Imgproc.circle(frame, new Point(x, 150), 40, new Scalar(255, 255, 0, 255), 15);
                break;
            default:
                Imgproc.circle(frame, new Point(x, 150), 40, new Scalar(0, 255, 255, 255), 15);
                break;
        }
    }

    /**
     * this function will only be called when the play button is pressed.
     *  call order : init -> onCameraSize -> (loop) { OnFrame -> (play button ON ?) OnGameFrame }
     *  (note that OnPreview & OnGameFrame will never be called together, only one mode
     *   can be active at a time)
     *
     * @param frame is the current frame
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
     * this function will be called ONCE when the "bake" button is pressed
     * its basically to calculate the track points etc..
     * call order : init -> onCameraSize -> OnFrame -> (bake button click) onBakeTrackImage
     * */
    public static void onBakeTrackImage(Mat frame){
        send("let_me_cook{}");
        sharingan.loadImage(frame);
        sharingan.startPreprocessing();
        sharingan.analyseMap();
        //sharingan.connectContours();
    }
}
