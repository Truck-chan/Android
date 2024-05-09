package theboyz.tkc.ip.preprocessing.components;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.ImagePreprocessorElement;

public class ClosingOperation implements ImagePreprocessorElement {
    @Override
    public Mat execute(Mat image) {
        int kernelSize = 9;
        // Create a structuring element (kernel) for morphological operations
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, kernelSize));

        // Apply morphological closing
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel);

        return image;
    }
}