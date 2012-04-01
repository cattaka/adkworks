package net.cattaka.positiondetector.task;

import com.qualcomm.QCAR.QCAR;

import android.os.AsyncTask;

public class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
    public interface LoadTrackerTaskListener {
        public void onLoadTrackerTaskFinished(boolean result);
    }
    
    private LoadTrackerTaskListener mListener;
    
    public LoadTrackerTask(LoadTrackerTaskListener listener) {
        super();
        mListener = listener;
    }

    protected Boolean doInBackground(Void... params) {
        // Initialize with invalid value
        int progressValue = -1;

        do {
            progressValue = QCAR.load();
            publishProgress(progressValue);

        } while (!isCancelled() && progressValue >= 0
                && progressValue < 100);

        return (progressValue > 0);
    }

    protected void onProgressUpdate(Integer... values) {
        // Do something with the progress value "values[0]", e.g. update
        // splash screen, progress bar, etc.
    }

    protected void onPostExecute(Boolean result) {
        if (mListener != null) {
            mListener.onLoadTrackerTaskFinished(result);
        }
    }
}
