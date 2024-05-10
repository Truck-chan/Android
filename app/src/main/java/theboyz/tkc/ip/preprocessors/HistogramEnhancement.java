package theboyz.tkc.ip.preprocessors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class HistogramEnhancement implements ImagePreprocessorElement {


    @Override
    public Mat execute(Mat image) {
        Mat out = new Mat();
        // Calculate minimum and maximum values
        Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(image);

        int a = 0, b = 255;
        int c = (int)minMaxResult.minVal;
        int d = (int)minMaxResult.maxVal;

        Mat constantMat = Mat.ones(image.size(), image.type());

        // out = image - c
        constantMat.setTo(new Scalar(-c));
        Core.add(image, constantMat, out);

        int mult = (b - a) / (d - c);
        constantMat.setTo(new Scalar(mult));
        Core.multiply(out, constantMat, out);

        constantMat.setTo( new Scalar(a));
        Core.add(out, constantMat, out);

        return out;
    }
}
