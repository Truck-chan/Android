package theboyz.tkc.ip.utils;

import org.opencv.core.Point;

public class MathUtils {

    static public int euclideanDistance(Point p1, Point p2)
    {
        return (int) (Math.pow (p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }


    static public int getSign(double x)
    {
        return x > 0 ? 1 : 0;
    }
}
