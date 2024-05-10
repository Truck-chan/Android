package theboyz.tkc.ip.utils;

import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;





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

    /**
     * ============================================================================================
     * ********************************** CONTOURS UTILS ******************************************
     * ============================================================================================
     */

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

    static public int getContourOfPoint(ArrayList<ArrayList<Point>> contours, Point point)
    {
        for (int i = 0; i < contours.size(); i++)
        {
            if (contours.get(i).contains(point)) {
                return i;
            }
        }
        return -1;
    }
    static public void removePointFromContour(ArrayList<Point> contour, Point point)
    {
        contour.remove(point);
    }

    static public int getLastContourParentWidth(ArrayList<Contour> contours, int idx)
    {
        Contour parent = contours.get(idx);

        while(parent.hierarchy.get(3) != -1)
        {
            parent = contours.get(parent.hierarchy.get(3));
        }


        Rect boundingRect = Imgproc.boundingRect(parent.contourPoints);
        return boundingRect.width;
    }

    static public ArrayList<Contour> limitedContoursArea(ArrayList<Contour> contours, int imageArea)
    {
        ArrayList<Contour> filteredContours = new ArrayList<>();

        double minArea = GlobalParameters.MIN_AREA_PERCENTAGE * imageArea;
        double maxArea = GlobalParameters.MAX_AREA_PERCENTAGE * imageArea;

        for (Contour contour : contours)
        {
            double area = Imgproc.contourArea(contour.contourPoints);

            if (area > maxArea || area < minArea)
                continue;

            filteredContours.add(contour);
        }

        return filteredContours;
    }

    static public ArrayList<Contour> getLeafContours(ArrayList<Contour> contours, int imageWidth)
    {
        ArrayList<Contour> filteredContours = new ArrayList<>();

        for (Contour contour : contours)
        {
            int childIdx = contour.hierarchy.get(2);
            int parentIdx = contour.hierarchy.get(3);

            boolean hasChild = childIdx != -1;
            boolean hasParent = parentIdx != -1;
            boolean wideParent = hasParent && getLastContourParentWidth(contours, parentIdx) > 0.4 * imageWidth;

            if (!hasChild && hasParent && wideParent)
            {
                filteredContours.add(contour);
            }
        }

        return filteredContours;
    }

    /**
     * ============================================================================================
     * ******************************* IMAGE MAKERS UTILS *****************************************
     * ============================================================================================
     */
    public static Mat makeGrayImageOutOfContours(ArrayList<Contour> contours, int width, int height)
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

    public static void putTextOnScreen(Mat image, ArrayList<String> text, ArrayList<Point> points)
    {
        for (int i = 0; i < points.size(); i++)
        {
            Imgproc.putText(image, text.get(i), points.get(i), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new org.opencv.core.Scalar(255, 0, 0), 2);
        }
    }

    /**
     * ============================================================================================
     * ******************************* PRINTING UTILS *********************************************
     * ============================================================================================
     */
    public static void printContours(ArrayList<ArrayList<Point>> graphs)
    {
        int i = 0;
        for(ArrayList<Point> contour : graphs)
        {
            System.out.println("Contour " + i + ":-");
            for (Point point : contour)
            {
                System.out.println("\t" +(point.x) + " " + point.y);
            }
            System.out.println("=========================================");
        }
    }
}
