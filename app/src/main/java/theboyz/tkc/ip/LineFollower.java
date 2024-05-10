package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.Point;

import java.util.ArrayList;

import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.utils.MathUtils;
import theboyz.tkc.ip.utils.structs.Line;

public class LineFollower {


    boolean inTheWay = false;
    private ArrayList<Point> track = new ArrayList<>();
    private int currentPointIdx = -1;
    private int targetPointIdx = -1;
    private Line carCoordinates = null;


    public void setTrack(ArrayList<Point> t) {track = t;}

    private void setInitialPoint()
    {
        if (track.isEmpty())
        {
            Log.i("Error", "setInitialPoint: Track Is Empty");
            return;
        }
        Point carLocation = carCoordinates.getMidPoint();
        currentPointIdx = 0;
        double minDistance = MathUtils.euclideanDistance(carLocation, track.get(currentPointIdx));
        for (int i = 0; i < track.size(); i++)
        {
            Point p = track.get(i);
            if (MathUtils.euclideanDistance(carLocation, p) < minDistance)
            {
                currentPointIdx = i;
                minDistance = MathUtils.euclideanDistance(carLocation, p);
            }
        }
    }


    private void setNextPoint(Line carDirection)
    {
        int n = track.size();
        int nextPointIdx = (currentPointIdx + 1 + n) % n;
        int prevPointIdx = (currentPointIdx - 1 + n) % n;

        Point nextPoint = track.get(nextPointIdx);
        Point prevPoint = track.get(prevPointIdx);

        double nextProjection = carDirection.dotProduct(nextPoint);
        double prevProjection = carDirection.dotProduct(prevPoint);

        if (nextProjection > prevProjection)
            targetPointIdx = nextPointIdx;
        else
            targetPointIdx = prevPointIdx;
    }

    private void checkPointDomainExpansion(Point carCenter)
    {
        if (MathUtils.euclideanDistance(carCenter, track.get(targetPointIdx)) <= GlobalParameters.CIRCLE_RADIUS_TO_SLOW_DOWN)
        {
            // TODO: send to the arduino to slowdown
            currentPointIdx = targetPointIdx;
            targetPointIdx = -1;
        }
        else
        {
            // TODO: send to arduino to keep going
        }
    }

    public void followLine(Line newCarCoordinates)
    {
        if(currentPointIdx == -1) setInitialPoint();
        if(targetPointIdx == -1) setNextPoint(newCarCoordinates);
        checkPointDomainExpansion(newCarCoordinates.getMidPoint());
    }


}
