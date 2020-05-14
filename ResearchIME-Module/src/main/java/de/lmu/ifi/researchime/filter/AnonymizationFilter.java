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


import de.lmu.ifi.researchime.EventBuffer;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;

public abstract class AnonymizationFilter {

    /**
     * Anonymizes portions of the events
     * @param events buffer containing events to be filtered
     * @return copy of the buffer with same amount of events but some of them anonymized
     */
    public EventBuffer filter(EventBuffer events) {
        EventBuffer result = new EventBuffer();
        resetFilter();
        for (Event event : events.getAll()){
            if(shouldAnonymizeEvent(event)){
                event.anonymize();
            }
            result.add(event);
        }
        return result;
    }

    protected abstract void resetFilter();

    protected abstract boolean shouldAnonymizeEvent(Event event);
}
