package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import theboyz.tkc.ip.preprocessors.ImagePreprocessorElement;

public class Sharingan {

    private Mat currentFrame;
    private final ImagePreprocessor preprocessor = new ImagePreprocessor();
    private final MapMaker mapMaker = new MapMaker();
    private final GraphConnector graphConnector = new GraphConnector();
    public final CarTracker carTracker = new CarTracker();
    public final LineFollower lineFollower = new LineFollower();
    public void loadImage(Mat image)
    {
        currentFrame = image.clone();
        image.copyTo(currentFrame);
    }


    public void drawMapBoundingBox(Mat frame)
    {
        if (mapMaker.borderRect.empty())
            return;
        Rect border = mapMaker.border;
        Point p1 = new Point(border.x, border.y);
        Point p2 = new Point(border.x + border.width, border.y);
        Point p4 = new Point(border.x, border.y + border.height);
        Point p3 = new Point(border.x + border.width, border.y + border.height);

        Imgproc.line(frame, p1, p2, new Scalar(255,0,0), 2,Imgproc.LINE_AA);
        Imgproc.line(frame, p2, p3, new Scalar(255,0,0), 2,Imgproc.LINE_AA);
        Imgproc.line(frame, p3, p4, new Scalar(255,0,0), 2,Imgproc.LINE_AA);
        Imgproc.line(frame, p4, p1, new Scalar(255,0,0), 2,Imgproc.LINE_AA);
    }

    public void addPreprocessorComponent(ImagePreprocessorElement element)
    {
        preprocessor.addComponent(element);
    }

    public void reset() {
        preprocessor.reset();
        mapMaker.reset();
    }
    public ArrayList<Mat> getPreprocessedImages() {return preprocessor.getPreprocessedImages();}
    public ArrayList<Mat> getMapMakerDebuggingImages() {return mapMaker.getDebuggingImages();}
    public ArrayList<Mat> getCarTrackerDebuggingImages() {return carTracker.getDebuggingImages();}


    public void startPreprocessing()
    {
        preprocessor.preprocess(currentFrame);
    }

    public void analyseMap()
    {
        try {
            mapMaker.frameSize = currentFrame.size();
            mapMaker.reset();
            mapMaker.setMapImage(preprocessor.finalPreprocessedImage());
            mapMaker.generateTrackContours();
            mapMaker.generateTurnsPoints();
            mapMaker.drawCriticalPointsOnImage(currentFrame);
            carTracker.trackMask = mapMaker.borderRect;

        }catch (Exception e)
        {
            Log.i("Exception Debug", "analyseMap: " + e.getMessage());
        }
    }

    public void connectContours()
    {
        try {
            graphConnector.setParameters(mapMaker.contoursGraph, mapMaker.intersectionLines);
            graphConnector.connectContours();
        } catch (Exception e)
        {
            Log.i("Exception Debug", "connectContours: " + e.getMessage());
        }
    }

    public void trackCar()
    {
        carTracker.setCurrentFrame(currentFrame);
        carTracker.findCar();
    }


    public void drawCarLocation(Mat frame)
    {
        carTracker.drawCarOnFrame(frame);
        lineFollower.putTargetPoint(frame);
    }

    public void followLine()
    {
        Log.i("Last Step", "followLine: entered");
        lineFollower.setTrack(graphConnector.lapLine);
        lineFollower.followLine(carTracker.getCarInformation());
    }
}
