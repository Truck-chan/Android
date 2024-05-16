package theboyz.tkc.ip.utils;

import theboyz.tkc.ui.vec2;

public class GlobalParameters {

    // Padding Component
    public static int PADDING_AMOUNT = 5;
    public static int PADDING_CONSTANT = 255;

    // Minimum Filter Kernel Size
    public static int KERNEL_SIZE = 8;


    // Gamma Correction Component
    public static double GAMMA_VALUE = 0.54;

    // Contour Detection
    public static double MIN_AREA_PERCENTAGE = 0.011;
    public static double MAX_AREA_PERCENTAGE = 0.24;
    public static double MAX_CONTOUR_POINTS = 400;
    public static double MIN_CONTOUR_POINTS = 100;


    // Homogenize parameters
    public static int BLOCK_SIZE = 32;

    public static double BLACK_PERCENTAGE = 0.5;

    // Contour Approximation
    public static double PERIMETER_PERCENTAGE = 0.015;
    public static double MIN_SPACING = 1500;

    // order is R G B
    public static double [] GREEN_THRESHES = {0,110,60,255,0,70};
    public static double [] RED_THRESHES   = {40, 255, 0, 60, 0, 60};
//    public static double [] RED_THRESHES   = {0 , 255, 0, 255, 0, 255};



    public static double CIRCLE_RADIUS_TO_SLOW_DOWN = 5000;

    public static vec2 p0 = new vec2(0,0);
    public static vec2 p1 = new vec2(1,0);
    public static vec2 p2 = new vec2(1,1);
    public static vec2 p3 = new vec2(0,1);

    public static vec2[] clipPoints = new vec2[]{
            new vec2(0.2f,0.2f),
            new vec2(0.8f,0.2f),
            new vec2(0.8f,0.8f),
            new vec2(0.2f,0.8f)
    };

}
