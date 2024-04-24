package theboyz.tkc.ip;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public interface ImagePreprocessorElement {
    public Mat execute(Mat image);
}



