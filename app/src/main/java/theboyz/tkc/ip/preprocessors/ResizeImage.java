package theboyz.tkc.ip.preprocessors;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ResizeImage implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        // Define the new dimensions for resizing
        int newWidth = 400;
        int newHeight = 180;

        System.out.println(image.size());

        // Create a new Mat for the resized image
        Mat resizedImage = new Mat();
        Log.i("INFO MESSAGE", "This is an info message: " + image.size().toString());

        // Resize the image
        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight));

        return resizedImage;
    }
}
