package net.cattaka.positiondetector.task;

import com.qualcomm.QCAR.QCAR;
import android.os.AsyncTask;

public class InitQCARTask extends AsyncTask<Void, Integer, Boolean> {
    public interface InitQCARTaskListener {
        public void onInitQCARTaskFinished(boolean result, int progressValue);
    }
    
    private InitQCARTaskListener mListener;
    private int mProgressValue = -1;
    
    public InitQCARTask(InitQCARTaskListener listener) {
        mListener = listener;
    }

    protected Boolean doInBackground(Void... params) {

        do {
            // QCAR.init() blocks until an initialization step is complete,
            // then it proceeds to the next step and reports progress in
            // percents (0 ... 100%)
            // If QCAR.init() returns -1, it indicates an error.
            // Initialization is done when progress has reached 100%.
            mProgressValue = QCAR.init();

            // Publish the progress value:
            publishProgress(mProgressValue);

            // We check whether the task has been canceled in the meantime
            // (by calling AsyncTask.cancel(true))
            // and bail out if it has, thus stopping this thread.
            // This is necessary as the AsyncTask will run to completion
            // regardless of the status of the component that started is.
        } while (!isCancelled() && mProgressValue >= 0
                && mProgressValue < 100);

        return (mProgressValue > 0);
    }

    protected void onProgressUpdate(Integer... values) {
        // Do something with the progress value "values[0]", e.g. update
        // splash screen, progress bar, etc.
    }

    protected void onPostExecute(Boolean result) {
        if (mListener != null) {
            mListener.onInitQCARTaskFinished(result, mProgressValue);
        }
    }
}

