package theboyz.tkc.ip.utils;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import java.util.List;

public class Contour
{
    public MatOfPoint contourPoints;
    public List<Integer> hierarchy;

    public Rect parentBB;

    Contour(MatOfPoint contourPoints, List<Integer> hierarchy)
    {
        this.contourPoints = contourPoints;
        this.hierarchy = hierarchy;
    }
}