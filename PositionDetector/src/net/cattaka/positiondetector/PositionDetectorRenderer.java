/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    FrameMarkersRenderer.java

@brief
    Sample for FrameMarkers

==============================================================================*/

package net.cattaka.positiondetector;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.cattaka.positiondetector.v4.PositionDetectorFragment;
import android.opengl.GLSurfaceView;

import com.qualcomm.QCAR.QCAR;

/** The renderer class for the FrameMarkers sample. */
public class PositionDetectorRenderer implements GLSurfaceView.Renderer {
    public boolean mIsActive = false;

    private int mNumOfTags;

    private TagState[] mTagStatus;

    /** Called when the surface is created or recreated. */
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        DebugLog.LOGD("GLRenderer::onSurfaceCreated");

        // Call native function to initialize rendering:
        PositionDetectorNative.initRendering();

        // Call QCAR function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        QCAR.onSurfaceCreated();
    }

    /** Called when the surface changed size. */
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        DebugLog.LOGD("GLRenderer::onSurfaceChanged");

        // Call native function to update rendering when render surface
        // parameters have changed:
        PositionDetectorNative.updateRendering(width, height);

        // Call QCAR function to handle render surface size changes:
        QCAR.onSurfaceChanged(width, height);
    }

    /** Called to draw the current frame. */
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our native function to render content
        PositionDetectorNative.renderFrame();

        { // 認識されたフレームのイベントを発生させる
            List<TagState> tagStatus = new ArrayList<TagState>(mNumOfTags);
            for (int i = 0; i < mNumOfTags; i++) {
                if (PositionDetectorNative.getTrakerPose(i, mTagStatus[i].getPoseMats())) {
                    if (mTagStatus[i].getTagEvent() == TagEvent.DISAPPEAR) {
                        mTagStatus[i].setTagEvent(TagEvent.APPEAR);
                        tagStatus.add(new TagState(i, mTagStatus[i].getTagEvent(), mTagStatus[i]
                                .getPoseMats()));
                    } else {
                        mTagStatus[i].setTagEvent(TagEvent.MOVE);
                        tagStatus.add(new TagState(i, mTagStatus[i].getTagEvent(), mTagStatus[i]
                                .getPoseMats()));
                    }
                } else {
                    if (mTagStatus[i].getTagEvent() != TagEvent.DISAPPEAR) {
                        mTagStatus[i].setTagEvent(TagEvent.DISAPPEAR);
                        tagStatus.add(new TagState(i, mTagStatus[i].getTagEvent(), mTagStatus[i]
                                .getPoseMats()));
                    } else {
                        mTagStatus[i].setTagEvent(TagEvent.DISAPPEAR);
                    }
                }
            }
            if (tagStatus.size() > 0) {
                mPositionDetector.onPositionDetectEvent(tagStatus);
            }
        }
    }

    private PositionDetectorFragment mPositionDetector;

    public PositionDetectorRenderer(PositionDetectorFragment frameMarkers, int numOfTags) {
        mPositionDetector = frameMarkers;
        mNumOfTags = numOfTags;
        mTagStatus = new TagState[mNumOfTags];
        for (int i = 0; i < mTagStatus.length; i++) {
            mTagStatus[i] = new TagState();
        }
    }
}
