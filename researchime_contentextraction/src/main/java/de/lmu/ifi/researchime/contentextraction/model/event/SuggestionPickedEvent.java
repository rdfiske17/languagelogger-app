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

package de.lmu.ifi.researchime.contentextraction.model.event;

public class SuggestionPickedEvent extends Event{

    private String suggestion;
    private Integer suggestionLength;

    public SuggestionPickedEvent(String suggestion){
        super(Event.Type.SUGGESTION_PICKED);
        this.suggestion = suggestion;
        if (suggestion != null){
            suggestionLength = suggestion.length();
        }
    }

    @Override
    public String toString() {
        return String.format("%s '%s'", super.toString(), suggestion);
    }

    @Override
    public String toBriefString() {
        return String.format("%s length: %d", super.toBriefString(), suggestionLength);
    }

    @Override
    public void anonymize() {
        super.anonymize();
        suggestion = null;
    }
}
