package theboyz.tkc.ip;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import theboyz.tkc.ImageProcessing;
import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ip.utils.MathUtils;
import theboyz.tkc.ip.utils.structs.Line;

public class LineFollower {


    boolean inTheWay = false;
    private ArrayList<Point> track = new ArrayList<>();
    private int currentPointIdx = -1;
    private int targetPointIdx = -1;
    private Line carCoordinates = null;


    private int direction = 1;


    public void setTrack(ArrayList<Point> t) {track = t;}

    private void setInitialPoint()
    {
        if (track.isEmpty() || carCoordinates == null)
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
        {
            direction = 1;
            targetPointIdx = nextPointIdx;
        }
        else
        {
            direction = -1;
            targetPointIdx = prevPointIdx;
        }
    }

    private void checkPointDomainExpansion(Point carCenter)
    {
        double distance = MathUtils.euclideanDistance(carCenter, track.get(targetPointIdx));
        if (distance <= GlobalParameters.CIRCLE_RADIUS_TO_SLOW_DOWN)
        {
            ImageProcessing.send("kemo");
            Log.i("bluetooth debug", "checkPointDomainExpansion: " + distance);
            // TODO: send to the arduino to slowdown
            currentPointIdx = targetPointIdx;
            targetPointIdx = (targetPointIdx + direction + track.size()) % track.size();
        }
        else
        {
            ImageProcessing.send("kemof");
            Log.i("bluetooth debug", "checkPointDomainExpansion: " + distance);
            // TODO: send to arduino to keep going
        }
    }

    public void followLine(Line newCarCoordinates)
    {
        if (newCarCoordinates == null)
            return;
        carCoordinates = newCarCoordinates;
        Log.i("Last Step", "Line follower - followLine: entered");
        if(currentPointIdx == -1)
        {
            setInitialPoint();
            Log.i("Last Step", "Line follower - currentPoint: set = " + track.get(currentPointIdx) );
        }
        if(targetPointIdx == -1)
        {
            setNextPoint(newCarCoordinates);
            Log.i("Last Step", "Line follower - targetPointIdx: set = " + targetPointIdx);
        }
        checkPointDomainExpansion(newCarCoordinates.getMidPoint());
    }


    public void putTargetPoint(Mat frame)
    {
        if (track != null && track.isEmpty() && targetPointIdx < 0)
            return;
        Imgproc.circle(frame, track.get(targetPointIdx), 25, new Scalar(255,0,255), 2);
    }


}
