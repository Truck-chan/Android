package theboyz.tkc.ip;

public class GlobalParameters {

    // Padding Component
    public static int PADDING_AMOUNT = 5;
    public static int PADDING_CONSTANT = 255;

    // Minimum Filter Kernel Size
    public static int KERNEL_SIZE = 8;


    // Gamma Correction Component
    public static double GAMMA_VALUE = 0.7;

    // Contour Detection
    public static double MIN_AREA_PERCENTAGE = 0.011;
    public static double MAX_AREA_PERCENTAGE = 0.39;
    public static double MAX_CONTOUR_POINTS = 400;
    public static double MIN_CONTOUR_POINTS = 100;


    // Homogenize parameters
    public static int BLOCK_SIZE = 32;

    public static double BLACK_PERCENTAGE = 0.5;

    // Contour Approximation
    public static double PERIMETER_PERCENTAGE = 0.015;
    public static double MIN_SPACING = 2000;

}
