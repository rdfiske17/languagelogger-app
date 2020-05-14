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

import java.util.Random;

import de.lmu.ifi.researchime.contentextraction.model.event.Event;

public class RandomNGramFilter extends AnonymizationFilter {

    private final int n;
    private final double p;
    private final Random random;

    private boolean isWithinNGram = false;
    private boolean skippedOne = true;
    private int touchDownCounter = 0;

    /**
     * Creates a filter which lets pass n-grams with probability p and anonymizes the remaining.
     * Uses touch down events to determine n-grams. Therefore an n-gram may also contain shift, backspace, ... presses.
     * Between two n-grams is a minimum gap of 1.
     * @param n n-gram size
     * @param p n-gram filter probability (between 0 and 1)
     */
    public RandomNGramFilter(int n, double p){
        this.n = n;
        this.p = p;
        this.random = new Random();
    }

    @Override
    protected void resetFilter() {
        isWithinNGram = false;
        skippedOne = true;
        touchDownCounter = 0;
    }

    @Override
    protected boolean shouldAnonymizeEvent(Event event) {
        if (Event.Type.TOUCH_DOWN.equals(event.getType())){
            if (!isWithinNGram){
                if (skippedOne){
                    if (random.nextDouble() < p) {
                        isWithinNGram = true;
                        skippedOne = false;
                    }
                }
                else{
                    skippedOne = true;
                }
            }
            else {
                touchDownCounter++;
                if(touchDownCounter == n){
                    isWithinNGram = false;
                    touchDownCounter = 0;
                }
            }
        }

        //policy: anonymize events that are not in n-gram
        return !isWithinNGram;
    }
}
