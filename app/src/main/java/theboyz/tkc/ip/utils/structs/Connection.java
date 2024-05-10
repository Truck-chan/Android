package theboyz.tkc.ip.utils.structs;

import org.opencv.core.Point;

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