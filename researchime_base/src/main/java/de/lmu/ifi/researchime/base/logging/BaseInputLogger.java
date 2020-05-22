package de.lmu.ifi.researchime.base.logging;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;

import android.support.v4.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.researchime.base.R;


/**
 * only for debugging purposes! Remove in production
 */
public class BaseInputLogger {

    private static Context context;
    protected static boolean isInitialized = false;

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // must be the same as in App Module (ApplicationController)
    public static final String NOTIFICATION_CHANNEL_ID = "RESEARCHIME_NOTIFICATION_CHANNEL";

    public static synchronized void init(Context aContext){
        if (aContext != null) {
            context = aContext;
        }
        if (context == null) {
            LogHelper.e("InputLogger","cannot log logs, context is not available");
        }

        isInitialized = true;
        LogHelper.d("InputLogger", "init completed");
    }









    protected static void logToNotification(String title, String text) {
        try {
            int notificationId = (int) System.currentTimeMillis();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.research_ic_lock_closed_light)
                    .setContentTitle(title)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setContentText(text)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setAutoCancel(true);
            //send notification
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, mBuilder.build());
        } catch (Exception e){
            LogHelper.e("InputLogger","posting notification failed");
        }
    }



}
