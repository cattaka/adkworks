
package net.cattaka.droiball.view;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

public class MyJavaCameraView extends JavaCameraView {
    private Mat tmpImage;

    public MyJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    protected void deliverAndDrawFrame(Mat frame) {
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (tmpImage == null) {
                tmpImage = new Mat();
            }
            if (tmpImage.width() != frame.height() || tmpImage.height() != frame.width()) {
                tmpImage.create(new Size(frame.height(), frame.width()), frame.type());
            }
            Core.transpose(frame, tmpImage);
            Core.flip(tmpImage, tmpImage, -1);
            super.deliverAndDrawFrame(tmpImage);
        } else {
            if (tmpImage == null) {
                tmpImage = new Mat();
            }
            Core.flip(frame, tmpImage, 1);
            super.deliverAndDrawFrame(tmpImage);
        }
    }
}
