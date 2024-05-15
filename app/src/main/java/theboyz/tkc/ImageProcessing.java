package theboyz.tkc;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import theboyz.tkc.ip.preprocessors.AdjustGamma;
import theboyz.tkc.ip.preprocessors.ApplyOtsuThreshold;
import theboyz.tkc.ip.preprocessors.ClosingOperation;
import theboyz.tkc.ip.preprocessors.ConvertToGray;
import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.preprocessors.Erosion;
import theboyz.tkc.ip.preprocessors.PadImage;
import theboyz.tkc.ip.Sharingan;

import theboyz.tkc.ui.vec2;
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


    private static final Sharingan sharingan;

    static {
        // will only be called once ..
        sharingan = new Sharingan();
        sharingan.addPreprocessorComponent(new ConvertToGray());
        sharingan.addPreprocessorComponent(new AdjustGamma());
        sharingan.addPreprocessorComponent(new ApplyOtsuThreshold());
        sharingan.addPreprocessorComponent(new ClosingOperation());
        sharingan.addPreprocessorComponent(new Erosion());
        sharingan.addPreprocessorComponent(new PadImage());
    }
    private static State currentState = State.BUILDING_MAP;

    /**
     * this will only be called once
     * use it to initialize stuff
     * will be the first function to be called
     * */
    public static void init(){

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

    private static Mat carMaskingFrame = new Mat();
    private static ArrayList<Point> maskingFramePoints = new ArrayList<>();


    private static Point getPointFromVec2(vec2 p, Size frameSize)
    {
        return new Point(
                p.x * frameSize.width,
                p.y * frameSize.height
        );
    }
    private static void makeNewMask(Size frameSize, int frameType)
    {
        List<Point> points = new ArrayList<>();
        points.add(getPointFromVec2(GlobalParameters.clipPoints[0], frameSize));
        points.add(getPointFromVec2(GlobalParameters.clipPoints[1], frameSize));
        points.add(getPointFromVec2(GlobalParameters.clipPoints[2], frameSize));
        points.add(getPointFromVec2(GlobalParameters.clipPoints[3], frameSize));

        maskingFramePoints.clear();
        maskingFramePoints.addAll(points);

        MatOfPoint matPt = new MatOfPoint();
        matPt.fromList(points);
        List<MatOfPoint> ppt = new ArrayList<MatOfPoint>();
        ppt.add(matPt);

        Log.i("Variable Debug", "makeNewMask: =============================");
        for (Point p : points)
        {
            Log.i("Variable Debug", "makeNewMask: point = " + p.toString());
        }

        carMaskingFrame.setTo(new Scalar(0, 0, 0));

        Size maskSize = new Size(frameSize.width, frameSize.height);
        Mat temp = new Mat(maskSize, frameType);
        temp.setTo(new Scalar(0,0,0));
        Imgproc.fillPoly(temp,
                ppt,
                new Scalar(255, 255, 255),
                Imgproc.LINE_8,
                0,
                new Point(0,0) );
        Imgproc.cvtColor(temp, carMaskingFrame, Imgproc.COLOR_RGBA2GRAY);
    }

    static private boolean maskPointsChanged(Size frameSize)
    {
        for (int i = 0; i < 4; i++)
        {
            if (
                    GlobalParameters.clipPoints[i].x * frameSize.width != maskingFramePoints.get(i).x
                            ||
                    GlobalParameters.clipPoints[i].y * frameSize.height != maskingFramePoints.get(i).y
            )
                return true;
        }
        return false;
    }

    static int x = 0;
    static long time = -1;
    public static void OnFrame(Mat frame)
    {
//        if (System.currentTimeMillis() - time > 500){
//            if (x == 0){
//                send("kemo");
//                x = 1;
//            }else{
//                x = 0;
//                send("kemof");
//            }
//            time = System.currentTimeMillis();
//            Log.i("Spam_TEST", "OnFrame: Spamming");
//        }
        // draw the bounding box for the image
        if (currentState != State.BUILDING_MAP)
        {
            if (maskingFramePoints.isEmpty() || maskPointsChanged(frame.size()))
            {
                try {
                    makeNewMask(frame.size(), frame.type());
                }
                catch (Exception e)
                {
                    Log.e("Exception Debug", "OnFrame: " , e);
                }
            }
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


        if (false){

            frame.setTo(new Scalar(0));
            makeNewMask(frame.size() , frame.type());
            carMaskingFrame.copyTo(frame);
            return;
        }
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

        sharingan.drawCarLocation(frame);

        if (skippedFrames > 15)
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
                sharingan.trackCar(carMaskingFrame);
                sharingan.followLine();
            } catch (Exception e)
            {
                Log.i("Exception Debug", "OnGameFrame: " + e.getMessage());
            }
        }
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
                sharingan.trackCar(carMaskingFrame);

        }
        lockState = false;
    }
}
