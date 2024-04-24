package theboyz.tkc.ip;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class PadImage implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        Mat paddedImage = new Mat();
        int amount = GlobalParameters.PADDING_AMOUNT;
        int constantValue = GlobalParameters.PADDING_CONSTANT;

        Core.copyMakeBorder(image,
                paddedImage,
                amount,
                amount,
                amount,
                amount,
                Core.BORDER_CONSTANT,
                new Scalar(constantValue)
        );
        return paddedImage;
    }
}
