package theboyz.tkc.ip.preprocessing.components;


import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.ImagePreprocessorElement;

public class MedianFilter implements ImagePreprocessorElement {


    @Override
    public Mat execute(Mat image) {
        Mat filteredImage = new Mat();
        Imgproc.medianBlur(image, filteredImage, 5);

        return filteredImage;
    }
}