package theboyz.tkc.ip;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class AdjustGamma implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        double gamma = GlobalParameters.GAMMA_VALUE;
        byte[] table = new byte[256];
        for (int i = 0; i < 256; i++) {
            double newValue = Math.pow(i / 255.0, 1.0 / gamma) * 255.0;
            table[i] = (byte) newValue;
        }

        // Convert lookup table to Mat
        Mat lut = new Mat(1, 256, CvType.CV_8U);
        lut.put(0, 0, table);

        // Apply gamma correction using LUT
        Mat correctedImage = new Mat();
        Core.LUT(image, lut, correctedImage);

        return correctedImage;
    }
}
