package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

import theboyz.tkc.ip.preprocessors.AdjustGamma;
import theboyz.tkc.ip.preprocessors.ClosingOperation;
import theboyz.tkc.ip.preprocessors.carfinder.ContrastStretch;
import theboyz.tkc.ip.preprocessors.GaussianFilter;
import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.utils.structs.Line;

public class CarTracker {
    private final ImagePreprocessor preprocessor = new ImagePreprocessor();
    private final Mat currentFrame = new Mat();
    private Line carCoordinates = new Line(new Point(), new Point(), 0 , 0);
    private final ArrayList<Mat> debuggingImages = new ArrayList<>();
    private Mat redObjects = new Mat();
    private Mat greenObjects = new Mat();

    public Mat trackMask = new Mat();

    private Mat preprocessChannel(Mat channel)
    {
        preprocessor.clearComponents();
        preprocessor.addComponent(new ContrastStretch());
//        preprocessor.addComponent(new HistogramEqualization());
        preprocessor.preprocess(channel);
        return preprocessor.finalPreprocessedImage();
    }
    private void preprocessFrame()
    {
        debuggingImages.add(currentFrame);

        preprocessor.clearComponents();
        preprocessor.addComponent(new AdjustGamma());
        preprocessor.addComponent(new GaussianFilter());
        preprocessor.preprocess(currentFrame);
        Mat img = preprocessor.finalPreprocessedImage().clone();

        List<Mat> channels = new ArrayList<>();
        Core.split(img, channels);

        for (int i = 0; i < 3; i++)
        {
            channels.set(i, preprocessChannel(channels.get(i)));
        }

        Core.merge(channels, img);

        debuggingImages.add(img);

        img.copyTo(currentFrame);
    }

    private Point findCenterPoints(Mat squareImage)
    {
        Mat binaryMask = new Mat();
        Core.compare(squareImage, new Scalar(254), binaryMask, Core.CMP_GE);

        Moments moments = Imgproc.moments(binaryMask);

        return new Point(moments.get_m10() / moments.get_m00(), moments.get_m01() / moments.get_m00());
    }

    public Mat removeChannels(Mat image, double[] Threshes)
    {
        Scalar lowerThreshold = new Scalar(Threshes[0], Threshes[2], Threshes[4]);
        Scalar upperThreshold = new Scalar(Threshes[1], Threshes[3], Threshes[5]);

        ArrayList<Mat> channels = new ArrayList<>();
        Core.split(image, channels);

        for (int i = 0; i < 3; i++)
        {
            Core.inRange(channels.get(i), new Scalar(lowerThreshold.val[i]),
                    new Scalar(upperThreshold.val[i]), channels.get(i));
        }

        Mat threshold = new Mat();
        Mat out = new Mat();

        Mat combinedChannel = new Mat();
        Core.bitwise_and(channels.get(0), channels.get(1), combinedChannel);
        Core.bitwise_and(combinedChannel, channels.get(2), threshold);
        Core.bitwise_and(threshold, trackMask, out);

        return out;
    }

    public void segment(Mat image) {
        double[] RedThreshes = GlobalParameters.RED_THRESHES;
        double[] GreenThreshes = GlobalParameters.GREEN_THRESHES;

        redObjects = image.clone();
        greenObjects = image.clone();

        redObjects   = removeChannels(redObjects,RedThreshes);
        greenObjects = removeChannels(greenObjects,GreenThreshes);

        preprocessor.clearComponents();
        preprocessor.addComponent(new ClosingOperation());

        redObjects = preprocessor.preprocess(redObjects);
        greenObjects = preprocessor.preprocess(greenObjects);


        debuggingImages.add(redObjects);
        debuggingImages.add(greenObjects);
    }

    public ArrayList<Mat> getDebuggingImages() {return debuggingImages;}

    public Line  getCarInformation(){return carCoordinates;}
    private void changeCarInformation()
    {
        Point x = findCenterPoints(redObjects);
        Point y = findCenterPoints(greenObjects);
        Line line = new Line(x,y, 0,0);

        Mat lineOnImage = currentFrame.clone();
        this.carCoordinates = line;
        Imgproc.line(lineOnImage, line.start, line.end, new Scalar(255,255,0), 2);
        debuggingImages.add(lineOnImage);
    }

    public void setCurrentFrame(Mat frame){frame.copyTo(currentFrame);}


    public void findCar()
    {
        debuggingImages.clear();

        if (currentFrame.empty())
        {
            Log.i("Error", "findCar: currentFrame is empty");
            return;
        }

        preprocessFrame();
        segment(currentFrame);
        changeCarInformation();
    }



    public void drawCarOnFrame(Mat frame)
    {
        if (carCoordinates == null)
            return;
        Imgproc.line(frame, carCoordinates.start, carCoordinates.end, new Scalar(255,255,0), 2, Imgproc.LINE_AA);
    }
}
