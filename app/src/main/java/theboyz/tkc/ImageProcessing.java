package theboyz.tkc;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;

import theboyz.tkc.ip.preprocessors.AdjustGamma;
import theboyz.tkc.ip.preprocessors.ApplyOtsuThreshold;
import theboyz.tkc.ip.preprocessors.ClosingOperation;
import theboyz.tkc.ip.preprocessors.ConvertToGray;
import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.preprocessors.Erosion;
import theboyz.tkc.ip.preprocessors.PadImage;
import theboyz.tkc.ip.Sharingan;

import uni.proj.ec.Command;


enum State
{
    BUILDING_MAP,
    PREVIEWING_CAR_DETECTION,
    RUNNING
}

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
    private static State currentState = State.BUILDING_MAP;

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
        sharingan.addPreprocessorComponent(new ClosingOperation());
        sharingan.addPreprocessorComponent(new Erosion());
        sharingan.addPreprocessorComponent(new PadImage());
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
    private static boolean lockState = false;
    public static void onGameStarted()
    {
        if (lockState) return;
        switch (currentState)
        {
            case BUILDING_MAP:
                currentState = State.PREVIEWING_CAR_DETECTION;
                sharingan.getPreprocessedImages().clear();
                sharingan.getMapMakerDebuggingImages().clear();
                break;
            case PREVIEWING_CAR_DETECTION:
                currentState = State.RUNNING;
                sharingan.getCarTrackerDebuggingImages().clear();
        }
        lockState = true;
    }

    /**
     * this will be called every time the camera sends a frame to the application
     * if you want to apply any post processing that affects every single function
     * do it here
     * call order : init -> onCameraSize -> (loop) { OnFrame }
     * */

    static int x=  0;
    public static void OnFrame(Mat frame)
    {
        if (x < 40)
        {
            x++; return;
        }

        x = 0;

        if (currentState == State.PREVIEWING_CAR_DETECTION)
        {
            printTrackBoundingBoxOnFrame(frame);
        }
    }

    public static Mat resizeTo(Mat image, Size imageSize)
    {
        if (image.size().width != imageSize.width)
        {
            int cropAmount = GlobalParameters.PADDING_AMOUNT;
            // Define the region of interest (ROI) using a Rect object
            Rect roi = new Rect(cropAmount, cropAmount, (int) imageSize.width, (int) imageSize.height);

            return new Mat(image, roi);
        }
        else
        {
            return image;
        }
    }

    /**
     * this will be called when the preview mode is active (the "eye" button is toggled ON)
     * call order : init -> onCameraSize -> (loop) { OnFrame -> (preview button ON ?) OnPreview }
     *
     * @param id is the number showing next to the "eye" button
     * you can think of id as the number of the preview you want to see
     *
     * @param frame is the current frame
     * */

    private static void previewMapMaker(int id, Mat frame)
    {
        Size frameSize = frame.size();

        ArrayList<Mat> preprocessedImages = sharingan.getPreprocessedImages();
        ArrayList<Mat> mapMakerImages =  sharingan.getMapMakerDebuggingImages();

        int preprocessingLength = preprocessedImages.size();
        int mapMakerLength = mapMakerImages.size();

        if (id < preprocessingLength)
        {
           resizeTo(preprocessedImages.get(id), frameSize).copyTo(frame);
        }
        else if (id - preprocessingLength < mapMakerLength)
        {
            resizeTo(mapMakerImages.get(id - preprocessingLength), frameSize).copyTo(frame);
        }
    }

    private static void previewTrackCar(int id, Mat frame)
    {
        Size frameSize = frame.size();

        if (id < sharingan.getCarTrackerDebuggingImages().size())
        {
            resizeTo(sharingan.getCarTrackerDebuggingImages().get(id), frameSize).copyTo(frame);
        }
    }


    public static void OnPreview(int id, Mat frame){

        if (id < 0)
            return;

        if (currentState == State.BUILDING_MAP)
        {
            previewMapMaker(id, frame);
        }
        else
        {
            previewTrackCar(id, frame);
        }
    }


    private static void printTrackBoundingBoxOnFrame(Mat frame)
    {
        sharingan.drawMapBoundingBox(frame);
    }

    /**
     * this function will only be called when the play button is pressed.
     *  call order : init -> onCameraSize -> (loop) { OnFrame -> (play button ON ?) OnGameFrame }
     *  (note that OnPreview & OnGameFrame will never be called together, only one mode
     *   can be active at a time)
     *
     * @param frame is the current frame
     * */

    static int skippedFrames = 0;

    public static void OnGameFrame(Mat frame){


        if (currentState == State.RUNNING)
            printTrackBoundingBoxOnFrame(frame);

        if (skippedFrames > 60)
        {
            skippedFrames = 0;
        }
        else
        {
            skippedFrames++;
            return;
        }

        if (currentState == State.RUNNING)
        {
            sharingan.loadImage(frame);
            try {
                sharingan.trackCar();
                sharingan.followLine();
            } catch (Exception e)
            {
                Log.i("Exception Debug", "OnGameFrame: " + e.getMessage());
            }
        }

        sharingan.drawCarLocation(frame);
    }


    /**
     * this function will be called ONCE when the "bake" button is pressed
     * its basically to calculate the track points etc..
     * call order : init -> onCameraSize -> OnFrame -> (bake button click) onBakeTrackImage
     * */
    public static void onBakeTrackImage(Mat frame){

        switch (currentState)
        {
            case BUILDING_MAP:
                sharingan.reset();
                sharingan.loadImage(frame);
                sharingan.startPreprocessing();
                sharingan.analyseMap();
                sharingan.connectContours();

                break;

            case PREVIEWING_CAR_DETECTION:
                sharingan.loadImage(frame);
                sharingan.trackCar();

        }
        lockState = false;
    }
}
