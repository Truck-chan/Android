package theboyz.tkc.ip;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MapMaker {
    private Mat mapImage = new Mat();

    private Mat processedImage = new Mat();

    public ArrayList<ArrayList<Point>> graphs = new ArrayList<>();
    public ArrayList<Line> intersectionLines = new ArrayList<>();

    private ArrayList<Contour> eliminateNoisyContours(int imageArea, ArrayList<Contour> contours)
    {
        ArrayList<Contour> filteredContours = new ArrayList<>();
        for (Contour contour : contours)
        {
            double area = Imgproc.contourArea(contour.contourPoints);

            if (area > GlobalParameters.MAX_AREA_PERCENTAGE * imageArea || area < GlobalParameters.MIN_AREA_PERCENTAGE * imageArea)
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

    private ArrayList<Contour> getLeafContours(ArrayList<Contour> contours)
    {
        ArrayList<Contour> filteredContours = new ArrayList<>();
        for (Contour contour : contours)
        {
            boolean hasChild = contour.hierarchy.get(2) != -1;
            if (!hasChild) filteredContours.add(contour);
        }
        return filteredContours;
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
        for (int i = 1; i <= points.size(); i++)
        {
            if (i == points.size() || euclideanDistance(points.get(i), points.get(i - 1)) > GlobalParameters.MIN_SPACING)
            {
                filteredPoints.add(points.get(i - 1));
            }
            else
            {
                int startContour = removePointFromContour(points.get(i));
                int endContour = removePointFromContour(points.get(i - 1));

                if (startContour != endContour) {
                    System.out.println(startContour + " " + endContour);

                    intersectionLines.add(new Line(points.get(i), points.get(i - 1), startContour, endContour));
                }
                i++;
            }
        }
        return filteredPoints;
    }

    public Mat getMapImage()
    {
        return mapImage;
    }

    public void setMapImage(Mat image)
    {
        if (ImageUtils.isBinaryImage(image))
            mapImage = image.clone();
    }


    public ArrayList<Contour> generateContours()
    {
        int imageWidth = mapImage.width();
        int imageHeight = mapImage.height();
        int imageArea = imageWidth * imageHeight;

        ArrayList<Contour> contours = ImageUtils.findContours(mapImage);
        ArrayList<Contour> filteredContours = eliminateNoisyContours(imageArea, contours);

        Mat reducedNoiseImage = ImageUtils.makeGrayImageOutOfContours(imageWidth, imageHeight, filteredContours);
        contours = ImageUtils.findContours(reducedNoiseImage);
        filteredContours = getLeafContours(contours);

        ImageUtils.showImage(reducedNoiseImage, "Reduced Noise");

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

