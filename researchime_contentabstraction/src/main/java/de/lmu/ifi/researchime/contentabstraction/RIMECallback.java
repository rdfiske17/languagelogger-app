package de.lmu.ifi.researchime.contentabstraction;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency;

public interface RIMECallback {

    /**
     *  method that will be called when the Categorization events are ready
     * @param abstractedActionEvents the extracted events
     * @return true if the parent-app did successfully accommodate the events. If false, the RIME content extraction module will keep the events and try again later
      */
    boolean onAbstractedActionEventsReady(List<AbstractedAction> abstractedActionEvents);

    /**
     *  method that will be called when the word frequency counts are updated
     * @param wordFrequencies the current accumulated word frequencies
     * @return true if the parent-app did successfully accommodate the events. If false, the RIME content extraction module will keep the events and try again later
     */
    boolean onWordFrequenciesChanged(List<WordFrequency> wordFrequencies);

}
