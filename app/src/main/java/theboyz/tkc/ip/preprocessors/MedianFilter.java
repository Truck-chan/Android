package theboyz.tkc.ip.preprocessors;


import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MedianFilter implements ImagePreprocessorElement {


    @Override
    public Mat execute(Mat image) {
        Mat filteredImage = new Mat();
        Imgproc.medianBlur(image, filteredImage, 5);

        return filteredImage;
    }
}