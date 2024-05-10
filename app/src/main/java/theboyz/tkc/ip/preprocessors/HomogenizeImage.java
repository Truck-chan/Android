package theboyz.tkc.ip.preprocessors;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import theboyz.tkc.ip.utils.GlobalParameters;

public class HomogenizeImage implements ImagePreprocessorElement {

    private boolean isRandom(Mat block)
    {
        // Calculate the co-occurrence matrix
        int[][] coOccurrenceMatrix = new int[2][2];

        int total = block.width() * block.height();
        for (int i = 0; i < block.rows(); i++)
        {
            for (int j = 0; j < block.cols(); j++)
            {
                int pixelValue = (int) block.get(i, j)[0] == 0 ? 1 : 0;
                if (i < block.rows() - 1)
                {
                    int nextPixelValue = (int) block.get(i + 1, j)[0] == 0 ? 1 : 0;
                    coOccurrenceMatrix[pixelValue][nextPixelValue]++;
                }

                if (j < block.cols() - 1)
                {
                    int nextPixelValue = (int) block.get(i, j + 1)[0] == 0 ? 1 : 0;
                    coOccurrenceMatrix[pixelValue][nextPixelValue]++;
                }
            }
        }



        double energy = 0.0;

        // Iterate over the elements of the matrix
        for (int i = 0; i < coOccurrenceMatrix.length; i++) {
            for (int j = 0; j < coOccurrenceMatrix[i].length; j++) {
                // Add the squared value of each element to the energy
                double value = coOccurrenceMatrix[i][j] * 1.0 / total;
                energy += Math.pow(value, 2);
            }
        }

        energy /= 4.0;

        Log.i("Homoginzer:", "energy = " + energy);

        return energy < 0.6;
    }

    private boolean tooManyEdges(Mat block)
    {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(block, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Assuming there is only one contour
        if (!contours.isEmpty()) {
            MatOfPoint contour = contours.get(0);
            int numEdges = contours.size();

            return numEdges > 4;
        }

        return true;
    }

    private void enhanceBlock(Point topLeft, Mat image, int originalImageWidth)
    {
        int block_dim = GlobalParameters.BLOCK_SIZE;
        int total_pixels = block_dim * originalImageWidth;

        Rect roi = new Rect((int) topLeft.x, (int) topLeft.y, originalImageWidth, block_dim);

        Mat block = new Mat(image, roi);

        double blackPixels = total_pixels - Core.countNonZero(block);

        if (blackPixels > 0.6 * total_pixels)
        {
            block.setTo(new Scalar(255));
        }


//        if (isRandom(block))
//        {
//            block.setTo(new Scalar(255));
//        }
//        Log.i("Homoginzer", "Exception = " + e.getMessage());

    }

    //        Log.i("Homogenizer:", "output image size = " + ret.size().toString());
    @Override
    public Mat execute(Mat image) {
        int n = GlobalParameters.BLOCK_SIZE;
        Mat paddedImage = PadImage.padImage(image, n);

        for (int i = 0; i + n < paddedImage.height(); i++)
        {
//            for (int j = 0; j + n < paddedImage.width(); j++)
//            {
            enhanceBlock(new Point(n, i), paddedImage, image.width());
//            }
        }

        Rect roi = new Rect(n, n, image.width(), image.height());
        Mat ret = new Mat(paddedImage, roi);


        return ret;
    }
}
