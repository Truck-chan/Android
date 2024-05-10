package theboyz.tkc.ip.preprocessors;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Erosion implements ImagePreprocessorElement {

    @Override
    public Mat execute(Mat image) {

        int kernelSize = 7;
        // Create a structuring element (kernel) for morphological operations
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, kernelSize));

        // Apply morphological closing
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_ERODE, kernel);

        return image;

    }
}
