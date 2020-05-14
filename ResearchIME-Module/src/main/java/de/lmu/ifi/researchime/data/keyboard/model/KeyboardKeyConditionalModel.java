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

import android.text.TextUtils;

public class KeyboardKeyConditionalModel {

    private String[] layoutSetConditions;
    private String modeCondition;
    private String imeActionCondition;
    private String keySpec;
    private String backgroundType;

    public boolean hasCondition(String layoutSetConditionParam, String modeConditionParam, int imeActionConditionParam){
        boolean hasCondition = false;
        if(layoutSetConditions != null){
            hasCondition |= hasLayoutSetCondition(layoutSetConditionParam);
        }
        if(modeCondition != null){
            hasCondition |= TextUtils.equals(modeCondition, modeConditionParam);
        }
        if(imeActionCondition != null){
            hasCondition |= KeyboardFlagsBuilder.getImeAction(imeActionCondition) == imeActionConditionParam;
        }
        return hasCondition;
    }

    private boolean hasLayoutSetCondition(String conditionParam){
        if(layoutSetConditions != null){
            for(String condition : layoutSetConditions){
                if(TextUtils.equals(conditionParam, condition)){
                    return true;
                }
            }
        }
        return false;
    }

    public String getKeySpec(){
        return keySpec;
    }

    public String getBackgroundType(){
        return backgroundType;
    }
}
