/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    FrameMarkers.cpp

@brief
    Sample for FrameMarkers

==============================================================================*/


#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/Marker.h>

#include "SampleUtils.h"
#include "CubeShaders.h"
#include "Q_object.h"

#ifdef __cplusplus
extern "C"
{
#endif

// OpenGL ES 2.0 specific:
unsigned int shaderProgramID    = 0;
GLint vertexHandle              = 0;
GLint normalHandle              = 0;
GLint mvpMatrixHandle           = 0;

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
static const float kLetterScale        = 21.0f / 2.0f;
static const float kLetterTranslate    = 21.0f / 2.0f;


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_setActivityPortraitMode(JNIEnv *, jclass clazz, jboolean isPortrait)
{
    isActivityInPortraitMode = isPortrait;
}

struct PoseCache {
	jboolean detectedFlag;
	QCAR::Matrix44F m;
};

int numOfTags = 0;
PoseCache *poseCache;

JNIEXPORT jboolean JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_getTrakerPose(JNIEnv * env, jclass clazz, jint idx, jfloatArray dst)
{
    jboolean result;
    if (0 <= idx && idx < numOfTags) {
        result = JNI_TRUE;
        jfloat* arrDst=env->GetFloatArrayElements(dst,NULL);
        for (int i=0;i<4*4;i++) {
            arrDst[i] = poseCache[idx].m.data[i];
        }
        env->ReleaseFloatArrayElements(dst, arrDst, 0);
        result = poseCache[idx].detectedFlag;
    } else {
        result = JNI_FALSE;
    }
    return result;
}

JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_renderFrame(JNIEnv *, jclass clazz)
{
    //LOG("Java_com_qualcomm_QCARSamples_FrameMarkers_GLRenderer_renderFrame");
 
    // Clear color and depth buffer 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    for(int tIdx = 0; tIdx < numOfTags; tIdx++) {
        poseCache[tIdx].detectedFlag = false;
    }

    // Did we find any trackables this frame?
    for(int tIdx = 0; tIdx < state.getNumActiveTrackables(); tIdx++)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(tIdx);
        QCAR::Matrix44F modelViewMatrix =
            QCAR::Tool::convertPose2GLMatrix(trackable->getPose());        
      
        
        // Check the type of the trackable:
        assert(trackable->getType() == QCAR::Trackable::MARKER);
        const QCAR::Marker* marker = static_cast<const QCAR::Marker*>(trackable);

        // Select which model to draw:
        const GLvoid* vertices = 0;
        const GLvoid* normals = 0;
        const GLvoid* indices = 0;
        const GLvoid* texCoords = 0;
        int numIndices = 0;

        if (0<=marker->getMarkerId() && marker->getMarkerId() < numOfTags) {
            poseCache[marker->getMarkerId()].m = modelViewMatrix;
            poseCache[marker->getMarkerId()].detectedFlag = true;
        }

        vertices = &QobjectVertices[0];
        normals = &QobjectNormals[0];
        indices = &QobjectIndices[0];
        texCoords = &QobjectTexCoords[0];
        numIndices = NUM_Q_OBJECT_INDEX;

        QCAR::Matrix44F modelViewProjection;

        SampleUtils::translatePoseMatrix(-kLetterTranslate,
                                         -kLetterTranslate,
                                         0.f,
                                         &modelViewMatrix.data[0]);
        SampleUtils::scalePoseMatrix(kLetterScale, kLetterScale, kLetterScale,
                                     &modelViewMatrix.data[0]);
        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
                                    &modelViewMatrix.data[0],
                                    &modelViewProjection.data[0]);

        glUseProgram(shaderProgramID);
 
        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0, vertices);
        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0, normals);

        glEnableVertexAttribArray(vertexHandle);
        glEnableVertexAttribArray(normalHandle);

        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
                           (GLfloat*)&modelViewProjection.data[0]);
        glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_SHORT, indices);

        SampleUtils::checkGlError("FrameMarkers render frame");

    }

    glDisable(GL_DEPTH_TEST);
    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);

    QCAR::Renderer::getInstance().end();
}


void
configureVideoBackground()
{
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.
                                getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);

    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;
    
    if (isActivityInPortraitMode)
    {
        //LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight
                                * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;
    }
    else
    {
        //LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight
                            * (screenWidth / (float)videoMode.mWidth);
    }

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_initApplicationNative(
                            JNIEnv* env, jclass clazz, jint width, jint height, jint argNumOfTags)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_initApplicationNative");
    
    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;
    numOfTags = argNumOfTags;
    poseCache = (PoseCache*)malloc(sizeof(PoseCache) * numOfTags);
}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_deinitApplicationNative(
                                                        JNIEnv* env, jclass clazz)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_deinitApplicationNative");
    free(poseCache);
}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_startCamera(JNIEnv *, jclass clazz)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_startCamera");

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init())
        return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(
                                QCAR::CameraDevice::MODE_DEFAULT))
        return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
        return;

    // Start the tracker:
    QCAR::Tracker::getInstance().start();
    
    // Cache the projection matrix:
    const QCAR::Tracker& tracker = QCAR::Tracker::getInstance();
    const QCAR::CameraCalibration& cameraCalibration =
                                    tracker.getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f,
                                            2000.0f);
}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_stopCamera(JNIEnv *, jclass clazz)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_stopCamera");

    QCAR::Tracker::getInstance().stop();

    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}

JNIEXPORT jboolean JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_setFlash(JNIEnv*, jclass clazz, jboolean flash)
{
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_startAutoFocus(JNIEnv*, jclass clazz)
{
    return QCAR::CameraDevice::getInstance().startAutoFocus()?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_setFocusMode(JNIEnv*, jclass clazz, jint mode)
{
    return QCAR::CameraDevice::getInstance().setFocusMode(mode)?JNI_TRUE:JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_initRendering(
                                                    JNIEnv* env, jclass clazz)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_initRendering");

    // Define clear color
    glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);
    
    shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader,
                                                            cubeFragmentShader);

    vertexHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexPosition");
    normalHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexNormal");
    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID,
                                                "modelViewProjectionMatrix");

}


JNIEXPORT void JNICALL
Java_net_cattaka_positiondetector_PositionDetectorNative_updateRendering(
                        JNIEnv* env, jclass clazz, jint width, jint height)
{
    LOG("Java_net_cattaka_positiondetector_PositionDetectorNative_updateRendering");
    
    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Reconfigure the video background
    configureVideoBackground();
}


#ifdef __cplusplus
}
#endif
