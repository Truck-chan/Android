package theboyz.tkc.ip;

import org.opencv.core.Mat;

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
        mapMaker.reset();
        mapMaker.setMapImage(preprocessor.finalPreprocessedImage());
        mapMaker.generateTrackContours();
        mapMaker.generateTurnsPoints();
        mapMaker.drawCriticalPointsOnImage(currentFrame);
    }

    public void connectContours()
    {
        graphConnector.setParameters(mapMaker.contoursGraph, mapMaker.intersectionLines);
        graphConnector.connectContours();
    }

    public void trackCar()
    {
        carTracker.setCurrentFrame(currentFrame);
        carTracker.findCar();
    }

    public void followLine()
    {
        lineFollower.setTrack(graphConnector.lapLine);
        lineFollower.followLine(carTracker.getCarInformation());
    }
}
