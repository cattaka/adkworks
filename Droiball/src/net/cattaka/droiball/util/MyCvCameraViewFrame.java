
package net.cattaka.droiball.util;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MyCvCameraViewFrame implements CvCameraViewFrame {
    private Mat mRgbaMat;

    private Mat mGrayMat;

    public MyCvCameraViewFrame(Mat rgbaMat, Mat grayMat) {
        super();
        mRgbaMat = rgbaMat;
        mGrayMat = grayMat;
    }

    @Override
    public Mat gray() {
        if (mGrayMat == null) {
            mGrayMat = new Mat(mRgbaMat.rows(), mRgbaMat.cols(), CvType.CV_8U);
            Imgproc.cvtColor(mRgbaMat, mRgbaMat, Imgproc.COLOR_RGBA2GRAY);
        }
        return mGrayMat;
    }

    @Override
    public Mat rgba() {
        if (mRgbaMat == null) {
            mRgbaMat = new Mat(mGrayMat.rows(), mGrayMat.cols(), CvType.CV_16SC4);
            Imgproc.cvtColor(mGrayMat, mRgbaMat, Imgproc.COLOR_GRAY2RGBA);
        }
        return mRgbaMat;
    }

}
