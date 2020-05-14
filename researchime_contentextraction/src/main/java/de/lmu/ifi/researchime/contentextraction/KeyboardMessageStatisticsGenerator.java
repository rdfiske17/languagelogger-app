package de.lmu.ifi.researchime.contentextraction;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentabstraction.model.MessageStatistics;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;


public class KeyboardMessageStatisticsGenerator {

    public static final String TAG = "KeyboardMessageStatisticsGenerator";

    public MessageStatistics createMessageStatistics(List<Event> bufferEvents){
        LogHelper.i(TAG,"creating MessageStatistics for " +bufferEvents.size() + " bufferevents...");
        MessageStatistics messageStatistics = new MessageStatistics();

        // filter to keep only Content Change events
        List<ContentChangeEvent> events = new ArrayList<>();
        for(Event event : bufferEvents){
            if (event instanceof ContentChangeEvent) {
                events.add((ContentChangeEvent)event);
            }
        }

        // ---- character count added -----
        Integer characterCountAdded = 0;
        if (events.size() == 1){
            characterCountAdded++;
        }
        if (events.size() >= 2) {
            for (int i = 1; i < events.size() - 1; i++) {
                Integer contentLengthDiff = events.get(i).getContentLength() - events.get(i-1).getContentLength();
                if (contentLengthDiff > 0) {
                    characterCountAdded += contentLengthDiff;
                }
            }
        }
        messageStatistics.setCharacterCountAdded(characterCountAdded);

        // ---- character count altered ------
        Integer characterCountAltered = events.size();
        messageStatistics.setCharacterCountAltered(characterCountAltered);

        // ---- character count submitted ----
        if (events.size() < 1){
            messageStatistics.setCharacterCountSubmitted(0);
        }
        else {
            Integer characterCountSubmitted;
            if(events.get(events.size() - 1) == null){
                characterCountSubmitted = 0;
            } else {
                characterCountSubmitted = events.get(events.size() - 1).getContentLength();
            }
            messageStatistics.setCharacterCountSubmitted(characterCountSubmitted);
        }

        // ---- source app ----
        if (events.size() > 0){
            String inputTargetPackageName = events.get(0).getFieldPackageName();
            messageStatistics.setInputTargetApp(inputTargetPackageName);
        }

        // ---- start and end time ----
        if (events.size() > 0){
            messageStatistics.setTimestampTypeStart(events.get(0).getTimestamp());
            messageStatistics.setTimestampTypeEnd(events.get(events.size()-1).getTimestamp());
        }

        // ---- fieldHintText ----
        if (events.size() > 0){
            for (Event event : events){
                if(!StringUtils.isEmpty(event.getFieldHintText())){
                    messageStatistics.setFieldHintText(event.getFieldHintText());
                    break;
                }
            }
        }

        messageStatistics.save();
        return messageStatistics;
    }


}
