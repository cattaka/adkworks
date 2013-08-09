
package net.cattaka.droiball.view;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

public class MyJavaCameraView extends JavaCameraView {
    private Mat tmpImage1;

    private Mat tmpImage2;

    public MyJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        tmpImage1 = new Mat();
        tmpImage2 = new Mat();
    }

    @Override
    protected void AllocateCache() {
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            int t = mFrameWidth;
            mFrameWidth = mFrameHeight;
            mFrameHeight = t;
            super.AllocateCache();
            t = mFrameWidth;
            mFrameWidth = mFrameHeight;
            mFrameHeight = t;
        } else {
            super.AllocateCache();
        }
    }

    @Override
    protected void deliverAndDrawFrame(final CvCameraViewFrame frame) {
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            CvCameraViewFrame flipFrame = new CvCameraViewFrame() {
                @Override
                public Mat rgba() {
                    Core.transpose(frame.rgba(), tmpImage1);
                    Core.flip(tmpImage1, tmpImage1, -1);
                    return tmpImage1;
                }

                @Override
                public Mat gray() {
                    Core.transpose(frame.gray(), tmpImage2);
                    Core.flip(tmpImage2, tmpImage2, -1);
                    return tmpImage2;
                }
            };
            super.deliverAndDrawFrame(flipFrame);
        } else {
            CvCameraViewFrame flipFrame = new CvCameraViewFrame() {
                @Override
                public Mat rgba() {
                    Core.flip(frame.rgba(), tmpImage1, 1);
                    return tmpImage1;
                }

                @Override
                public Mat gray() {
                    Core.flip(frame.gray(), tmpImage2, 1);
                    return tmpImage2;
                }
            };
            super.deliverAndDrawFrame(flipFrame);
        }
    }

}
