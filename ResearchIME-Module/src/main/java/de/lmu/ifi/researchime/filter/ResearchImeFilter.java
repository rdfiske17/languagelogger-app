/*
 * Copyright (C) 2016 - 2018 ResearchIME Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lmu.ifi.researchime.filter;

import java.util.Arrays;

import de.lmu.ifi.researchime.contentextraction.model.event.Event;
import de.lmu.ifi.researchime.contentextraction.model.event.TouchEvent;

public class ResearchImeFilter extends RandomNGramFilter {

    /**
     * some specific key codes that are justified to ignore from anonymization handling
     */
    private static final String[] NON_ANONYMOUS_KEY_CODES = new String[]{
            "delete",
            "languageSwitch",
            "symbol"
    };

    /**
     * we always anonymize content change events because they would reveal the complete content,
     * not only the content of the n-gram
     * we also want to always anonymize suggestion-pick and auto-correct events
     * because they could reveal more than n continuous symbols
     */
    private static final Event.Type[] ANONYMOUS_EVENT_TYPES = new Event.Type[]{
            Event.Type.CONTENT_CHANGE,
            Event.Type.AUTO_CORRECT,
            Event.Type.SUGGESTION_PICKED
    };

    /**
     * Creates a filter which lets pass n-grams with probability p and anonymizes the remaining.
     * Uses touch down events to determine n-grams. Therefore an n-gram may also contain shift, backspace, ... presses.
     * Between two n-grams is a minimum gap of 1.
     *
     * @param nGramFilterN n-gram size
     * @param nGramFilterP n-gram filter probability (between 0 and 1)
     */
    public ResearchImeFilter(int nGramFilterN, double nGramFilterP) {
        super(nGramFilterN, nGramFilterP);
    }

    @Override
    protected boolean shouldAnonymizeEvent(Event event) {
        if (shouldPreventFromAnonymization(event)) {
            //don't trigger the n-gram-iterator - it's justified as the n-gram-policy stays untouched
            return false;
        }

        boolean isRandomFilterAnonymous = super.shouldAnonymizeEvent(event);
        return isRandomFilterAnonymous || shouldAnonymizeEventType(event.getType());
    }

    private boolean shouldPreventFromAnonymization(Event event) {
        if (event instanceof TouchEvent) {
            return Arrays.asList(NON_ANONYMOUS_KEY_CODES).contains(((TouchEvent) event).getCode());
        }
        return false;
    }

    private boolean shouldAnonymizeEventType(Event.Type eventType) {
        return Arrays.asList(ANONYMOUS_EVENT_TYPES).contains(eventType);
    }
}
