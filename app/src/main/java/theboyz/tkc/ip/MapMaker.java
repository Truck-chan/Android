package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import theboyz.tkc.ip.Contour;
import theboyz.tkc.ip.GlobalParameters;
import theboyz.tkc.ip.ImageUtils;
import theboyz.tkc.ip.Line;


public class MapMaker {
    private Mat mapImage = new Mat();

    public Mat processedImage = new Mat();

    public ArrayList<ArrayList<Point>> graphs = new ArrayList<>();
    public ArrayList<Line> intersectionLines = new ArrayList<>();

    private ArrayList<Contour> eliminateNoisyContours(int imageArea, ArrayList<Contour> contours)
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

    private void sortCriticalPoints(ArrayList<Point> points)
    {
        Comparator<Point> comparator = new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                if (p1.x != p2.x) return Double.compare(p2.x, p1.x);
                return Double.compare(p2.y, p1.y);
            }
        };

        Collections.sort(points, comparator);
    }

    private int getLastParentWidth(ArrayList<Contour> contours, int idx)
    {
        Contour parent = contours.get(idx);

        while(parent.hierarchy.get(3) != -1)
        {
            parent = contours.get(parent.hierarchy.get(3));
        }


        Rect boundingRect = Imgproc.boundingRect(parent.contourPoints);
        return boundingRect.width;
    }

    private ArrayList<Contour> getLeafContours(ArrayList<Contour> contours, int imageWidth, int imageHeight)
    {
        ArrayList<Contour> filteredContours = new ArrayList<>();

        Log.i("Get Leaf Contours:", "====================");
        for (Contour contour : contours)
        {
            boolean hasChild = contour.hierarchy.get(2) != -1;

            if (!hasChild)
            {
                int parentIdx = contour.hierarchy.get(3);
                if (parentIdx != -1)
                {
                    if (getLastParentWidth(contours, parentIdx) > 0.4 * imageWidth) {

//                        Log.i("Get Leaf Contours:", "Filtered Area = " + Imgproc.contourArea(contour.contourPoints));
                        filteredContours.add(contour);
                    }
                }
            }
        }

        return eliminateNoisyContours(imageHeight * imageWidth , filteredContours);
    }

    private int euclideanDistance(Point p1, Point p2)
    {
        return (int) (Math.pow (p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private int removePointFromContour(Point point)
    {

        for (int i = 0; i < graphs.size(); i++)
        {
            if (graphs.get(i).contains(point)) {
                graphs.get(i).remove(point);
                return i;
            }
        }
        return -1;
    }

    private ArrayList<Point> eliminateVeryNearPoints(ArrayList<Point> points)
    {
        sortCriticalPoints(points);
        ArrayList<Point> filteredPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++)
        {
            boolean isValid = true;
            for (int j = i + 1; j < points.size(); j++)
            {
                if (euclideanDistance(points.get(i), points.get(j)) <= GlobalParameters.MIN_SPACING)
                {
                    int startContour = removePointFromContour(points.get(i));
                    int endContour = removePointFromContour(points.get(j));

                    if (startContour != endContour) {
                        System.out.println(startContour + " " + endContour);

                        intersectionLines.add(new Line(points.get(i), points.get(j), startContour, endContour));
                    }
                    isValid = false;
                    break;
                }
            }
            if (isValid)
                filteredPoints.add(points.get(i));

        }
        return filteredPoints;
    }

    public void setMapImage(Mat image)
    {
        if (ImageUtils.isBinaryImage(image))
            mapImage = image.clone();
    }

    public Mat reducedNoiseImage;
    public ArrayList<Contour> generateContours()
    {
        int imageWidth = mapImage.width();
        int imageHeight = mapImage.height();
        int imageArea = imageWidth * imageHeight;

        ArrayList<Contour> contours = ImageUtils.findContours(mapImage, new Mat());
        reducedNoiseImage = ImageUtils.makeGrayImageOutOfContours(imageWidth, imageHeight, contours);
        processedImage = reducedNoiseImage;

        ArrayList<Contour> filteredContours = eliminateNoisyContours(imageArea, contours);
        reducedNoiseImage = ImageUtils.makeGrayImageOutOfContours(imageWidth, imageHeight, filteredContours);



        contours = ImageUtils.findContours(reducedNoiseImage, new Mat());

        filteredContours = getLeafContours(contours, imageWidth, imageHeight);


        processedImage = ImageUtils.makeGrayImageOutOfContours(imageWidth, imageHeight, filteredContours);

        return filteredContours;
    }

    public void generateCriticalPoints(ArrayList<Contour> contours)
    {
        int j = 0;
        ArrayList<Point> criticalPoints = new ArrayList<>();

        for (Contour contour : contours)
        {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.contourPoints.toArray());
            double epsilon = GlobalParameters.PERIMETER_PERCENTAGE * Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);

            graphs.add(new ArrayList<Point>());

            for (int i = 0; i < approxCurve.rows(); i++) {
                Point point = new Point(approxCurve.get(i, 0)[0], approxCurve.get(i, 0)[1]);
                Log.i("CRITICAL POINT:" , "point = " + point.toString());
                criticalPoints.add(point);
                graphs.get(j).add(point);
            }

            j++;
        }

        eliminateVeryNearPoints(criticalPoints);
    }

    public Mat drawCriticalPointsOnMap()
    {
        Mat rgbImage = new Mat();
        Imgproc.cvtColor(mapImage, rgbImage, Imgproc.COLOR_GRAY2RGB);

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();


        for (ArrayList<Point> contour : graphs) {
            for (Point point : contour) {
                Scalar color = new Scalar(0, 0, 255); // Red color
                int radius = 5; // Radius of the circle
                Imgproc.circle(rgbImage, point, radius, color, -1); // -1 indicates a filled circle

                points.add(point);
                text.add((point.x) + " " + point.y);
            }

            for (Line line : intersectionLines) {
                Imgproc.line(rgbImage, line.start, line.end, new Scalar(255,0,0), 2);
            }
        }
        ImageUtils.putTextOnScreen(rgbImage, text, points);
        return rgbImage;
    }

    public void printContours()
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

