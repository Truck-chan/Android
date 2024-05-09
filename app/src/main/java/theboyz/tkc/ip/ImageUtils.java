package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


class Contour
{
    MatOfPoint contourPoints;
    List<Integer> hierarchy;

    Contour(MatOfPoint contourPoints, List<Integer> hierarchy)
    {
        this.contourPoints = contourPoints;
        this.hierarchy = hierarchy;
    }
}


public class ImageUtils {


    private static final String TAG = "ImageUtils";

    static public boolean isBinaryImage(Mat image)
    {
        if (image.channels() != 1) return false;
        int imageWidth = image.width();
        int imageHeight = image.height();

        for (int row = 0; row < image.height(); row++)
        {
            for (int column = 0; column < image.width(); column++)
            {
                if (image.get(row, column)[0] != 0 && image.get(row, column)[0] != 255) return false;
            }
        }
        return true;
    }

    // image is assumed to be binary, so it will throught error if it wasn't
    static public ArrayList<Contour> findContours(Mat image, Mat out){

        if (!ImageUtils.isBinaryImage(image))
            return new ArrayList<>();


        image.copyTo(out);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(out, contours, i, new Scalar(0, 255, 0), 2);
        }

        ArrayList<Contour> contoursList = new ArrayList<>();

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contourPoints = contours.get(i);
            List<Integer> hierarchyRow = new ArrayList<>();

            for (int j = 0; j < 4; j++)  hierarchyRow.add((int) hierarchy.get(0,i)[j]);

            contoursList.add(new Contour(contourPoints, hierarchyRow));
        }

        return contoursList;
    }

    public static Mat makeGrayImageOutOfContours(int width, int height, ArrayList<Contour> contours)
    {
        ArrayList<MatOfPoint> originalContours = new ArrayList<>();

        for (Contour contour : contours)
        {
            originalContours.add(contour.contourPoints);
        }

        Mat image = new Mat(height, width, CvType.CV_8UC1, new Scalar(0));
        Imgproc.drawContours(image, originalContours, -1, new Scalar(255), 2);

        return image.clone();
    }

    public static void showImage(Mat image, String title) {
        //HighGui.imshow(title, image);
        //System.out.println(image.size());
        Log.i(TAG, "showImage: " + image.size());
        //HighGui.waitKey();
    }

    public static void putTextOnScreen(Mat image, ArrayList<String> text, ArrayList<Point> points)
    {
        for (int i = 0; i < points.size(); i++)
        {
            Imgproc.putText(image, text.get(i), points.get(i), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new org.opencv.core.Scalar(255, 0, 0), 2);
        }

        //HighGui.imshow("Title", image);
        //HighGui.waitKey();
    }
}
