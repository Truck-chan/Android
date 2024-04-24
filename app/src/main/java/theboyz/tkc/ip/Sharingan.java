package theboyz.tkc.ip;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;

public class Sharingan {

    private Mat currentImage;
    public Mat mapImage;
    private final ImagePreprocessor preprocessor = new ImagePreprocessor();
    private final MapMaker mapMaker = new MapMaker();
    private GraphConnector graphConnector;

    public Sharingan()
    {
    }

    public void loadImage(String path)
    {
        currentImage = Imgcodecs.imread(path);
        ImageUtils.showImage(currentImage, "Image");
    }

    public void loadImage(Mat image)
    {
        currentImage = image.clone();
    }

    public void addPreprocessorComponent(ImagePreprocessorElement element)
    {
        preprocessor.addComponent(element);
    }

    public void startPreprocessing()
    {
        currentImage = preprocessor.preprocess(currentImage);
    }

    public Mat getCurrentImage(){return currentImage;}

    public void analyseMap()
    {
        mapMaker.setMapImage(currentImage);
        ArrayList<Contour> contours = mapMaker.generateContours();
        mapMaker.generateCriticalPoints(contours);
        mapImage = mapMaker.drawCriticalPointsOnMap();
        mapMaker.printContours();
    }

    public void connectContours()
    {
        graphConnector = new GraphConnector(mapMaker.graphs, mapMaker.intersectionLines);
        graphConnector.connectTwoContours();
    }


}
