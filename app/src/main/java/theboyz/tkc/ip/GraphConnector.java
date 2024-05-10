package theboyz.tkc.ip;

import static theboyz.tkc.ip.utils.MathUtils.euclideanDistance;
import static theboyz.tkc.ip.utils.MathUtils.getSign;

import android.util.Log;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import theboyz.tkc.ip.utils.structs.Line;
import theboyz.tkc.ip.utils.structs.PointContour;

public class GraphConnector {
    private static final String TAG = "GraphConnector";
    private final ArrayList<ArrayList<Point>> trackGraphs = new ArrayList<>();
    private final ArrayList<Line> intersections = new ArrayList<>();

    // =========================================================================
    private final Map<PointContour, PointContour> connections = new HashMap<>();
    public final ArrayList<Point> lapLine = new ArrayList<>();

    public void setParameters(ArrayList<ArrayList<Point>> contoursToAnalyse, ArrayList<Line> lines)
    {
        trackGraphs.clear();
        intersections.clear();

        for (ArrayList<Point> graph : contoursToAnalyse)
        {
            trackGraphs.add(new ArrayList<>(graph));
        }
        intersections.addAll(lines);
    }

    private ArrayList<ArrayList<Point>> getLastPoints(ArrayList<Point> originalGraph, Line line)
    {
        int prevSign = 1;
        Point prev = new Point(0, 0);

        ArrayList<Point> graph = new ArrayList<>(originalGraph);
        graph.add(originalGraph.get(0));

        ArrayList<ArrayList<Point>> lastPoints = new ArrayList<>();
        lastPoints.add(new ArrayList<>());
        lastPoints.add(new ArrayList<>());

        for (Point point : graph)
        {
            double crossProduct = line.edgeFunction(point);

            if (prev.x == 0 && prev.y == 0)
            {
                prev.x = point.x;
                prev.y = point.y;
                prevSign = getSign(crossProduct);
                continue;
            }

            int currentSign = getSign(crossProduct);

            if ( getSign(crossProduct) != prevSign)
            {
                lastPoints.get(currentSign).add(point);
                lastPoints.get(prevSign).add(prev);
            }

            prevSign = getSign(crossProduct);
            prev = point;
        }


        return lastPoints;
    }

    private Point getNearestPointsToIntersection(ArrayList<Point> graph, Point intersection)
    {
        double minDistance = Double.MAX_VALUE;
        Point minPoint = new Point(0, 0);
        for (Point point : graph)
        {
            double calculatedDistance = euclideanDistance(intersection, point);

            if (calculatedDistance < minDistance)
            {
                minDistance = calculatedDistance;
                minPoint = point;
            }
        }
        return minPoint;
    }

    private void printContourPoints(ArrayList<ArrayList<Point>> points)
    {
        Log.i(TAG, "printContourPoints: Upper Points -------------***");

        for (int i = 0; i < 2; i++)
        {
            Log.i(TAG, "printContourPoints: Array List-" + i);
            for (int j = 0; j < 2; j++)
            {
                Log.i(TAG, "Point-"+ j + ":" + points.get(i).get(j).toString());
            }
        }
    }

    private void connectSeparatedContours()
    {
        Map<PointContour, PointContour> ret = new HashMap<>();
        for (Line line : intersections)
        {
            ArrayList<Point> contourA = trackGraphs.get(line.startContour);
            ArrayList<Point> contourB = trackGraphs.get(line.endContour);

            ArrayList<ArrayList<Point>> contourAPoints = getLastPoints(contourA, line);
            ArrayList<ArrayList<Point>> contourBPoints = getLastPoints(contourB, line);

            printContourPoints(contourAPoints);
            printContourPoints(contourBPoints);

            Point midPoint = line.getMidPoint();
            for (int i = 0; i < 2; i++)
            {
                Point A = getNearestPointsToIntersection(contourAPoints.get(i), midPoint);
                Point B = getNearestPointsToIntersection(contourBPoints.get(1 - i), midPoint);

                ret.put(new PointContour(A, line.startContour), new PointContour(B, line.endContour));
                ret.put(new PointContour(B, line.endContour), new PointContour(A, line.startContour));
                Log.i(TAG, "connectTwoContours => " + "Connect Point (" + A.x + " " + A.y + ") to Point (" + B.x + " " + B.y + ")");
            }
        }

        connections.putAll(ret);
    }

    private void makeLapLine()
    {
        int currentContour = 0;
        int currentIdx = 1;

        Point initPoint = trackGraphs.get(currentContour).get(currentIdx);
        Point nextPoint = initPoint;
        Point currentPoint = nextPoint;
        boolean flag = false;
        int direction = 1;
        do {
            currentPoint = nextPoint;

            PointContour key = new PointContour(currentPoint, currentContour);
            if (connections.containsKey(key) && !flag)
            {
                PointContour pc = connections.get(key);
                if (pc == null)
                {
                    Log.i("Exception Debug", "makeLapLine: a point contour was null");
                    return;
                }
                nextPoint = pc.point;
                currentContour = pc.contourNumber;
                currentIdx = trackGraphs.get(currentContour).indexOf(nextPoint);
                direction *= -1;
                flag = true;
            }
            else
            {
                int n = trackGraphs.get(currentContour).size();
                currentIdx = (currentIdx % n + direction + n) % n;
                nextPoint = trackGraphs.get(currentContour).get(currentIdx);
                flag = false;
            }

            lapLine.add(currentPoint);
        } while (nextPoint != initPoint);
    }
    
    public void connectContours()
    {
        connectSeparatedContours();
        makeLapLine();
        Log.i(TAG, "connectContours: Connected Track = " + lapLine.toString());
    }
}




