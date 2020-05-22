package de.lmu.ifi.researchime.contentextraction.logging;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.ifi.lmu.researchime.contentabstraction.BuildConfig;
import de.ifi.lmu.researchime.contentabstraction.R;
import de.lmu.ifi.researchime.base.logging.BaseInputLogger;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;


/**
 * only for debugging purposes! Remove in production
 */
public class InputLogger {

    private static Context context;
    private static File logDirectory;
    private static boolean isInitialized = false;

    // must be the same as in App Module (ApplicationController)
    public static final String NOTIFICATION_CHANNEL_ID = "RESEARCHIME_NOTIFICATION_CHANNEL";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

    public static synchronized void init(Context aContext){
        if (aContext != null) {
            context = aContext;
        }
        if (context == null) {
            LogHelper.e("InputLogger","cannot log logs, context is not available");
        }

        try {
            if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_FILE) {
                logDirectory = new File(Environment.getExternalStorageDirectory() + "/ResearchIME");
                if (!logDirectory.exists()) {
                    logDirectory.mkdirs();
                }
            }
            isInitialized = true;
            LogHelper.d("InputLogger", "init completed");
        } catch (Exception e) {
            LogHelper.e("InputLogger","could not init csv writer");
        }
    }

    public static synchronized void log(Context aContext, List<Event> events, List<ContentChangeEvent> highLevelEvents) {
        if (!isInitialized) {
            init(aContext);
        }

        if (events == null){
            return;
        }
        boolean containsAtLeastOneContentChangeEvent = false;
        for (Event event : events){
            if(event instanceof de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) {
                containsAtLeastOneContentChangeEvent = true;
                break;
            }
        }
        if (!containsAtLeastOneContentChangeEvent) {
            return;
        }

        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_FILE) {
            try {
                File logFile = new File(logDirectory, "log-rime-ce-wordextraction.csv");
                if (!logFile.exists()) {
                    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(logFile, true));
                    fileWriter.append("\"").append("date").append("\",");
                    fileWriter.append("\"").append("content before").append("\",");
                    fileWriter.append("\"").append("content after").append("\",");
                    fileWriter.newLine();
                    fileWriter.close();
                }

                BufferedWriter fileWriter = new BufferedWriter(new FileWriter(logFile, true));
                fileWriter.append("\"").append(DATE_FORMAT.format(new Date())).append("\"").append(",");
                fileWriter.append("\"").append(getFirstContentChangeEvent(events).getContent()).append("\"").append(",");
                fileWriter.append("\"").append(getLastContentChangeEvent(events).getContent()).append("\"").append(",");
                for (ContentChangeEvent contentChangeEvent : highLevelEvents) {
                    fileWriter.append("\"");
                    fileWriter.append(contentChangeEvent.getType().name()).append(":");
                    if (contentChangeEvent.isWordAddedEvent()) {
                        fileWriter.append((contentChangeEvent).getContentUnitAfter().getAsString());
                    } else if (contentChangeEvent.isWordChangedEvent()) {
                        fileWriter.append(contentChangeEvent.getContentUnitBefore().getAsString()).append("->").append(contentChangeEvent.getContentUnitAfter().getAsString());
                    } else if (contentChangeEvent.isWordRemovedEvent()) {
                        fileWriter.append(contentChangeEvent.getRemovedWord().getAsString());
                    }
                    fileWriter.append("\",");
                }
                fileWriter.newLine();
                fileWriter.close();
            } catch (Exception e) {
                LogHelper.e("InputLogger", "writing input event log to file failed");
            }
        }

        StringBuilder eventsStringBuilder = new StringBuilder();
        for(ContentChangeEvent contentChangeEvent : highLevelEvents) {
            if(contentChangeEvent.isWordAddedEvent()){
                eventsStringBuilder.append("ADDED:").append(contentChangeEvent.getAddedWord());
            }
            else if(contentChangeEvent.isWordChangedEvent()){
                eventsStringBuilder.append("CHANGED:").append(contentChangeEvent.getContentUnitBefore()).append("->").append(contentChangeEvent.getContentUnitAfter());
            }
            else if(contentChangeEvent.isWordRemovedEvent()){
                eventsStringBuilder.append("REMOVED:").append(contentChangeEvent.getRemovedWord());
            }
            eventsStringBuilder.append(", ");
        }
        String notificationText = new StringBuilder()
                .append(getFirstContentChangeEvent(events).getContent())
                .append("->")
                .append(getLastContentChangeEvent(events).getContent())
                .append(" => ")
                .append(eventsStringBuilder).toString();
        //build notification
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_NOTIFICATION) {
            int notificationId = (int) System.currentTimeMillis();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.research_ic_lock_closed_light)
                    .setContentTitle(highLevelEvents.size() + " high level events extracted")
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setContentText(notificationText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setAutoCancel(true);
            //send notification
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, mBuilder.build());
        }
    }





    private static de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent getFirstContentChangeEvent(List<Event> events){
        for(Event event : events){
            if (event instanceof de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) {
                return (de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) event;
            }
        }
        return null;
    }

    private static de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent getLastContentChangeEvent(List<Event> events){
        de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent cce = null;
        for(Event event : events){
            if (event instanceof de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) {
                cce = (de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) event;
            }
        }
        return cce;
    }

}
