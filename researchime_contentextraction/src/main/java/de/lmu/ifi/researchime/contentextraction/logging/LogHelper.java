package de.lmu.ifi.researchime.contentextraction.logging;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.lmu.ifi.researchime.contentextraction.BuildConfig;


public class LogHelper {
    private static final String LOG_PREFIX = "researchime_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static final SimpleDateFormat LOGFILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH");

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }


    public static void v(String tag, Object... messages) {
        // Only log VERBOSE if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.VERBOSE, null, messages);
        }
    }

    public static void d(String tag, Object... messages) {
        // Only log DEBUG if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, messages);
        }
    }

    public static void i(String tag, Object... messages) {
        log(tag, Log.INFO, null, messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag, Log.WARN, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages) {
        log(tag, Log.WARN, t, messages);
    }

    public static void e(String tag, Object... messages) {
        log(tag, Log.ERROR, null, messages);
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
    }

    public static void log(String tag, int level, Throwable t, Object... messages) {
        //if (LogHelper.isLoggable(tag, level)) {
        if (BuildConfig.DEBUG || BuildConfig.LOG_TO_FILE) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
            try {
                writeToFile(new Date().toString() + " [" + level + "," + tag + "] " + message);
            } catch (Exception e) {
            }
        }
    }

    public static void logOnCreate(String tag) {
        d(tag, "onCreate()");
    }

    public static void logOnCreateDone(String tag) {
        d(tag, "onCreate done");
    }

    public static void logOnReceive(String tag) {
        d(tag, "onReceive()");
    }

    public static void logOnReceiveDone(String tag) {
        d(tag, "onReceive done");
    }

    public static void logOnChange(String tag) {
        d(tag, "onChange()");
    }

    public static void logOnChangeDone(String tag) {
        d(tag, "onChange done");
    }

    public static void logDoInBackground(String tag) {
        d(tag, "doInBackground()");
    }

    public static void logBuildJob(String tag) {
        d(tag, "Building job");
    }

    public static void logBuildJobDone(String tag) {
        d(tag, "Built job");
    }

    public static void logScheduleJob(String tag, String jobInfo) {
        d("scheduleJob()");
        d("Job Info: " + jobInfo);
    }

    public static void logSaveEntry(String tag, Object... values) {
        d(tag, "saveEntry()");
        logCommaSeparatedMessagesWithPrefix(tag, "Going to save:", values);
    }

    public static void logMetaData(String tag, Object... values) {
        logCommaSeparatedMessagesWithPrefix(tag, "Meta Data:", values);
    }

    public static void logCommaSeparatedMessagesWithPrefix(String tag, String prefix, Object... values) {
        String message;
        if (values != null && values.length == 1) {
            message = values[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (values != null) for (Object v : values) {
                sb.append(v);
                sb.append(", ");
            }
            message = sb.substring(0, sb.length() - 2);
        }
        d(tag, prefix + " " + message);
    }

    private static void writeToFile(String text) {
        if (BuildConfig.LOG_TO_FILE) {

            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/ResearchIME");
            File logDirectory = new File(appDirectory + "/logs");

            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdirs();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdirs();
            }


            File logFile = new File(logDirectory, "log-" + LOGFILE_NAME_DATE_FORMAT.format(new Date()) + ".txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("LogHelper: cannot write logfile to " + logFile.getAbsolutePath());
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                System.out.println("LogHelper: cannot write logfile to " + logFile.getAbsolutePath());
            }
        }
    }
}

