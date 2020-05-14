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

package de.lmu.ifi.researchime.data.keyboard.model;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class KeyboardModel implements Serializable {

    private long startDate;
    private List<KeyboardLayoutModel> altAlphabetLayouts;
    private KeyboardLayoutModel defaultAlphabetLayout;
    private KeyboardLayoutModel symbolsLayout;
    private KeyboardLayoutModel symbolsShiftedLayout;

    private String id;
    private String name;

    private boolean isTrackingEnabled = true;
    private boolean showHandPosture = true;
    private boolean anonymizeInputEvents = true;

    public String getKeyboardId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public KeyboardLayoutModel getAlphabetLayout(Locale locale) {
        if(altAlphabetLayouts != null){
            for(KeyboardLayoutModel layout : altAlphabetLayouts){
                if(layout.hasLocale(locale)){
                    return layout;
                }
            }
        }
        return defaultAlphabetLayout;
    }

    public KeyboardLayoutModel getSymbolsLayout(){
        return symbolsLayout;
    }

    public KeyboardLayoutModel getSymbolsShiftedLayout() {
        return symbolsShiftedLayout;
    }

    public boolean isTrackingEnabled(){
        return isTrackingEnabled;
    }

    public boolean showHandPosture(){
        return showHandPosture;
    }

    public boolean isAnonymizeInputEvents() {
        return anonymizeInputEvents;
    }
}
