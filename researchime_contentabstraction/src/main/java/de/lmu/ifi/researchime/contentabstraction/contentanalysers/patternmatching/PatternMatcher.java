package de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentUnit;

public interface PatternMatcher {

    /**
     *
     * @param contentUnit the content to be analysed. Usually the contentAfter property of a ContentChangeEvent
     * @return the matching substrings
     */
    List<String> match(ContentUnit contentUnit);

}
