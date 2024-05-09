package theboyz.tkc.ip.preprocessing.components;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.ImagePreprocessorElement;

public class ConvertToGray implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        Mat grayscale = new Mat();
        Imgproc.cvtColor(image, grayscale, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
        return grayscale;
    }
}
