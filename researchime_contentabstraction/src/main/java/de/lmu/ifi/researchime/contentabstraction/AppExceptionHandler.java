package de.lmu.ifi.researchime.contentabstraction;

import de.lmu.ifi.researchime.base.logging.LogHelper;

/*
 * https://medium.com/@ssaurel/how-to-auto-restart-an-android-application-after-a-crash-or-a-force-close-error-1a361677c0ce
 * Called on every exception, that makes the app close
 */
public class AppExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "AppExceptionHandler";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogHelper.e(TAG, ex);

        System.exit(2);
    }
}
