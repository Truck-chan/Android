package theboyz.tkc.ip.preprocessing.components;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import theboyz.tkc.ip.GlobalParameters;
import theboyz.tkc.ip.ImagePreprocessorElement;

public class MinimumFilter implements ImagePreprocessorElement {
    @Override
    public Mat execute(Mat image) {
            int kernelSize = GlobalParameters.KERNEL_SIZE;

            // Create a structuring element (kernel) for the erosion
            Mat kernel = Mat.ones(kernelSize, kernelSize, CvType.CV_8U);

            // Perform the erosion operation
            Imgproc.erode(image, image, kernel);

            // Return the resulting image
            return image;
        }
}