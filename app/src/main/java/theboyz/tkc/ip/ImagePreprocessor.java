package theboyz.tkc.ip;

import org.opencv.core.Mat;
import java.util.ArrayList;

public class ImagePreprocessor {
    private ArrayList<ImagePreprocessorElement> imagePreprocessorElements = new ArrayList<ImagePreprocessorElement>();

    public ImagePreprocessor() {}

    public void addComponent(ImagePreprocessorElement element)
    {
        imagePreprocessorElements.add(element);
    }

    public Mat preprocess(Mat image, ArrayList<Mat> out)
    {
        Mat preprocessed = image;
        for (ImagePreprocessorElement element : imagePreprocessorElements)
        {
            preprocessed = element.execute(preprocessed);
            Mat cloned = new Mat();
            preprocessed.copyTo(cloned);
            out.add(cloned);
        }
        return preprocessed;
    }

}
