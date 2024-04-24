package theboyz.tkc.ip;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GraphConnector {

    private ArrayList<ArrayList<Point>> graphs;

    private ArrayList<Line> intersections;

    private void setgraphs(ArrayList<ArrayList<Point>> toAnalyse)
    {
        graphs = new ArrayList<>();
        for (ArrayList<Point> graph : toAnalyse)
        {
            graphs.add(new ArrayList<>(graph));
        }
    }

    public GraphConnector(ArrayList<ArrayList<Point>> graphs, ArrayList<Line> intersections)
    {
        setgraphs(graphs);
        this.intersections = intersections;
    }

    private Point getMidPoint(Point A, Point B)
    {
        return new Point((A.x + B.x) / 2, (A.y + B.y) / 2);
    }

    private double euclideanDistance(Point A, Point B)
    {
        return (Math.pow(A.x - B.x, 2) + Math.pow(A.y + B.y, 2));
    }

    private int getSign(double x)
    {
        if (x > 0) return 1;
        else return -1;
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

        int toggler = 0;

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

            if ( getSign(crossProduct) != prevSign)
            {
                lastPoints.get(toggler).add(point);
                lastPoints.get(1 - toggler).add(prev);
                toggler = 1 - toggler;
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


    public void connectTwoContours()
    {
        for (Line line : intersections)
        {
            ArrayList<Point> contourA = graphs.get(line.startContour);
            ArrayList<Point> contourB = graphs.get(line.endContour);

            ArrayList<ArrayList<Point>> contourAPoints = getLastPoints(contourA, line);
            ArrayList<ArrayList<Point>> contourBPoints = getLastPoints(contourB, line);

            Point midPoint = getMidPoint(line.start, line.end);
            for (int i = 0; i < 2; i++)
            {
                Point A = getNearestPointsToIntersection(contourAPoints.get(i), midPoint);
                Point B = getNearestPointsToIntersection(contourBPoints.get(1 - i), midPoint);

                System.out.println("Connect Point (" + A.x + " " + A.y + ") to Point (" + B.x + " " + B.y + ")");
            }

        }
    }
}



class Line
{
    Point start;
    Point end;

    int startContour;
    int endContour;

    Line(Point start, Point end, int startContour, int endContour){
        this.start = start;
        this.end = end;

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
