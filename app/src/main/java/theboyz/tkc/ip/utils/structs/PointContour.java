package theboyz.tkc.ip.utils.structs;

import org.opencv.core.Point;

import java.util.Objects;

public class PointContour
{
    public Point point;
    public int contourNumber;
    public PointContour(Point p, int c) {
        point = p;
        contourNumber = c;
    }

    // Override equals() and hashCode() methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointContour customKey = (PointContour) o;
        return point.x == customKey.point.x && point.y == customKey.point.y && contourNumber == customKey.contourNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point.x, point.y, contourNumber);
    }
}
