
package net.cattaka.positiondetector.v4;

import java.util.List;

import net.cattaka.positiondetector.DebugLog;
import net.cattaka.positiondetector.MyGLView;
import net.cattaka.positiondetector.OnPositionDetectorListener;
import net.cattaka.positiondetector.PositionDetectorNative;
import net.cattaka.positiondetector.PositionDetectorRenderer;
import net.cattaka.positiondetector.TagState;
import net.cattaka.positiondetector.task.InitQCARTask;
import net.cattaka.positiondetector.task.LoadTrackerTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.qualcomm.QCAR.QCAR;

public class PositionDetectorFragment extends Fragment implements
        InitQCARTask.InitQCARTaskListener, LoadTrackerTask.LoadTrackerTaskListener {
    // Application status constants:
    enum AppStatus {
        UNINITED, INIT_APP, INIT_QCAR, INIT_APP_AR, INIT_TRACKER, INITED, CAMERA_STOPPED, CAMERA_RUNNING,
    };

    private OnPositionDetectorListener mListener;

    // Our OpenGL view:
    private MyGLView mGlView;

    // Our renderer:
    private PositionDetectorRenderer mRenderer;

    // Display size of the device
    private int mScreenWidth = 0;

    private int mScreenHeight = 0;

    private int mNumOfTags = 21;

    // The current application status
    private AppStatus mAppStatus = AppStatus.UNINITED;

    // The async tasks to initialize the QCAR SDK
    private InitQCARTask mInitQCARTask;

    private LoadTrackerTask mLoadTrackerTask;

    // QCAR initialization flags
    private int mQCARFlags = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("FrameMarkers::onCreate");

        // Query the QCAR initialization flags:
        mQCARFlags = getInitializationFlags();

        // Update the application status to start initializing application
        updateApplicationStatus(AppStatus.INIT_APP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MyGLView mGlView = new MyGLView(getActivity());
        mGlView.setTag("MyGlView");
        mGlView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        {
            int depthSize = 16;
            int stencilSize = 0;
            boolean translucent = QCAR.requiresAlpha();
            // ViewGroup glViewFrame = (ViewGroup)
            // getView().findViewById(R.id.glViewFrame);
            mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
            mRenderer = new PositionDetectorRenderer(this, mNumOfTags);
            mGlView.setRenderer(mRenderer);
            // glViewFrame.addView(mGlView, new
            // LayoutParams(LayoutParams.FILL_PARENT,
            // LayoutParams.FILL_PARENT));
        }
        return mGlView;
    }

    /** Configure QCAR with the desired version of OpenGL ES. */
    private int getInitializationFlags() {
        return QCAR.GL_20;
    }

    /** Called when the activity will start interacting with the user. */
    public void onResume() {
        DebugLog.LOGD("FrameMarkers::onResume");
        super.onResume();

        // QCAR-specific resume operation
        QCAR.onResume();

        // We may start the camera only if the QCAR SDK has already been
        // initialized
        if (mAppStatus == AppStatus.CAMERA_STOPPED)
            updateApplicationStatus(AppStatus.CAMERA_RUNNING);

        // Resume the GL view:
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    /** Called when the system is about to start resuming a previous activity. */
    public void onPause() {
        DebugLog.LOGD("FrameMarkers::onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // QCAR-specific pause operation
        QCAR.onPause();

        if (mAppStatus == AppStatus.CAMERA_RUNNING) {
            updateApplicationStatus(AppStatus.CAMERA_STOPPED);
        }
    }

    /** The final call you receive before your activity is destroyed. */
    public void onDestroy() {
        DebugLog.LOGD("FrameMarkers::onDestroy");
        super.onDestroy();

        // Cancel potentially running tasks
        if (mInitQCARTask != null && mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED) {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }

        if (mLoadTrackerTask != null
                && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED) {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }

        // Do application deinitialization in native code
        PositionDetectorNative.deinitApplicationNative();

        // Deinitialize QCAR SDK
        QCAR.deinit();

        System.gc();
    }

    /**
     * NOTE: this method is synchronized because of a potential concurrent
     * access by FrameMarkers::onResume() and InitQCARTask::onPostExecute().
     */
    private synchronized void updateApplicationStatus(AppStatus appStatus) {
        // Exit if there is no change in status
        if (mAppStatus == appStatus)
            return;

        // Store new status value
        mAppStatus = appStatus;

        // Execute application state-specific actions
        switch (mAppStatus) {
            case INIT_APP:
                // Initialize application elements that do not rely on QCAR
                // initialization
                initApplication();

                // Proceed to next application initialization status
                updateApplicationStatus(AppStatus.INIT_QCAR);
                break;

            case INIT_QCAR:
                // Initialize QCAR SDK asynchronously to avoid blocking the
                // main (UI) thread.
                // This task instance must be created and invoked on the UI
                // thread and it can be executed only once!
                try {
                    QCAR.setInitParameters(getActivity(), mQCARFlags);
                    mInitQCARTask = new InitQCARTask(this);
                    mInitQCARTask.execute();
                } catch (Exception e) {
                    DebugLog.LOGE("Initializing QCAR SDK failed");
                }
                break;

            case INIT_APP_AR:
                // Initialize Augmented Reality-specific application elements
                // that may rely on the fact that the QCAR SDK has been
                // already initialized
                initApplicationAR();

                // Proceed to next application initialization status
                updateApplicationStatus(AppStatus.INIT_TRACKER);
                break;

            case INIT_TRACKER:
                // Load the tracking data set
                //
                // This task instance must be created and invoked on the UI
                // thread and it can be executed only once!
                try {
                    mLoadTrackerTask = new LoadTrackerTask(this);
                    mLoadTrackerTask.execute();
                } catch (Exception e) {
                    DebugLog.LOGE("Loading tracking data set failed");
                }
                break;

            case INITED:
                mRenderer.mIsActive = true;
                updateApplicationStatus(AppStatus.CAMERA_RUNNING);

                break;

            case CAMERA_STOPPED:
                // Call the native function to stop the camera
                PositionDetectorNative.stopCamera();
                break;

            case CAMERA_RUNNING:
                // Call the native function to start the camera
                PositionDetectorNative.startCamera();
                boolean result = PositionDetectorNative.startAutoFocus();
                DebugLog.LOGI("Autofocus requested"
                        + (result ? " successfully."
                                : ".  Not supported in current mode or on this device."));
                break;

            default:
                throw new RuntimeException("Invalid application state");
        }
    }

    private void initApplication() {
        int screenOrientation = getActivity().getResources().getConfiguration().orientation;

        // Pass on screen orientation info to native code
        PositionDetectorNative
                .setActivityPortraitMode(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Query display dimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright.
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /** Initializes AR application components. */
    private void initApplicationAR() {
        // Do application initialization in native code (e.g. registering
        // callbacks, etc.)
        PositionDetectorNative.initApplicationNative(mScreenWidth, mScreenHeight, mNumOfTags);
    }

    // TODO
    // /**
    // * Invoked the first time when the options menu is displayed to give the
    // * Activity a chance to populate its Menu with menu items.
    // */
    // public boolean onCreateOptionsMenu(Menu menu) {
    // super.onCreateOptionsMenu(menu);
    //
    // menu.add("Toggle flash");
    // menu.add("Autofocus");
    //
    // SubMenu focusModes = menu.addSubMenu("Focus Modes");
    // focusModes.add("Auto Focus").setCheckable(true);
    // focusModes.add("Fixed Focus").setCheckable(true);
    // focusModes.add("Infinity").setCheckable(true);
    // focusModes.add("Macro Mode").setCheckable(true);
    //
    // return true;
    // }

    /** Invoked when the user selects an item from the Menu */
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Toggle flash")) {
            mFlash = !PositionDetectorNative.isFlash();
            boolean result = PositionDetectorNative.setFlash(mFlash);
            DebugLog.LOGI("Toggle flash " + (mFlash ? "ON" : "OFF") + " "
                    + (result ? "WORKED" : "FAILED") + "!!");
        } else if (item.getTitle().equals("Autofocus")) {
            boolean result = PositionDetectorNative.startAutoFocus();
            DebugLog.LOGI("Autofocus requested"
                    + (result ? " successfully."
                            : ".  Not supported in current mode or on this device."));
        } else {
            int arg = -1;
            if (item.getTitle().equals("Auto Focus"))
                arg = 0;
            if (item.getTitle().equals("Fixed Focus"))
                arg = 1;
            if (item.getTitle().equals("Infinity"))
                arg = 2;
            if (item.getTitle().equals("Macro Mode"))
                arg = 3;

            if (arg != -1) {
                item.setChecked(true);
                if (checked != null)
                    checked.setChecked(false);
                checked = item;

                boolean result = PositionDetectorNative.setFocusMode(arg);

                DebugLog.LOGI("Requested Focus mode " + item.getTitle()
                        + (result ? " successfully." : ".  Not supported on this device."));
            }
        }

        return true;
    }

    private MenuItem checked;

    private boolean mFlash = false;

    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName) {
        try {
            System.loadLibrary(nLibName);
            DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
            return true;
        } catch (UnsatisfiedLinkError ulee) {
            DebugLog.LOGE("The library lib" + nLibName + ".so could not be loaded");
        } catch (SecurityException se) {
            DebugLog.LOGE("The library lib" + nLibName + ".so was not allowed to be loaded");
        }

        return false;
    }

    public void setPoseText(float[][] mat) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = mat;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mListener != null) {
                    List<?> tagStatus = (List<?>)msg.obj;
                    for (Object obj : tagStatus) {
                        TagState tagState = (TagState)obj;
                        mListener.onPositionDetectEvent(tagState);
                    }
                }
                return true;
            }
            return false;
        }
    });

    @Override
    public void onInitQCARTaskFinished(boolean result, int progressValue) {
        // Done initializing QCAR, proceed to next application
        // initialization status:
        if (result) {
            DebugLog.LOGD("InitQCARTask::onPostExecute: QCAR initialization" + " successful");

            updateApplicationStatus(AppStatus.INIT_APP_AR);
        } else {
            // Create dialog box for display error:
            AlertDialog dialogError = new AlertDialog.Builder(getActivity()).create();
            dialogError.setButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Exiting application
                    System.exit(1);
                }
            });

            String logMessage;

            // NOTE: Check if initialization failed because the device is
            // not supported. At this point the user should be informed
            // with a message.
            if (progressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED) {
                logMessage = "Failed to initialize QCAR because this " + "device is not supported.";
            } else if (progressValue == QCAR.INIT_CANNOT_DOWNLOAD_DEVICE_SETTINGS) {
                logMessage = "Network connection required to initialize camera "
                        + "settings. Please check your connection and restart "
                        + "the application. If you are still experiencing "
                        + "problems, then your device may not be currently " + "supported.";
            } else {
                logMessage = "Failed to initialize QCAR.";
            }

            // Log error:
            DebugLog.LOGE("InitQCARTask::onPostExecute: " + logMessage + " Exiting.");

            // Show dialog box with error message:
            dialogError.setMessage(logMessage);
            dialogError.show();
        }
    }

    @Override
    public void onLoadTrackerTaskFinished(boolean result) {
        DebugLog.LOGD("LoadTrackerTask::onPostExecute: execution "
                + (result ? "successful" : "failed"));

        // Done loading the tracker, update application status:
        updateApplicationStatus(AppStatus.INITED);
    }

    public boolean startAutoFocus() {
        return PositionDetectorNative.startAutoFocus();
    }

    public void setOnPositionDetectorListener(OnPositionDetectorListener listener) {
        mListener = listener;
    }

    public void onPositionDetectEvent(List<TagState> tagStatus) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = tagStatus;
        mHandler.sendMessage(msg);
    }
}
