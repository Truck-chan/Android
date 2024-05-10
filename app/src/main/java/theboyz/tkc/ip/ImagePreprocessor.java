package theboyz.tkc.ip;

import org.opencv.core.Mat;
import java.util.ArrayList;

import theboyz.tkc.ip.preprocessors.ImagePreprocessorElement;

public class ImagePreprocessor {
    private final ArrayList<ImagePreprocessorElement> imagePreprocessorElements = new ArrayList<ImagePreprocessorElement>();

    private final ArrayList<Mat> preprocessedImages = new ArrayList<>();

    public ArrayList<Mat> getPreprocessedImages()
    {
        return preprocessedImages;
    }

    public Mat finalPreprocessedImage(){
        int lastIdx = preprocessedImages.size() - 1;
        return preprocessedImages.get(lastIdx).clone();
    }

    public void addComponent(ImagePreprocessorElement element)
    {
        imagePreprocessorElements.add(element);
    }

    public void clearComponents()
    {
        imagePreprocessorElements.clear();
    }
    public void reset()
    {
        preprocessedImages.clear();
    }

    public Mat preprocess(Mat image)
    {
        Mat preprocessed = image;
        preprocessedImages.clear();

        for (ImagePreprocessorElement element : imagePreprocessorElements)
        {
            preprocessed = element.execute(preprocessed);
            preprocessedImages.add(preprocessed.clone());
        }

        return preprocessed;
    }

}
