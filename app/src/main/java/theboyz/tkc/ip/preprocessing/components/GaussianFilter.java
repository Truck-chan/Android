package theboyz.tkc.ip.preprocessing.components;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.ImagePreprocessorElement;

public class GaussianFilter implements ImagePreprocessorElement {
    @Override
    public Mat execute(Mat image) {
        Mat filteredImage = new Mat();
        Imgproc.GaussianBlur(image, filteredImage, new Size(3, 3), 3);

        return filteredImage;
    }
}
