package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphConnector {

    ArrayList<Point> connectedMap;
    private static final String TAG = "GraphConnector";
    private ArrayList<ArrayList<Point>> graphs;

    private ArrayList<Line> intersections;

    public GraphConnector(ArrayList<ArrayList<Point>> graphs, ArrayList<Line> intersections)
    {
        setgraphs(graphs);
        this.intersections = intersections;
    }

    private void setgraphs(ArrayList<ArrayList<Point>> toAnalyse)
    {
        graphs = new ArrayList<>();
        for (ArrayList<Point> graph : toAnalyse)
        {
            graphs.add(new ArrayList<>(graph));
        }
    }

    // some math stuff
    private Point getMidPoint(Point A, Point B)
    {
        return new Point((A.x + B.x) / 2, (A.y + B.y) / 2);
    }

    private double euclideanDistance(Point A, Point B)
    {
        return (Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
    }

    private int getSign(double x)
    {
        return x > 0 ? 1 : 0;
    }

    // some geometry stuff
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
//        Log.i(TAG, "printContourPoints: Upper Points -------------***");

//        for (int i = 0; i < 2; i++)
//        {
//            Log.i(TAG, "printContourPoints: Array List-" + i);
//            for (int j = 0; j < 2; j++)
//            {
//                Log.i(TAG, "Point-"+ j + ":" + points.get(i).get(j).toString());
//            }
//        }
    }

    public Map<PointContour, PointContour> connectSeparatedContours()
    {
        Map<PointContour, PointContour> ret = new HashMap<>();
        for (Line line : intersections)
        {
            ArrayList<Point> contourA = graphs.get(line.startContour);
            ArrayList<Point> contourB = graphs.get(line.endContour);

            ArrayList<ArrayList<Point>> contourAPoints = getLastPoints(contourA, line);
            ArrayList<ArrayList<Point>> contourBPoints = getLastPoints(contourB, line);

            printContourPoints(contourAPoints);
            printContourPoints(contourBPoints);

            Point midPoint = getMidPoint(line.start, line.end);
            for (int i = 0; i < 2; i++)
            {
                Point A = getNearestPointsToIntersection(contourAPoints.get(i), midPoint);
                Point B = getNearestPointsToIntersection(contourBPoints.get(1 - i), midPoint);

                ret.put(new PointContour(A, line.startContour), new PointContour(B, line.endContour));
                ret.put(new PointContour(B, line.endContour), new PointContour(A, line.startContour));
                Log.i(TAG, "connectTwoContours => " + "Connect Point (" + A.x + " " + A.y + ") to Point (" + B.x + " " + B.y + ")");
            }
        }

        return ret;
    }


//    ArrayList<Point> lapLine = new ArrayList<>();
//    Map<Point, Boolean> visited = new HashMap<>();
//    public void makeLapLine(Map<PointContour, PointContour> connections, Point current, int contour = 0)
//    {
//        if (visited.containsKey(current))
//            return;
//        visited.put(current , true);
//
//        if (!connections.containsKey(current))  // if the point is in the connection points
//        {
//            lapLine.add(current);
//            makeLapLine();
//        }
//        else
//        {
//
//        }
//
//    }
    
    public ArrayList<Connection> connectContours()
    {
        Map<PointContour, PointContour> connections = connectSeparatedContours();

        return new ArrayList<>();
//        makeLapLine(connections);
    }
}

class PointContour
{
    Point point;
    int contourNumber;
    PointContour(Point p, int c) {
        point = p;
        contourNumber = c;
    }
}

class Connection
{
    Point point_1;
    int cont_1;

    Point point_2;
    int cont_2;

    Connection(Point x, int xc, Point y, int yc)
    {
        point_1 = x;
        cont_1 = xc;
        point_2 = y;
        cont_2 = yc;
    }
}

class Line
{
    private static final String TAG = "Line";
    Point start;
    Point end;

    int startContour;
    int endContour;

    Line(Point start, Point end, int startContour, int endContour){
        this.start = start;
        this.end = end;

        if (this.start.x > this.end.x)
        {
            Point temp = this.end.clone();
            this.end = this.start.clone();
            this.start = temp.clone();
        }

//        Log.i(TAG, "Line: start point = " + this.start.toString() + ", end point = " + this.end.toString());

        this.startContour = startContour;
        this.endContour = endContour;
    }

    public Point subtractPoint(Point p, Point q)
    {
        return new Point(p.x - q.x, p.y - q.y);
    }

    public double crossProduct(Point p, Point q)
    {
        return (p.x * q.y - p.y * q.x);
    }

    public double edgeFunction(Point point)
    {
        return crossProduct(subtractPoint(end, start), subtractPoint(point, start));
    }
}
