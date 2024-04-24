package theboyz.tkc.ip;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ApplyOtsuThreshold implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        Mat binary = new Mat();
        Imgproc.threshold(image, binary, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return binary;
    }
}
