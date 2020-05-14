package de.lmu.ifi.researchime.contentabstraction.logging;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.ifi.lmu.researchime.contentabstraction.BuildConfig;
import de.lmu.ifi.researchime.base.logging.BaseInputLogger;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedActionRawContent;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedWordAction;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;

public class InputLogger extends BaseInputLogger {

    public static synchronized void log(Context aContext, List<AbstractedAction> actions, String listName) {
        if (!isInitialized) {
            init(aContext);
        }

        // build debug output
        StringBuilder notificationTextBuilder = new StringBuilder();
        notificationTextBuilder.append(listName).append(": ");
        for(AbstractedAction action : actions) {
            notificationTextBuilder.append(action.toString());
        }
        String notificationText = notificationTextBuilder.toString();

        // --- build notification ---
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_NOTIFICATION) {
            logToNotification(actions.size() + " actions categorized", notificationText);
        }

        // --- log to file ---
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_FILE) {
            //logToFile("categorization",notificationText);
            logToTableFile(actions);
        }
    }

    public static void logWhitelistWords(Context aContext, Map<String,Integer> wordsFound, LogicalWordList logicalWordList){
        if (!isInitialized) {
            init(aContext);
        }

        // --- build log ---
        StringBuilder notificationTextBuilder = new StringBuilder();
        for(Map.Entry<String,Integer> aWord : wordsFound.entrySet()){
            notificationTextBuilder.append("\""+aWord.getKey()+"\":"+(aWord.getValue()));
            notificationTextBuilder.append(" ; ");
        }
        String notificationText = notificationTextBuilder.toString();

        // --- build notification ---
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_NOTIFICATION) {
            logToNotification("whitelist lookup", notificationText);
        }

        // --- log to file ---
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_FILE) {
            logToTableFile(wordsFound, logicalWordList);
        }
    }

    protected static void logToTableFile(List<AbstractedAction> abstractedActions) {
        // create log folder
        File logDirectory = new File(Environment.getExternalStorageDirectory() + "/ResearchIME");
        try {
            if (!logDirectory.exists()) {
                logDirectory.mkdirs();
            }

            File logFile = new File(logDirectory,"log-rime-ca-categorization.csv");
            if (!logFile.exists()) {
                logFile.createNewFile();
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append("\"date\"").append(",");
                buf.append("\"content change type\"").append(",");
                buf.append("\"raw content before\"").append(",");
                buf.append("\"raw content after\"").append(",");
                buf.append("\"applied logical category list\"").append(",");
                buf.append("\"category before\"").append(",");
                buf.append("\"category after\"").append(",");
                buf.newLine();
                buf.close();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            for (AbstractedAction abstractedAction : abstractedActions) {
                buf.append("\"").append(DATE_FORMAT.format(new Date())).append("\"").append(",");
                if (abstractedAction.getContentChangeEvent() != null && abstractedAction.getContentChangeEvent().getType() != null) {
                    buf.append("\"").append(abstractedAction.getContentChangeEvent().getType().name()).append("\"").append(",");
                } else {
                    buf.append("\"\",");
                }
                if (abstractedAction.getContentChangeEvent() != null && abstractedAction.getContentChangeEvent().getContentUnitBefore() != null) {
                    buf.append("\"").append(abstractedAction.getContentChangeEvent().getContentUnitBefore().getContent()).append("\"").append(",");
                } else {
                    buf.append("\"\",");
                }
                if (abstractedAction.getContentChangeEvent() != null && abstractedAction.getContentChangeEvent().getContentUnitAfter() != null) {
                    buf.append("\"").append(abstractedAction.getContentChangeEvent().getContentUnitAfter().getContent()).append("\"").append(",");
                } else {
                    buf.append("\"\",");
                }
                if (abstractedAction.getLogicalCategoryList() != null) {
                    buf.append("\"").append(abstractedAction.getLogicalCategoryList().getLogicallistName()).append("\"").append(",");
                } else {
                    buf.append("\"\",");
                }
                if (abstractedAction instanceof AbstractedWordAction) {
                    AbstractedWordAction abstractedWordAction = (AbstractedWordAction) abstractedAction;
                    buf.append("\"").append(abstractedWordAction.getCategoryBefore()).append("\"").append(",");
                    buf.append("\"").append(abstractedWordAction.getCategoryAfter()).append("\"").append(",");
                }
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            System.out.println("LogHelper: cannot write logfile to "+logDirectory.getAbsolutePath());
        }
    }

    protected static void logToTableFile(Map<String,Integer> wordsFound, LogicalWordList logicalWordList){
        // create log folder
        File logDirectory = new File(Environment.getExternalStorageDirectory() + "/ResearchIME");
        try {
            if (!logDirectory.exists()) {
                logDirectory.mkdirs();
            }

            File logFile = new File(logDirectory,"log-rime-ca-wordfrequency.csv");
            if (!logFile.exists()) {
                logFile.createNewFile();
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append("\"date\"").append(",");
                buf.append("\"applied logical category list\"").append(",");
                buf.append("\"word\"").append(",");
                buf.append("\"count\"").append(",");
                buf.newLine();
                buf.close();
            }

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            for (Map.Entry<String,Integer> word : wordsFound.entrySet()) {
                buf.append("\"").append(DATE_FORMAT.format(new Date())).append("\"").append(",");
                if (logicalWordList.getLogicallistName() != null) {
                    buf.append("\"").append(logicalWordList.getLogicallistName()).append("\"").append(",");
                }
                buf.append("\"").append(word.getKey()).append("\",");
                buf.append(word.getValue().toString()).append(",");
                buf.newLine();
            }
            buf.close();
        } catch (IOException e) {
            System.out.println("LogHelper: cannot write logfile to "+logDirectory.getAbsolutePath());
        }
    }

    public static void logPatternMatch(Context context, String message, String matcherName, AbstractedAction abstractedAction){
        if (!isInitialized) {
            init(context);
        }
        String matchingItem = "-";
        switch(abstractedAction.getContentUnitEventType()){
            case ADDED:
                matchingItem = "ADDED ";
                if (abstractedAction instanceof AbstractedActionRawContent){
                    matchingItem += ((AbstractedActionRawContent) abstractedAction).getRawContentAfter();
                }
                else if (abstractedAction instanceof AbstractedWordAction){
                    matchingItem += ((AbstractedWordAction) abstractedAction).getCategoryAfter();
                }
                break;
            case CHANGED:
                matchingItem = "CHANGED ";
                if (abstractedAction instanceof AbstractedActionRawContent){
                    matchingItem += ((AbstractedActionRawContent) abstractedAction).getRawContentBefore();
                    matchingItem += " -> ";
                    matchingItem += ((AbstractedActionRawContent) abstractedAction).getRawContentAfter();
                }
                else if (abstractedAction instanceof AbstractedWordAction){
                    matchingItem += ((AbstractedWordAction) abstractedAction).getCategoryBefore();
                    matchingItem += " -> ";
                    matchingItem += ((AbstractedWordAction) abstractedAction).getCategoryAfter();
                }
                break;
            case REMOVED:
                matchingItem = "REMOVED ";
                if (abstractedAction instanceof AbstractedActionRawContent){
                    matchingItem += ((AbstractedActionRawContent) abstractedAction).getRawContentBefore();
                }
                else if (abstractedAction instanceof AbstractedWordAction){
                    matchingItem += ((AbstractedWordAction) abstractedAction).getCategoryBefore();
                }
                break;
        }
        if (de.lmu.ifi.researchime.base.BuildConfig.LOG_TO_NOTIFICATION) {
            logToNotification(
                    "Pattern Match",
                    "matcher " + matcherName + ": " + matchingItem + " in msg " + message
            );
        }
    }

}
