package com.github.pkunk.progressquest.ui.util;

import android.app.Activity;
import android.os.AsyncTask;
import com.github.pkunk.progressquest.ui.TextProgressBar;

/**
* User: pkunk
* Date: 2012-01-28
*/
public class TaskBarUpdater extends AsyncTask {

    private final TextProgressBar taskBar;

    public TaskBarUpdater(Activity activity, int resId) {
        super();
        taskBar = (TextProgressBar) activity.findViewById(resId);
    }

    @Override
    protected Object doInBackground(Object... params) {
        while (!isCancelled()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        taskBar.incrementProgressBy(100);
        taskBar.setText((taskBar.getProgress() * 100 / taskBar.getMax()) + "%");
    }
}
