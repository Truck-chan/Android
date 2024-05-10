package theboyz.tkc.ip.utils.structs;

import org.opencv.core.Point;

public class Line
{
    private static final String TAG = "Line";
    public Point start;
    public Point end;

    public int startContour;
    public int endContour;

    public Line(Point start, Point end, int startContour, int endContour){
        this.start = start;
        this.end = end;

        if (this.start.x > this.end.x)
        {
            Point temp = this.end.clone();
            this.end = this.start.clone();
            this.start = temp.clone();
        }

        this.startContour = startContour;
        this.endContour = endContour;
    }

    public Point subtractPoint(Point p, Point q)
    {
        return new Point(p.x - q.x, p.y - q.y);
    }

    public Point getMidPoint()
    {
        return new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
    }
    public double crossProduct(Point p, Point q)
    {
        return (p.x * q.y - p.y * q.x);
    }

    public double dotProduct(Point p)
    {
        Point A = subtractPoint(p, start);
        Point B = subtractPoint(end, start);

        return A.x * B.x + A.y * B.y;
    }
    public double edgeFunction(Point point)
    {
        return crossProduct(subtractPoint(end, start), subtractPoint(point, start));
    }
}


