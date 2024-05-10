package theboyz.tkc.ip.preprocessors.carfinder;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.preprocessors.ImagePreprocessorElement;

public class HistogramEqualization implements ImagePreprocessorElement {

    @Override
    public Mat execute(Mat image) {
        Mat out = new Mat();
        Imgproc.equalizeHist(image, out);
        return out;
    }
}
