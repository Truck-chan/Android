package theboyz.tkc.ip.preprocessors;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class GaussianFilter implements ImagePreprocessorElement {
    @Override
    public Mat execute(Mat image) {
        Mat filteredImage = new Mat();
        Imgproc.GaussianBlur(image, filteredImage, new Size(7, 7), 3);

        return filteredImage;
    }
}
