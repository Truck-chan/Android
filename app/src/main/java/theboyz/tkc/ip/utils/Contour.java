package theboyz.tkc.ip.utils;
import org.opencv.core.MatOfPoint;
import java.util.List;

public class Contour
{
    public MatOfPoint contourPoints;
    public List<Integer> hierarchy;

    Contour(MatOfPoint contourPoints, List<Integer> hierarchy)
    {
        this.contourPoints = contourPoints;
        this.hierarchy = hierarchy;
    }
}