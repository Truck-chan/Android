package theboyz.tkc.ip;

import static theboyz.tkc.ip.utils.ImageUtils.getContourOfPoint;
import static theboyz.tkc.ip.utils.ImageUtils.getLeafContours;
import static theboyz.tkc.ip.utils.ImageUtils.limitedContoursArea;
import static theboyz.tkc.ip.utils.ImageUtils.removePointFromContour;
import static theboyz.tkc.ip.utils.MathUtils.euclideanDistance;

import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import theboyz.tkc.ip.utils.Contour;
import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.utils.ImageUtils;
import theboyz.tkc.ip.utils.structs.Line;


public class MapMaker {
    private Mat mapImage = new Mat();
    private final ArrayList<Mat> debuggingImages = new ArrayList<>();
    public ArrayList<ArrayList<Point>> contoursGraph = new ArrayList<>();
    public ArrayList<Line> intersectionLines = new ArrayList<>();

    public final ArrayList<Point> turnPoints = new ArrayList<>();

    private final ArrayList<Contour> trackContours = new ArrayList<>();

    // TODO: can be refactored later
    private void approximateCurveToLines()
    {
        if (trackContours.isEmpty())
        {
            Log.i("Error", "approximateCurveToLines: track contours is empty");
            return;
        }

        turnPoints.clear();

        for (int j = 0; j < trackContours.size(); j++)
        {
            Contour contour = trackContours.get(j);

            MatOfPoint2f contour2f = new MatOfPoint2f(contour.contourPoints.toArray());
            double epsilon = GlobalParameters.PERIMETER_PERCENTAGE * Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);

            contoursGraph.add(new ArrayList<Point>());

            for (int i = 0; i < approxCurve.rows(); i++)
            {
                Point point = new Point(approxCurve.get(i, 0)[0], approxCurve.get(i, 0)[1]);
                turnPoints.add(point);
                contoursGraph.get(j).add(point);
            }
        }

    }

    private void eliminateVeryNearPoints()
    {
        ArrayList<Point> filteredPoints = new ArrayList<>();
        for (int i = 0; i < turnPoints.size(); i++)
        {
            int startContour = getContourOfPoint(contoursGraph, turnPoints.get(i));
            if (startContour == -1) continue; // point is already removed

            boolean isValid = true;
            for (int j = i + 1; j < turnPoints.size(); j++)
            {
                if (euclideanDistance(turnPoints.get(i), turnPoints.get(j)) <= GlobalParameters.MIN_SPACING)
                {
                    int endContour = getContourOfPoint(contoursGraph, turnPoints.get(j));
                    if (endContour == -1) continue;

                    if (startContour != endContour)
                    {
                        Line l = new Line(turnPoints.get(i), turnPoints.get(j), startContour, endContour);
                        intersectionLines.add(l);
                        removePointFromContour(contoursGraph.get(startContour), turnPoints.get(i));
                        removePointFromContour(contoursGraph.get(endContour), turnPoints.get(j));
                    }
                    else
                    {
                        removePointFromContour(contoursGraph.get(endContour), turnPoints.get(j));
                    }
                    isValid = false;
                    break;
                }
            }
            if (isValid)
                filteredPoints.add(turnPoints.get(i));
        }

        turnPoints.clear();
        turnPoints.addAll(filteredPoints);
    }
    public ArrayList<Mat> getDebuggingImages(){return debuggingImages;}

    public void setMapImage(Mat image)
    {
        if (ImageUtils.isBinaryImage(image))
        {
            mapImage = image.clone();

        }
        else
            Log.i("Error", "setMapImage: map image is not binary");
    }

    public void reset()
    {
        debuggingImages.clear();
        turnPoints.clear();
        contoursGraph.clear();
        intersectionLines.clear();
        trackContours.clear();
    }
    public void generateTrackContours()
    {
        if(mapImage.empty())
        {
            Log.i("Error", "generateTrackContours: map image is empty");
            return;
        }

        trackContours.clear();

        int imageWidth = mapImage.width();
        int imageHeight = mapImage.height();
        int imageArea = imageWidth * imageHeight;
        Mat pipelineImage = mapImage.clone();

        // find all contours in the image and draw it on a black image
        ArrayList<Contour> contours = ImageUtils.findContours(pipelineImage, new Mat());
        pipelineImage = ImageUtils.makeGrayImageOutOfContours(contours, imageWidth, imageHeight);

        // add for preview
        debuggingImages.add(pipelineImage);

        // remove all noisy contours - a noisy contour has area greater than or less than some range
        // draw these contours on an image again
        ArrayList<Contour> filteredContours = ImageUtils.limitedContoursArea(contours, imageArea);
        pipelineImage = ImageUtils.makeGrayImageOutOfContours(filteredContours, imageWidth, imageHeight);

        // add for preview
        debuggingImages.add(pipelineImage);

        contours = ImageUtils.findContours(pipelineImage, new Mat());

        try {
            // find contours for the smaller ones and get all contours with no children
            filteredContours = getLeafContours(contours, imageWidth);
        }
        catch (Exception e)
        {
            Log.i("Exception Debug", "generateTrackContours: " + e.getMessage());
        }
        filteredContours = limitedContoursArea(filteredContours, imageArea);
        pipelineImage = ImageUtils.makeGrayImageOutOfContours(filteredContours, imageWidth, imageHeight);

        // add for preview
        debuggingImages.add(pipelineImage);

        trackContours.addAll(filteredContours);
    }

    public void generateTurnsPoints()
    {
        approximateCurveToLines();
        eliminateVeryNearPoints();
    }

    public void drawCriticalPointsOnImage(Mat originalGrayMap)
    {
        if (originalGrayMap.empty())
        {
            Log.i("Error", "drawCriticalPointsOnImage: originalGrayMap is empty");
        }

        Mat rgbImage = originalGrayMap.clone();
        if (originalGrayMap.get(0,0).length < 3)
        {
            Imgproc.cvtColor(originalGrayMap, rgbImage, Imgproc.COLOR_GRAY2RGB);
        }

        ArrayList<Point> points = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();

        for (ArrayList<Point> contour : contoursGraph) {
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

        debuggingImages.add(rgbImage);
    }

}

