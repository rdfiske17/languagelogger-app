package de.lmu.ifi.researchime.contentextraction;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;

public class FullMessageWordExtractor extends AbstractWordExtractor {

    public static final String TAG = "FullMessageWordExtractor";

    /**
     * like @see extractHigherLevelContentChangeEvents , but does only regard the resulting full message
     * @param allEvents
     * @return
     */
    @Override
    public List<ContentChangeEvent> extractHigherLevelContentChangeEvents(InputContent inputContent, List<Event> allEvents, String initialContent) {
        LogHelper.i(TAG,"extractHigherLevelContentChangeEvents() with "+allEvents.size()+" events");
        // filter to keep only Content Change events
        List<Event> events = new ArrayList<>();
        for(Event event : allEvents){
            if (event instanceof de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) {
                events.add(event);
            }
        }

        // extract the higher-level content change events
        if (events.size() < 1){
            return new ArrayList<>();
        }

        //we do not know the initial content. assumptions:
        String finalContent = ((de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent) events.get(events.size()-1)).getContent();

        List<ContentChangeEvent> highLevelEvents = new ArrayList<>();

        List<String> words = splitSentenceIntoWords(finalContent);
        for(String word : words){
            highLevelEvents.add(new ContentChangeEvent(
                    inputContent,
                    ContentUnitEventType.CONTAINED,
                    null,
                    new ContentUnit(word),
                    events.get(events.size()-1).getTimestamp()
            ));
        }

        return highLevelEvents;
    }



}
