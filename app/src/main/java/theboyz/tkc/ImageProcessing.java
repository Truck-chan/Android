package theboyz.tkc;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.preprocessing.components.AdjustGamma;
import theboyz.tkc.ip.preprocessing.components.ApplyOtsuThreshold;
import theboyz.tkc.ip.preprocessing.components.ClosingOperation;
import theboyz.tkc.ip.preprocessing.components.ConvertToGray;
import theboyz.tkc.ip.GlobalParameters;
import theboyz.tkc.ip.preprocessing.components.Erosion;
import theboyz.tkc.ip.preprocessing.components.GaussianFilter;
import theboyz.tkc.ip.preprocessing.components.HistogramEnhancement;
import theboyz.tkc.ip.preprocessing.components.HomogenizeImage;
import theboyz.tkc.ip.preprocessing.components.PadImage;
import theboyz.tkc.ip.Sharingan;

import theboyz.tkc.ip.preprocessing.components.MinimumFilter;
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
//        sharingan.addPreprocessorComponent(new GaussianFilter());
//        sharingan.addPreprocessorComponent(new HistogramEnhancement());
        sharingan.addPreprocessorComponent(new ApplyOtsuThreshold());
//        sharingan.addPreprocessorComponent(new MinimumFilter());
//        sharingan.addPreprocessorComponent(new HomogenizeImage());
        sharingan.addPreprocessorComponent(new ClosingOperation());
        sharingan.addPreprocessorComponent(new Erosion());

//        sharingan.addPreprocessorComponent(new MedianFilter());



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
    public static void onGameStarted(){}

    /**
     * this will be called every time the camera sends a frame to the application
     * if you want to apply any post processing that affects every single function
     * do it here
     * call order : init -> onCameraSize -> (loop) { OnFrame }
     * */

    public static void OnFrame(Mat frame){
    }



    static float x = 40;

    public static Mat resizeTo(Mat image, Size imageSize)
    {
        if (image.size().width != imageSize.width)
        {
            int cropAmount = GlobalParameters.PADDING_AMOUNT;
            // Define the region of interest (ROI) using a Rect object
            Rect roi = new Rect(cropAmount, cropAmount, (int) imageSize.width, (int) imageSize.height);

            // Extract the ROI using the submat() method
            Mat croppedImage = new Mat(image, roi);

            return croppedImage;
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
    public static void OnPreview(int id, Mat frame){
        x += 3;
        if (x > 400){
            x = 40;
        }

        Size frameSize = frame.size();

        int offset = sharingan.preprocessedImages.size();

        if (id < offset && id >= 0)
        {
           resizeTo(sharingan.preprocessedImages.get(id), frameSize).copyTo(frame);
        }
        else if (id == offset)
        {
            resizeTo(sharingan.mapMaker.reducedNoiseImage, frameSize).copyTo(frame);
        }
        else if (id == offset + 1)
        {
            resizeTo(sharingan.mapMaker.processedImage, frameSize).copyTo(frame);
        }
        else if (id == offset + 2)
        {
            resizeTo(sharingan.mapImage, frameSize).copyTo(frame);
        }
        else
        {
            Imgproc.circle(frame, new Point(x, 150), 40, new Scalar(0, 255, 255, 255), 15);
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
        resizeTo(sharingan.getCurrentImage(), frame.size()).copyTo(frame);
    }


    /**
     * this function will be called ONCE when the "bake" button is pressed
     * its basically to calculate the track points etc..
     * call order : init -> onCameraSize -> OnFrame -> (bake button click) onBakeTrackImage
     * */
    public static void onBakeTrackImage(Mat frame){
//        send("let_me_cook{}");
        sharingan.loadImage(frame);
        sharingan.startPreprocessing();
        sharingan.analyseMap();
//        sharingan.connectContours();
    }
}
