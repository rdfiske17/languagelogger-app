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

import android.graphics.Typeface;

import java.util.List;

public class KeyboardKeyModel {

    private float xPos;
    private boolean widthFillRight;
    private float keyWidth;
    private String backgroundType;
    private float visualInsetsLeft;
    private float visualInsetsRight;
    private String[] labelFlags;
    private String[] actionFlags;
    private String[] moreKeys;
    private String[] additionalMoreKeys;
    private String hintLabel;
    private String iconDisabled;
    private String keySpec;
    private String typeface;
    private int textSize;
    private int hintTextSize;

    private List<KeyboardKeyConditionalModel> conditional;

    public float getXPos() {
        return xPos;
    }

    public boolean isWidthFillRight() {
        return widthFillRight;
    }

    public float getWidthPercentage() {
        return keyWidth;
    }

    public String getBackgroundType(String layoutCondition, String modeCondition, int imeAction) {
        if(conditional != null){
            for(KeyboardKeyConditionalModel model : conditional){
                if(model.hasCondition(layoutCondition, modeCondition, imeAction)){
                    return model.getBackgroundType() == null ? backgroundType : model.getBackgroundType();
                }
            }
        }
        return backgroundType;
    }

    public float getVisualInsetsLeft() {
        return visualInsetsLeft;
    }

    public float getVisualInsetsRight() {
        return visualInsetsRight;
    }

    public int getKeyLabelFlags() {
        return KeyboardFlagsBuilder.getKeyLabelFlags(labelFlags);
    }

    public int getActionFlags() {
        return KeyboardFlagsBuilder.getKeyActionFlags(actionFlags);
    }

    public String[] getMoreKeys() {
        return moreKeys;
    }

    public String[] getAdditionalMoreKeys() {
        return additionalMoreKeys;
    }

    public String getHintLabel() {
        return hintLabel;
    }

    public String getIconDisabled() {
        return iconDisabled;
    }

    public String getKeySpec(String layoutCondition, String modeCondition, int imeAction) {
        if(conditional != null){
            for(KeyboardKeyConditionalModel model : conditional){
                if(model.hasCondition(layoutCondition, modeCondition, imeAction)){
                    return model.getKeySpec() == null ? keySpec : model.getKeySpec();
                }
            }
        }
        return keySpec;
    }

    public boolean hasTypeface(){
        return getTypeface() != null;
    }

    public Typeface getTypeface(){
        if(typeface != null){
            switch (typeface.toLowerCase()){
                case "serif" : return Typeface.SERIF;
                case "monospace" : return Typeface.MONOSPACE;
                case "bold": return Typeface.DEFAULT_BOLD;
            }
        }
        return Typeface.DEFAULT;
    }

    public boolean hasTextSize(){
        return getTextSize() > 0;
    }

    public int getTextSize(){
        return textSize;
    }

    public boolean hasHintTextSize(){
        return getHintTextSize() > 0;
    }

    public int getHintTextSize(){
        return hintTextSize;
    }
}
