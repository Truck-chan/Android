package theboyz.tkc.ip.preprocessors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import theboyz.tkc.ip.utils.GlobalParameters;

public class PadImage implements ImagePreprocessorElement {
    public Mat execute(Mat image) {
        return padImage(image, GlobalParameters.PADDING_AMOUNT);
    }

    public static Mat padImage(Mat image, int amount)
    {
        Mat paddedImage = new Mat();
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
