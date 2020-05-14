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

import org.apache.commons.lang3.StringUtils;

public class AutoCorrectEvent extends Event {

    private String autoCorrectBefore;
    private String autoCorrectAfter;
    private Integer autoCorrectBeforeLength;
    private Integer autoCorrectAfterLength;
    private Integer autoCorrectLevenshteinDistance;
    private Integer autoCorrectOffset;

    public AutoCorrectEvent(String before, String after, int offset) {
        super(Type.AUTO_CORRECT);
        this.autoCorrectBefore = before;
        this.autoCorrectAfter = after;
        this.autoCorrectOffset = offset;

        // calculating the levenshtein distance.
        // we could also do this retrospectively but not if the event is anonymized
        // if it is anonymized we still want to know the edit distance and the word lengths
        if (before != null && after != null){
            autoCorrectLevenshteinDistance = StringUtils.getLevenshteinDistance(before, after);
            autoCorrectBeforeLength = before.length();
            autoCorrectAfterLength = after.length();
        }
    }

    @Override
    public String toString() {
        return String.format("%s replaced '%s' with '%s' at offset %d", super.toString(), autoCorrectBefore, autoCorrectAfter, autoCorrectOffset);
    }

    @Override
    public String toBriefString() {
        return String.format("%s dist: %d", super.toBriefString(), autoCorrectLevenshteinDistance);
    }

    @Override
    public void anonymize() {
        super.anonymize();
        autoCorrectBefore = null;
        autoCorrectAfter = null;
    }
}
