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

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

import de.lmu.ifi.researchime.data.base.BaseModel;

public class KeyboardContainer implements BaseModel<KeyboardContainer>, Serializable {

    private String enabledKeyboardId;
    private List<KeyboardModel> keyboards;

    private String activeLayoutId;

    public void onKeyboardActive(String keyboardId) {
        this.activeLayoutId = keyboardId;
    }

    public List<KeyboardModel> getKeyboards(){
        return keyboards;
    }

    public String getActiveLayoutId(){
        return activeLayoutId;
    }

    public String getEnabledKeyboardId(){
        return enabledKeyboardId;
    }

    public @Nullable KeyboardModel getEnabledKeyboard(){
        if(!TextUtils.isEmpty(enabledKeyboardId) && keyboards != null){
            for(KeyboardModel model : keyboards){
                if(TextUtils.equals(enabledKeyboardId, model.getKeyboardId())){
                    return model;
                }
            }
        }
        return null;
    }

    public boolean isTrackingEnabled(){
        KeyboardModel model = getEnabledKeyboard();
        return model == null || model.isTrackingEnabled();
    }

    public boolean showHandPosture(){
        KeyboardModel model = getEnabledKeyboard();
        return model == null || model.showHandPosture();
    }

    public boolean isAnonymizeInputEvents(){
        KeyboardModel model = getEnabledKeyboard();
        return model == null || model.isAnonymizeInputEvents();
    }

    @Override
    public boolean isEmpty() {
        return keyboards == null ||  enabledKeyboardId == null;
    }

    @Override
    public void set(KeyboardContainer data) {
        enabledKeyboardId = data.enabledKeyboardId;
        keyboards = data.keyboards;
    }
}
